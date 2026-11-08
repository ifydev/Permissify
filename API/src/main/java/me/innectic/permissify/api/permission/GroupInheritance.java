package me.innectic.permissify.api.permission;

import java.util.*;

public class GroupInheritance {

    public static List<Permission> getPermissions(PermissionGroup group, Map<String, PermissionGroup> groups) {
        Map<String, PermissionGroup> lookup = lookup(groups);
        Map<String, Permission> permissions = new LinkedHashMap<>();
        collect(group, lookup, new HashSet<>(), permissions);
        return List.copyOf(permissions.values());
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

    private static void collect(PermissionGroup group, Map<String, PermissionGroup> groups, Set<String> visited, Map<String, Permission> permissions) {
        if (!visited.add(group.getName().toLowerCase(Locale.ROOT))) return;
        group.getParents().stream().sorted(String.CASE_INSENSITIVE_ORDER).map(groups::get).filter(Objects::nonNull)
                .forEach(parent -> collect(parent, groups, visited, permissions));
        group.getPermissions().forEach(permission -> permissions.put(permission.getPermission(),
                new Permission(permission.getPermission(), permission.isGranted())));
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
