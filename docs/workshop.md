# Steam Workshop text for SettingsForge

Paste the description into the upload box in the game. Steam uses BBCode, not Markdown.

## Title

SettingsForge (Client-Side)

## Short summary

A Mod Settings page in the game's Settings screen, plus a small library so other mods can add checkboxes, sliders and dropdowns.

## Tags

Client-side, Mod, Utility (pick whatever fits from the list the game shows)

## Description

```
[h1]SettingsForge[/h1]
Necesse has no settings screen for mods. SettingsForge adds one.

[h2]For players[/h2]
[list]
[*] Adds a [b]Mod Settings[/b] button to the Settings screen, under the vanilla Mods button
[*] Mods that support it put their checkboxes, sliders and dropdowns on that page
[*] Save keeps your changes, Cancel (or Esc) throws them away
[*] Settings are saved by the game itself with the rest of your client settings
[*] Includes a small demo section so you can see how it works
[/list]
SettingsForge does nothing on its own besides the demo. It is a library, so you only need it when another mod tells you to install it.

[h2]For mod authors[/h2]
Register your settings in a few lines and read them back by id. Labels come from your own language file.
[code]
public static final ModOptions options = ModOptions.create("my.modid")
        .addSection("display")
        .addBoolean("showClock", true)
        .addInt("clockScale", 100, 50, 200)
        .addSelection("clockCorner", 0, "topleft", "topright");

public ModSettings initSettings() {
    return options;
}
[/code]
Full API and language file keys are in the README on GitHub.

[h2]Safe by design[/h2]
There is one small hook into the game's Settings screen. If a game update ever breaks it, the Mod Settings button just does not show up and a line is written to the log. The game keeps working.

[h2]Requirements[/h2]
[list]
[*] Client-side only, servers do not need it
[*] Built for Necesse 1.3.3
[/list]

[url=https://github.com/Kkthnx-Necesse/SettingsForge]Source and API docs on GitHub[/url]
```

## Change note for version 1.0

First release.
