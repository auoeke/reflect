package net.auoeke.reflect.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.generator.base.method.expression.GetField;

@Testable
public class ConstantPoolBuilder extends TestBuilder {
    private ConstantPoolBuilder() {
        super("net.auoeke.reflect.ConstantPool");

        this.pub();
    }

    @Override
    @Test
    protected void all() throws Throwable {
        super.all();
    }

    @Override
    @AfterEach
    protected void tearDown() {
        super.tearDown();
    }

    @Test
    public void generate() {
        for (Method method : Methods.getMethods(Classes.ConstantPool)) {
            Class<?> returnType = method.getReturnType();

            if (returnType.getModule().isExported(returnType.getPackageName()) && (method.getModifiers() & Modifier.NATIVE) == 0) {
                this.methodHandle(new GetField().of(Classes.class).name("ConstantPool"), method);
            }
        }
    }
}
