package org.mozilla.javascript.interpreterv2.instruction;

import org.mozilla.javascript.CallFrameV2;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.interpreterv2.InstructionFormatter;

public class EnumClose extends Instruction {
    private final int localBlockRef;
    private final boolean onThrow;

    public EnumClose(int localBlockRef, boolean onThrow) {
        this.localBlockRef = localBlockRef;
        this.onThrow = onThrow;
    }

    @Override
    public void interpret(Context cx, CallFrameV2 frame) {
        Object val = frame.getLocal(localBlockRef);
        ScriptRuntime.enumClose(val, cx, onThrow);

        frame.pc += 1;
    }

    @Override
    public int stackChange() {
        return 0;
    }

    @Override
    public String toDebugString() {
        return InstructionFormatter.formatInstruction(
                this, "localBlockRef", localBlockRef, "onThrow", onThrow);
    }
}
