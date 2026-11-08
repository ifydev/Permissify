package me.innectic.permissify.api.module.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.module.PermissifyModule;
import me.innectic.permissify.api.module.annotation.Handles;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public class ModuleProvider {
    @Getter private Map<String, PermissifyModule> modules = new HashMap<>();
    @Getter private Map<String, Module> eventHandlers = new HashMap<>();

    public void registerModule(Class<? extends PermissifyModule> module, String source, Object plugin) {
        try {
            Constructor<? extends PermissifyModule> moduleConstructor = module.getConstructor();
            PermissifyModule constructedModule = moduleConstructor.newInstance();

            modules.put(constructedModule.getModuleName(), constructedModule);

            // Initialize the module
            constructedModule.initialize(plugin);

            Arrays.stream(constructedModule.getClass().getMethods()).filter(method -> method.isAnnotationPresent(Handles.class)).forEach(method -> {
                Handles handleAnnotation = method.getAnnotation(Handles.class);
                String event = handleAnnotation.event();

                // Store the handler
                eventHandlers.put(event, new Module(constructedModule.getModuleName(), method));
                reportRegistration(source, event);
            });
            PermissifyAPI.get().ifPresent(api -> api.getLogger().log(Level.WARNING,
                    String.format("Module %s has been registered!", constructedModule.getModuleName())));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // TODO: Permissify error reporting
            e.printStackTrace();
        }
    }

    public void end(Object plugin) {
        modules.values().stream().forEach(module -> module.deinitialize(plugin));
    }

    public Object pushEvent(String event, Object... arguments) {
        if (!eventHandlers.containsKey(event)) return null;
        try {
            return eventHandlers.get(event).getMethod().invoke(modules.get(eventHandlers.get(event).getModule()), arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void reportRegistration(String moduleName, String event) {
        if (moduleName.equals("permissify")) return;
        PermissifyAPI.get().ifPresent(api -> api.getLogger().log(Level.WARNING,
                String.format("Module %s has registered event %s. Try removing it before reporting any bugs!", moduleName, event)));
    }

    @AllArgsConstructor
    private class Module {
        @Getter private String module;
        @Getter private Method method;
    }
}
