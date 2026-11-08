package me.innectic.permissify.api.permission;

import java.util.List;

public record PermissionDecision(String permission, boolean granted, String membership, String source,
                                 List<String> path, int weight, long expiresAt) {
    public PermissionDecision {
        path = List.copyOf(path);
    }
}
