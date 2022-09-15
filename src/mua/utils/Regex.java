package mua.utils;

import mua.types.Boolean;
import mua.types.Number;
import mua.types.Value;
import mua.types.Word;

public class Regex {

    public static final String[] KEYWORDS = {
            "make","thing","print","read","add","sub","mul","div",
            "mod","erase","isname","run","eq","gt","lt","and","or",
            "not","if","isnumber","isword","islist","isbool","isempty",
            "readlist", "return", "export", "word", "sentence",
            "list", "join", "first", "last", "butfirst", "butlast",
            "random", "int", "sqrt", "save", "load", "erall", "poall",
            "import"
    };

    public static final String NUMBER_PATTERN = "[0123456789]+";
    public static final String WORD_PATTERN = "\".*";
    public static final String WORD_PATTERN_INLIST = ".*";
    public static final String BOOLEAN_PATTERN = "true|false";
    public static final String ID_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";
    public static final String MATHOP_PATTERN = "[+\\-*/%]";

    private static final String LB_START = "\\[.*";
    private static final String LP_START = "\\(.*";

    public static boolean checkNumber(String s) {
        return s.matches(NUMBER_PATTERN);
    }

    public static boolean checkWord(String s, boolean inList) {
        return inList ? s.matches(WORD_PATTERN_INLIST) : s.matches(WORD_PATTERN);
    }

    public static boolean checkBoolean(String s) {
        return s.matches(BOOLEAN_PATTERN);
    }

    public static boolean checkIdentifier(String s) {
        return s.matches(ID_PATTERN);
    }

    public static boolean checkKeyword(String s, String keyword) {
        return s.equalsIgnoreCase(keyword);
    }

    public static boolean checkLB(String s) {
        return s.matches(LB_START);
    }

    public static boolean checkLP(String s) {
        return s.matches(LP_START);
    }

    public static boolean checkColon(String s) {
        return s.charAt(0) == ':';
    }

    public static boolean checkMathOp(String s) {
        return s.matches(MATHOP_PATTERN);
    }

    public static Value createFromString(String s, boolean inList) {
        if(Regex.checkBoolean(s)){
            return new Boolean(s);
        }
        if(Regex.checkNumber(s)){
            return new Number(s);
        }
        if(Regex.checkWord(s, inList)){
            return new Word(s);
        }
        return null;
    }
}
