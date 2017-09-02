package me.innectic.permissify.api.group.ladder.impl;

import me.innectic.permissify.api.group.ladder.AbstractLadder;
import me.innectic.permissify.api.group.ladder.LadderLevel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public class LadderBuilder extends AbstractLadder {

    public LadderBuilder(Map<UUID, Integer> players, List<LadderLevel> levels) {
        this.players = players;
        this.levels = levels;
    }

    @Override
    public void registerLadders() {
        // This does nothing, since we set all the ladders in the constructor
    }
}
