package me.innectic.permissify.api.audit;

public record AuditEntry(long id, long timestamp, String actor, String actorId, String type,
                         String action, String target, String details, String command) {}
