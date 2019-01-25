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
package me.innectic.permissify.spigot.injector;

import lombok.AllArgsConstructor;
import me.innectic.permissify.spigot.utils.MiscUtil;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 5/8/18
 */
@AllArgsConstructor
public class PermissibleInjector {

    private final boolean copy;  // :revisit remove that?

    private Optional<Field> getPermissibleFieldForPlayer(Player player) throws NoSuchFieldException {
        Class<?> entity = MiscUtil.getBukkitClass("entity.CraftHumanEntity");
        if (!entity.isAssignableFrom(player.getClass())) return Optional.empty();
        Field permissibleField = entity.getDeclaredField("perm");
        permissibleField.setAccessible(true);
        return Optional.of(permissibleField);
    }

    private void copyPermissibles(PermissibleBase newBase, PermissibleBase oldBase) throws NoSuchFieldException, IllegalAccessException {
        Field attachmentField = PermissibleBase.class.getDeclaredField("attachments");
        attachmentField.setAccessible(true);

        List permissibles = (List) attachmentField.get(newBase);
        permissibles.clear();
        permissibles.addAll((List) attachmentField.get(oldBase));
        newBase.recalculatePermissions();
    }

    public Optional<Permissible> inject(Player player, Permissible permissible) throws NoSuchFieldException, IllegalAccessException {
        Optional<Field> permissibleField = getPermissibleFieldForPlayer(player);
        if (!permissibleField.isPresent()) return Optional.empty();

        Permissible oldPermissible = (Permissible) permissibleField.get().get(player);
        if (copy && (permissible.getEffectivePermissions() instanceof PermissibleBase)) {
            PermissibleBase newBase = (PermissibleBase) permissible;
            PermissibleBase oldBase = (PermissibleBase) oldPermissible.getEffectivePermissions();
            copyPermissibles(oldBase, newBase);
        }
        permissibleField.get().set(player, permissible);
        return Optional.of(oldPermissible);
    }

    public Optional<Permissible> getPermissible(Player player) throws NoSuchFieldException {
        return Optional.ofNullable((Permissible) getPermissibleFieldForPlayer(player).map(p -> {
            try {
                return p.get(player);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).orElse(null));
    }
}
