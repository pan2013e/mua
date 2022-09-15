package mua.types;

import mua.utils.General;

import java.io.Serializable;

public abstract class Value implements Serializable {
    public abstract void repr(boolean LF);

    public boolean equals(Value rhs) {
        General.err("Warning: Comparing %s to %s always returns false\n",
                this.getClass(), rhs.getClass());
        return false;
    }

    public abstract boolean isEmpty();

}
