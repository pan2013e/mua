package mua.utils;

import mua.Main;
import mua.runtime.Executor;
import mua.types.Word;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class General {

    public static final String CLASS_PATH =
            URLDecoder.decode(Objects.requireNonNull(Main.class.getResource("/")).toString().substring(5),
                    StandardCharsets.UTF_8);

    public static String getComponentPath(String component) {
        component = component.replace(".", File.separator);
        return CLASS_PATH + component;
    }

    public static void loadLib(String lib) {
        try {
            String path = getComponentPath(lib);
            if(!path.endsWith(".mua")) {
                path = path + ".mua";
            }
            Executor.deserializeMemLayout(new Word(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadLib(String... libs) {
        for(var lib : libs) {
            loadLib(lib);
        }
    }

    public static void err(String arg, Object... var) {
        System.err.printf(arg, var);
    }

}
