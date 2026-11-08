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

## Group inheritance

`/permissify group inherit A B` makes B inherit A's permissions. Both groups
must already exist. The command requires `permissify.basic` and
`permissify.group.inherit` (or superadmin access).

```
/permissify group addpermission A mine.a.access
/permissify group addpermission B mine.b.access
/permissify group inherit A B
/permissify group listpermissions B
```

B now grants `mine.a.access` and `mine.b.access`. Changes to A apply to B and
its descendants immediately, including the client command list. Inheritance
can span multiple levels and include multiple parents. Self-inheritance and
cycles are rejected. A group's own permission values take precedence over
inherited values; direct player permissions retain their existing precedence.

Remove a direct inheritance link with `/permissify group uninherit A B`.
This requires `permissify.group.uninherit`. B can still inherit A through
another parent, if another path exists.

Inheritance survives cache reloads, server restarts and profile save/load.
Older profiles without inheritance still load. Deleting a parent removes its
inheritance links. Group membership, primary group and formatting remain
separate from inherited permissions.

## Temporary access

Both commands accept an optional positive duration in seconds:

```
/permissify player addpermission Innectic example.permission 3600
/permissify player addgroup Innectic VIP 2592000
```

Deadlines are stored as absolute timestamps in the database. Time offline or
with the server stopped still counts. Overdue entries are removed on startup;
while running, the plugin checks expiry every second and refreshes affected
players' permissions and command lists. Cache reloads and profiles preserve
the original deadlines. Removing an entry and adding it permanently does not
leave an old timer that can revoke the replacement.

The database adds expiry columns automatically. Existing entries remain
permanent because previous versions did not store their deadlines. Remove
and re-add any old temporary grants that should now have a saved expiry.

## Rank tracks

Tracks define promotion order separately from group inheritance:

```
/permissify track create prison
/permissify track append prison A
/permissify track append prison B
/permissify track append prison C
/permissify track info prison
/permissify track list
/permissify player promote Innectic prison
/permissify player demote Innectic prison
```

The first promotion adds a player to the first rank when they are not already
on the track. Further promotions and demotions replace their current track
membership with the adjacent rank. Unrelated memberships are preserved. The
primary group follows the move when it was the old rank; an unrelated primary
group is preserved. Temporary rank memberships keep their original expiry.

Moving beyond either end is rejected. Players with multiple memberships on
the same track must have the extra memberships removed before moving.

```
/permissify track remove prison B
/permissify track delete prison
```

Editing or deleting a track does not remove player memberships. Deleting an
actual group also removes it from tracks. Tracks persist through restarts and
profile save/load; older profiles without tracks or expiry data still load.

Track management uses `permissify.track.create`, `.append`, `.remove`, `.info`,
`.list` and `.delete`. Movement uses `permissify.player.promote` and
`permissify.player.demote`. Commands also require `permissify.basic` unless
the sender has superadmin access. Help and completion use the same checks.

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
