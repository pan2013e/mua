package mua.types;

import mua.runtime.Executor;
import mua.runtime.MemLayout;
import mua.scanner.Lexer;
import mua.scanner.Token;
import mua.utils.Cast;
import mua.utils.Return;

import java.io.IOException;

public class Instruction extends Value {

    private final Token.Keyword operator;
    private final java.util.List<?> operands;

    public Instruction(Token.Keyword operator, java.util.List<?> operands) {
        this.operator = operator;
        this.operands = operands;
    }

    public Value execute() throws Return {
        if(operator == null) {
            MemLayout.getInstance().recordTrace(this);
            if(Cast.isa(Word.class, (Value) operands.get(0))) {
                Function func = MemLayout.getInstance().getFunc(((Word) operands.get(0)).string);
                Value[] vs = new Value[1];
                vs[0] = (Value) operands.get(1);
                return Executor.executeFunc(func, vs);
            } else {
                Function func = (Function) operands.get(0);
                Value[] vs = new Value[operands.size()-1];
                for(int i = 1;i < operands.size();i++) {
                    Value next = (Value) operands.get(i);
                    while(Cast.isa(Reference.class, next)) {
                        next = ((Reference) next).wantValue();
                    }
                    vs[i-1] = next;
                }
                return Executor.executeFunc(func, vs);
            }
        }
        switch(operator) {
            case PRINT -> {
                return Executor.executePrint((Value) operands.get(0));
            }
            case MAKE -> {
                Executor.executeMake((Value) operands.get(0), (Value) operands.get(1));
                return (Value) operands.get(1);
            }
            case RETURN -> {
                Value s = (Value) operands.get(0);
                Value v = Cast.isa(Reference.class, s) ? ((Reference) s).wantValue() : s;
                throw new Return(v);
            }
            case READ -> {
                return Executor.executeRead(new Lexer());
            }
            case ISBOOL, ISEMPTY, ISLIST, ISWORD, ISNUMBER -> {
                return Executor.isXXX(operator, (Value) operands.get(0));
            }
            case ADD, SUB, MUL, DIV, MOD -> {
                return Executor.arithmetic(operator, (Value) operands.get(0), (Value) operands.get(1));
            }
            case EQ, LT, GT -> {
                return Executor.binaryComp(operator, (Value) operands.get(0), (Value) operands.get(1));
            }
            case AND, OR -> {
                return Executor.binaryLogic(operator, (Value) operands.get(0), (Value) operands.get(1));
            }
            case NOT -> {
                return Executor.unaryLogic(operator, (Value) operands.get(0));
            }
            case READLIST -> {
                return Executor.executeReadList(new Lexer());
            }
            case ERASE -> {
                return Executor.executeErase((String) operands.get(0));
            }
            case IF -> {
                try {
                    return Executor.executeIf((Value) operands.get(0), (Value) operands.get(1), (Value) operands.get(2));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case EVAL -> {
                try {
                    return Executor.eval((String) operands.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case EXPORT -> {
                return Executor.executeExport((Value) operands.get(0));
            }
            case FIRST, LAST, BUTFIRST, BUTLAST -> {
                return Executor.unaryWord(operator, (Value) operands.get(0));
            }
            case RUN -> {
                return Executor.executeList((Value) operands.get(0));
            }
            case SQRT, INT, RANDOM -> {
                return Executor.unaryMath(operator, (Value) operands.get(0));
            }
            default -> throw new UnsupportedOperationException("Unsupported instruction");
        }
    }

    @Override
    public void repr(boolean LF) {
        System.out.printf("Instruction@0x%s%s", this.hashCode(), LF ? "\n" : "");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
