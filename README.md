# Permissify

Permissify is a next-generation permission plugin for Spigot and Sponge.

We're trying to build a permissions plugins that is dead-simple to use, commands that don't confuse you,
and most importantly, *no errors caused by the plugin*.

# Builds

You can download the latest version of Permissify from the [releases](https://github.com/IfyDev/Permissify/releases) page.

## Commands

Permissify commands over RCON require `allow-rcon: true` in the plugin's
`config.yml`. The default is `false`, including when the option is absent
from an existing config. Restart after changing it. This setting applies to
Permissify commands; the local server console remains available.

Player commands use the player name first, for example:

```
/permissify player addgroup Innectic testing
/permissify player addpermission Innectic example.permission [seconds]
/permissify player primarygroup Innectic [group]
```

Names match exactly, ignoring case. Online players and players known to this
server can be targeted. /perm is an alias for /permissify.

Profile loads merge the saved groups, memberships and permissions into the
current database. They preserve unrelated records and save a backup first.

## Translations

Players receive messages using `Player#getLocale()`. Language files are loaded
from `plugins/Permissify/languages` at startup. Start the plugin once to create
`en_US.json`, then copy it to a locale such as `es_ES.json` and translate the
values. Restart after adding or editing a file. Only English is bundled.

Locales ignore case and accept `_` or `-`. Missing messages fall back from the
exact locale (`es_MX`) to the language (`es`), then the server language, then
built-in English. Set `language: en_US` in `config.yml` to choose the server
language, which is also used for console and command blocks. An invalid file
logs a warning and does not prevent other locales from loading.

Files use UTF-8 JSON. Keep the message keys, command names, `<GROUP>`-style
placeholders and `&` color codes. `PERMISSIFY_PREFIX` changes the prefix used
by messages containing `<PREFIX>`. Help pages are arrays of pages; error
messages are arrays of lines. Files can contain just the keys they change:

```json
{
    "GROUP_CREATED": "<PREFIX>&e&lGrupo <GROUP> creado."
}
```

The API has no Spigot dependency:

```java
LanguageManager manager = api.getLanguageManager();
manager.load("es_ES", Path.of("languages", "es_ES.json"));
Language language = manager.getLanguage("es_es");
String message = language.format("GROUP_CREATED", Map.of("GROUP", "friends"));
```

`Language` reads strings, lists and help pages through `getMessage`,
`getMessages` and `getPages`. Each instance keeps its own fallback chain, so
handling another player's locale does not change an existing response.
`load(Path)` sets the server language; `getLanguage()` selects it without a
client locale. Failed loads leave the previous catalog intact. Messages live
in the language files; `PermissifyConstants` holds permissions and the profile
version.

# Contributing

Take a look at the `Issues` page to get started! Make sure to follow the code standards, though!

# Standards

The standards for Permissify's code are really simple.

Make sure things that will be used between the different implementations are in the API
Keep messages in the language files and permissions in the constants file.
Use your brain...

Pull requests that don't follow the standards will be ignored until they meet the standards.

And then all of the following stuff:

## FOUR. SPACE. INDENTION.

### Indention is bad, avoid it.
#### Don't:

```java
if (player.hasPermision("potato.salad")) {
    if (player.getName().equals("PotatoMemes")) {
        player.sendMessage("Wow, you're cool!");
    }
}
```

#### Do:

```java
if (!player.hasPermission("potato.salad")) return;
if (!player.getName().equals("PotatoMemes")) return;
player.sendMessage("Wow, you're cool!");
```

### Put `{`, `(` and similar on the same line

#### Don't
```java
public void things()
{
    if (something)
    {
        // Stuff
    }
}
```

#### Do:
```java
public void things() {
    if (something) {
        // stuff
    }
}
```
