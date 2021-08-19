package net.auoeke.reflect.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.asm.ClassNode2;
import net.auoeke.reflect.experimental.ReflectionFactory;
import net.auoeke.reflect.util.Logger;
import net.auoeke.reflect.util.Retainable;
import net.auoeke.reflect.util.Util;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.objectweb.asm.Opcodes;

@Testable
public class ExperimentalTest {
    @Retainable
    static long test2 = 0xFF0000000BL;
    static int test = 2;
    static int test1 = 97;
    byte byte0;
    byte byte1;
    float instance = 54.2F;

    @Test
    void trustedFinal() {
        Logger.log(Accessor.get(Fields.field(String.class, "value"), "trustedFinal"));
    }

    @Test
    void factory() {
        Field field = ReflectionFactory.field(ExperimentalTest.class, int.class, Modifier.STATIC, 0);
        assert Accessor.getInt(field) == 0xB;
        assert Accessor.getLong(field) == 0xFF0000000BL;

        field = ReflectionFactory.field(ExperimentalTest.class, int.class, Modifier.STATIC, 1);
        assert Accessor.getInt(field) == 2;

        field = ReflectionFactory.field(ExperimentalTest.class, int.class, Modifier.STATIC, 2);
        assert Accessor.getInt(field) == 97;

        Accessor.put(field, 23);
        assert test1 == 23;

        Util.bp();
    }

    @Test
    void defineClass() throws Throwable {
        var node = new ClassNode2(Opcodes.ACC_PUBLIC, "reflect/test123");
        var type = Unsafe.trustedLookup.defineClass(node.bytecode());
        var bp = true;
    }
}
