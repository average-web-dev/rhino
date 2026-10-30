/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests.es6;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.testutils.Utils;

/**
 * Tests for a {@code yield} in places where the compiler keeps typed values on the operand stack,
 * which a yield saves and restores as plain objects.
 */
public class GeneratorYieldCodegenTest {

    private static final String RUN =
            "var it = g();\n" + "var r1 = it.next().value;\n" + "r1 + '|' + it.next('x').value;\n";

    @Test
    public void yieldInNewArguments() {
        String code =
                "function F(a, b) { this.v = a + b; }\n"
                        + "function* g() { return new F((yield 1), 'y').v; }\n"
                        + RUN;

        Utils.assertWithAllModes_ES6("1|xy", code);
    }

    @Test
    public void twoYieldsInNewArguments() {
        String code =
                "function F(a, b) { this.v = a + b; }\n"
                        + "function* g() { return new F((yield 1), (yield 2)).v; }\n"
                        + "var it = g();\n"
                        + "[it.next().value, it.next('a').value, it.next('b').value].join();\n";

        Utils.assertWithAllModes_ES6("1,2,ab", code);
    }

    @Test
    public void yieldInNewArgumentsOfVariable() {
        String code =
                "function F(a) { this.v = a; }\n"
                        + "function* g() { var C = F; return new C((yield 1)).v; }\n"
                        + RUN;

        Utils.assertWithAllModes_ES6("1|x", code);
    }

    @Test
    public void yieldInEvalArgument() {
        String code = "function* g() { var x = 'local'; return eval((yield 1)); }\n" + RUN;

        Utils.assertWithAllModes_ES6("1|local", code);
    }

    @Test
    public void yieldInArraySpread() {
        String code = "function* g() { return [...[(yield 1)]].join(); }\n" + RUN;

        Utils.assertWithAllModes_ES6("1|x", code);
    }

    @Test
    public void yieldAsArraySpreadSource() {
        String code = "function* g() { return [...(yield 1)].join(); }\n" + RUN;

        Utils.assertWithAllModes_ES6("1|x", code);
    }

    @Test
    public void yieldInArrayElementAfterSpread() {
        String code =
                "function* g() { var a = [0]; return [...a, (yield 1), , 2].join(); }\n" + RUN;

        Utils.assertWithAllModes_ES6("1|0,x,,2", code);
    }

    @Test
    public void yieldStarInArrayElementAfterSpread() {
        String code =
                "function* inner() { yield 1; return 5; }\n"
                        + "function* g() { var a = [0]; return [...a, yield* inner()].join(); }\n"
                        + RUN;

        Utils.assertWithAllModes_ES6("1|0,5", code);
    }

    @Test
    public void yieldInObjectLiteralWithSpread() {
        String code =
                "function* g() {\n"
                        + "  var o = {a: 0};\n"
                        + "  var r = {...o, b: (yield 1)};\n"
                        + "  return JSON.stringify(r);\n"
                        + "}\n"
                        + RUN;

        Utils.assertWithAllModes_ES6("1|{\"a\":0,\"b\":\"x\"}", code);
    }
}
