# TerminalConsoleAppender
TerminalConsoleAppender is a custom [Log4j2 Appender] that prints all messages to a [JLine 3 Terminal]. JLine can be
used to extend the regular console using ANSI colors as well as command history or command completion.

## Usage
1. Add a dependency on TerminalConsoleAppender:

    ```
    net.minecrell:terminalconsoleappender:1.0.0
    ```

    You also need one of the native terminal implementations of JLine 3 for the full set of features. I recommend using
    `terminal-jna`:

    ```
    org.jline:jline-terminal-jna:3.3.1
    ```

2. Configure `TerminalConsoleAppender` in your Log4j configuration:

    ```xml
    <TerminalConsole name="Console">
        <PatternLayout pattern="[%d{HH:mm:ss} %level]: %msg%n"/>
    </TerminalConsole>
    ```

    The `TerminalConsole` appender replaces the regular `Console` appender in your configuration file.

3. That's it! To make it work at runtime you need to have the following dependencies available at runtime:

    ```
    net.minecrell:terminalconsoleappender
    org.jline:jline-terminal
    org.jline:jline-reader
    ```

    When using `jline-terminal-jna` additionally:

    ```
    org.jline:jline-terminal-jna
    net.java.dev.jna:jna
    ```

### Console input
The appender is designed to be used in an application with simultaneous input and output. JLine can extend your console
with a persistent input line as well as command history and command completion.

Use the `LineReader` interface to read input from the console:

```java
Terminal terminal = TerminalConsoleAppender.getTerminal();
if (terminal != null) {
    LineReader reader = LineReaderBuilder.builder()
        .appName("Example App") // TODO: Replace with your app name
        .terminal(terminal)
        .build()

    // Important to make the appender aware of the reader
    TerminalConsoleAppender.setReader(reader);

    try {
        String line;
        while ((line = reader.readLine("> ")) != null) {
            // TODO: Execute command with the line
        }
    } catch (UserInterruptException e) {
        // Called when CTRL + C is typed
        // TODO: You should stop your app here
    } finally {
        // Note: At this point the `LineReader` is no longer readable
        // The appender isn't aware of this so you should remove it manually to avoid errors
        TerminalConsoleAppender.setReader(null);
    }

} else {
    // JLine isn't enabled or not supported
    // TODO: Usually, you should fall back to reading from standard input here
}
```

This setup will automatically handle the persistent input line and command history for you. If you'd like to use
command completion you need to implement JLine's `Completer` interface (or use one of the builtin completers).
You can then set it using `.completer(Completer)` when building the `LineReader`.

**Note:** If you'd like to allow tab completion with empty input you need to disable inserting raw tabs for the
`LineReader`:

```java
reader.unsetOpt(LineReader.Option.INSERT_TAB);
```

#### Usage in Eclipse
Currently, Eclipse is unable to render control sequences such as `\r` (to reset the line) or ANSI escape codes.
(See [Bug 76936](https://bugs.eclipse.org/bugs/show_bug.cgi?id=76936)). Unfortunately, after more than 12 years
there is still no fix available.

For now, the only solution is to disable JLine completely in Eclipse. You can instruct `TerminalConsoleAppender` to
fall back to standard output by setting the `jline.enable` system property to `false`, e.g. by adding VM options to
your run configuration:

```
-Djline.enable=false
```

### Colorizing console output
JLine will automatically render ANSI color escape codes in supported terminals under Windows, Mac and Linux.
To use them, you need to instruct Log4j to insert them into log messages:

- You can use the [patterns included in Log4j](https://logging.apache.org/log4j/2.x/manual/layouts.html#Patterns)
  in your `PatternLayout`, e.g. `%highlight` or `%style`. It is recommended to use the `noConsoleNoAnsi` option for
  `PatternLayout` to omit them in unsupported environments:

  ```xml
  <TerminalConsole>
      <PatternLayout noConsoleNoAnsi="true" pattern="%highlight{[%d{HH:mm:ss} %level]: %msg}%n"/>
  </TerminalConsole>
  ```

- You can use the simplified `%highlightError` pattern bundled with TerminalConsoleAppender. It will only mark
  errors red and warnings yellow and keep all other messages as-is. It will automatically disable itself
  in unsupported environments.

  ```xml
  <TerminalConsole>
      <PatternLayout pattern="%highlightError{[%d{HH:mm:ss} %level]: %msg}%n"/>
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
