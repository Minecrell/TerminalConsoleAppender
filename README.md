# TerminalConsoleAppender
TerminalConsoleAppender is a custom [Log4j2 Appender] that prints all messages to a [JLine 3 Terminal]. JLine can be
used to extend the regular console using ANSI colors as well as command history or command completion.

## Supported environments
Not all environments can support all the features. By default, TerminalConsoleAppender only enables JLine output and
console colors when running in an environment with an attached terminal session (so usually only when starting the
application from an interactive console session). It will automatically disable the features if the console output is
redirected to a file or to another application (e.g. in web control panels).

Some unsupported environments may still support a subset of the features (e.g. ANSI console colors). In these cases,
extra system properties exist to override the default behaviour. They can be added on the command line or in the
application itself:

| Name | Description |
| ---- | ----------- |
| `-Dterminal.jline=<true/false>` | Enables/disables the extended JLine input (persistent input line, command completion) |
| `-Dterminal.ansi=<true/false>` | Enables/disables the output of ANSI escapes codes (used for colors) |
| `-Dterminal.keepMinecraftFormatting=true` | Output raw Minecraft formatting codes to the console output. |

## Usage
1. Add a dependency on TerminalConsoleAppender:

    ```
    net.minecrell:terminalconsoleappender:1.2.0
    ```

    If you package all dependencies into a single JAR file, ensure that all transitive dependencies are included:

    ```
    org.jline:jline-terminal
    org.jline:jline-reader
    ```

    JLine 3 provides different native terminal implementations that are required for Windows support and extend the terminal
    with some advanced features on Linux. You can choose between [JNA] and [Jansi]. There should be no functional difference,
    so it is mostly a matter of preference.

    For full functionality, you need to add an explicit dependency on one of the terminal implementations:

    |      | [Jansi]  | [JNA]     |
    | ---: | ------ | ------- |
    | **Dependencies** | `org.jline:jline-terminal-jansi:3.12.1` | `org.jline:jline-terminal-jna:3.12.1` |
    | | (`org.fusesource.jansi:jansi`) | (`net.java.dev.jna:jna`) |
    | **Size** | ~280KB | ~1400KB |

2. Configure `TerminalConsoleAppender` in your Log4j configuration:

    ```xml
    <TerminalConsole name="Console">
        <PatternLayout pattern="[%d{HH:mm:ss} %level]: %msg%n"/>
    </TerminalConsole>
    ```

    The `TerminalConsole` appender replaces the regular `Console` appender in your configuration file.

    **Note:** To avoid JLine from blocking your application in some edge cases, it is recommended that you make use of
    [Async Loggers](https://logging.apache.org/log4j/2.x/manual/async.html) or
    [Async Appenders](https://logging.apache.org/log4j/2.x/manual/appenders.html#AsyncAppender) to write messages
    asynchronously.

### Console input
The appender is designed to be used in an application with simultaneous input and output. JLine can extend your console
with a persistent input line as well as command history and command completion.

TerminalConsoleAppender includes `SimpleTerminalConsole` as a base class handling console input
with opinionated defaults. It also serves as a reference implementation if you would like to
have a custom implementation.

To use it, extend `SimpleTerminalConsole` and implement the methods:

```java
public class ExampleConsole extends SimpleTerminalConsole {

    @Override
    protected boolean isRunning() {
        // TODO: Return true if your application is still running
    }

    @Override
    protected void runCommand(String command) {
        // TODO: Run command
    }

    @Override
    protected void shutdown() {
        // TODO: Shutdown your application cleanly (e.g. because CTRL+C was pressed)
    }
    
}
```

You can then start reading commands. Note that this method won't return unless your application
is stopping or an error occurred, so you should start it in a separate console thread.

```java
new ExampleConsole().start();
```

This setup will automatically handle the persistent input line and command history for you. If you'd like to use
command completion you need to implement JLine's `Completer` interface (or use one of the builtin completers).
You can then set it using `.completer(Completer)` when building the `LineReader`.

Override the `buildReader` method in your `ExampleConsole` to set additional options:

```java
    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder
                .appName("Example") // TODO: Replace with your application name
                .completer(new ExampleCommandCompleter())
        );
    }
```

If you'd like to use a custom console input implementation, take a look at the
[source code of `SimpleTerminalConsole`](https://github.com/Minecrell/TerminalConsoleAppender/blob/master/src/main/java/net/minecrell/terminalconsole/SimpleTerminalConsole.java)
to see how it works (as the name says, it's pretty simple!).

### Colorizing console output
JLine will automatically render ANSI color escape codes in supported terminals under Windows, Mac and Linux.
To use them, you need to instruct Log4j to insert them into log messages:

- You can use the [patterns included in Log4j](https://logging.apache.org/log4j/2.x/manual/layouts.html#Patterns)
  in your `PatternLayout`, e.g. `%highlight` or `%style`. It is recommended to use the `noConsoleNoAnsi` option for
  `PatternLayout` to omit them in unsupported environments:

  ```xml
  <TerminalConsole>
      <PatternLayout noConsoleNoAnsi="true" pattern="%highlight{[%d{HH:mm:ss} %level]: %msg%n%xEx}"/>
  </TerminalConsole>
  ```

- You can use the simplified `%highlightError` pattern bundled with TerminalConsoleAppender. It will only mark
  errors red and warnings yellow and keep all other messages as-is. It will automatically disable itself
  in unsupported environments.

  ```xml
  <TerminalConsole>
      <PatternLayout pattern="%highlightError{[%d{HH:mm:ss} %level]: %msg%n%xEx}"/>
  </TerminalConsole>
  ```

- You can [implement custom `PatternConverter`s](https://logging.apache.org/log4j/2.x/manual/extending.html#PatternConverters)
  that add colors to the logging output. TerminalConsoleAppender contains 
  [an example implementation](https://github.com/Minecrell/TerminalConsoleAppender/blob/master/src/main/java/net/minecrell/terminalconsole/MinecraftFormattingConverter.java)
  for [Minecraft formatting codes](http://minecraft.gamepedia.com/Formatting_codes) that replaces them with appropriate
  ANSI colors:

  ```xml
  <TerminalConsole>
      <PatternLayout pattern="[%d{HH:mm:ss} %level]: %minecraftFormatting{%msg}%n"/>
  </TerminalConsole>
  ```

  It can be also configured with the `strip` option to strip all formatting codes instead (e.g. for the log file):

  ```xml
  <TerminalConsole>
      <PatternLayout pattern="[%d{HH:mm:ss} %level]: %minecraftFormatting{%msg}{strip}%n"/>
  </TerminalConsole>
  ```

[Log4j2 Appender]: https://logging.apache.org/log4j/2.x/manual/appenders.html
[JLine 3 Terminal]: https://github.com/jline/jline3
[JNA]: https://github.com/java-native-access/jna
[Jansi]: https://github.com/fusesource/jansi
