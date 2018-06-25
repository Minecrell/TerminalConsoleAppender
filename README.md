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
    net.minecrell:terminalconsoleappender:1.0.0
    ```

    JLine 3 provides different native terminal implementations that are required for Windows support and extend the terminal
    with some advanced features on Linux. By default, TerminalConsoleAppender depends on the JNA terminal implementation.

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

    Unless you've chosen a different terminal implementation, you will also need:

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
        .build();
    
    // This disables JLine's implementation of Bash's Event Designators
    // These usually don't behave as expected in a simple console session
    // See https://github.com/PaperMC/Paper/issues/1171#issuecomment-399709202 for details
    reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);

    // Important to make the appender aware of the reader
    TerminalConsoleAppender.setReader(reader);

    try {
        String line;

        while (true) {
            try {
                line = reader.readLine("> ");
            } catch (EndOfFileException ignored) {
                // This is thrown when the user indicates end of input using CTRL + D
                // For most applications it doesn't make sense to stop reading input
                // You can either disable console input at this point, or just continue
                // reading normally.
                continue;
            }

            if (line == null) {
                break;
            }

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
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
        String line;
        while ((line = reader.readLine()) != null) {
            // TODO: Execute command with the line
        }
    }
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
