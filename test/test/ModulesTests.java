package test;

import java.lang.invoke.MethodHandles;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Modules;
import net.auoeke.uncheck.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class ModulesTests {
    @Test
    void open() {

    }

    @Test
    void open1() {
    }

    @Test
    void open2() {
    }

    @Test
    void open3() {
    }

    @Test
    void anonymize() {
    }

    @Test
    void anonymize1() {
    }

    @Test
    void anonymizeAll() {
    }

    @Test
    void anonymize2() {
    }

    @Test
    void anonymizeAll1() {
    }

    @Test
    void anonymizeAll2() throws Throwable {
        ThrowingRunnable getUnsafe = () -> {
            var theUnsafe = Classes.load("jdk.internal.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafe.trySetAccessible();
            theUnsafe.get(null);
        };

        ThrowingRunnable getIMPL_LOOKUP = () -> {
            var field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.trySetAccessible();
            field.get(null);
        };

        Assert.exception("jdk.internal.misc.Unsafe should be inaccessible.", getUnsafe);
        Assert.exception("MethodHandles.Lookup#IMPL_LOOKUP should be inaccessible.", getIMPL_LOOKUP);
        Modules.anonymizeAll();
        getUnsafe.run();
        getIMPL_LOOKUP.run();
    }

    @Test
    void allLayers() {
    }

    @Test
    void layers() {
    }

    @Test
    void allLayers1() {
    }

    @Test
    void modules() {
    }

    @Test
    void allModules() {
    }
}
