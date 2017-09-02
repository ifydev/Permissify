/*
*
* This file is part of Permissify, licensed under the MIT License (MIT).
* Copyright (c) Innectic
* Copyright (c) contributors
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
 */
package me.innectic.permissify.api.group.ladder;

import lombok.Getter;

import java.util.*;

/**
 * @author Innectic
 * @since 9/1/2017
 */
public abstract class AbstractLadder {
    @Getter protected Map<UUID, Integer> players = new HashMap<>();
    @Getter protected List<LadderLevel> levels = new ArrayList<>();

    public final void reset() {
        drop();
        registerLadders();
    }

    private void drop() {
        players = new HashMap<>();
        levels = new ArrayList<>();
    }

    public abstract void registerLadders();

    public final void addPlayer(UUID uuid, int position) {
        players.put(uuid, position);
        // TODO: Apply extra permissions
    }

    public final void removePlayer(UUID uuid) {
        if (players.containsKey(uuid)) players.remove(uuid);
        // TODO: Remove extra permissions
    }

    @Override
    public String toString() {
        return "AbstractLadder [" +
                "players=" + players +
                ", levels=" + levels +
                "]";
    }
}
