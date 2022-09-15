package mua.types;

import mua.parser.Parser;
import mua.runtime.MemLayout;
import mua.utils.Cast;
import mua.utils.Return;

import java.util.ArrayList;

public class Function extends Value {

    public final java.util.List<String> args;
    public final java.util.List<Instruction> body;

    public final String name;

    public Function(Value v, List f) throws Return {
        args = new ArrayList<>();
        name = Cast.tryCastToString(v);
        assert f.list.size() == 2 && f.list.get(0) instanceof List
                && f.list.get(1) instanceof List;
        for(var args : ((List) f.list.get(0)).list) {
            assert args instanceof Word;
            this.args.add(((Word) args).string);
        }
        MemLayout.getInstance().addFunc(name, this);
        StringBuilder buf = new StringBuilder();
        for(var it : ((List) f.list.get(1)).list){
            if(it instanceof Word) {
                buf.append(((Word) it).string);
            }
            if(it instanceof Reference) {
                buf.append(":").append(((Reference) it).name);
            }
            if(it instanceof List) {
                buf.append(((List) it).toText());
            }
            buf.append(" ");
        }
        String inst = buf.toString();
        Parser parser = new Parser();
        parser.parseString(inst, name, false, true);
        body = parser.getContext();
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("Function@%s%s", name, LF ? "\n" : "");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
