/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.testutils.Utils;

/**
 * Tests for {@link Context#setMaximumStringLength(int)}, {@link
 * Context#setMaximumArrayBufferLength(int)} and {@link Context#setMaximumArgumentsLength(int)}.
 */
public class ResourceLimitsTest {
    private static final int MAX = 1000;

    private static final String PRELUDE =
            "function check(f) {\n"
                    + "  try { var r = f(); return 'ok ' + (r && r.length); }\n"
                    + "  catch (e) { return (e instanceof RangeError ? 'RangeError: ' : 'other: ') + e.message; }\n"
                    + "}\n"
                    + "var s = 'x'.repeat(600);\n";

    private static final String STRING_ERROR = "RangeError: Invalid string length";

    private static void assertResult(String expected, String script) {
        Utils.runWithAllModes(
                cx -> {
                    cx.setLanguageVersion(Context.VERSION_ES6);
                    cx.setMaximumStringLength(MAX);
                    cx.setMaximumArrayBufferLength(MAX);
                    cx.setMaximumArgumentsLength(MAX);
                    Scriptable scope = cx.initStandardObjects();
                    Object result = cx.evaluateString(scope, PRELUDE + script, "test.js", 1, null);
                    assertEquals(expected, Context.toString(result));
                    return null;
                });
    }

    private static void assertStringLimit(String expression) {
        assertResult(STRING_ERROR, "check(function() { return " + expression + "; })");
    }

    private static void assertWithinLimit(int length, String expression) {
        assertResult("ok " + length, "check(function() { return " + expression + "; })");
    }

    @Test
    public void concatenation() {
        assertWithinLimit(1000, "s + 'x'.repeat(400)");
        assertStringLimit("s + s");
        assertStringLimit("s + 1 + s");
        assertStringLimit("`${s}${s}`");
        assertStringLimit("(function() { var r = s; r += s; return r; })()");
        assertStringLimit("(function() { var r = 'x'; while (true) r = r + r; })()");
    }

    @Test
    public void stringMethods() {
        assertWithinLimit(1000, "'ab'.repeat(500)");
        assertStringLimit("'ab'.repeat(501)");
        assertWithinLimit(1000, "s.padStart(1000)");
        assertStringLimit("s.padStart(1001)");
        assertStringLimit("s.padEnd(1001, 'ab')");
        assertStringLimit("s.concat(s)");
        assertStringLimit("''.concat(s, 'y', s)");
        assertStringLimit("String.prototype.concat.call(s, s, s)");
    }

    @Test
    public void replace() {
        assertWithinLimit(1000, "s.replace('x', 'x'.repeat(401))");
        assertStringLimit("s.replace('x', '$&'.repeat(402))");
        assertStringLimit("s.replace('x', function() { return s; })");
        assertStringLimit("s.replaceAll('x', 'xy')");
        assertStringLimit("s.replace(/x/g, 'xy')");
        assertStringLimit("s.replace(/x/g, function() { return 'xy'; })");
        assertStringLimit("s.replace(/(x+)/, '$1$1')");
        assertStringLimit("RegExp.prototype[Symbol.replace].call(/x/g, s, 'xy')");
        assertWithinLimit(600, "s.replace(/x/g, 'y')");
    }

    @Test
    public void join() {
        assertWithinLimit(999, "new Array(500).fill('x').join()");
        assertStringLimit("new Array(501).fill('x').join()");
        assertStringLimit("[s, s].join('')");
        assertStringLimit("String([s, s])");
        assertStringLimit("(function() { var a = []; a.length = 2000; return a.join(); })()");
        assertStringLimit("Array.prototype.join.call({ length: 2000 }, ',')");
        assertStringLimit("Array.prototype.join.call({ length: 2, 0: s, 1: s })");
        assertStringLimit("new Uint8Array(600).join(',')");
        assertStringLimit("new Uint8Array(600).toString()");
    }

    @Test
    public void jsonStringify() {
        assertWithinLimit(602, "JSON.stringify(s)");
        assertStringLimit("JSON.stringify([s, s])");
        assertStringLimit("JSON.stringify({ a: s, b: s })");
        assertStringLimit(
                "(function() { var a = []; a.length = 1000; return JSON.stringify(a); })()");
        assertStringLimit("JSON.stringify(new Array(100).fill(1), null, 10)");
    }

    @Test
    public void arrayBuffer() {
        assertResult("ok undefined", "check(function() { return new ArrayBuffer(1000).length; })");
        assertResult(
                "RangeError: Array buffer allocation of 1001 bytes exceeds the limit of 1000 bytes",
                "check(function() { return new ArrayBuffer(1001); })");
        assertResult(
                "RangeError: Array buffer allocation of 1004 bytes exceeds the limit of 1000 bytes",
                "check(function() { return new Int32Array(251); })");
        assertResult(
                "RangeError: Array buffer allocation of 1001 bytes exceeds the limit of 1000 bytes",
                "check(function() { return Uint8Array.from({ length: 1001 }); })");
    }

    @Test
    public void applyArguments() {
        assertResult(
                "ok 1000",
                "check(function() { return (function() { return arguments; }).apply(null, { length: 1000 }); })");
        String error = "RangeError: Too many arguments in function call (only 1000 allowed)";
        assertResult(
                error,
                "check(function() { return (function() {}).apply(null, { length: 1e9 }); })");
        assertResult(
                error,
                "check(function() { return Math.max.apply(null, { length: 2 ** 32 - 1 }); })");
        assertResult(
                error,
                "check(function() { return Reflect.apply(function() {}, null, { length: 1e9 }); })");
        assertResult(
                error, "check(function() { return Reflect.construct(Object, { length: 1e9 }); })");
    }

    @Test
    public void defaultsAreUnlimited() {
        try (Context cx = Context.enter()) {
            assertEquals(Integer.MAX_VALUE, cx.getMaximumStringLength());
            assertEquals(Integer.MAX_VALUE, cx.getMaximumArrayBufferLength());
            assertEquals(Integer.MAX_VALUE, cx.getMaximumArgumentsLength());
        }
    }

    @Test
    public void negativeLimitsAreRejected() {
        try (Context cx = Context.enter()) {
            assertThrows(IllegalArgumentException.class, () -> cx.setMaximumStringLength(-1));
            assertThrows(IllegalArgumentException.class, () -> cx.setMaximumArrayBufferLength(-1));
            assertThrows(IllegalArgumentException.class, () -> cx.setMaximumArgumentsLength(-1));
        }
    }
}
