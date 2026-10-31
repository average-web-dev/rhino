/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests.es6;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.testutils.Utils;

/** Tests that a for-of loop closes its iterator (IteratorClose) when it is left early. */
public class ForOfIteratorCloseTest {

    /**
     * {@code iter(n, ret)} returns an iterator over 0..n-1 that logs "n" for each call of next.
     * {@code ret} becomes its return method, {@code R} is a return method that logs "R".
     */
    private static final String ITER =
            "var log = [];\n"
                    + "function iter(n, ret) {\n"
                    + "  var i = 0;\n"
                    + "  var o = {\n"
                    + "    next() {\n"
                    + "      log.push('n');\n"
                    + "      return i < n ? {value: i++, done: false} : {done: true};\n"
                    + "    }\n"
                    + "  };\n"
                    + "  if (ret !== undefined) o['return'] = ret;\n"
                    + "  o[Symbol.iterator] = function() { return this; };\n"
                    + "  return o;\n"
                    + "}\n"
                    + "function R() { log.push('R'); return {}; }\n";

    private static void assertLog(String expected, String code) {
        Utils.assertWithAllModes_ES6(expected, ITER + code + "\nlog.join();");
    }

    @Test
    public void breakCloses() {
        assertLog("n,R", "for (var x of iter(3, R)) { break; }");
    }

    @Test
    public void normalCompletionDoesNotClose() {
        assertLog("n,n,n", "for (var x of iter(2, R)) {}");
    }

    @Test
    public void continueDoesNotClose() {
        assertLog("n,n,n", "for (var x of iter(2, R)) { continue; }");
    }

    @Test
    public void labeledContinueOfOuterLoopCloses() {
        assertLog(
                "n,R,n,R",
                "outer: for (var k = 0; k < 2; k++) {\n"
                        + "  for (var x of iter(3, R)) { continue outer; }\n"
                        + "}");
    }

    @Test
    public void labeledBreakOfOuterLoopClosesInnerFirst() {
        assertLog(
                "n,n,R2,R1",
                "outer: for (var a of iter(3, function() { log.push('R1'); return {}; })) {\n"
                        + "  for (var b of iter(3, function() { log.push('R2'); return {}; })) {\n"
                        + "    break outer;\n"
                        + "  }\n"
                        + "}");
    }

    @Test
    public void returnCloses() {
        assertLog(
                "n,R,v",
                "function f() { for (var x of iter(3, R)) { return 'v'; } } log.push(f());");
    }

    @Test
    public void throwCloses() {
        assertLog(
                "n,R,e",
                "try { for (var x of iter(3, R)) { throw 'e'; } } catch (e) { log.push(e); }");
    }

    @Test
    public void throwWinsOverErrorFromReturn() {
        assertLog(
                "n,e",
                "try {\n"
                        + "  for (var x of iter(3, function() { throw 'r'; })) { throw 'e'; }\n"
                        + "} catch (e) { log.push(e); }");
    }

    @Test
    public void errorFromReturnOnBreakIsThrown() {
        assertLog(
                "n,r",
                "try {\n"
                        + "  for (var x of iter(3, function() { throw 'r'; })) { break; }\n"
                        + "} catch (e) { log.push(e); }");
    }

    @Test
    public void returnResultMustBeAnObject() {
        assertLog(
                "n,true,n,true",
                "try { for (var x of iter(3, function() { return 1; })) break; }\n"
                        + "catch (e) { log.push(e instanceof TypeError); }\n"
                        + "try { for (var x of iter(3, function() {})) break; }\n"
                        + "catch (e) { log.push(e instanceof TypeError); }");
    }

    @Test
    public void returnResultIsIgnoredOnThrow() {
        assertLog(
                "n,e",
                "try { for (var x of iter(3, function() { return 1; })) throw 'e'; }\n"
                        + "catch (e) { log.push(e); }");
    }

    @Test
    public void missingReturnMethod() {
        assertLog("n,n", "for (var x of iter(3)) break; for (var x of iter(3, null)) break;");
    }

    @Test
    public void errorFromNextDoesNotClose() {
        assertLog(
                "nx",
                "var o = { next() { throw 'nx'; }, return: R };\n"
                        + "o[Symbol.iterator] = function() { return this; };\n"
                        + "try { for (var x of o) {} } catch (e) { log.push(e); }");
    }

    @Test
    public void errorFromDoneGetterDoesNotClose() {
        assertLog(
                "d",
                "var o = { next() { return { get done() { throw 'd'; } }; }, return: R };\n"
                        + "o[Symbol.iterator] = function() { return this; };\n"
                        + "try { for (var x of o) {} } catch (e) { log.push(e); }");
    }

    @Test
    public void errorInDestructuringCloses() {
        assertLog(
                "R,true",
                "var o = { next() { return {value: null, done: false}; }, return: R };\n"
                        + "o[Symbol.iterator] = function() { return this; };\n"
                        + "try { for (var {p} of o) {} } catch (e) { log.push(e instanceof TypeError); }");
    }

    @Test
    public void nextIsReadOnce() {
        String code =
                "var reads = 0, calls = 0;\n"
                        + "var o = {\n"
                        + "  get next() {\n"
                        + "    reads++;\n"
                        + "    var i = 0;\n"
                        + "    return function() {\n"
                        + "      if (++calls > 5) return {done: true};\n"
                        + "      return i++ < 2 ? {value: i, done: false} : {done: true};\n"
                        + "    };\n"
                        + "  }\n"
                        + "};\n"
                        + "o[Symbol.iterator] = function() { return this; };\n"
                        + "var sum = 0;\n"
                        + "for (var x of o) { sum += x; }\n"
                        + "sum + ',' + reads;";

        Utils.assertWithAllModes_ES6("3,1", code);
    }

    @Test
    public void breakRunsFinallyOfGenerator() {
        assertLog(
                "fin",
                "function* g() { try { yield 1; yield 2; } finally { log.push('fin'); } }\n"
                        + "for (var x of g()) break;");
    }

    @Test
    public void generatorReturnClosesLoopInside() {
        assertLog(
                "n,R",
                "function* g() { for (var x of iter(5, R)) yield x; }\n"
                        + "var it = g(); it.next(); it.return();");
    }

    @Test
    public void lexicalBindingsStillWork() {
        String code =
                "var r = [];\n"
                        + "for (let x of [1, 2, 3]) { r.push(x); if (x == 2) break; }\n"
                        + "r.join() + ',' + typeof x;";

        Utils.assertWithAllModes_ES6("1,2,undefined", code);
    }

    @Test
    public void builtInIterators() {
        String code =
                "var r = [];\n"
                        + "for (var x of [1, 2, 3]) { if (x == 2) break; r.push(x); }\n"
                        + "for (var [k, v] of new Map([['a', 1], ['b', 2]])) { r.push(k + v); break; }\n"
                        + "for (var x of new Set([4, 5])) { r.push(x); }\n"
                        + "for (var c of 'hi') { r.push(c); }\n"
                        + "r.join();";

        Utils.assertWithAllModes_ES6("1,a1,4,5,h,i", code);
    }
}
