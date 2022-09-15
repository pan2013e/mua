package mua.runtime;

import mua.utils.General;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private final static HashMap<String, String> config = new HashMap<>();

    public static void setConfig(String path) {
        try {
            var in = new FileInputStream(General.getComponentPath(path));
            Scanner sc = new Scanner(in);
            while(sc.hasNext()) {
                String[] s = sc.next(".*=.*").split("=");
                config.put(s[0], s[1]);
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Unable to load config " + path);
        }

    }

    public static String get(String key) {
        String res = config.get(key);
        if(res == null) {
            res = System.getenv(key);
        }
        return res;
    }

    public static <T> T get(String k, Class<T> cls) {
        String v = get(k);
        if(v == null) {
            return null;
        }
        try {
            Method vOf = cls.getMethod("valueOf", String.class);
            return cls.cast(vOf.invoke(null, v));
        } catch (Exception e) {
            return null;
        }
    }

}
