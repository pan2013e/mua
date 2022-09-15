package mua.utils;

import mua.types.*;
import mua.types.Boolean;
import mua.types.Comparable;
import mua.types.Number;

public class Cast {

    public static boolean isa(Class<?> cls, Value v) {
        return v != null && v.getClass() == cls;
    }

    public static <T> T dynCast(Class<T> cls, Value v) {
        if(isa(cls, v)) {
            return cls.cast(v);
        } else {
            return null;
        }
    }

    public static Number tryNumber(String s){
        try {
            return new Number(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Number tryCastToNumber(Value s) {
        Number r;
        if(isa(Number.class, s)) {
            r = (Number) s;
        } else {
            Value v = s;
            if(isa(Reference.class, s)) {
                v = ((Reference) s).wantValue();
            }
            assert v instanceof Word;
            r = tryNumber(((Word) v).string);
            if(r == null) {
                throw new IllegalArgumentException("Illegal operand");
            }
        }
        return r;
    }

    public static String tryCastToString(Value s) {
        if(isa(Number.class, s)) {
            return ((Number) s).number.toString();
        } else if(isa(Boolean.class, s)) {
            return String.valueOf(((Boolean) s).bool);
        } else if(isa(Word.class, s)) {
            return ((Word) s).string;
        } else if(isa(Reference.class, s)) {
            return tryCastToString(((Reference) s).wantValue());
        } else {
            throw new ClassCastException("Cannot do type cast");
        }
    }

    public static boolean tryCastToBoolean(Value s) {
        if(isa(Boolean.class, s)){
            return ((Boolean) s).bool;
        } if(isa(Word.class, s)) {
            if(((Word) s).string.equalsIgnoreCase("true")) {
                return true;
            } else if(((Word) s).string.equalsIgnoreCase("false")) {
                return false;
            } else {
                throw new ClassCastException("Cannot do type cast");
            }
        } else if(isa(Reference.class, s)){
            return tryCastToBoolean(((Reference) s).wantValue());
        } else {
            throw new ClassCastException("Cannot do type cast");
        }
    }

    public static List tryCastToList(Value s) {
        if(isa(List.class, s)){
            return (List) s;
        } else if(isa(Reference.class, s)){
            return tryCastToList(((Reference) s).wantValue());
        } else {
            throw new ClassCastException("Cannot do type cast");
        }
    }

    public static Word tryCastToWord(Value s) {
        return new Word(tryCastToString(s));
    }

    public static boolean isComparable(Value... vs) {
        for(Value v : vs) {
            if(!Comparable.class.isAssignableFrom(v.getClass())){
                return false;
            }
        }
        return true;
    }

    public static Value tryDeref(Value s) {
        if(isa(Reference.class, s)) {
            return ((Reference) s).wantValue();
        }
        return s;
    }
}
