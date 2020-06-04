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
import java.util.List;

@Plugin(name = "TCAhighlight", category = PatternConverter.CATEGORY)
@ConverterKeys({ "TCAhighlight" })
@PerformanceSensitive("allocation")
public class TCAHighlightConverter {
    protected static final Logger LOGGER = StatusLogger.getLogger();

    /**
     * Gets a new instance of the {@link HighlightErrorConverter} with the
     * specified options.
     *
     * @param config The current configuration
     * @param options The pattern options
     * @return The new instance
     */
    public static @Nullable HighlightConverter newInstance(Configuration config, String[] options) {
        if (options.length != 1) {
            LOGGER.error("Incorrect number of options on highlightError. Expected 1 received " + options.length);
            return null;
        }
        if (options[0] == null) {
            LOGGER.error("No pattern supplied on highlightError");
            return null;
        }

        if (!TerminalConsoleAppender.isAnsiSupported()) {
            List<String> optionList = new ArrayList<>();
            optionList.add(options[0]);
            optionList.add("disableAnsi=true");
            options = optionList.toArray(new String[0]);
        }
        return HighlightConverter.newInstance(config, options);
    }
}
