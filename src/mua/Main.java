package mua;

import mua.parser.Parser;
import mua.runtime.Config;
import mua.runtime.Executor;
import mua.runtime.MemLayout;
import mua.runtime.Reflex;
import mua.types.Word;
import mua.utils.General;

public class Main {

    static {
        General.loadLib("mua.lib.basics", "mua.lib.math");
        Config.setConfig("mua.runtime.properties");
        Reflex.initProperties(MemLayout.getInstance());
    }

    private static void parseArgs(String[] args) {
        for(var arg : args) {
            if(arg.matches("-X.*")) {
                continue;
            }
            try {
                if(Boolean.TRUE.equals(Config.get("mua.stats.time", Boolean.class))) {
                    long start = System.currentTimeMillis();
                    Executor.deserializeMemLayout(new Word(arg));
                    long end = System.currentTimeMillis();
                    System.out.println("Execution time: " + (end - start) + "ms");
                } else {
                    Executor.deserializeMemLayout(new Word(arg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
        Parser parser = new Parser();
        parser.REPL();
    }
}
