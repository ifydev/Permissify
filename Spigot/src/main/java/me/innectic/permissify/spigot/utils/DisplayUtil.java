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
package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.api.PermissifyConstants;

import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.api.database.ConnectionError;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class DisplayUtil implements me.innectic.permissify.api.util.DisplayUtil {

    @Override
    public void displayError(ConnectionError error, Optional<Exception> exception) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!PermissionUtil.hasPermissionOrSuperAdmin(player, PermissifyConstants.PERMISSIFY_ADMIN)) continue;
                var language = LanguageUtil.getLanguage(plugin.getPermissifyAPI().getLanguageManager(), player);
                String reportable = language.getMessage(error == ConnectionError.DATABASE_EXCEPTION ? "REPORT_YES" : "REPORT_NO");
                language.getMessages("PERMISSIFY_ERROR").stream()
                        .map(line -> line.replace("<ERROR_TYPE>", language.getMessage("CONNECTION_" + error.name())))
                        .map(line -> line.replace("<SHOULD_REPORT>", reportable))
                        .map(ColorUtil::makeReadable).forEach(player::sendMessage);
            }
        });
    }
}