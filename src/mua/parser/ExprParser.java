package mua.parser;

import mua.runtime.Executor;
import mua.runtime.MemLayout;
import mua.scanner.Lexer;
import mua.scanner.Token;
import mua.types.Function;
import mua.types.Number;
import mua.types.Value;
import mua.types.Word;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class ExprParser implements Serializable {

    private final Lexer lexer;

    private final java.util.List<Token> prefixOps = new ArrayList<>(){{
        add(Token.Keyword.ADD);
        add(Token.Keyword.SUB);
        add(Token.Keyword.MUL);
        add(Token.Keyword.DIV);
        add(Token.Keyword.MOD);
    }};

    private final java.util.List<Token> infixOps = new ArrayList<>(){{
        add(Token.Char.ADD);
        add(Token.Char.SUB);
        add(Token.Char.MUL);
        add(Token.Char.DIV);
        add(Token.Char.MOD);
    }};

    private final Stack<Value> vStack = new Stack<>();
    private final Stack<Token.Char> opStack = new Stack<>();

    public ExprParser(String s) {
        String expr = s.trim();
        assert s.charAt(0) == '(' && s.endsWith(")");
        expr = expr.substring(1, s.length() - 1);
        lexer = new Lexer(preprocess(expr), false);
    }

    public Value eval() throws IOException {
        while(lexer.hasNext()) {
            parse();
        }
        while(!opStack.isEmpty()) {
            Value v1 = vStack.pop(), v2 = vStack.pop();
            Token tt = Token.Keyword.valueOf(opStack.pop().toString().toUpperCase());
            vStack.push(Executor.arithmetic(tt, v2, v1));
        }
        return vStack.pop();
    }

    private Value wantValue() throws IOException {
        Token t = lexer.nextToken();
        if(t == Token.Identifier.NAME) {
            return new Word(lexer.bufferedToken);
        } else if(t == Token.Expression.EXPR) {
            ExprParser exprParser = new ExprParser(lexer.bufferedToken);
            return exprParser.eval();
        } else if(t == Token.Literal.NUMBER) {
            return new Number(lexer.bufferedToken);
        } else {
            return null;
        }
    }

    private void parse() throws IOException {
        Token t = lexer.nextToken();
        if(t == Token.Identifier.NAME) {
            Function func = MemLayout.getInstance().getFunc(lexer.bufferedToken);
            Value[] vs = new Value[func.args.size()];
            for(int i = 0;i < func.args.size();i++){
                vs[i] = wantValue();
            }
            vStack.push(Executor.executeFunc(func, vs));
        } else if(prefixOps.contains(t)) {
            Value s1 = wantValue();
            Value s2 = wantValue();
            vStack.push(Executor.arithmetic(t, s1, s2));
        } else if(infixOps.contains(t)){
            if(opStack.isEmpty()) {
                opStack.push((Token.Char) t);
            } else {
                if(Token.Char.hasHigherPriority((Token.Char) t, opStack.peek())) {
                    opStack.push((Token.Char) t);
                } else {
                    do {
                        Value v1 = vStack.pop(), v2 = vStack.pop();
                        Token tt = Token.Keyword.valueOf(opStack.pop().toString().toUpperCase());
                        vStack.push(Executor.arithmetic(tt, v2, v1));
                    } while (!opStack.isEmpty() && !Token.Char.hasHigherPriority((Token.Char) t, opStack.peek()));
                    opStack.push((Token.Char) t);
                }
            }
        } else if(t == Token.Expression.EXPR) {
            ExprParser exprParser = new ExprParser(lexer.bufferedToken);
            vStack.push(exprParser.eval());
        } else if(t == Token.Keyword.THING){
            Value v = wantValue();
            assert v instanceof Word;
            vStack.push(Executor.executeThing(((Word) v).string));
        } else {
            assert t == Token.Literal.NUMBER;
            vStack.push(new Number(lexer.bufferedToken));
        }
    }

    private final java.util.List<Character> Ops = new ArrayList<>() {
        {
            this.add('+');
            this.add('-');
            this.add('*');
            this.add('/');
            this.add('%');
            this.add('(');
            this.add(')');
        }
    };

    private boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isOp(char c) {
        return this.Ops.contains(c);
    }

    private String preprocess(String s) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0;i < s.length() - 1;i++) {
            char c = s.charAt(i), next = s.charAt(i+1);
            buf.append(c);
            if(isOp(c)) {
                buf.append(' ');
            }
            if(isOp(c) && c != ')' && next == '-') {
                buf.append("0 ");
            }
            if(isDigit(c) && !isDigit(next)) {
                buf.append(' ');
            }
            if(isLetter(c) && !isLetter(next)) {
                buf.append(' ');
            }
        }
        buf.append(s.charAt(s.length() - 1));
        return buf.toString().trim();
    }

}