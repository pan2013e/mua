package mua.parser;

import mua.runtime.Executor;
import mua.runtime.MemLayout;
import mua.scanner.Lexer;
import mua.scanner.Token;
import mua.types.*;
import mua.types.Boolean;
import mua.types.Number;
import mua.utils.Cast;
import mua.utils.Return;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Parser implements Serializable {
    private Lexer lexer;

    private final java.util.List<Instruction> context = new ArrayList<>();
    private String funcName = null;
    private boolean codeGen = false;
    private boolean inList = false;
    private boolean afterThing = false;

    private Value lastResult;

    public void REPL() {
        lexer = new Lexer();
        try {
            parse();
        } catch (Return ignore) {
        }
    }

    public void parseString(String s, String funcName, boolean inList, boolean codeGen) throws Return {
        lexer = new Lexer(s, inList);
        this.inList = inList;
        this.funcName = funcName;
        this.codeGen = codeGen;
        parse();
    }

    public void parse() throws Return {
        while(lexer.hasNext()) {
            try {
                lexer.firstWord = true;
                lastResult = wantValue();
            } catch (Return r) {
                throw r;
            } catch (Exception e) {
                MemLayout.getInstance().printStackTrace(e.getMessage());
//                e.printStackTrace();
            }
        }
    }

    public java.util.List<Instruction> getContext() {
        return context;
    }

    public Value getLastResult() {
        return lastResult;
    }

    private Value parseMake() throws IOException, Return {
        Value v1 = wantValue();
        Value value = wantValue();
        // Check function defs.
        if(value instanceof List && ((List) value).list.size() == 2
                && ((List) value).list.get(0) instanceof List
                && ((List) value).list.get(1) instanceof List) {
            return new Function(v1, (List) value);
        }
        if(!codeGen) Executor.executeMake(v1, value);
        else {
            context.add(new Instruction(Token.Keyword.MAKE, new ArrayList<>(){{add(v1);add(value);}}));
        }
        return value;
    }

    private Value parsePrint() throws IOException, Return {
        Value v = wantValue();
        if(!codeGen) return Executor.executePrint(v);
        else return new Reference(addContext(Token.Keyword.PRINT, new ArrayList<>(){{add(v);}}));
    }

    private Value parseErase() throws IOException {
        String name = wantWord();
        assert name.length() > 1 && name.charAt(0) == '"';
        name = name.substring(1);
        if(!codeGen) return Executor.executeErase(name);
        else {
            String finalName = name;
            return new Reference(addContext(Token.Keyword.ERASE, new ArrayList<>(){{add(finalName);}}));
        }
    }

    private Value parseReturn() throws IOException, Return {
        Value v = wantValue();
        if(!codeGen && !inList) {
            Executor.exit(v);
        } else if (!codeGen) {
            assert v != null;
            if(Cast.isa(Reference.class, v)) {
                throw new Return(((Reference) v).wantValue());
            } else {
                throw new Return(v);
            }
        } else {
            context.add(new Instruction(Token.Keyword.RETURN, new ArrayList<>() {{
                add(v);
            }}));
        }
        return v;
    }

    private Value parseExport() throws Return, IOException {
        Value v = wantValue();
        if(!codeGen) return Executor.executeExport(v);
        else {
            return new Reference(addContext(Token.Keyword.EXPORT, new ArrayList<>(){{add(v);}}));
        }
    }

    private Value parseImport() throws Return, IOException {
        Value v = wantValue();
        if(!codeGen){
            String s = Cast.tryCastToString(v);
            return Executor.executeImport(s);
        } else {
            return new Reference(addContext(Token.Keyword.IMPORT, new ArrayList<>(){{add(v);}}));
        }
    }

    private Value functionCall(Function func) throws Return, IOException {
        if(codeGen) {
            java.util.List<Value> list = new ArrayList<>();
            list.add(func);
            for(int i = 0;i < func.args.size();i++){
                Value v = wantValue();
                list.add(v);
            }
            Instruction inst = new Instruction(null, list);
            context.add(inst);
            return new Reference(inst);
        } else {
            Value[] vs = new Value[func.args.size()];
            for(int i = 0;i < func.args.size();i++){
                vs[i] = wantValue();
            }
            return Executor.executeFunc(func, vs);
        }
    }

    private Value functionCall(String f) throws IOException, Return {
        Function func = MemLayout.getInstance().getFunc(f);
        return functionCall(func);
    }

    private Value highOrderCall(String f) throws IOException, Return {
        java.util.List<Value> list = new ArrayList<>();
        list.add(new Word(f));
        list.add(wantValue());
        Instruction inst = new Instruction(null, list);
        context.add(inst);
        return new Reference(inst);
    }

    private String wantName() {
        String name = lexer.next();
        assert name.length() > 1;
        if(!inList && !codeGen) {
            assert name.charAt(0) == '"';
            name = name.substring(1);
        }
        return name;
    }

    private String wantWord() throws IOException {
        return want(Token.Literal.WORD);
    }

    private Instruction addContext(Token.Keyword t, java.util.List<?> list) {
        Instruction inst = new Instruction(t, list);
        context.add(inst);
        return inst;
    }

    /*
     * Value -> <Number> | <Boolean> | <Word> | <List>
     * Value -> :<Value> | thing <Value>
     * Value -> read | op <...>
     * Value -> <func> <args...>
     * Value -> <EXPR>
     */
    private Value wantValue() throws IOException, Return {
        Token t = lexer.nextToken();
        if(inList && !afterThing && t == Token.Literal.WORD && Executor.isFunc(lexer.bufferedToken)) {
            return functionCall(lexer.bufferedToken);
        }
        if(t == Token.Identifier.NAME) {
            if(codeGen && !afterThing) {
                String identifier = lexer.bufferedToken;
                Function current = MemLayout.getInstance().getFunc(funcName);
                if(current != null && current.args.contains(identifier)) {
                    return highOrderCall(identifier);
                }
            }
            // Identifier in symbol table
            if(Executor.isName(lexer.bufferedToken)){
                // If identifier is variable
                // Should not appear at the first position
                if(Executor.isVar(lexer.bufferedToken)) {
                    if(lexer.firstWord) {
                        throw new IllegalArgumentException("Illegal Operation");
                    } else {
                        return new Word(lexer.bufferedToken);
                    }
                }
                if(Executor.isFunc(lexer.bufferedToken)) {
                    if(afterThing) {
                        return new Word(lexer.bufferedToken);
                    } else {
                        return functionCall(lexer.bufferedToken);
                    }
                }
                // If identifier is function, treat as function call
            // Identifier that does not exist
            // Should not appear at the first position
            }
            else if(!lexer.firstWord) {
                return new Word(lexer.bufferedToken);
            }
//            else if(lexer.firstWord){
//                throw new IllegalArgumentException("Illegal Operation");
//            }
        }
        lexer.firstWord = false;
        var tokens = new ArrayList<Token>(){{
            add(Token.Literal.BOOLEAN);
            add(Token.Literal.NUMBER);
            add(Token.Literal.WORD);
            add(Token.Expression.LIST);
        }};
        if(tokens.contains(t)) { // Literal
            String s = lexer.bufferedToken;
            return switch (t.toString().toUpperCase()) {
                case "NUMBER" -> new Number(s);
                case "WORD" -> inList ? new Word(s) : new Word(s.substring(1));
                case "BOOLEAN" -> new Boolean(s);
                case "LIST" -> new List(s);
                default -> throw new UnsupportedOperationException("Unsupported type");
            };
        } else if(t == Token.Keyword.MAKE){
            return parseMake();
        } else if(t == Token.Keyword.THING) {
            afterThing = true;
            Value v = wantValue();
            afterThing = false;
            assert v instanceof Word;
            String s = ((Word) v).string;
            if(!codeGen && !inList) return Executor.executeThing(s);
            else return new Reference(s);
        } else if(t == Token.Keyword.READ) {
            if(!codeGen) return Executor.executeRead(lexer);
            else return new Reference(addContext(Token.Keyword.READ, null));
        } else if(t == Token.Expression.EXPR) {
            if(!codeGen) return Executor.eval(lexer.bufferedToken);
            else return new Reference(addContext(Token.Keyword.EVAL, new ArrayList<>(){{add(lexer.bufferedToken);}}));
        } else if(t == Token.Keyword.ISBOOL || t == Token.Keyword.ISNUMBER
                || t == Token.Keyword.ISWORD || t == Token.Keyword.ISLIST
                || t == Token.Keyword.ISEMPTY) {
            Value s = wantValue();
            if(!codeGen) return Executor.isXXX(t, s);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s);}}));
        } else if(t == Token.Keyword.PRINT) {
            return parsePrint();
        } else if(t == Token.Keyword.ADD || t == Token.Keyword.SUB
                || t == Token.Keyword.MUL || t == Token.Keyword.DIV
                || t == Token.Keyword.MOD) {
            Value s1 = wantValue();
            Value s2 = wantValue();
            if(!codeGen) return Executor.arithmetic(t, s1, s2);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s1);add(s2);}}));
        } else if (t == Token.Keyword.EQ || t == Token.Keyword.LT || t == Token.Keyword.GT) {
            Value s1 = wantValue();
            Value s2 = wantValue();
            if(!codeGen) return Executor.binaryComp(t, s1, s2);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s1);add(s2);}}));
        } else if(t == Token.Keyword.AND || t == Token.Keyword.OR) {
            Value s1 = wantValue();
            Value s2 = wantValue();
            if(!codeGen) return Executor.binaryLogic(t, s1, s2);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s1);add(s2);}}));
        } else if(t == Token.Keyword.NOT) {
            Value s = wantValue();
            return Executor.unaryLogic(t, s);
        } else if(t == Token.Keyword.READLIST) {
            return Executor.executeReadList(lexer);
        } else if(t == Token.Keyword.ERASE) {
            return parseErase();
        } else if(t == Token.Keyword.RUN) {
            Value s = wantValue();
            if(!codeGen) return Executor.executeList(s);
            else return new Reference(addContext(Token.Keyword.RUN, new ArrayList<>(){{add(s);}}));
        } else if(t == Token.Keyword.ISNAME) {
            return new Boolean(Executor.isName(wantWord().substring(1)));
        } else if(t == Token.Keyword.IF) {
            Value cond = wantValue();
            Value s1 = wantValue();
            Value s2 = wantValue();
            if(!codeGen) return Executor.executeIf(cond, s1, s2);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(cond);add(s1);add(s2);}}));
        } else if(t == Token.Keyword.RETURN) {
            return parseReturn();
        } else if(t == Token.Keyword.LOAD) {
            Value s = wantValue();
            return Executor.deserializeMemLayout(s);
        } else if(t == Token.Keyword.SAVE) {
            Value s = wantValue();
            return Executor.serializeMemLayout(s);
        } else if(t == Token.Keyword.RANDOM || t == Token.Keyword.INT || t == Token.Keyword.SQRT) {
            Value s = wantValue();
            if(!codeGen) return Executor.unaryMath(t, s);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s);}}));
        } else if(t == Token.Keyword.WORD || t == Token.Keyword.SENTENCE
            || t == Token.Keyword.LIST || t == Token.Keyword.JOIN) {
            Value s1 = wantValue();
            Value s2 = wantValue();
            return Executor.binaryWord(t, s1, s2);
        } else if(t == Token.Keyword.FIRST || t == Token.Keyword.LAST
            || t == Token.Keyword.BUTFIRST || t == Token.Keyword.BUTLAST) {
            Value s = wantValue();
            if(!codeGen) return Executor.unaryWord(t, s);
            else return new Reference(addContext((Token.Keyword) t, new ArrayList<>(){{add(s);}}));
        } else if(t == Token.Keyword.EXPORT) {
            return parseExport();
        } else if(t == Token.Keyword.IMPORT) {
            return parseImport();
        }
        else {
            if(!codeGen) {
                throw new UnsupportedOperationException("Unspported keyword: " + lexer.bufferedToken);
            }
            else {
                return new Reference(addContext(Token.Keyword.UNKNOWN, null));
            }
        }
    }

    private String want(Token token) throws IOException {
        Token t = lexer.nextToken();
        if(t != token){
            throw new IllegalArgumentException();
        }
        return lexer.bufferedToken;
    }

}
