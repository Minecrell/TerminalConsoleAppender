package net.minecrell.terminalconsole;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.HighlightConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapped version of {@link HighlightConverter} that uses
 * {@link TerminalConsoleAppender} to detect if Ansi escape codes can be used
 * to highlight errors and warnings in the console.
 *
 * <p>If configured, it will mark using the colors from {@link HighlightConverter}.
 * It can be only used together with
 * {@link TerminalConsoleAppender}.</p>
 *
 * <p>{@link TerminalConsoleAppender#ANSI_OVERRIDE_PROPERTY} may be used
 * to force the use of ANSI colors even in unsupported environments.</p>
 *
 * <p><b>Example usage:</b> {@code %TCAhighlight{%level: %message}}</p>
 */
@Plugin(name = "TCAhighlight", category = PatternConverter.CATEGORY)
@ConverterKeys({ "TCAhighlight" })
@PerformanceSensitive("allocation")
public class TCAHighlightConverter {
    protected static final Logger LOGGER = StatusLogger.getLogger();

    /**
     * Gets a new instance of the {@link HighlightConverter} with the
     * specified options.
     * The difference to the vanilla {@link HighlightConverter} is that
     * ANSI is disabled automatically when no terminal is present.
     *
     * @param config The current configuration
     * @param options The pattern options
     * @return The new instance
     */
    public static @Nullable HighlightConverter newInstance(Configuration config, String[] options) {

        if (!TerminalConsoleAppender.isAnsiSupported()) {
            List<String> optionList = new ArrayList<>(Arrays.asList(options));
            optionList.add("disableAnsi=true");
            options = optionList.toArray(new String[0]);
        }
        return HighlightConverter.newInstance(config, options);
    }
}
