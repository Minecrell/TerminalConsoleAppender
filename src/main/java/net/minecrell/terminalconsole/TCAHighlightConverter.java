/*
 * The MIT License (MIT)
 *
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

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.HighlightConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

/**
 * A wrapped version of {@link HighlightConverter} that uses
 * {@link TerminalConsoleAppender} to detect if ANSI escape codes can be used
 * to highlight errors and warnings in the console.
 *
 * <p>If configured, it will mark using the colors from {@link HighlightConverter}.
 * It can be only used together with
 * {@link TerminalConsoleAppender}.</p>
 *
 * <p>{@link TerminalConsoleAppender#ANSI_OVERRIDE_PROPERTY} may be used
 * to force the use of ANSI colors even in unsupported environments.</p>
 *
 * <p><b>Example usage:</b> {@code %highlightTCA{%level: %message}}</p>
 */
@Plugin(name = "highlightTCA", category = PatternConverter.CATEGORY)
@ConverterKeys({ "highlightTCA" })
@PerformanceSensitive("allocation")
public final class TCAHighlightConverter {

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
            int len = options.length;
            options = Arrays.copyOf(options, len + 1);
            options[len] = "disableAnsi=true";
        }
        return HighlightConverter.newInstance(config, options);
    }
}
