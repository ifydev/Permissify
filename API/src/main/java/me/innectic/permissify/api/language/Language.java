package me.innectic.permissify.api.language;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Language {

    private static final Pattern PLACEHOLDER = Pattern.compile("<([^<>]+)>");
    private final List<JsonObject> catalogs;

    Language(List<JsonObject> catalogs) {
        this.catalogs = List.copyOf(catalogs);
    }

    public String getMessage(String key) {
        JsonElement value = find(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new IllegalArgumentException("Not a message: " + key);
        return render(value.getAsString());
    }

    /**
     * Replace <PLACEHOLDERS> without changing their values.
     */
    public String format(String key, Map<String, ?> placeholders) {
        Matcher matcher = PLACEHOLDER.matcher(getMessage(key));
        return matcher.replaceAll(match -> {
            Object value = placeholders.get(match.group(1));
            return Matcher.quoteReplacement(value == null ? match.group() : value.toString());
        });
    }

    public List<String> getMessages(String key) {
        JsonElement value = find(key);
        if (!value.isJsonArray()) throw new IllegalArgumentException("Not a message list: " + key);
        return value.getAsJsonArray().asList().stream().map(line -> render(line.getAsString())).toList();
    }

    public List<List<String>> getPages(String key) {
        JsonElement value = find(key);
        if (!value.isJsonArray()) throw new IllegalArgumentException("Not a page list: " + key);
        return value.getAsJsonArray().asList().stream().map(page -> page.getAsJsonArray().asList().stream()
                .map(line -> render(line.getAsString())).toList()).toList();
    }

    private String render(String value) {
        return value.replace("<PREFIX>", find("PERMISSIFY_PREFIX").getAsString());
    }

    private JsonElement find(String key) {
        for (JsonObject catalog : catalogs) {
            if (catalog.has(key)) return catalog.get(key);
        }
        throw new IllegalArgumentException("Unknown message: " + key);
    }
}
