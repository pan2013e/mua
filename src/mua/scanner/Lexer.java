package mua.scanner;

import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

import mua.utils.Regex;

public class Lexer implements Serializable {

    private transient final Scanner sc;

    private boolean colonFlag = false;
    private boolean inList = false;

    public boolean firstWord = false;
    public String bufferedToken;

    public Lexer(String s, boolean inList) {
        this.inList = inList;
        sc = new Scanner(s);
    }

    public Lexer(){
        sc = new Scanner(System.in);
    }

    public boolean hasNext() {
        return sc.hasNext();
    }

    /* For error recovery */
    public void discardNext() {
        sc.next();
    }

    public void discardLine() {
        sc.nextLine();
    }

    private String extractSeq(String first, char left, char right) throws IOException {
        String next = first;
        int lb = 0;
        StringBuilder buf = new StringBuilder();
        do {
            if(lb != 0) {
                next = sc.next();
            }
            for(int i = 0; i < next.length(); i++){
                if(next.charAt(i) == left){
                    lb++;
                }
                if(next.charAt(i) == right){
                    lb--;
                }
                if(lb < 0){
                    throw new IOException(String.format("Unexpected token: %s", next));
                }
            }
            buf.append(next);
            if(lb != 0) {
                buf.append(' ');
            }
        } while(lb != 0);
        return buf.toString().trim();
    }

    public Token nextToken() throws IOException {
        String next;
        if(colonFlag) {
            next = bufferedToken;
            colonFlag = false;
        } else {
            next = sc.next();
            bufferedToken = next;
        }
        if(Regex.checkColon(next)) {
            bufferedToken = bufferedToken.substring(1);
            colonFlag = true;
            return Token.Keyword.THING;
        }
        if(Regex.checkLB(next)) {
            bufferedToken = extractSeq(next, '[', ']');
            return Token.Expression.LIST;
        }
        if(Regex.checkLP(next)) {
            bufferedToken = extractSeq(next, '(', ')');
            return Token.Expression.EXPR;
        }
        for(var keyword : Regex.KEYWORDS) {
            if(Regex.checkKeyword(next, keyword)) {
                return Token.Literal.Keyword.valueOf(keyword.toUpperCase());
            }
        }
        if(Regex.checkNumber(next)) {
            return Token.Literal.NUMBER;
        }
        if(Regex.checkWord(next, inList)) {
            return Token.Literal.WORD;
        }
        if(Regex.checkBoolean(next)) {
            return Token.Literal.BOOLEAN;
        }
        if(Regex.checkIdentifier(next)) {
            return Token.Identifier.NAME;
        }
        if(Regex.checkMathOp(next)) {
            return Token.Char.get(next);
        }
        throw new IOException(String.format("Unexpected token: %s", next));
    }

    public String next() {
        String s =  sc.next();
        bufferedToken = s;
        return s;
    }

    public String nextLine() {
        String s = sc.nextLine();
        bufferedToken = s;
        return s;
    }

}
