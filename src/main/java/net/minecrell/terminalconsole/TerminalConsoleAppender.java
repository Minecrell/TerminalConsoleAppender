/*
 * TerminalConsoleAppender
 * Copyright (c) 2017 Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.terminalconsole;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * An {@link Appender} that uses the jline3 {@link Terminal} to print messages
 * to the console.
 *
 * <p>The jline {@link Terminal} extends the regular console output with support
 * for Ansi escape codes on Windows. Additionally, it's {@link LineReader}
 * interface can be used to implement enhanced console input, with an
 * persistent input line, as well as command history and command completion.</p>
 *
 * <p>The {@code TerminalConsole} appender replaces the default {@code Console}
 * appender in your log4j configuration. By default, log4j will automatically
 * close the standard output when the original {@code Console} appender is
 * removed. Consequently, it is necessary to keep an unused {@code Console}
 * appender.</p>
 *
 * <p><b>Example usage:</b>
 * <pre>{@code  <TerminalConsole>
 *     <PatternLayout pattern="[%d{HH:mm:ss} %level]: %msg%n"/>
 * </TerminalConsole>
 *
 * <Console name="SysOut" target="SYSTEM_OUT"/>}</pre></p>
 *
 * <p>To use the enhanced console input it is necessary to set the
 * {@link LineReader} using {@link #setReader(LineReader)}. The appender will
 * then automatically redraw the current prompt. When creating the
 * {@link LineReader} it's important to use the {@link Terminal}
 * returned by {@link #getTerminal()}. Additionally, the reader should
 * be removed from the appender as soon as it's no longer accepting
 * input (for example when the user interrupted input using CTRL + C.</p>
 *
 * <p>By default, the jline {@link Terminal} is enabled for all environments.
 * This may cause problems when running the project in environments which do
 * not support special features like Ansi escape codes. In these cases, you
 * can manually disable the jline terminal by setting {@code jline.enable}
 * system property to {@code false}.</p>
 */
@Plugin(name = TerminalConsoleAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class TerminalConsoleAppender extends AbstractAppender {

    public static final String PLUGIN_NAME = "TerminalConsole";

    private static final PrintStream out = System.out;

    private static boolean initialized;
    @Nullable private static Terminal terminal;
    @Nullable private static LineReader reader;

    /**
     * Returns the {@link Terminal} that is used to print messages to the
     * console. May be {@code null} if forcibly disabled using the
     * {@code jline.enable} system property or if running in an unsupported
     * environment.
     *
     * @return The terminal, or null if not supported
     */
    @Nullable
    public static Terminal getTerminal() {
        return terminal;
    }

    /**
     * Returns the currently configured {@link LineReader} that is used to
     * read input from the console. May be null if no {@link LineReader}
     * was configured by the environment.
     *
     * @return The current line reader, or null if none
     */
    @Nullable
    public static LineReader getReader() {
        return reader;
    }

    /**
     * Sets the {@link LineReader} that is used to read input from the console.
     * Setting the {@link LineReader} will allow the appender to automatically
     * redraw the input line when a new log message is added.
     *
     * <p><b>Note:</b> The specified {@link LineReader} must be created with
     * the terminal returned by {@link #getTerminal()}.</p>
     *
     * @param newReader The new line reader
     */
    public static void setReader(@Nullable LineReader newReader) {
        if (newReader != null && newReader.getTerminal() != terminal) {
            throw new IllegalArgumentException("Reader was not created with TerminalConsoleAppender.getTerminal()");
        }

        reader = newReader;
    }

    /**
     * Constructs a new {@link TerminalConsoleAppender}.
     *
     * @param name The name of the appender
     * @param filter The filter, can be {@code null}
     * @param layout The layout to use
     * @param ignoreExceptions If {@code true} exceptions encountered when
     *     appending events are logged, otherwise they are propagated to the
     *     caller
     */
    protected TerminalConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        initializeTerminal();
    }

    private static void initializeTerminal() {
        if (!initialized) {
            initialized = true;

            if (PropertiesUtil.getProperties().getBooleanProperty("jline.enable", true)
                    && System.getProperty("FORGE_FORCE_FRAME_RECALC") == null) {
                try {
                    terminal = TerminalBuilder.builder().dumb(true).build();
                } catch (IOException e) {
                    LOGGER.error("Failed to initialize terminal. Falling back to standard output", e);
                }
            } else {
                // The property is set by ForgeGradle only for Eclipse.
                // Eclipse doesn't support colors and characters like \r so enabling jline on it will
                // just cause a lot of issues with empty lines and weird characters.
                //      Also see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=76936
                LOGGER.warn("Disabling terminal, you're running in an unsupported environment.");
            }
        }
    }

    @Override
    public void append(LogEvent event) {
        if (terminal != null) {
            if (reader != null) {
                // Draw the prompt line again if a reader is available
                reader.callWidget(LineReader.CLEAR);
                terminal.writer().print(getLayout().toSerializable(event));
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            } else {
                terminal.writer().print(getLayout().toSerializable(event));
            }

            terminal.writer().flush();
        } else {
            out.print(getLayout().toSerializable(event));
        }
    }

    /**
     * Creates a new {@link TerminalConsoleAppender}.
     *
     * @param name The name of the appender
     * @param filter The filter, can be {@code null}
     * @param layout The layout, can be {@code null}
     * @param ignoreExceptions If {@code true} exceptions encountered when
     *     appending events are logged, otherwise they are propagated to the
     *     caller
     * @return The new appender
     */
    @PluginFactory
    public static TerminalConsoleAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") @Nullable Layout<? extends Serializable> layout,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {

        if (name == null) {
            LOGGER.error("No name provided for TerminalConsoleAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new TerminalConsoleAppender(name, filter, layout, ignoreExceptions);
    }

}
