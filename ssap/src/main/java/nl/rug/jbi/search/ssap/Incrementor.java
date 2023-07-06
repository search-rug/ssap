package nl.rug.jbi.search.ssap;

import nl.rug.jbi.search.ssap.model.Instance;
import nl.rug.jbi.search.ssap.model.Role;
import nl.rug.jbi.search.ssap.model.System;
import nl.rug.jbi.search.ssap.util.Constants;
import nl.rug.jbi.search.ssap.util.ProjectContainer;
import nl.rug.jbi.search.ssap.util.ProjectParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static void incrementPatternList(System system, Map<String, Set<String>> parents, ProjectContainer pc) {
        system.patternList.forEach(p -> {
            if (p.instanceList == null || p.instanceList.isEmpty()) return;
            switch (p.name) {
                case Constants.FACTORY_METHOD: p.instanceList.stream().forEach(i -> incrementFactoryMethod(i,parents));
                    break;
                case Constants.PROTOTYPE: p.instanceList.stream().forEach(i -> incrementPrototype(i,parents,pc));
                    //case "Singleton": //Nothing to be done
                    //case "(Object)Adapter-Command": //Nothing to be done
                    break;
                case Constants.COMPOSITE: p.instanceList.stream().forEach(i -> incrementComposite(i,parents,pc));
                    break;
                case Constants.DECORATOR: p.instanceList.stream().forEach(i -> incrementDecorator(i,parents,pc));
                    break;
                case Constants.OBSERVER: p.instanceList.stream().forEach(i -> incrementObserver(i,parents,pc));
                    break;
                case Constants.STATE_STRATEGY: p.instanceList.stream().forEach(i -> incrementStateStrategy(i,parents,pc));
                    break;
                case Constants.TEMPLATE_METHOD: p.instanceList.stream().forEach(i -> incrementTemplateMethod(i,parents,pc));
                    //case "Visitor": TODO (not necessary for the study)
                    break;
                case Constants.PROXY:
                case Constants.PROXY2: p.instanceList.stream().forEach(i -> incrementProxy(i,parents,pc));
                    break;
            }
        });
    }

    /** Updates an instance of Factory Method by adding ConcreteCreator's and Product's. */
    private static void incrementFactoryMethod(Instance instance, Map<String, Set<String>> parents) {
        instance.roleList.stream().filter(role -> role.name.equals(Constants.CREATOR)).forEach(r -> {
            for (Map.Entry<String, Set<String>>  parent : parents.entrySet()) {
                if (parent.getValue().contains(r.element)) {
                    instance.roleList.add(new Role(parent.getKey(), Constants.CONCRETE_CREATOR));
                }
            }
        });

        instance.roleList.stream().filter(role -> role.name.equals(Constants.FACTORY_METHOD_PARENTHESIS)).forEach(r -> {
            Matcher matcher = elementRegex.matcher(r.element);
            try {
                String rName = matcher.group(3);
                instance.roleList.add(new Role(rName, Constants.PRODUCT));
            } catch (Exception e) {
                logger.debug("FactoryMethod: no elementRegex match");
            }
        });
    }

    /** Updates an instance of Prototype by adding ConcretePrototype's. */
    private static void incrementPrototype(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.PROTOTYPE, Constants.CONCRETE_PROTOTYPE);
    }

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

    private static void incrementDecorator(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
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
                            .forEach(cc -> instance.roleList.add(new Role(cc, Constants.CONCRETE_DECORATOR)));
                });
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

    private static List<String> getCandidates(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        String component = findFirstElement(instance, Constants.COMPONENT);
        List<String> methods = instance.roleList.stream()
                .filter(r -> r.name.equals(Constants.OPERATION_PARENTHESIS))
                .map(role -> {
                    Matcher elemnetMatcher = elementRegex.matcher(role.element);
                    try {
                        String mSign = elemnetMatcher.group(2);
                        Matcher methodMatcher = methodRegex.matcher(mSign);
                        String mName = methodMatcher.group(1);
                        return mName;
                    } catch (Exception e) {
                        logger.debug("getCandidates: no elementRegex or methodRegex match");
                        return "";
                    }
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

    private static void incrementObserver(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.OBSERVER, Constants.CONCRETE_OBSERVER);
    }

    private static void incrementStateStrategy(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.STATE_SLASH_STRATEGY, Constants.CONCRETE_STATE_SLASH_STRATEGY);
    }

    private static void incrementTemplateMethod(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        addRolesFromNonInterfaces(instance, parents, pc, Constants.ABSTRACT_CLASS, Constants.CONCRETE_CLASS);
    }

    private static void addRolesFromNonInterfaces(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc, String filter, String roleName) {
        instance.roleList.stream().filter(role -> role.name.equals(filter)).forEach(r -> {
            parents.entrySet().stream()
                    .filter(parent -> parent.getValue().contains(r.element))
                    .map(parent -> ProjectParser.getFirstNonInterfaces(pc, parent.getKey(), parents))
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(s -> instance.roleList.add(new Role(s, roleName)));
        });
    }

    private static void incrementProxy(Instance instance, Map<String, Set<String>> parents, ProjectContainer pc) {
        String proxy = findFirstElement(instance, Constants.PROXY);
        String realSubject = findFirstElement(instance, Constants.REAL_SUBJECT);
        Set<String> proxyParents = ProjectParser.getAllSuperclasses(proxy, parents);
        Set<String> rsParents = ProjectParser.getAllSuperclasses(realSubject, parents);
        String roleElement = findFirstElement(instance, Constants.REQUEST_PARANTHESIS);
        String mName;
        try {
            Matcher elementMatcher = elementRegex.matcher(roleElement);
            String mSign = elementMatcher.group(2);
            Matcher methodMatcher = methodRegex.matcher(mSign);
            mName = methodMatcher.group(1);
        } catch (Exception e) {
            logger.debug("incrementProxy: no elementRegex or methodRegex match");
            return;
        }

        proxyParents.stream()
                .filter(rsParents::contains)
                .forEach(s -> {
                    if (parents.containsKey(s) && ProjectParser.getMethodsFromClassFile(pc, s).contains(mName)) {
                        instance.roleList.add(new Role(s, Constants.SUBJECT));
                    }

                });
    }

    private static String findFirstElement(Instance instance, String filter) {
        return instance.roleList.stream()
                .filter(r -> r.name.equals(filter))
                .findFirst()
                .orElse(new Role("", ""))
                .element;
    }
}
