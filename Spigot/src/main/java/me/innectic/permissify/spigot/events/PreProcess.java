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
package me.innectic.permissify.spigot.events;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.module.registry.ModuleProvider;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class PreProcess implements Listener {

    private Set<String> validWhisperCommands = new HashSet<>(Arrays.asList("/msg", "/whisper", "/tell"));

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!PermissifyMain.getInstance().isHandleChat()) return;
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent()) return;
        String command = e.getMessage();
        if (!validWhisperCommands.contains(command)) return;
        e.setCancelled(true);
        // We don't care about the command at the beginning
        String[] arguments = ArgumentUtil.getRemainingArgs(1, command.split(" "));
        Player player = Bukkit.getPlayer(arguments[0]);
        if (player == null) {
            e.getPlayer().sendMessage(PermissifyConstants.INVALID_PLAYER);
            return;
        }
        String message = String.join(" ", ArgumentUtil.getRemainingArgs(1, arguments));
        String response = (String) PermissifyMain.getInstance().getPermissifyAPI().getModuleProvider().pushEvent("whisper",
                e.getPlayer().getUniqueId(), e.getPlayer().getName(), player.getUniqueId(), player.getName(), message);
        if (response == null) return;
        String readable = ColorUtil.makeReadable(response);

        player.sendMessage(readable);
        e.getPlayer().sendMessage(readable);
    }
}
