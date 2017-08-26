package me.innectic.permissify.api.profile;

import lombok.AllArgsConstructor;

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
     * @param profile the profile file
     * @return        the profile, fulfilled if done, empty otherwise.
     */
    public Optional<PermissifyProfile> deserialize(String profile) {
        Optional<PermissifyProfile> deserialized = Optional.empty();
        try {
            long start = System.currentTimeMillis();
            FileInputStream inputStream = new FileInputStream(profile);
            ObjectInputStream input = new ObjectInputStream(inputStream);
            deserialized = Optional.of((PermissifyProfile) input.readObject());
            long end = System.currentTimeMillis();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserialized;
    }
}
