package nl.rug.jbi.search.ssap;

import org.objectweb.asm.*;

import java.io.IOException;

public class ClassGenerator {

    public static final String BASIC_CLASS = "BasicClass";
    public static final String ABSTRACT_CLASS = "AbstractClass";
    public static final String INTERFACE= "Interface";
    public static final String IMPLEMENTER_CLASS = "ImplementerClass";
    public static final String EXTENDER_CLASS = "ExtenderClass";
    public static final String MANY_PARENTS_CLASS = "ManyParentsClass";

    public static final String BASIC_METHOD = "basicMethod";

    public static final String[] ALL_MOCK_CLASSES = new String[]{BASIC_CLASS, ABSTRACT_CLASS, INTERFACE, IMPLEMENTER_CLASS, EXTENDER_CLASS, MANY_PARENTS_CLASS};
    public static final String[] NO_PARENT_CLASSES = new String[]{BASIC_CLASS};
    public static final String[] ABSTRACT_AND_EXTENDER = new String[]{ABSTRACT_CLASS, EXTENDER_CLASS};
    public static final String[] INTERFACE_AND_IMPLEMENTER = new String[]{INTERFACE, IMPLEMENTER_CLASS};
    public static final String[] MANY_PARENTS_CLASSES = new String[]{ABSTRACT_CLASS, EXTENDER_CLASS, INTERFACE, MANY_PARENTS_CLASS};

    /**
     *
     * @return a byte[] representing various classes and an interface
     * @throws IOException
     */
    public static byte[] generateClass(String className) throws IOException {
        // Create a ClassWriter with COMPUTE_FRAMES and COMPUTE_MAXS flags
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String superClass = "java/lang/Object";

        switch (className) {
            case BASIC_CLASS:
                // Generate a basic class
                visitClass(cw, BASIC_CLASS, superClass, null, Opcodes.ACC_PUBLIC);
                break;
            case ABSTRACT_CLASS:
                // Generate an abstract class
                visitClass(cw, ABSTRACT_CLASS, superClass, new String[0], Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT);
                break;
            case EXTENDER_CLASS:
                // Generate a class extending the abstract class
                visitClass(cw, EXTENDER_CLASS, ABSTRACT_CLASS, new String[0], Opcodes.ACC_PUBLIC);
                break;
            case INTERFACE:
                // Generate an interface
                generateInterface(cw, INTERFACE);
                break;
            case IMPLEMENTER_CLASS:
                // Generate a class implementing the interface
                visitClass(cw, IMPLEMENTER_CLASS, superClass, new String[]{INTERFACE}, Opcodes.ACC_PUBLIC);
                break;
            case MANY_PARENTS_CLASS:
                // Generate a class that implements an interface and extends a superclass that itself has a paraent
                visitClass(cw, MANY_PARENTS_CLASS, EXTENDER_CLASS, new String[]{INTERFACE}, Opcodes.ACC_PUBLIC);
            default:
                break;
        }

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
    private static void visitClass(ClassWriter cw, String className, String superClass, String[] interfaces, int access) {
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

        // Creating a method; the abstract class will only have an abstract method
        MethodVisitor mv = cw.visitMethod(access, BASIC_METHOD, "()V", null, null);
        if (!className.equals(ABSTRACT_CLASS)) {
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

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
