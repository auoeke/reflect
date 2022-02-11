package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;

public class JavaLangAccess {
    public static final Object javaLangAccess = Invoker.invoke(Invoker.findStatic(Classes.SharedSecrets, "getJavaLangAccess", Classes.JavaLangAccess));

    private static final MethodHandle getConstantPool = Invoker.bind(javaLangAccess, "getConstantPool", Classes.ConstantPool, Class.class);

    public static Object getConstantPool(Class<?> klass) {
        return getConstantPool.invoke(klass);
    }
}
