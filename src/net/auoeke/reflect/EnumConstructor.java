package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import static net.auoeke.reflect.Reflect.run;

public class EnumConstructor<E extends Enum<E>> {
    private static final int ENUM_VALUES = Flags.PRIVATE | Flags.STATIC | Flags.SYNTHETIC | Flags.FINAL;

    private static final MethodHandle getEnumVars;
    private static final Pointer enumConstantPointer;
    private static final Pointer enumConstantDirectoryPointer;
    private static final CacheMap<Class<?>, Pointer> arrayFields = CacheMap.identity();
    private static final CacheMap<Class<?>, EnumConstructor<?>> constructors = CacheMap.identity();

    public final Class<E> type;
    public final MethodHandle constructor;

    protected EnumConstructor(Class<E> type, Class<?>... parameterTypes) {
        this(constructor(type, parameterTypes));
    }

    protected EnumConstructor(Constructor<E> constructor) {
        this.type = constructor.getDeclaringClass();
        this.constructor = Invoker.unreflectConstructor(constructor).asSpreader(Object[].class, constructor.getParameterCount() - 2);
    }

    public static <E extends Enum<E>> Constructor<E> constructor(Class<E> type, Object... arguments) {
        return Constructors.find(Types.DEFAULT_CONVERSION, 2, type, arguments);
    }

    public static <E extends Enum<E>> Constructor<E> constructor(long flags, Class<E> type, Object... arguments) {
        return Constructors.find(flags, 2, type, arguments);
    }

    public static <E extends Enum<E>> Constructor<E> constructor(Class<E> type, Class<?>... parameterTypes) {
        return Constructors.find(2, type, parameterTypes);
    }

    public static <E extends Enum<E>> EnumConstructor<E> get(long flags, Class<E> type, Object... arguments) {
        return (EnumConstructor<E>) constructors.computeIfAbsent(type, type1 -> new EnumConstructor<>(constructor(flags, (Class<E>) type1, arguments)));
    }

    public static <E extends Enum<E>> EnumConstructor<E> get(Class<E> type, Object... arguments) {
        return get(Types.DEFAULT_CONVERSION, type, arguments);
    }

    public static <E extends Enum<E>> EnumConstructor<E> get(Class<E> type) {
        return (EnumConstructor<E>) constructors.computeIfAbsent(type, type1 -> new EnumConstructor<>(constructor(type)));
    }

    public static <E extends Enum<E>> EnumConstructor<E> get(Class<E> type, Class<?>... parameterTypes) {
        return (EnumConstructor<E>) constructors.computeIfAbsent(type, type1 -> new EnumConstructor<>((Class<E>) type1, parameterTypes));
    }

    public static <E extends Enum<E>> E add(Class<E> type, String name, Object... arguments) {
        return add(Types.DEFAULT_CONVERSION, type, nextOrdinal(type), name, arguments);
    }

    public static <E extends Enum<E>> E add(Class<E> type, int ordinal, String name, Object... arguments) {
        return add(Types.DEFAULT_CONVERSION, type, ordinal, name, arguments);
    }

    public static <E extends Enum<E>> E add(long flags, Class<E> type, String name, Object... arguments) {
        return add(flags, type, nextOrdinal(type), name, arguments);
    }

    public static <E extends Enum<E>> E add(long flags, Class<E> type, int ordinal, String name, Object... arguments) {
        return add(type, construct(flags, type, ordinal, name, arguments));
    }

    public static <E extends Enum<E>> E add(Class<E> type, E enumConstant) {
        var enumArrayPointer = enumArray(type);
        E[] values = enumArrayPointer.getT();
        var newValues = Arrays.copyOf(values, values.length + 1);
        newValues[values.length] = enumConstant;

        enumArrayPointer.putReference(newValues);
        enumConstants(type).putReference(newValues);
        enumConstantDirectory(type).putReference(null);

        return enumConstant;
    }

    public static <E extends Enum<E>> E construct(Class<E> type, String name, Object... arguments) {
        return get(Types.DEFAULT_CONVERSION, type, arguments).construct(nextOrdinal(type), name, arguments);
    }

    public static <E extends Enum<E>> E construct(Class<E> type, int ordinal, String name, Object... arguments) {
        return get(Types.DEFAULT_CONVERSION, type, arguments).construct(ordinal, name, arguments);
    }

    public static <E extends Enum<E>> E construct(long flags, Class<E> type, String name, Object... arguments) {
        return get(flags, type, arguments).construct(nextOrdinal(type), name, arguments);
    }

    public static <E extends Enum<E>> E construct(long flags, Class<E> type, int ordinal, String name, Object... arguments) {
        return get(flags, type, arguments).construct(ordinal, name, arguments);
    }

    public static Pointer enumConstantDirectory(Class<?> type) {
        return enumConstantDirectoryPointer.clone().bind(getEnumVars == null ? type : run(() -> getEnumVars.invoke(type)));
    }

    public static Pointer enumConstants(Class<?> type) {
        return enumConstantPointer.clone().bind(getEnumVars == null ? type : run(() -> getEnumVars.invoke(type)));

    }

    public static Pointer enumArray(Class<?> type) {
        return arrayFields.computeIfAbsent(type, type1 -> {
            for (var field : Fields.direct(type1)) {
                if (isValueField(field)) {
                    type1.getEnumConstants();

                    return new Pointer().staticField(field);
                }
            }

            return null;
        });
    }

    public static boolean isValueField(Field field) {
        if (Flags.not(field, ENUM_VALUES)) {
            return false;
        }

        var owner = field.getDeclaringClass();
        return owner.isEnum() && field.getType().componentType() == owner;
    }

    public static int nextOrdinal(Class<?> type) {
        return type.getEnumConstants().length;
    }

    public E construct(int ordinal, String name, Object... arguments) {
        return run(() -> (E) this.constructor.invoke(name, ordinal, arguments));
    }

    public E construct(String name, Object... arguments) {
        return this.construct(nextOrdinal(this.type), name, arguments);
    }

    public E add(int ordinal, String name, Object... arguments) {
        return add(this.type, this.construct(ordinal, name, arguments));
    }

    public E add(String name, Object... arguments) {
        return this.add(nextOrdinal(this.type), name, arguments);
    }

    static {
        var type = Classes.load(Class.class.getName() + "$EnumVars");

        if (type == null) {
            enumConstantPointer = new Pointer().instanceField(Class.class, "enumConstants");
            enumConstantDirectoryPointer = new Pointer().instanceField(Class.class, "enumConstantDirectory");
            getEnumVars = null;
        } else {
            enumConstantPointer = new Pointer().instanceField(type, "cachedEnumConstants");
            enumConstantDirectoryPointer = new Pointer().instanceField(type, "cachedEnumConstantDirectory");
            getEnumVars = Invoker.findSpecial(Class.class, "getEnumVars", type);
        }
    }
}
