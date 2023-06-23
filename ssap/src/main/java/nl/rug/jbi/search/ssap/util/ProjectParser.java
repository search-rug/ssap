package nl.rug.jbi.search.ssap.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilities for Java projects (ProjectContainer) (e.g., get methods from a given class).
 *
 * @author Andrei Dumitriu, Daniel Feitosa
 */
public class ProjectParser {

    private static ProjectParser projectParser = null;
    private ProjectParser() {}

    public static ProjectParser getInstance() {
        if (projectParser == null) {
            projectParser = new ProjectParser();
        }
        return projectParser;
    }

    private static Boolean isInterface (ClassNode cn) {
        return (cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    private static Boolean isAbstract(MethodNode mn) {
        return (mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
    }

    private static ClassNode readClassNode(InputStream classIS) throws IOException {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(new DataInputStream(classIS));
        cr.accept(cn, ClassReader.SKIP_DEBUG);
        return cn;
    }

    /**
     * Get a map of all classes to their respective parents (i.e., superclass + interfaces)
     *
     * @param pc Jar or directory containing all class files
     * @return Map of classes to parents
     */
    public static HashMap<String, Set<String>> getParentsMap(ProjectContainer pc) throws IOException {
        HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
        CallbackFunction<Void> callback = (is) -> {
            ClassNode cn = readClassNode(is);
            String n = cn.name.replaceAll("/", ".");
            Set<String> p = new HashSet<>();
            p.add(cn.superName.replaceAll("/", "."));
            String[] interfaces = (String[]) cn.interfaces.toArray();
            for (String interfaceName : interfaces) {
                p.add(interfaceName.replaceAll("/", "."));
            }
            map.put(n, p);
            return null;
        };

        pc.forEachClass(callback);

        return map;
    }

    /**
     * Get all parents (incl. interfaces) of a class, recursively checking parents as well.
     *
     * @param className Class to be processed
     * @param parents A map of all classes to their respective parents
     * @return Set of class names
     */
    public static Set<String> getAllSuperclasses(String className, Map<String, Set<String>> parents) {
        Set<String> set = parents.getOrDefault(className, new HashSet<>());
        for (String parent : set) {
            set.addAll(getAllSuperclasses(parent, parents));
        }
        return set;
    }

    /**
     * Get direct children of a given class
     *
     * @param className Class to be processed
     * @param parents A map of all classes to their respective parents
     * @return List of classes
     */
    public static Set<String> getSubclasses(String className, Map<String, Set<String>> parents) {
        return parents.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(className))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get a list of all declared methods inside a class/interface
     *
     * @param pc Jar or directory containing all class files
     * @param className Class to be processed
     * @return List of method names
     */
    public static List<String> getMethodsFromClassFile(ProjectContainer pc, String className) {
        try {
            ClassNode cn = readClassNode(pc.getClassStream(className));
            return cn.methods.stream()
                    .map(method -> method.name)
                    .collect(Collectors.toList());
        }catch (Throwable e) {
            return new ArrayList<>();
        }
    }

    /**
     * Check if a class is implementing a given method. If not, it recursively checks all children, finding the first
     * implementation for each child. In the latter case, a list of classes is provided (i.e., one or more classes
     * per child).
     *
     * @param pc Jar or directory containing all class files
     * @param className Class being tested
     * @param methodName Method to be searched
     * @param parents A map of all classes to their respective parents
     * @return Set of class names
     */
    public static Set<String> getFirstImplementation(ProjectContainer pc, String className, String methodName, Map<String, Set<String>> parents) {
        try {
            ClassNode cn = readClassNode(pc.getClassStream(className));
            MethodNode mn = cn.methods.stream()
                    .filter(methodNode -> methodNode.name.equals(methodName))
                    .findFirst()
                    .get();
            if(isAbstract(mn)) {
                return getSubclasses(className, parents)
                        .stream()
                        .map(subclass -> getFirstImplementation(pc, subclass, methodName, parents))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
            }
            Set<String> set = new HashSet<>();
            set.add(className);
            return set;
        }catch (Throwable e) {
            return new HashSet<>();
        }
    }

    public static Set<String> getFirstNonInterfaces(ProjectContainer pc, String className, Map<String, Set<String>> parents) {
        try {
            ClassNode cn = readClassNode(pc.getClassStream(className));
            if(isInterface(cn)) {
                return getSubclasses(className, parents)
                        .stream()
                        .map(subclass -> getFirstNonInterfaces(pc, subclass, parents))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
            }
            Set<String> set = new HashSet<>();
            set.add(className);
            return set;
        }catch (Throwable e) {
            return new HashSet<>();
        }
    }
}
