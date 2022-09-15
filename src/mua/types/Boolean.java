package mua.types;

public class Boolean extends Word {

    public boolean bool;

    public Boolean(String s){
        super(s);
        bool = java.lang.Boolean.parseBoolean(s);
    }

    public Boolean(boolean b) {
        super(String.valueOf(b));
        bool = b;
    }

    @Override
    public String toString() {
        return String.valueOf(bool);
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("%s%s", bool, LF ? "\n" : "");
    }

    @Override
    public boolean equals(Value rhs) {
        if(!(rhs instanceof Boolean _rhs)) {
            return false;
        }
        return bool == _rhs.bool;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
