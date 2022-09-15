package mua.runtime;

import mua.types.Instruction;
import mua.types.Value;

import java.io.Serializable;
import java.util.HashMap;

public class StackFrame implements Serializable {

    public String name;
    public final HashMap<String, Value> table = new HashMap<>();

    public Instruction pc = null;

    public StackFrame(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("<%s@0x%x>", name, this.hashCode());
    }

}
