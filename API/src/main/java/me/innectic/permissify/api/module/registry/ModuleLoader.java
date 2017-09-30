package me.innectic.permissify.api.module.registry;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.module.registry.schema.ModuleFile;

import java.io.*;
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
    public void loadModules(Object plugin) {
        File moduleFolder = new File(moduleDirectory);
        if (!moduleFolder.exists()) moduleFolder.mkdir();
        if (!moduleFolder.isDirectory()) return;
        File[] potentialModules = moduleFolder.listFiles();
        if (potentialModules == null) return;

        Gson gson = new Gson();
        Arrays.stream(potentialModules).map(File::getName).filter(n -> n.endsWith(".jar")).filter(n -> n.contains("."))
                .map(n -> n.split("\\.")[0]).forEach(file -> {
            File jarFile;
            jarFile = new File(moduleDirectory + "/" + file + ".jar");
            try {
                URLClassLoader child = new URLClassLoader(new URL[]{jarFile.toURL()}, this.getClass().getClassLoader());
                InputStream pmodStream = child.findResource(file + ".pmod").openStream();
                if (pmodStream == null) return;

                BufferedInputStream inputStream = new BufferedInputStream(pmodStream);
                InputStreamReader reader = new InputStreamReader(inputStream);
                ModuleFile moduleFile = gson.fromJson(reader, ModuleFile.class);

                Class loading = Class.forName(moduleFile.getMain(), true, child);
                if (loading == null) return;
                PermissifyAPI.get().ifPresent(api ->
                        api.getModuleProvider().registerModule(loading, moduleFile.getName(), plugin));
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
