package me.innectic.permissify.api.module.registry.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 9/4/2017
 */
@AllArgsConstructor
public class ModuleFile {
    @Getter private int version;
    @Getter private String name;
    @Getter private String main;

    @Override
    public String toString() {
        return "ModuleFile [" +
                "version=" + version +
                ", name=" + name +
                ", main=" + main +
                " ]";
    }
}
