package mua.types;

import mua.runtime.MemLayout;
import mua.utils.Return;

public class Reference extends Value {

    public String name = null;
    public Instruction fcall = null;

    public Value wantValue() {
        if(name != null) {
            return MemLayout.getInstance().get(name);
        } else {
            try {
                return fcall.execute();
            } catch (Return r) {
                r.printStackTrace();
                return null;
            }
        }
    }

    public Reference(String n) {
        name = n;
    }

    public Reference(Instruction inst) {
        fcall = inst;
    }

    @Override
    public String toString() {
        return "Reference to " + (name != null ? name : fcall.toString());
    }

    @Override
    public void repr(boolean LF) {
        wantValue().repr(LF);
    }

    public void repr(boolean LF, boolean expand) {
        if(!expand) {
            System.out.printf(":%s%s", name, LF ? "\n" : "");
        } else {
            repr(LF);
        }
    }

    @Override
    public boolean equals(Value rhs) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return name != null && fcall != null;
    }
}
