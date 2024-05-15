import org.objectweb.asm.*;

import java.io.IOException;

public class ClassGenerator {

    /**
     *
     * @return a byte[] representing various classes and an interface
     * @throws IOException
     */
    public static byte[] generateClasses() throws IOException {
        // Create a ClassWriter with COMPUTE_FRAMES and COMPUTE_MAXS flags
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Define the superclass and interfaces
        String superClass = "java/lang/Object";

        // Generate a basic class
        generateClass(cw, "BasicClass", superClass, null, Opcodes.ACC_PUBLIC);

        // Generate an abstract class extending BasicClass
        generateClass(cw, "AbstractClass", "BasicClass", new String[0], Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT);

        // Generate an interface
        generateInterface(cw, "Interface");

        // Generate a class implementing Interface
        generateClass(cw, "ImplementerClass", superClass, new String[]{"Interface"}, Opcodes.ACC_PUBLIC);

        // Generate a class extending ImplementerClass
        generateClass(cw, "ExtenderClass", "ImplementerClass", new String[0], Opcodes.ACC_PUBLIC);

        return cw.toByteArray();
    }

    /**
     *
     * @param cw the ClassWriter responsible for creating this class
     * @param className the name of the generated class
     * @param superClass superclass of the generated class
     * @param interfaces interfaces implemented by the generated class
     * @param access what kind of access the class should have, e.g. public (=1)
     */
    private static void generateClass(ClassWriter cw, String className, String superClass, String[] interfaces, int access) {
        // Visiting the class we're generating
        cw.visit(Opcodes.V1_8, access, className, null, superClass, interfaces);

        // Creating a default constructor
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0); // Load "this"
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass, "<init>", "()V", false); // Call super constructor
        constructor.visitInsn(Opcodes.RETURN); // Return
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        // Creating a method
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "basicMethod", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // End the class definition
        cw.visitEnd();
    }

    /**
     *
     * @param cw the ClassWriter responsible for creating this interface
     * @param interfaceName the name of the generated interface
     */
    private static void generateInterface(ClassWriter cw, String interfaceName) {
        // Visit the interface we're generating
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_INTERFACE, interfaceName, null, "java/lang/Object", null);

        cw.visitEnd();
    }
}
