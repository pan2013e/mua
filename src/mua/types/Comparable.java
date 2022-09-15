package mua.types;

public interface Comparable<T> {

    boolean lt(T rhs);
    boolean gt(T rhs);
    boolean eq(T rhs);

    default boolean ne(T rhs) {
        return !eq(rhs);
    }

    default boolean le(T rhs) {
        return lt(rhs) || eq(rhs);
    }

    default boolean ge(T rhs) {
        return gt(rhs) || eq(rhs);
    }

}
