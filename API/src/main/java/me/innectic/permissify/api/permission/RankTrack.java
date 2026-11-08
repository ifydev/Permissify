package me.innectic.permissify.api.permission;

import java.util.List;

public record RankTrack(String name, List<String> groups) {
    public RankTrack {
        groups = List.copyOf(groups);
    }
}
