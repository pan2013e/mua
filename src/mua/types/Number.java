package mua.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Number extends Word implements Comparable<Value> {

    public BigDecimal number;

    public Number(String s) {
        super(s);
        number = new BigDecimal(s);
    }

    public Number(String s, int scale) {
        super(s);
        number = new BigDecimal(s);
        number = number.setScale(scale, RoundingMode.HALF_UP);
    }

    public boolean isInteger() {
        return number.compareTo(new BigDecimal(number.toBigInteger().toString())) == 0;
    }

    public void setScale(int scale) {
        number = number.setScale(scale, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return number.toString();
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("%s%s",
                isInteger() ? number.toBigInteger().toString() : number.toString(),
                LF ? "\n" : "");
    }

    @Override
    public boolean equals(Value rhs) {
        if(!(rhs instanceof Number _rhs)) {
            return false;
        }
        return number.equals(_rhs.number);
    }

    public boolean eq(Value rhs) {
        return equals(rhs);
    }

    public boolean lt(Value rhs) {
        if(!(rhs instanceof Number _rhs)) {
            return false;
        }
        return number.compareTo(_rhs.number) < 0;
    }

    public boolean gt(Value rhs) {
        if(!(rhs instanceof Number _rhs)) {
            return false;
        }
        return number.compareTo(_rhs.number) > 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
