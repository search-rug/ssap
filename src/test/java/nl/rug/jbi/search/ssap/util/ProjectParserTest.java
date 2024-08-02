package nl.rug.jbi.search.ssap.util;

import nl.rug.jbi.search.ssap.MockedContainer;

import static nl.rug.jbi.search.ssap.ClassGenerator.*;
import static nl.rug.jbi.search.ssap.util.ProjectParser.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectParserTest {

    @Test
    public void testGetParentsMapNoParents() throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        assertTrue(parentsMap.get(BASIC_CLASS).isEmpty());
    }

    @Test
    public void testGetParentsMapWithInterface() throws IOException {
        ProjectContainer pc = new MockedContainer(INTERFACE_AND_IMPLEMENTER);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> parents = parentsMap.get(IMPLEMENTER_CLASS);
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(INTERFACE));


    }

    @Test
    public void testGetParentsMapWithSuperclass() throws IOException {
        ProjectContainer pc = new MockedContainer(ABSTRACT_AND_EXTENDER);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> parents = parentsMap.get(EXTENDER_CLASS);
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(ABSTRACT_CLASS));
    }

    @Test
    public void testGetParentsMapWithSuperclassAndInterface() throws IOException {
        ProjectContainer pc = new MockedContainer(MANY_PARENTS_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> parents = parentsMap.get(MANY_PARENTS_CLASS);
        assertNotNull(parents);
        assertEquals(2, parents.size());
        assertTrue(parents.contains(EXTENDER_CLASS));
        assertTrue(parents.contains(INTERFACE));
    }

    @Test
    public void testGetAllAncestorsNoParents() throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> ancestors = getAllAncestors(BASIC_CLASS, parentsMap);
        assertNotNull(ancestors);
        assertEquals(0, ancestors.size());
    }

    @Test
    public void testGetAllAncestorsDepthOne() throws IOException {
        ProjectContainer pc = new MockedContainer(ABSTRACT_AND_EXTENDER);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> ancestors = getAllAncestors(EXTENDER_CLASS, parentsMap);
        assertNotNull(ancestors);
        assertEquals(1, ancestors.size());
        assertTrue(ancestors.contains(ABSTRACT_CLASS));
    }

    @Test
    public void testGetAllAncestorsDepthTwo() throws IOException {
        ProjectContainer pc = new MockedContainer(MANY_PARENTS_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> ancestors = getAllAncestors(MANY_PARENTS_CLASS, parentsMap);
        assertNotNull(ancestors);
        assertEquals(3, ancestors.size());
        assertTrue(ancestors.contains(ABSTRACT_CLASS));
        assertTrue(ancestors.contains(EXTENDER_CLASS));
        assertTrue(ancestors.contains(INTERFACE));
    }

    @Test
    public void testGetSubclassesNoSubclass() throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> subclasses = getSubclasses(BASIC_CLASS, parentsMap);

        assertNotNull(subclasses);
        assertEquals(0, subclasses.size());
    }

    @Test
    public void testGetSubclasses() throws IOException {
        ProjectContainer pc = new MockedContainer(MANY_PARENTS_CLASSES);

        HashMap<String, Set<String>> parentsMap;
        parentsMap = getParentsMap(pc);

        assertNotNull(parentsMap);

        Set<String> subclasses = getSubclasses(ABSTRACT_CLASS, parentsMap);

        assertNotNull(subclasses);
        assertEquals(1, subclasses.size());
        assertTrue(subclasses.contains(EXTENDER_CLASS));
    }

    @Test
    public void testGetMethodsFromClassFile() throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);
        List<String> methods = getMethodsFromClassFile(pc, BASIC_CLASS);
        assertNotNull(methods);
        assertEquals(1, methods.size());
        assertTrue(methods.contains(BASIC_METHOD));
    }

    @Test
    public void testGetFirstImplementationDepthZero () throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);

        Map<String, Set<String>> parentsMap = getParentsMap(pc);

        Set<String> firstImplementation = getFirstImplementation(pc, BASIC_CLASS, BASIC_METHOD, parentsMap);

        assertNotNull(firstImplementation);
        assertEquals(1, firstImplementation.size());
        assertTrue(firstImplementation.contains(BASIC_CLASS));
    }

    @Test
    public void testGetFirstImplementationDepthOne () throws IOException {
        ProjectContainer pc = new MockedContainer(ABSTRACT_AND_EXTENDER);

        Map<String, Set<String>> parentsMap = getParentsMap(pc);

        Set<String> firstImplementation = getFirstImplementation(pc, ABSTRACT_CLASS, BASIC_METHOD, parentsMap);

        assertNotNull(firstImplementation);
        assertEquals(1, firstImplementation.size());
        assertTrue(firstImplementation.contains(EXTENDER_CLASS));
    }

    @Test
    public void testGetFirstNonInterfacesDepthZero () throws IOException {
        ProjectContainer pc = new MockedContainer(NO_PARENT_CLASSES);

        Map<String, Set<String>> parentsMap = getParentsMap(pc);

        Set<String> nonInterfaces = getFirstNonInterfaces(pc, BASIC_CLASS, parentsMap);

        assertNotNull(nonInterfaces);
        assertEquals(1, nonInterfaces.size());
        assertTrue(nonInterfaces.contains(BASIC_CLASS));
    }

    @Test
    public void testGetFirstNonInterfacesDepthOne () throws IOException {
        ProjectContainer pc = new MockedContainer(INTERFACE_AND_IMPLEMENTER);

        Map<String, Set<String>> parentsMap = getParentsMap(pc);

        Set<String> nonInterfaces = getFirstNonInterfaces(pc, INTERFACE, parentsMap);

        assertNotNull(nonInterfaces);
        assertEquals(1, nonInterfaces.size());
        assertTrue(nonInterfaces.contains(IMPLEMENTER_CLASS));
    }
}
