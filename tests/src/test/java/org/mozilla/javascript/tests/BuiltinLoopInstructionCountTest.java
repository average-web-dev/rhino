/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.testutils.Utils;

/**
 * Built-ins which loop up to the {@code length} of an array-like object must reach {@link
 * Context#observeInstructionCount(int)}, so that an embedding can interrupt them.
 */
public class BuiltinLoopInstructionCountTest {
    private static final class QuotaExceededException extends RuntimeException {}

    private static final class QuotaFactory extends ContextFactory {
        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setLanguageVersion(Context.VERSION_ES6);
            cx.setInstructionObserverThreshold(10_000);
            return cx;
        }

        @Override
        protected void observeInstructionCount(Context cx, int instructionCount) {
            // Allow the script to set up, but nothing like 2^32 iterations.
            long count = (Long) cx.getThreadLocal("count") + instructionCount;
            if (count > 500_000) throw new QuotaExceededException();
            cx.putThreadLocal("count", count);
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Array.prototype.indexOf.call(big, 1)",
                "Array.prototype.lastIndexOf.call(big, 1)",
                "Array.prototype.includes.call(big, 1)",
                "Array.prototype.join.call(int32, '')",
                "Array.prototype.toString.call(int32)",
                "Array.prototype.toLocaleString.call(int32)",
                "Array.prototype.fill.call(big, 1)",
                "Array.prototype.reverse.call(big)",
                "Array.prototype.copyWithin.call(big, 1, 0)",
                "Array.prototype.sort.call(int32)",
                "Array.prototype.slice.call(int32, 0)",
                "Array.prototype.splice.call(big, 1, 1)",
                "Array.prototype.splice.call(int32, 1, 0, 1, 2)",
                "Array.prototype.shift.call(int32)",
                "Array.prototype.unshift.call(int32, 1)",
                "Array.prototype.forEach.call(big, function() {})",
                "Array.prototype.map.call(int32, function() {})",
                "Array.prototype.filter.call(big, function() {})",
                "Array.prototype.some.call(big, function() {})",
                "Array.prototype.every.call(big, function() {})",
                "Array.prototype.find.call(big, function() {})",
                "Array.prototype.findIndex.call(big, function() {})",
                "Array.prototype.findLast.call(big, function() {})",
                "Array.prototype.findLastIndex.call(big, function() {})",
                "Array.prototype.reduce.call(big, function() {}, 0)",
                "Array.prototype.reduceRight.call(big, function() {}, 0)",
                "Array.prototype.flat.call(big)",
                "Array.prototype.flatMap.call(big, function(x) { return x; })",
                "Array.prototype.toSorted.call(int32)",
                "Array.prototype.toReversed.call(int32)",
                "Array.prototype.toSpliced.call(int32, 0, 1)",
                "Array.prototype.with.call(int32, 0, 1)",
                "[].concat(spreadable)",
                "Array.from({ length: 2 ** 32 - 1 })",
                "JSON.stringify(sparse)",
                "sparse.join('')",
                "sparse.sort()",
            })
    public void loopIsInterruptible(String expression) {
        String script =
                "var big = { length: 2 ** 53 - 1 };\n"
                        + "var int32 = { length: 2 ** 31 - 1 };\n"
                        + "var spreadable = { length: 2 ** 32 - 1 };\n"
                        + "spreadable[Symbol.isConcatSpreadable] = true;\n"
                        + "var sparse = [];\n"
                        + "sparse.length = 2 ** 31 - 1;\n"
                        + expression
                        + ";\n";
        Utils.runWithAllModes(
                new QuotaFactory(),
                cx -> {
                    cx.putThreadLocal("count", 0L);
                    Scriptable scope = cx.initStandardObjects();
                    assertThrows(
                            QuotaExceededException.class,
                            () -> cx.evaluateString(scope, script, "test.js", 1, null));
                    return null;
                });
    }
}
