package net.auoeke.template;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.asm.ClassNode2;
import net.auoeke.reflect.asm.MethodNode2;
import org.objectweb.asm.Type;

/* a class template */
public abstract class Template {
    private static final List<Class<?>> supportedAnnotations = List.of(Copy.class, Repeat.class);

    private final ClassNode2 classNode = new ClassNode2().reader(this.getClass().getName()).read();

    public Template() {
        Methods.of(this.getClass()).forEach(method -> {
            for (var annotations = Stream.of(method.getDeclaredAnnotations()).filter(annotation -> supportedAnnotations.contains(annotation.annotationType())).toList(); !annotations.isEmpty();) {
                var node = this.node(method);


            }
        });
    }

    private MethodNode2 node(Method method) {
        return (MethodNode2) this.classNode.methods.stream().filter(node -> node.name.equals(method.getName()) && node.desc.equals(Type.getMethodDescriptor(method))).findAny().get();
    }
}
