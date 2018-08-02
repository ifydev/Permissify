# Permissify

Permissify is a next-generation permission plugin for Spigot and Sponge.

We're trying to build a permissions plugins that is dead-simple to use, commands that don't confuse you,
and most importantly, *no errors caused by the plugin*.

# Builds

You can download the latest version of Permissify from the [releases](https://github.com/IfyDev/Permissify/releases) page.

# Contributing

Take a look at the `Issues` page to get started! Make sure to follow the code standards, though!

# Standards

The standards for Permissify's code are really simple.

Make sure things that will be used between the different implementations are in the API
Make sure messages & permissions are in the constants file.
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
