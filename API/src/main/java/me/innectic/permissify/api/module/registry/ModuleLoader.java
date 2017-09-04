package me.innectic.permissify.api.module.registry;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.module.registry.schema.ModuleFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * @author Innectic
 * @since 9/4/2017
 */
@AllArgsConstructor
public class ModuleLoader {
    private final String moduleDirectory;

    /**
     * Load all modules in the module directory.
     */
    public void loadModules() {
        File moduleFolder = new File(moduleDirectory);
        if (!moduleFolder.exists()) moduleFolder.mkdir();
        if (!moduleFolder.isDirectory()) return;
        File[] potentialModules = moduleFolder.listFiles();
        if (potentialModules == null) return;

        Gson gson = new Gson();
        Arrays.stream(potentialModules).forEach(file -> {
            File jarFile;
            File pmodFile;

            String baseName = file.getName().contains(".") ? file.getName().split(".")[0] : null;
            if (baseName == null) return;

            jarFile = new File(moduleDirectory + "/" + baseName + ".jar");
            pmodFile = new File(moduleDirectory + "/" + baseName + ".pmod");

            try {
                Reader reader = new FileReader(pmodFile);
                ModuleFile moduleFile = gson.fromJson(reader, ModuleFile.class);

                URLClassLoader child = new URLClassLoader(new URL[]{jarFile.toURL()}, this.getClass().getClassLoader());
                Class loading = Class.forName(moduleFile.getMain(), true, child);
                if (loading == null) return;
                PermissifyAPI.get().ifPresent(api ->
                        api.getModuleProvider().registerModule(loading, moduleFile.getName()));
            } catch (FileNotFoundException | MalformedURLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
}
