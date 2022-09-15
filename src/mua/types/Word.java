package mua.types;

public class Word extends Value implements Comparable<Value> {

    public String string;

    public Word(String s){
        if(!s.isEmpty()) {
            string = s.charAt(0) == '"' ? s.substring(1) : s;
        }
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("%s%s", string, LF ? "\n" : "");
    }

    @Override
    public boolean equals(Value rhs) {
        if(!(rhs instanceof Word _rhs)) {
            return false;
        }
        return string.equals(_rhs.string);
    }

    public boolean eq(Value rhs) {
        return equals(rhs);
    }

    public boolean lt(Value rhs) {
        if(!(rhs instanceof Word _rhs)) {
            return false;
        }
        return string.compareTo(_rhs.string) < 0;
    }

    public boolean gt(Value rhs) {
        if(!(rhs instanceof Word _rhs)) {
            return false;
        }
        return string.compareTo(_rhs.string) > 0;
    }

    @Override
    public boolean isEmpty() {
        return string.isEmpty();
    }
}
