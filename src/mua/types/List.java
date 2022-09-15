package mua.types;

import mua.scanner.Lexer;
import mua.scanner.Token;

import java.io.IOException;
import java.util.ArrayList;

public class List extends Value {
    public java.util.List<Value> list = new ArrayList<>();

    public List() {}

    public List(String s) throws IOException {
        assert s.charAt(0) == '[' && s.endsWith("]");
        String temp = s.substring(1, s.length() - 1).trim();
        Lexer lexer = new Lexer(temp, true);
        while (lexer.hasNext()){
            Token tk = lexer.nextToken();
            String bufferedToken = lexer.bufferedToken;
            if(tk instanceof Token.Keyword && tk != Token.Keyword.THING) {
                list.add(new Word(tk.toString().toLowerCase()));
                continue;
            }
            switch(tk.toString().toUpperCase()){
                case "NUMBER" -> list.add(new Number(bufferedToken));
                case "WORD", "EXPR" -> list.add(new Word(bufferedToken));
                case "THING"  -> {
                    list.add(new Reference(bufferedToken));
                    lexer.nextToken();
                }
                case "BOOLEAN"-> list.add(new Boolean(bufferedToken));
                case "LIST"   -> list.add(new List(bufferedToken));
                default -> throw new UnsupportedOperationException("List parsing error");
            }
        }
    }

    public String toText() {
        StringBuilder buf = new StringBuilder();
        buf.append("[ ");
        for(var it : list){
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
        buf.append(" ]");
        return buf.toString();
    }

    public void add(Value v) {
        list.add(v);
    }

    @Override
    public String toString() {
        return toText();
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("%s","[ ");
        for(var it : list){
            if(it instanceof Reference) ((Reference) it).repr(false, false);
            else it.repr(false);
            System.out.print(" ");
        }
        System.out.printf("%c%s",']', LF ? "\n" : "");
    }

    @Override
    public boolean equals(Value rhs) {
        if(!(rhs instanceof List _rhs)) {
            return false;
        }
        if(list.size() != _rhs.list.size()) {
            return false;
        }
        for(int i=0;i< list.size();i++) {
            if(!list.get(i).equals(_rhs.list.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
