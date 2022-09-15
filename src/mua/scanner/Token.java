package mua.scanner;

public interface Token {

    enum Literal implements Token {
        NUMBER,
        WORD,
        BOOLEAN,
    }

    enum Identifier implements Token {
        NAME
    }

    enum Expression implements Token {
        LIST, // [...]
        EXPR  // (...)
    }

    enum Keyword implements Token {
        MAKE,
        THING,
        PRINT,
        READ,
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        ERASE,
        ISNAME,
        RUN,
        EQ,
        GT,
        LT,
        AND,
        OR,
        NOT,
        IF,
        ISNUMBER,
        ISWORD,
        ISLIST,
        ISBOOL,
        ISEMPTY,
        READLIST,
        RETURN,
        EXPORT,
        WORD,
        SENTENCE,
        LIST,
        JOIN,
        FIRST,
        LAST,
        BUTFIRST,
        BUTLAST,
        RANDOM,
        INT,
        SQRT,
        SAVE,
        LOAD,
        ERALL,
        POALL,
        IMPORT,

        EVAL,
        UNKNOWN,
    }

    enum Char implements Token {
        ADD,
        SUB,
        MUL,
        DIV,
        MOD;

        public static Char get(String s) {
            return switch (s) {
                case "+" -> ADD;
                case "-" -> SUB;
                case "*" -> MUL;
                case "/" -> DIV;
                case "%" -> MOD;
                default -> throw new IllegalArgumentException("Illegal math operator");
            };
        }

        private static int getPriority(Char c) {
            return switch (c) {
                case ADD, SUB -> 0;
                case MUL, DIV, MOD -> 1;
            };
        }

        public static boolean hasHigherPriority(Char c1, Char c2) {
            return getPriority(c1) > getPriority(c2);
        }

    }

}
