package me.innectic.permissify.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 7/1/2017
 */
@AllArgsConstructor
public enum FormatterType {
    CHAT("chat"), WHISPER("whisper");
    @Getter private String usageName;
}
