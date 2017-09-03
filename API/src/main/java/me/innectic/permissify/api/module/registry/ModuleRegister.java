package me.innectic.permissify.api.module.registry;

import lombok.Getter;
import me.innectic.permissify.api.module.PermissifyModule;
import me.innectic.permissify.api.module.annotation.Handles;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public class ModuleRegister {
    @Getter private static Method chatHandler;
    @Getter private static Method whisperHandler;

    @Getter private static Map<String, PermissifyModule> modules = new HashMap<>();

    public static void setChatHandler(Method chatHandler, String source) {
        if (!source.equals("internal"))
            System.out.printf("Permissify chat handler has been overriden by %s! Disable before reporting any bugs!\n", source);
        ModuleRegister.chatHandler = chatHandler;
    }

    public static void setWhisperHandler(Method whisperHandler, String source) {
        if (!source.equals("internal"))
            System.out.printf("Permissify whisper handler has been overriden by %s! Disable before reporting any bugs!\n", source);
        ModuleRegister.whisperHandler = whisperHandler;
    }

    public static void registerModule(Class<? extends PermissifyModule> module, String source) {
        try {
            Constructor<? extends PermissifyModule> moduleConstructor = module.getConstructor();
            PermissifyModule constructedModule = moduleConstructor.newInstance();

            modules.put(constructedModule.getModuleName(), constructedModule);

            Arrays.stream(constructedModule.getClass().getMethods()).filter(method -> method.isAnnotationPresent(Handles.class)).forEach(method -> {
                Handles handleAnnotation = method.getAnnotation(Handles.class);
                if (handleAnnotation.value().equals("chat")) ModuleRegister.setChatHandler(method, source);
                else if (handleAnnotation.value().equals("whisper")) ModuleRegister.setWhisperHandler(method, source);
            });
            System.out.println(String.format("Module %s has been registered!", constructedModule.getModuleName()));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // TODO: Permissify error reporting
            e.printStackTrace();
        }
    }
}
