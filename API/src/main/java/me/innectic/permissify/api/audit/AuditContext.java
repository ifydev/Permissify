package me.innectic.permissify.api.audit;

public final class AuditContext implements AutoCloseable {

    public record Actor(String name, String id, String type, String command) {}

    private static final ThreadLocal<Actor> CURRENT = new ThreadLocal<>();
    private final Actor previous;

    private AuditContext(Actor actor) {
        previous = CURRENT.get();
        CURRENT.set(actor);
    }

    public static AuditContext open(String name, String id, String type, String command) {
        return new AuditContext(new Actor(name, id, type, command));
    }

    public static Actor actor() {
        Actor actor = CURRENT.get();
        return actor == null ? new Actor("SYSTEM", "", "SYSTEM", "") : actor;
    }

    @Override
    public void close() {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
}
