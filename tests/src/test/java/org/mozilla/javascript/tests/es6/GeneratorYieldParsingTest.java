/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests.es6;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.testutils.Utils;

/**
 * Tests for parsing {@code yield} expressions in ES6 generators.
 *
 * <p>Calls with a {@code yield} in their arguments are only tested in interpreted mode, because the
 * compiler generates invalid bytecode for them (with or without parentheses).
 */
public class GeneratorYieldParsingTest {

    @Test
    public void yieldAsOnlyArgument() {
        String code =
                "function* g() { return String(yield 1); }\n"
                        + "var it = g();\n"
                        + "var r1 = it.next();\n"
                        + "var r2 = it.next(42);\n"
                        + "r1.value + '|' + r2.value + '|' + r2.done;\n";

        Utils.assertWithAllModes_ES6("1|42|true", code);
    }

    @Test
    public void yieldBetweenArguments() {
        String code =
                "function f() { return Array.prototype.join.call(arguments, ','); }\n"
                        + "function* g() { return f('a', yield 'b', 'c'); }\n"
                        + "var it = g();\n"
                        + "it.next().value + '|' + it.next('x').value;\n";

        Utils.assertWithAllModes_ES6("b|a,x,c", code);
    }

    @Test
    public void yieldWithoutOperandAsArgument() {
        String code =
                "function f() { return Array.prototype.join.call(arguments, ','); }\n"
                        + "function* g() { return f(yield) + '/' + f(yield, 1); }\n"
                        + "var it = g();\n"
                        + "it.next(); it.next('a');\n"
                        + "var r = it.next('b');\n"
                        + "r.value + '|' + r.done;\n";

        Utils.assertWithAllModes_ES6("a/b,1|true", code);
    }

    @Test
    public void yieldStarAsArgument() {
        String code =
                "function* inner() { yield 1; return 2; }\n"
                        + "function* g() { return String(yield* inner()); }\n"
                        + "var it = g();\n"
                        + "it.next().value + '|' + it.next().value;\n";

        Utils.assertWithAllModes_ES6("1|2", code);
    }

    @Test
    public void yieldOperandStopsAtComma() {
        String code =
                "function* g() { var x = 0, r; r = yield x, x = 5; return r + ':' + x; }\n"
                        + "var it = g();\n"
                        + "var r1 = it.next();\n"
                        + "var r2 = it.next('v');\n"
                        + "r1.value + '|' + r2.value;\n";

        Utils.assertWithAllModes_ES6("0|v:5", code);
    }

    @Test
    public void yieldStatementFollowedByComma() {
        String code =
                "var log = [];\n"
                        + "function* g() { yield 1, log.push('after'); }\n"
                        + "var it = g();\n"
                        + "var r1 = it.next();\n"
                        + "var before = log.length;\n"
                        + "it.next();\n"
                        + "r1.value + '|' + before + '|' + log.join();\n";

        Utils.assertWithAllModes_ES6("1|0|after", code);
    }

    @Test
    public void yieldAfterComma() {
        String code =
                "function* g() { var a = (0, yield 1); return a; }\n"
                        + "var it = g();\n"
                        + "it.next().value + '|' + it.next('x').value;\n";

        Utils.assertWithAllModes_ES6("1|x", code);
    }

    @Test
    public void yieldInConditional() {
        String code =
                "function* g(c) { return c ? yield 1 : yield; }\n"
                        + "var a = g(true), b = g(false);\n"
                        + "a.next().value + '|' + a.next('x').value + '|'\n"
                        + "  + b.next().value + '|' + b.next('y').value;\n";

        Utils.assertWithAllModes_ES6("1|x|undefined|y", code);
    }

    @Test
    public void yieldAsArgumentInGeneratorMethod() {
        String code =
                "var o = { *m() { return String(yield 1); } };\n"
                        + "var a = o.m();\n"
                        + "a.next().value + '|' + a.next('x').value;\n";

        Utils.assertWithAllModes_ES6("1|x", code);
    }

    @Test
    public void legacyGeneratorStillRequiresParentheses() {
        String code = "function g() { String(yield 1); }\n";

        Utils.assertEvaluatorException_1_8("yield expression must be parenthesized", code);
    }

    @Test
    public void legacyGeneratorParenthesizedArgument() {
        String code =
                "function g() { var r = String((yield 1)); yield r; }\n"
                        + "var it = g();\n"
                        + "it.next() + '|' + it.send('x');\n";

        Utils.assertWithAllModes_1_8("1|x", code);
    }
}
