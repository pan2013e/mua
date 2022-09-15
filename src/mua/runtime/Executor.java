package mua.runtime;

import mua.parser.ExprParser;
import mua.parser.Parser;
import mua.scanner.Lexer;
import mua.scanner.Token;
import mua.types.*;
import mua.types.Boolean;
import mua.types.Comparable;
import mua.types.Number;
import mua.utils.Cast;
import mua.utils.General;
import mua.utils.Regex;
import mua.utils.Return;

import java.io.*;
import java.math.MathContext;
import java.math.RoundingMode;

public class Executor {

    public static Value executeRead(Lexer lexer){
        String s = lexer.next();
        s = "\"" + s;
        return Regex.createFromString(s, false);
    }

    public static Value executeThing(String s){
        Value v = MemLayout.getInstance().get(s);
        if(v == null){
            throw new RuntimeException("Error: Undefined variable " + s);
        }
        return v;
    }

    public static Value executePrint(Value v){
        if(v == null) {
            System.out.println("<null>");
            return null;
        }
        v.repr(true);
        return v;
    }

    public static void executeMake(Value v1, Value v){
        String s = Cast.tryCastToString(v1);
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        if(t instanceof Word && ((Word) t).string.charAt(0) == '"'){
            ((Word) t).string = ((Word) t).string.substring(1);
        }
        MemLayout.getInstance().addCurrent(s, t);
    }

    public static Value eval(String expr) throws IOException {
        ExprParser exprParser = new ExprParser(expr);
        return exprParser.eval();
    }

    public static Value isBool(Value v){
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        boolean res = t instanceof Boolean;
        return new Boolean(res);
    }

    public static Value isNumber(Value v){
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        boolean res = t instanceof Number;
        return new Boolean(res);
    }

    public static Value isWord(Value v){
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        boolean res = t instanceof Word;
        return new Boolean(res);
    }

    public static Value isList(Value v){
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        boolean res = t instanceof List;
        return new Boolean(res);
    }

    public static Value arithmetic(Token t, Value s1, Value s2) {
        Number l, r;
        l = Cast.tryCastToNumber(s1);
        r = Cast.tryCastToNumber(s2);
        return switch(t.toString().toUpperCase()){
            case "ADD" -> new Number(String.valueOf(l.number.add(r.number)), 1);
            case "SUB" -> new Number(String.valueOf(l.number.subtract(r.number)), 1);
            case "MUL" -> new Number(String.valueOf(l.number.multiply(r.number)), 1);
            case "DIV" -> new Number(String.valueOf(l.number.divide(r.number,1,RoundingMode.HALF_UP)), 1);
            case "MOD" -> new Number(String.valueOf(l.number.remainder(r.number)), 0);
            default -> throw new UnsupportedOperationException("Unsupported arithmetic");
        };
    }

    public static Value isEmpty(Value s) {
        Value t = Cast.tryDeref(s);
        if(t == null) {
            return new Boolean(true);
        }
        return new Boolean(t.isEmpty());
    }


    public static Value binaryComp(Token t, Value s1, Value s2) {
        Value l = Cast.tryDeref(s1), r = Cast.tryDeref(s2);
        switch(t.toString().toUpperCase()) {
            case "EQ" -> {
                return new Boolean(l.equals(r));
            }
            case "LT" -> {
                if(Cast.isComparable(l, r)) {
                    System.out.println("test");
                    return new Boolean(((Comparable) l).lt(r));
                }
                return new Boolean(false);
            }
            case "GT" -> {
                if(Cast.isComparable(l, r)) {
                    return new Boolean(((Comparable) l).gt(r));
                }
                return new Boolean(false);
            }
            default -> throw new UnsupportedOperationException("Unsupported binary operator");
        }
    }

    public static Value executeReadList(Lexer lexer) {
        lexer.discardLine();
        String s = lexer.nextLine();
        String[] str = s.split(" ");
        List l = new List();
        for(var t : str) {
            l.add(Regex.createFromString(t, true));
        }
        return l;
    }

    public static Value binaryLogic(Token t, Value s1, Value s2) {
        boolean l, r;
        l = Cast.tryCastToBoolean(s1);
        r = Cast.tryCastToBoolean(s2);
        switch (t.toString().toUpperCase()){
            case "AND" -> {
                return new Boolean(l && r);
            }
            case "OR" -> {
                return new Boolean(l || r);
            }
            default -> throw new UnsupportedOperationException("Unsupported binary operator");
        }
    }

    public static Value unaryLogic(Token t, Value s) {
        assert t == Token.Keyword.NOT;
        boolean b = Cast.tryCastToBoolean(s);
        return new Boolean(!b);
    }

    public static Value executeErase(String name) {
        return MemLayout.getInstance().erase(name);
    }

    public static Value executeList(Value s) throws Return {
        Value t = Cast.isa(Reference.class, s) ? ((Reference) s).wantValue() : s;
        if(Cast.isa(List.class, t)) {
            String inst = ((List) t).toText().trim();
            if(inst.charAt(0) == '[' && inst.endsWith("]")) {
                inst = inst.substring(1, inst.length() - 1);
            }
            Parser parser = new Parser();
            parser.parseString(inst, MemLayout.getInstance().getFunctionName(), true, false);
            return parser.getLastResult();
        } else {
            throw new IllegalArgumentException("Illegal argument");
        }
    }

    public static boolean isName(String s) {
        return MemLayout.getInstance().get(s) != null;
    }

    public static boolean isVar(String s) {
        Value v = MemLayout.getInstance().get(s);
        return v != null && !Cast.isa(Function.class, v);
    }

    public static boolean isFunc(String s) {
        Value v = MemLayout.getInstance().get(s);
        return Cast.isa(Function.class, v);
    }

    public static Value executeIf(Value cond, Value s1, Value s2) throws IOException, Return {
        Value t = Cast.isa(Reference.class, cond) ? ((Reference) cond).wantValue() : cond;
        Value t1 = Cast.isa(Reference.class, s1) ? ((Reference) s1).wantValue() : s1;
        Value t2 = Cast.isa(Reference.class, s2) ? ((Reference) s2).wantValue() : s2;
        assert t1 instanceof List && t2 instanceof List;
        t = new Boolean(Cast.tryCastToBoolean(t));
        if(((Boolean) t).bool) {
            return executeList(t1);
        } else {
            return executeList(t2);
        }
    }

    public static void exit(Value v) {
        Number n = Cast.tryCastToNumber(v);
        System.exit(Integer.parseInt(n.number.toString()));
    }

    public static Value serializeMemLayout(Value v) throws IOException {
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        assert t instanceof Word;
        var out = new ObjectOutputStream(new FileOutputStream(((Word) t).string));
        out.writeObject(MemLayout.getInstance());
        out.close();
        return v;
    }

    private static boolean isJavaSerializationFile(String file) throws IOException {
        var in = new FileInputStream(file);
        return in.read() == 0xac && in.read() == 0xed;
    }

    public static Value deserializeMemLayout(Value v) throws IOException, Return {
        Value t = Cast.isa(Reference.class, v) ? ((Reference) v).wantValue() : v;
        assert t instanceof Word;
        String file = ((Word) t).string;
        try {
            if(isJavaSerializationFile(file)) {
                var in = new ObjectInputStream(new FileInputStream(file));
                MemLayout.setDeserializedObject(in.readObject());
                in.close();
            } else {
                var in = new FileInputStream(file);
                Parser parser = new Parser();
                parser.parseString(new String(in.readAllBytes()), MemLayout.getInstance().getFunctionName(), false, false);
                in.close();
            }
            return new Boolean(true);
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            return new Boolean(false);
        }
    }

    public static Value unaryMath(Token t, Value s) {
        Number n = Cast.tryCastToNumber(s);
        switch (t.toString().toUpperCase()) {
            case "RANDOM" -> {
                double d = Math.random() * Double.parseDouble(n.number.toString());
                return new Number(Double.toString(d), 3);
            }
            case "SQRT" -> {
                return new Number(n.number.sqrt(new MathContext(10)).toString(), 3);
            }
            case "INT" -> {
                double d = Math.floor(Double.parseDouble(n.number.toString()));
                return new Number(Double.toString(d), 0);
            }
            default -> throw new UnsupportedOperationException("Unsupported math operation");
        }
    }

    private static void iterateAndAdd(List l, Value s) {
        if(Cast.isa(List.class, s)) {
            for(var v : ((List) s).list) {
                iterateAndAdd(l, v);
            }
        } else {
            l.add(s);
        }
    }

    public static Value binaryWord(Token t, Value s1, Value s2) {
        switch (t.toString().toUpperCase()) {
            case "WORD" -> {
                assert s1 instanceof Word && s2 instanceof Word;
                String s = ((Word) s1).string;
                if(Cast.isa(Number.class, s2)) {
                    s = s + ((Number) s2).number.toString();
                } else if(Cast.isa(Boolean.class, s2)) {
                    s = s + ((Boolean) s2).bool;
                } else {
                    s = s + ((Word) s2).string;
                }
                return new Word(s);
            }
            case "SENTENCE" -> {
                List l = new List();
                iterateAndAdd(l, s1);
                iterateAndAdd(l, s2);
                return l;
            }
            case "JOIN" -> {
                assert Cast.isa(List.class, s1);
                ((List) s1).add(s2);
                return s1;
            }
            case "LIST" -> {
                return new List(){{
                    add(s1);
                    add(s2);
                }};
            }
            default -> throw new UnsupportedOperationException("Unsupported operation");
        }
    }

    public static Value unaryWord(Token t, Value s) {
        boolean mode;
        Value v = s;
        Word w = null;
        List l = null;
        if(s instanceof Reference) {
            v = ((Reference) s).wantValue();
        }
        if(v instanceof Word) {
            mode = true;
            w = Cast.tryCastToWord(v);
            if(w.string.isEmpty()) {
                throw new IllegalArgumentException("Cannot operate on empty string");
            }
        } else {
            assert Cast.isa(List.class, v);
            mode = false;
            l = (List) v;
            if(l.list.isEmpty()) {
                throw new IllegalArgumentException("Cannot operate on empty list");
            }
        }
        switch (t.toString().toUpperCase()) {
            case "FIRST" -> {
                if(mode) {
                    return new Word(w.string.substring(0, 1));
                } else {
                    return l.list.get(0);
                }
            }
            case "LAST" -> {
                if(mode) {
                    int length = w.string.length();
                    return new Word(w.string.substring(length - 1, length));
                } else {
                    return l.list.get(l.list.size() - 1);
                }
            }
            case "BUTFIRST" -> {
                if(mode) {
                    return new Word(w.string.substring(1));
                } else {
                    List finalL = l;
                    return new List(){{
                        for(int i = 1; i < finalL.list.size(); i++) {
                            add(finalL.list.get(i));
                        }
                    }};
                }
            }
            case "BUTLAST" -> {
                if(mode) {
                    int length = w.string.length();
                    return new Word(w.string.substring(0, length - 1));
                } else {
                    List finalL = l;
                    return new List(){{
                        for(int i = 0; i < finalL.list.size() - 1; i++) {
                            add(finalL.list.get(i));
                        }
                    }};
                }
            }
            default -> throw new UnsupportedOperationException("Unsupported operation");
        }
    }

    public static Value executeFunc(Function func, Value[] args) {
        MemLayout.getInstance().enter(func.name);
        for(int i = 0;i < func.args.size();i++){
            Value v = args[i];
            while(v instanceof Reference) {
                v = ((Reference) v).wantValue();
            }
            MemLayout.getInstance().addCurrent(func.args.get(i), v);
        }
        Value ret = null;
        for(var inst : func.body){
            try {
                ret = inst.execute();
            } catch (Return r) {
                ret = r.getReturnValue();
                break;
            }
        }
        MemLayout.getInstance().exit();
        return ret;
    }

    public static Value isXXX(Token t, Value s) {
        switch((Token.Keyword) t){
            case ISBOOL -> {
                return isBool(s);
            }
            case ISLIST -> {
                return isList(s);
            }
            case ISNUMBER -> {
                return isNumber(s);
            }
            case ISWORD -> {
                return isWord(s);
            }
            case ISEMPTY -> {
                return isEmpty(s);
            }
            default -> throw new UnsupportedOperationException("Unsupported operation");

        }
    }

    public static Value executeExport(Value v) {
        String s = Cast.tryCastToString(v);
        return MemLayout.getInstance().export(s);
    }

    public static Value executeImport(String s) {
        General.loadLib("mua.lib." + s);
        return new Boolean(true);
    }
}