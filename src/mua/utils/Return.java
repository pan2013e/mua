package mua.utils;

import mua.types.Value;

public class Return extends Exception {
    private final Value ret;

    public Return(Value v) {
        super();
        ret = v;
    }

    public Value getReturnValue() {
        return ret;
    }

}
