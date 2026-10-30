/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests.es6;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.testutils.Utils;

/**
 * Tests for Functions Rest Parameters.
 *
 * @author Ronald Brill
 */
public class FunctionsRestParametersTest {

    @Test
    public void oneRestArg() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return restArgs;\n"
                        + "}\n"
                        + "rest(1, 'abc', 2, '##').toString();\n";

        Utils.assertWithAllModes_ES6("1,abc,2,##", code);
    }

    @Test
    public void oneRestArgActivation() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  try {\n"
                        + "    return restArgs;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "  rest(1, 'abc', 2, '##').toString();\n";

        Utils.assertWithAllModes_ES6("1,abc,2,##", code);
    }

    @Test
    public void oneRestArgNothingProvided() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return restArgs;\n"
                        + "}\n"
                        + "var r = rest();\n"
                        + "'' + Array.isArray(r) + '-' + r.length;\n";

        Utils.assertWithAllModes_ES6("true-0", code);
    }

    @Test
    public void oneRestArgNothingProvidedActivation() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  try {\n"
                        + "    return restArgs;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "var r = rest();\n"
                        + "'' + Array.isArray(r) + '-' + r.length;\n";

        Utils.assertWithAllModes_ES6("true-0", code);
    }

    @Test
    public void oneRestArgOneProvided() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return restArgs;\n"
                        + "}\n"
                        + "var r = rest('xy');\n"
                        + "'' + Array.isArray(r) + '-' + r.length;\n";

        Utils.assertWithAllModes_ES6("true-1", code);
    }

    @Test
    public void oneRestArgOneProvidedActivation() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  try {\n"
                        + "    return restArgs;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "var r = rest('xy');\n"
                        + "'' + Array.isArray(r) + '-' + r.length;\n";

        Utils.assertWithAllModes_ES6("true-1", code);
    }

    @Test
    public void twoRestArg() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  return restArgs;\n"
                        + "}\n"
                        + "rest(1, 'abc', 2, '##').toString();\n";

        Utils.assertWithAllModes_ES6("abc,2,##", code);
    }

    @Test
    public void twoRestArgActivation() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  try {\n"
                        + "    return restArgs;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "rest(1, 'abc', 2, '##').toString();\n";

        Utils.assertWithAllModes_ES6("abc,2,##", code);
    }

    @Test
    public void twoRestArgNothingProvided() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  return '' + typeof v + ' - ' + Array.isArray(restArgs) + '-' + restArgs.length;\n"
                        + "}\n"
                        + "rest();\n";

        Utils.assertWithAllModes_ES6("undefined - true-0", code);
    }

    @Test
    public void twoRestArgNothingProvidedActivation() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  try {\n"
                        + "    return '' + typeof v + ' - ' + Array.isArray(restArgs) + '-' + restArgs.length;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "rest();\n";

        Utils.assertWithAllModes_ES6("undefined - true-0", code);
    }

    @Test
    public void twoRestArgOneProvided() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  return v + ' - ' + Array.isArray(restArgs) + '-' + restArgs.length;\n"
                        + "}\n"
                        + "rest('77');";

        Utils.assertWithAllModes_ES6("77 - true-0", code);
    }

    @Test
    public void twoRestArgOneProvidedActivation() {
        String code =
                "function rest(v, ...restArgs) {\n"
                        + "  try {\n"
                        + "    return v + ' - ' + Array.isArray(restArgs) + '-' + restArgs.length;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "rest('77');";

        Utils.assertWithAllModes_ES6("77 - true-0", code);
    }

    @Test
    public void arguments() {
        String code =
                "function rest(arg, ...restArgs) {\n"
                        + "  return arguments.length;\n"
                        + "}\n"
                        + "'' + rest('77') + '-' + rest(1, 2, 3, 4);\n";

        Utils.assertWithAllModes_ES6("1-4", code);
    }

    @Test
    public void argumentsActivation() {
        String code =
                "function rest(arg, ...restArgs) {\n"
                        + "  try {\n"
                        + "    return arguments.length;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "'' + rest('77') + '-' + rest(1, 2, 3, 4);\n";

        Utils.assertWithAllModes_ES6("1-4", code);
    }

    @Test
    public void argLength() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return restArgs.length;\n"
                        + "}\n"
                        + "  rest(1,2) + '-' + rest(1) + '-' + rest();\n";

        Utils.assertWithAllModes_ES6("2-1-0", code);
    }

    @Test
    public void argLengthActivation() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  try {\n"
                        + "    return restArgs.length;\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "  rest(1,2) + '-' + rest(1) + '-' + rest();\n";

        Utils.assertWithAllModes_ES6("2-1-0", code);
    }

    @Test
    public void length() {
        String code =
                "function foo1(...restArgs) {}\n"
                        + "function foo2(arg, ...restArgs) {}\n"
                        + "foo1.length + '-' + foo2.length;\n";

        Utils.assertWithAllModes_ES6("0-1", code);
    }

    @Test
    public void string1() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return restArgs.length;\n"
                        + "}\n"
                        + "rest.toString();\n";

        Utils.assertWithAllModes_ES6(
                "function rest(...restArgs) {\n  return restArgs.length;\n}", code);
    }

    @Test
    public void string2() {
        String code =
                "function rest( arg ,  ...restArgs ) {\n"
                        + "  return restArgs.length;\n"
                        + "}\n"
                        + "rest.toString();\n";

        Utils.assertWithAllModes_ES6(
                "function rest( arg ,  ...restArgs ) {\n  return restArgs.length;\n}", code);
    }

    @Test
    public void trailingComma() {
        String code = "function rest(...restArgs,) {\n" + "  return restArgs;\n" + "}\n";

        Utils.assertEvaluatorExceptionES6("parameter after rest parameter (test#1)", code);
    }

    @Test
    public void twoRestParams() {
        String code = "function rest(...rest1, ...rest2) {\n" + "  return restArgs;\n" + "}\n";

        Utils.assertEvaluatorExceptionES6("parameter after rest parameter (test#1)", code);
    }

    @Test
    public void paramAfterRestParam() {
        String code = "function rest(...rest1, param) {\n" + "  return restArgs;\n" + "}\n";

        Utils.assertEvaluatorExceptionES6("parameter after rest parameter (test#1)", code);
    }

    @Test
    public void restArgsViaApply() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  return JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "rest.apply(null, [1, 'abc', 2.5]);\n";

        Utils.assertWithAllModes_ES6("[1,\"abc\",2.5]", code);
    }

    @Test
    public void restArgsViaApplyActivation() {
        String code =
                "function rest(...restArgs) {\n"
                        + "  try {\n"
                        + "    return JSON.stringify(restArgs);\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "rest.apply(null, [1, 'abc', 2.5]);\n";

        Utils.assertWithAllModes_ES6("[1,\"abc\",2.5]", code);
    }

    @Test
    public void restArgsViaApplyWithLeadingParams() {
        String code =
                "function rest(a, b, ...restArgs) {\n"
                        + "  return a + '|' + b + '|' + JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "[rest.apply(null, [1, 2, 3, 4]), rest.apply(null, [1, 2]),"
                        + " rest.apply(null, [1]), rest.apply(null)].join(' ');\n";

        Utils.assertWithAllModes_ES6(
                "1|2|[3,4] 1|2|[] 1|undefined|[] undefined|undefined|[]", code);
    }

    @Test
    public void restArgsViaApplyArgumentsForwarding() {
        String code =
                "function rest(a, ...restArgs) {\n"
                        + "  return a + '|' + JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "function fwd() {\n"
                        + "  return rest.apply(this, arguments);\n"
                        + "}\n"
                        + "fwd(1, 2, 3);\n";

        Utils.assertWithAllModes_ES6("1|[2,3]", code);
    }

    @Test
    public void restArgsViaCall() {
        String code =
                "function rest(a, ...restArgs) {\n"
                        + "  return a + '|' + JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "rest.call(null, 1, 2, 3);\n";

        Utils.assertWithAllModes_ES6("1|[2,3]", code);
    }

    @Test
    public void restArgsViaBind() {
        String code =
                "function rest(a, ...restArgs) {\n"
                        + "  return a + '|' + JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "[rest.bind(null, 1)(2, 3), rest.bind(null, 1, 2)(3),"
                        + " rest.bind(null, 1, 2, 3)(), rest.bind(null, 1).apply(null, [2, 3]),"
                        + " rest.bind(null, 1).call(null, 2, 3)].join(' ');\n";

        Utils.assertWithAllModes_ES6("1|[2,3] 1|[2,3] 1|[2,3] 1|[2,3] 1|[2,3]", code);
    }

    @Test
    public void restArgsViaBindActivation() {
        String code =
                "function rest(a, ...restArgs) {\n"
                        + "  try {\n"
                        + "    return a + '|' + JSON.stringify(restArgs);\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "[rest.bind(null, 1)(2, 3), rest.bind(null, 1).apply(null, [2, 3])].join(' ');\n";

        Utils.assertWithAllModes_ES6("1|[2,3] 1|[2,3]", code);
    }

    @Test
    public void restArgsViaReflectApply() {
        String code =
                "function rest(a, ...restArgs) {\n"
                        + "  return a + '|' + JSON.stringify(restArgs);\n"
                        + "}\n"
                        + "Reflect.apply(rest, null, [1, 2, 3]);\n";

        Utils.assertWithAllModes_ES6("1|[2,3]", code);
    }
}
