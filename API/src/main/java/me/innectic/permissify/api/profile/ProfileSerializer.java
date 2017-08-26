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
package me.innectic.permissify.api.profile;

import java.io.*;
import java.util.Optional;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class ProfileSerializer {

    /**
     * Serialize a permissify profile into a permissify profile file
     *
     * @param profile       the profile to serialize
     * @param baseDirectory the base of the directory to store the profiles in
     * @param profileName   the name of the profile
     * @return              if the profile was successfully stored
     */
    public boolean serialize(PermissifyProfile profile, String baseDirectory, String profileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream(baseDirectory + "/profiles/" + profileName + ".permissify");
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(profile);
            out.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Deserialize a permissify profile file into a real permissify profile
     *
     * @param profile       the profile file
     * @param baseDirectory the base directory that files are located in
     * @return              the profile, fulfilled if done, empty otherwise.
     */
    public Optional<PermissifyProfile> deserialize(String profile, String baseDirectory) {
        Optional<PermissifyProfile> deserialized = Optional.empty();
        try {
            FileInputStream inputStream = new FileInputStream(baseDirectory + "/profiles/" + profile + ".permissify");
            ObjectInputStream input = new ObjectInputStream(inputStream);
            deserialized = Optional.of((PermissifyProfile) input.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserialized;
    }
}
