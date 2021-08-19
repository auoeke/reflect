package net.auoeke.reflect.test;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarFile;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.ReflectTest;
import net.auoeke.reflect.asm.ClassNode2;
import net.auoeke.reflect.experimental.Classes2;
import net.auoeke.reflect.experimental.generics.Generics;
import net.auoeke.reflect.experimental.generics.TypeArgument;
import net.auoeke.reflect.generics.GenericTypeAware;
import net.auoeke.reflect.generics.GenericTypeAwareTest;
import net.auoeke.reflect.util.Logger;
import net.auoeke.reflect.util.Util;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.openjdk.jol.info.ClassData;
import user11681.uncheck.ThrowingFunction;

public class IrrelevantTest {
    public static <T, R> R var(T object, Function<T, R> function) {
        return function.apply(object);
    }

    public static <T> void var(T object, Consumer<T> consumer) {
        consumer.accept(object);
    }

    @Test
    public void accessFlags() {
        Class<?> klass = ReflectTest.class;
        ClassData data = ClassData.parseInstance(klass);

        Util.bp();
    }

    @Test
    public void addClass() {
        Classes2.addClass(String.class, Integer.class);

        String integer = (String) (Object) 0;
        Integer string = (Integer) (Object) "";
    }

    @Test
    public void multipleInheritance() {
        long IntegerPointer = Classes.klass(Integer.class) & 0xFFFFFFFFL;
        long StringPointer = Classes.klass(String.class) & 0xFFFFFFFFL;

        Unsafe.putAddress(IntegerPointer + 4 * 19, StringPointer);

        String string = (String) (Object) 0;
    }

    @Test
    public void genericMetadata() {
        Type[] interfaces = GenericTypeAwareTest.class.getGenericInterfaces();
        Type superclass = GenericTypeAwareTest.class.getGenericSuperclass();
        Type[] parameters = GenericTypeAware.class.getTypeParameters();
        List<TypeArgument> typeArguments = Generics.typeArguments(GenericTypeAwareTest.Sub.Sub1.class);

        Util.bp();
    }

    @Test
    public void genericTypeAware() {
        GenericTypeAwareTest typeAware = new GenericTypeAwareTest();

        Logger.log(typeAware.type);
    }

    @Test
    void enumMethodHandle() throws Throwable {
        Invoker.findConstructor(RetentionPolicy.class, String.class, int.class).invoke(null, 1);
    }

    @Test
    void getObject() throws Throwable {
        class C {
            final int field = 374;
        }

        assert (Integer) Unsafe.getObject(new C(), Unsafe.objectFieldOffset(C.class.getDeclaredField("field"))) == 374;
    }

    @Test
    void allocateClass() throws Throwable {
        Unsafe.allocateInstance(Class.class);
    }

    @Test
    void ensureJava8() throws Throwable {
         Files.list(Path.of(System.getProperty("user.dir")).getParent().resolve("build").resolve("libs"))
            .filter(jar -> jar.getFileName().toString().matches("reflect-\\d+\\.\\d+\\.\\d+\\.jar"))
            .map((ThrowingFunction<Path, JarFile>) jar -> new JarFile(jar.toFile()))
            .forEach(artifact -> {
                assert artifact.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .map(entry -> new ClassNode2().reader(artifact, entry).read())
                    .allMatch(klass -> klass.version == Opcodes.V1_8);
            });

        throw new AssertionError("class version");
    }

    @Test
    void visibilities() {
        Consumer<Function<Class<?>, Member[]>> count = members -> {
            int pub = 0;
            int pri = 0;

            for (Class<?> type : ByteBuddyAgent.install().getAllLoadedClasses()) {
                for (Member method : members.apply(type)) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        ++pub;
                    } else if (Modifier.isPrivate(method.getModifiers())) {
                        ++pri;
                    }
                }
            }

            System.out.printf("private: %d;%npublic: %d;%n%f%% private%n%n", pri, pub, pri / (float) (pub + pri) * 100);
        };

        System.out.println("fields");
        count.accept(Fields::rawFields);

        System.out.println("methods");
        count.accept(Methods::getRawMethods);
    }

    static {
        var(new Object() {
            Object field0 = "field0";
            Object field1 = "field1";
        }, Object -> {
            Object.field0 = "new0";
            Object.field1 = "new1";
        });

        System.setProperty("jol.tryWithSudo", "true");
    }
}