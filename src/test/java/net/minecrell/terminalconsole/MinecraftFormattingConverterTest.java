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

import org.junit.jupiter.api.Test;

import static net.minecrell.terminalconsole.MinecraftFormattingConverter.ANSI_RESET;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecraftFormattingConverterTest {

    private static String format(String s, boolean ansi) {
        StringBuilder result = new StringBuilder(s);
        MinecraftFormattingConverter.format(s, result, 0, ansi);
        return result.toString();
    }

    @Test
    public void replaceLiteral() {
        assertEquals("Hello World!", format("Hello World!", true));
    }

    @Test
    public void replaceSingle() {
        assertEquals("\u001B[0;31;1mHello" + ANSI_RESET, format("§cHello", true));
    }

    @Test
    public void replaceMultiple() {
        assertEquals("abcabc\u001B[0;31;1mHello \u001B[0;34;1m\u001B[21mWorld!" + ANSI_RESET,
                format("abcabc§cHello §9§lWorld!", true));
    }

    @Test
    public void replaceUppercase() {
        assertEquals("\u001B[0;31;1mHello World!" + ANSI_RESET, format("§CHello World!", true));
    }

    @Test
    public void stripLiteral() {
        assertEquals("Hello World!", format("Hello World!", false));
    }

    @Test
    public void stripSimple() {
        assertEquals("Hello", format("§cHello", false));
    }

    @Test
    public void stripMultiple() {
        assertEquals("abcabcHello World!", format("abcabc§cHello §9§lWorld!", false));
    }

    @Test
    public void stripUppercase() {
        assertEquals("Hello World!", format("§CHello World!", false));
    }

}
