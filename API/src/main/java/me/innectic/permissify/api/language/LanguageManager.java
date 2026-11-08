package me.innectic.permissify.api.language;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    private final JsonObject defaults;
    private final Map<String, JsonObject> languages = new ConcurrentHashMap<>();
    private volatile JsonObject translations = new JsonObject();

    public LanguageManager() {
        var stream = LanguageManager.class.getResourceAsStream("/languages/en_US.json");
        if (stream == null) throw new IllegalStateException("Missing default language");
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            defaults = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read default language", e);
        }
    }

    public void load(Path file) throws IOException {
        translations = read(file);
    }

    public void load(String locale, Path file) throws IOException {
        String name = normalize(locale);
        if (name.isEmpty()) throw new IllegalArgumentException("Invalid locale: " + locale);
        languages.put(name, read(file));
    }

    /**
     * Use the client locale, then the server language, then English.
     */
    public Language getLanguage(String locale) {
        List<JsonObject> catalogs = new ArrayList<>();
        String name = normalize(locale);
        while (!name.isEmpty()) {
            JsonObject catalog = languages.get(name);
            if (catalog != null) catalogs.add(catalog);
            int separator = name.lastIndexOf('-');
            name = separator == -1 ? "" : name.substring(0, separator);
        }
        catalogs.add(translations);
        catalogs.add(defaults);
        return new Language(catalogs);
    }

    public Language getLanguage() {
        return getLanguage(null);
    }

    public void useEnglish() {
        translations = new JsonObject();
    }

    public void saveDefault(Path file) throws IOException {
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(defaults, writer);
        }
    }

    private JsonObject read(Path file) throws IOException {
        JsonObject loaded;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) throw new IllegalArgumentException("Language must be a JSON object");
            loaded = parsed.getAsJsonObject();
        }
        for (var entry : loaded.entrySet()) {
            validate(entry.getKey(), entry.getValue(), defaults.get(entry.getKey()));
        }
        return loaded;
    }

    private String normalize(String locale) {
        if (locale == null || locale.isBlank()) return "";
        try {
            return new Locale.Builder().setLanguageTag(locale.replace('_', '-')).build().toLanguageTag().toLowerCase(Locale.ROOT);
        } catch (IllformedLocaleException e) {
            return "";
        }
    }

    private void validate(String key, JsonElement value, JsonElement fallback) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            if (fallback == null || fallback.isJsonPrimitive()) return;
        } else if (value.isJsonArray() && (fallback == null || fallback.isJsonArray())) {
            var array = value.getAsJsonArray();
            if (fallback != null && !fallback.getAsJsonArray().isEmpty() && array.isEmpty())
                throw new IllegalArgumentException("Empty message list: " + key);
            JsonElement item = fallback == null || fallback.getAsJsonArray().isEmpty()
                    ? null : fallback.getAsJsonArray().get(0);
            for (JsonElement child : array) validate(key, child, item);
            return;
        }
        throw new IllegalArgumentException("Invalid message: " + key);
    }
}
