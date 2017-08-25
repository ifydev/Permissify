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
package me.innectic.permissify.api.database.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.innectic.permissify.api.database.DatabaseHandler;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Innectic
 * @since 6/8/2017
 */
@AllArgsConstructor
public enum HandlerType {

    MYSQL(SQLHandler.class, "MySQL"), SQLITE(SQLHandler.class, "SQLite");

    @Getter private Class<? extends DatabaseHandler> handler;
    @Getter private String displayName;

    /**
     * Get the type of a handler from a name
     *
     * @param type the name of the handler to attempt to find
     * @return the handler. Filled if found, empty otherwise.
     */
    public static Optional<HandlerType> findType(String type) {
        return Arrays.stream(values()).filter(handler -> handler.getDisplayName().equalsIgnoreCase(type)).findFirst();
    }
}
