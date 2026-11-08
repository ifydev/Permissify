package me.innectic.permissify.api.permission;

import java.util.*;

public class GroupInheritance {

    public static List<Permission> getPermissions(PermissionGroup group, Map<String, PermissionGroup> groups) {
        return resolve(group, groups).values().stream()
                .map(rule -> new Permission(rule.permission(), rule.granted())).toList();
    }

    public static Map<String, PermissionDecision> resolve(PermissionGroup group, Map<String, PermissionGroup> groups) {
        return collect(group, lookup(groups), new HashSet<>(), new HashMap<>());
    }

    public static boolean wins(PermissionDecision candidate, PermissionDecision current) {
        return candidate.weight() > current.weight()
                || (candidate.weight() == current.weight() && !candidate.granted() && current.granted());
    }

    public static boolean inheritsFrom(PermissionGroup child, PermissionGroup parent, Map<String, PermissionGroup> groups) {
        return reaches(child, parent.getName(), lookup(groups), new HashSet<>());
    }

    public static boolean canInherit(PermissionGroup parent, PermissionGroup child, Map<String, PermissionGroup> groups) {
        return !parent.getName().equalsIgnoreCase(child.getName()) && !inheritsFrom(parent, child, groups);
    }

    public static void validate(Map<String, PermissionGroup> groups) {
        Map<String, PermissionGroup> lookup = lookup(groups);
        Set<String> complete = new HashSet<>();
        for (PermissionGroup group : lookup.values()) validate(group, lookup, new HashSet<>(), complete);
    }

    private static Map<String, PermissionGroup> lookup(Map<String, PermissionGroup> groups) {
        Map<String, PermissionGroup> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        groups.values().forEach(group -> lookup.put(group.getName(), group));
        return lookup;
    }

    private static Map<String, PermissionDecision> collect(PermissionGroup group, Map<String, PermissionGroup> groups,
                                                          Set<String> active, Map<String, Map<String, PermissionDecision>> cache) {
        String key = group.getName().toLowerCase(Locale.ROOT);
        if (cache.containsKey(key)) return cache.get(key);
        Map<String, PermissionDecision> rules = new TreeMap<>();
        if (!active.add(key)) return rules;
        group.getParents().stream().map(groups::get).filter(Objects::nonNull)
                .sorted(Comparator.comparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER)).forEach(parent ->
                    collect(parent, groups, active, cache).forEach((node, inherited) -> {
                        PermissionDecision candidate = new PermissionDecision(node, inherited.granted(), parent.getName(),
                                inherited.source(), inherited.path(), parent.getWeight(), 0);
                        PermissionDecision current = rules.get(node);
                        if (current == null || wins(candidate, current)) rules.put(node, candidate);
                    }));
        group.getPermissions().forEach(permission -> rules.put(permission.getPermission().toLowerCase(Locale.ROOT),
                new PermissionDecision(permission.getPermission().toLowerCase(Locale.ROOT), permission.isGranted(), group.getName(),
                        group.getName(), List.of(), group.getWeight(), 0)));
        rules.replaceAll((node, rule) -> {
            List<String> route = new ArrayList<>();
            route.add(group.getName());
            route.addAll(rule.path());
            return new PermissionDecision(node, rule.granted(), group.getName(), rule.source(), route, group.getWeight(), 0);
        });
        active.remove(key);
        cache.put(key, rules);
        return rules;
    }

    private static boolean reaches(PermissionGroup group, String target, Map<String, PermissionGroup> groups, Set<String> visited) {
        if (!visited.add(group.getName().toLowerCase(Locale.ROOT))) return false;
        for (String name : group.getParents()) {
            if (name.equalsIgnoreCase(target)) return true;
            PermissionGroup parent = groups.get(name);
            if (parent != null && reaches(parent, target, groups, visited)) return true;
        }
        return false;
    }

    private static void validate(PermissionGroup group, Map<String, PermissionGroup> groups, Set<String> active, Set<String> complete) {
        String name = group.getName().toLowerCase(Locale.ROOT);
        if (complete.contains(name)) return;
        if (!active.add(name)) throw new IllegalArgumentException("Group inheritance contains a cycle");
        for (String parentName : group.getParents()) {
            PermissionGroup parent = groups.get(parentName);
            if (parent == null) throw new IllegalArgumentException("Unknown parent group: " + parentName);
            validate(parent, groups, active, complete);
        }
        active.remove(name);
        complete.add(name);
    }
}
