package mua.runtime;

import mua.types.Function;
import mua.types.Instruction;
import mua.types.Value;
import mua.utils.Cast;
import mua.utils.General;
import mua.utils.Property;

import java.io.Serializable;
import java.util.Stack;

public class MemLayout implements Serializable {

    private static final MemLayout INSTANCE = new MemLayout();

    private final StackFrame global = new StackFrame("main");

    private final Stack<StackFrame> Layout = new Stack<>(){{push(global);}};

    @Property(name = "system.stack.limit", type = Integer.class)
    private Integer STACK_LIMIT;

    private MemLayout() {}

    public static MemLayout getInstance() {
        return INSTANCE;
    }

    public static void setDeserializedObject(Object m) {
        MemLayout mem = (MemLayout) m;
        for(int i = 0;i < Math.max(mem.Layout.size(), INSTANCE.Layout.size());i++) {
            if(i > INSTANCE.Layout.size() - 1) {
                INSTANCE.Layout.push(mem.Layout.get(i));
            } else {
                for(var key : INSTANCE.Layout.get(i).table.keySet()) {
                    INSTANCE.Layout.get(i).table.put(key, mem.Layout.get(i).table.get(key));
                }
            }
        }
    }

    public void enter(String name) {
        if(Layout.size() > STACK_LIMIT) {
            throw new RuntimeException("Stack overflow: exceeded limit " + STACK_LIMIT);
        }
        StackFrame sf = new StackFrame(name);
        Layout.push(sf);
    }

    public String getFunctionName() {
        return Layout.peek().name;
    }

    public void exit() {
        Layout.pop();
    }

    public void addCurrent(String k, Value v) {
        Layout.peek().table.put(k, v);
    }

    public void addGlobal(String k, Value v) {
        global.table.put(k, v);
    }

    public void addFunc(String k, Function v) {
        Layout.peek().table.put(k, v);
    }

    public Value erase(String s) {
        for(int i = Layout.size() - 1; i >= 0; i--) {
            var stk = Layout.get(i);
            if(stk.table.containsKey(s)){
                Value ret = stk.table.get(s);
                stk.table.remove(s);
                return ret;
            }
        }
        return null;
    }

    public Value get(String s) {
        // Reverse Order
        for(int i = Layout.size() - 1; i >= 0; i--) {
            var stk = Layout.get(i);
            if(stk.table.containsKey(s)){
                return stk.table.get(s);
            }
        }
        return null;
    }

    public Value getCurrent(String s) {
        if(Layout.peek().table.containsKey(s)){
            return Layout.peek().table.get(s);
        }
        return null;
    }

    public Function getFunc(String s){
        for(int i = Layout.size() - 1; i >= 0; i--) {
            var stk = Layout.get(i);
            if(stk.table.containsKey(s) && Cast.isa(Function.class, stk.table.get(s))){
                return (Function) stk.table.get(s);
            }
        }
        return null;
    }

    public Value export(String s) {
        Value v = get(s);
        if(v == null) {
            return null;
        }
        addGlobal(s, v);
        return v;
    }

    public void recordTrace(Instruction inst) {
        Layout.peek().pc = inst;
    }

    public void printStackTrace(String exception) {
        General.err("%s\n", exception);
        for(int i = Layout.size() - 1; i >= 0; i--) {
            General.err("  - at %s\n", Layout.get(i).toString());
            if(Layout.get(i).pc != null) {
                General.err("  ");
                Layout.get(i).pc.repr(true);
            }
        }
    }

}
