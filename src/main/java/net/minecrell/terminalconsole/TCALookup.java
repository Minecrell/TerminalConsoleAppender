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

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link StrLookup} that returns properties specific to
 * {@link TerminalConsoleAppender}. The following properties are supported:
 *
 * <ul>
 *     <li>{@code ${tca:disableAnsi}}: Can be used together with
 *     {@code PatternLayout} to disable ANSI colors for patterns like
 *     {@code %highlight} or {@code %style} if ANSI colors are unsupported
 *     or are disabled for {@link TerminalConsoleAppender}.
 *
 *     <p><b>Example usage:</b>
 *     {@code <PatternLayout ... disableAnsi="${tca:disableAnsi}">}</p></li>
 * </ul>
 */
@Plugin(name = "tca", category = StrLookup.CATEGORY)
public final class TCALookup extends AbstractLookup {

    /**
     * Lookup key that returns if ANSI colors are unsupported/disabled.
     */
    public final static String KEY_DISABLE_ANSI = "disableAnsi";

    @Override
    @Nullable
    public String lookup(LogEvent event, String key) {
        if (KEY_DISABLE_ANSI.equals(key)) {
            return String.valueOf(!TerminalConsoleAppender.isAnsiSupported());
        }
        return null;
    }

}
