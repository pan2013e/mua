package mua.runtime;

import mua.utils.Property;

import java.util.Arrays;

public class Reflex {

    public static void initProperties(Object instance) {
        Class<?> cls = instance.getClass();
        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Property.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Property annot = field.getAnnotation(Property.class);
                        field.set(instance, annot.type().cast(Config.get(annot.name(), annot.type())));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

}
