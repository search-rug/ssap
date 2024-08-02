package nl.rug.jbi.search.ssap;

import nl.rug.jbi.search.ssap.model.Instance;
import nl.rug.jbi.search.ssap.model.Role;
import nl.rug.jbi.search.ssap.model.System;
import nl.rug.jbi.search.ssap.util.Constants;
import nl.rug.jbi.search.ssap.util.ProjectContainer;
import nl.rug.jbi.search.ssap.util.ProjectParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Incrementor {

    private static Incrementor incrementor = null;

    public static final Logger logger = LogManager.getLogger(Incrementor.class);

    private static final Pattern elementRegex = Pattern.compile("(.*)::(.*):(.*)");
    private static final Pattern elementRegex_NoReturn = Pattern.compile("(.*)::(.*)");
    private static final Pattern methodRegex = Pattern.compile("(.*)(\\(.*\\))");

    private Incrementor() {}

    public Incrementor getIncrementor() {
        if(incrementor == null) {
            incrementor = new Incrementor();
        }
        return incrementor;
    }

    /**
     * Increments a provided list of pattern instances by adding extra information regarding instances elements.
     * The additions are described in the document 'SSA+.md'
     *
     * @param system List of patterns to be updated
     * @param parents Map of system classes to their parents
     * @param pc The project being analyzed
     */
    public static void incrementPatternList(System system, Map<String, Set<String>> parents, ProjectContainer pc) {
        system.patternList.forEach(p -> {
            if (p.instanceList == null || p.instanceList.isEmpty()) return;
            switch (p.name) {
                case Constants.FACTORY_METHOD: p.instanceList.stream().forEach(i -> incrementFactoryMethod(i,parents));
                    break;
                case Constants.PROTOTYPE: p.instanceList.stream().forEach(i -> incrementPrototype(i,parents,pc));
                    break;
                // case "Singleton": Nothing to be done
                // case "(Object)Adapter-Command": Nothing to be done
                case Constants.COMPOSITE: p.instanceList.stream().forEach(i -> incrementComposite(i,parents,pc));
                    break;
                case Constants.DECORATOR: p.instanceList.stream().forEach(i -> incrementDecorator(i,parents,pc));
                    break;
                case Constants.OBSERVER: p.instanceList.stream().forEach(i -> incrementObserver(i,parents,pc));
                    break;
                case Constants.STATE_STRATEGY: p.instanceList.stream().forEach(i -> incrementStateStrategy(i,parents,pc));
                    break;
                case Constants.TEMPLATE_METHOD: p.instanceList.stream().forEach(i -> incrementTemplateMethod(i,parents,pc));
                    break;
                //case "Visitor": TODO (not necessary for the study)
                case Constants.PROXY:
                case Constants.PROXY2: p.instanceList.stream().forEach(i -> incrementProxy(i,parents,pc));
                    break;
            }
        });
    }

    /** Updates an instance of Factory Method by adding ConcreteCreator's and Product's. */
    private static void incrementFactoryMethod(Instance instance, Map<String, Set<String>> parents) {
        List<Role> newRoles = new ArrayList<>();
        instance.roleList.stream().filter(role -> role.name.equals(Constants.CREATOR)).forEach(r -> {
            for (Map.Entry<String, Set<String>>  parent : parents.entrySet()) {
                if (parent.getValue().contains(r.element)) {
                    newRoles.add(new Role(parent.getKey(), Constants.CONCRETE_CREATOR));
                }
            }
        });

        instance.roleList.stream().filter(role -> role.name.equals(Constants.FACTORY_METHOD_PARENTHESIS)).forEach(r -> {
            Matcher matcher = elementRegex.matcher(r.element);

            if (matcher.matches()) {
                try {
                    String rName = matcher.group(3);
                    newRoles.add(new Role(rName, Constants.PRODUCT));
                } catch (Exception e) {
                    logger.error("FactoryMethod: no elementRegex match");
                }
            }
        });

        instance.roleList.addAll(newRoles);
    }

    /** Updates an instance of Prototype by adding ConcretePrototype's. */
    private static void incrementPrototype(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.PROTOTYPE, Constants.CONCRETE_PROTOTYPE);
    }

    /** Updates an instance of Composite by adding Leaves. */
    private static void incrementComposite(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        List<String> candidates = getCandidates(instance, parents, pc);
        List<String> composites = instance.roleList.stream()
                .filter(parent -> parent.name.equals(Constants.COMPOSITE))
                .map(parent -> parent.element)
                .collect(Collectors.toList());
        candidates.forEach(c -> {
            if (!composites.contains(c)) {
                instance.roleList.add(new Role(c, Constants.LEAF));
            }
        });

    }

    /** Updates an instance of Decorator by adding ConcreteDecorator's and ConcreteComponent's. */
    private static void incrementDecorator(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        List<Role> newRoles = new ArrayList<>();
        List<String> candidates = getCandidates(instance, parents, pc);
        List<String> decorators = instance.roleList.stream()
                .filter(r -> r.name.equals(Constants.DECORATOR))
                .map(r -> r.element)
                .collect(Collectors.toList());
        decorators.stream()
                .forEach(d -> {
                    parents.entrySet().stream()
                            .filter(p -> p.getValue().contains(d))
                            .map(p -> ProjectParser.getFirstNonInterfaces(pc, p.getKey(), parents))
                            .flatMap(Collection::stream)
                            .distinct()
                            .forEach(cc -> newRoles.add(new Role(cc, Constants.CONCRETE_DECORATOR)));
                });
        instance.roleList.addAll(newRoles);
        List<String> concrDecorators = instance.roleList.stream()
                .filter(r -> r.name.equals(Constants.CONCRETE_DECORATOR))
                .map(r -> r.element)
                .collect(Collectors.toList());
        candidates.stream()
                .forEach(c -> {
                    if (!decorators.contains(c) && !concrDecorators.contains(c)) {
                        instance.roleList.add(new Role(c, Constants.CONCRETE_COMPONENT));
                    }
                });
    }

    /** Find candidates to add for the Composite or Decorator patterns */
    private static List<String> getCandidates(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        String component = findFirstElement(instance, Constants.COMPONENT);
        List<String> methods = instance.roleList.stream()
                .filter(r -> r.name.equals(Constants.OPERATION_PARENTHESIS))
                .map(role -> {
                    String mName = "";
                    Matcher elemnetMatcher = elementRegex.matcher(role.element);
                    if (elemnetMatcher.matches()) {
                        try {
                            String mSign = elemnetMatcher.group(2);
                            Matcher methodMatcher = methodRegex.matcher(mSign);
                            if (methodMatcher.matches()) {
                                mName = methodMatcher.group(1);
                            }
                        } catch (Exception e) {
                            logger.debug("getCandidates: no elementRegex or methodRegex match");
                        }
                    }
                    return mName;
                })
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList());
        List<String> candidates = methods.stream()
                .map(s -> ProjectParser.getFirstImplementation(pc, component, s, parents))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            candidates = parents.entrySet().stream()
                    .filter(parent -> parent.getValue().contains(component))
                    .map(parent -> ProjectParser.getFirstNonInterfaces(pc, parent.getKey(), parents))
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return candidates;
    }

    /** Updates an instance of Observer by adding ConcreteObserver's. */
    private static void incrementObserver(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.OBSERVER, Constants.CONCRETE_OBSERVER);
    }

    /** Updates an instance of State/Strategy by adding ConcreteState/Strategy's. */
    private static void incrementStateStrategy(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.STATE_SLASH_STRATEGY, Constants.CONCRETE_STATE_SLASH_STRATEGY);
    }

    /** Updates an instance of Template Method by adding ConcreteClass's. */
    private static void incrementTemplateMethod(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.ABSTRACT_CLASS, Constants.CONCRETE_CLASS);
    }

    /** Add new roles from non-interface classes */
    private static void addRolesFromNonInterfaces(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc, String filter, String roleName) {
        List<Role> newRoles = new ArrayList<>();
        instance.roleList.stream().filter(role -> role.name.equals(filter)).forEach(r -> {
            parents.entrySet().stream()
                    .filter(parent -> parent.getValue().contains(r.element))
                    .map(parent -> ProjectParser.getFirstNonInterfaces(pc, parent.getKey(), parents))
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(s -> newRoles.add(new Role(s, roleName)));
        });
        instance.roleList.addAll(newRoles);
    }

    /** Updates an instance of Proxy by adding Subject. */
    private static void incrementProxy(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        String proxy = findFirstElement(instance, Constants.PROXY);
        String realSubject = findFirstElement(instance, Constants.REAL_SUBJECT);
        Set<String> proxyParents = ProjectParser.getAllAncestors(proxy, parents);
        Set<String> rsParents = ProjectParser.getAllAncestors(realSubject, parents);
        String roleElement = findFirstElement(instance, Constants.REQUEST_PARANTHESIS);
        String mName="";
        Matcher elementMatcher = elementRegex.matcher(roleElement);
        if (elementMatcher.matches()) {
            try {
                String mSign = elementMatcher.group(2);
                Matcher methodMatcher = methodRegex.matcher(mSign);
                if (methodMatcher.matches()) {
                    mName = methodMatcher.group(1);
                }
            } catch (Exception e) {
                logger.debug("incrementProxy: no elementRegex or methodRegex match");
                return;
            }
        }
        if (!mName.isEmpty()) {
            String finalMName = mName;
            proxyParents.stream()
                    .filter(rsParents::contains)
                    .forEach(s -> {
                        if (parents.containsKey(s) && ProjectParser.getMethodsFromClassFile(pc, s).contains(finalMName)) {
                            instance.roleList.add(new Role(s, Constants.SUBJECT));
                        }

                    });
        }
    }

    /** Find the first element of the instance that matches the given filter. */
    private static String findFirstElement(Instance instance, String filter) {
        return instance.roleList.stream()
                .filter(r -> r.name.equals(filter))
                .findFirst()
                .orElse(new Role("", ""))
                .element;
    }
}
