# SettingsForge (Client-Side)

A settings library for Necesse mods. Necesse has no settings screen for mods, so every mod ends up with a key bind or a config file. SettingsForge adds a **Mod Settings** page to the game's Settings screen, and a small API any mod can use to put checkboxes, sliders and dropdowns on it.

Values are saved by the game itself through its normal mod settings (`ModSettings`), together with the rest of the client settings. There is no extra file format.

---

## For players

Install it like any mod. It does nothing on its own except show a demo section. Mods that use it add their own settings to the same page.

- Open **Settings** from the main menu or from inside a world
- Click **Mod Settings** (under the vanilla Mods button, above Back)
- Change things, then **Save** or **Cancel**. Escape also cancels

Client-side only. Servers do not need it. Built for Necesse **1.3.3**.

If a game update changes the Settings screen in a way the mod cannot handle, the Mod Settings button simply does not appear and one line is written to the log. The game keeps working.

---

## For mod authors

### 1. Depend on it

In your `build.gradle`, add the SettingsForge jar to your classpath (the `mods` folder is already on it, drop the jar in `./mods/`). If the library is optional for you, list it as an optional dependency so it loads first when present:

```groovy
project.ext.modOptionalDependencies = ["kkthnx.settingsforge"]
```

If it is required, use `modDependencies` instead.

### 2. Register your settings

```java
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import settingsforge.api.ModOptions;

@ModEntry
public class MyMod {

    public static final ModOptions options = ModOptions.create("my.modid")
            .addSection("display")
            .addBoolean("showClock", true)
            .addInt("clockScale", 100, 50, 200)
            .addSelection("clockCorner", 0, "topleft", "topright", "bottomleft");

    // The game saves and loads whatever you return here
    public ModSettings initSettings() {
        return options;
    }
}
```

Read values anywhere:

```java
if (MyMod.options.getBoolean("showClock")) {
    int scale = MyMod.options.getInt("clockScale");
    String corner = MyMod.options.getSelectionChoice("clockCorner"); // "topleft"
    int cornerIndex = MyMod.options.getSelection("clockCorner");     // 0
}
```

### 3. Add labels

Put them in your mod's language file at `resources/locale/en.lang`, section `settingsui`:

```
[settingsui]
my.modid.title=My Mod
my.modid.section.display=Display
my.modid.showClock=Show clock
my.modid.clockScale=Clock size
my.modid.clockCorner=Clock corner
my.modid.clockCorner.topleft=Top left
my.modid.clockCorner.topright=Top right
my.modid.clockCorner.bottomleft=Bottom left
```

| Key | Used for | If missing |
| --- | --- | --- |
| `<modId>.title` | Heading for your mod | The mod name |
| `<modId>.section.<key>` | Section heading | The section key |
| `<modId>.<id>` | Label of a setting | The id |
| `<modId>.<id>.<choice>` | Label of a dropdown choice | The choice name |

Other languages work the same way (`de.lang`, `fr.lang` and so on). Missing translations fall back to English, then to the plain id.

### API reference

`ModOptions.create(String modId)` returns the settings holder for a mod. Calling it twice with the same id returns the same object.

| Method | Notes |
| --- | --- |
| `addSection(key)` | Everything added after it is listed under this heading |
| `addBoolean(id, default)` | Checkbox |
| `addInt(id, default, min, max)` | Slider. Values are clamped to the range |
| `addSelection(id, defaultIndex, choices...)` | Dropdown. Choices are short names, not display text |
| `getBoolean(id)` `getInt(id)` | Current value |
| `getSelection(id)` | Index of the chosen choice |
| `getSelectionChoice(id)` | Name of the chosen choice |
| `setBoolean` `setInt` `setSelection` | Change a value in memory, for example from a key bind |
| `save()` | Write all client settings to disk |

The add methods return the `ModOptions` so calls can be chained.

Rules worth knowing:

- Ids and section keys use letters, digits and underscores
- Each id is also the key in the save file. A mod that already saves a setting under the same name keeps its players' values when it moves to this library
- A dropdown is saved by choice name, not position, so you can reorder or insert choices later
- Nothing here throws. A bad id, a duplicate or a wrong type read is logged once and ignored, so a typo cannot stop your mod loading
- Save and Cancel are handled by the page. Your code only ever sees saved values

### Optional dependency

To use SettingsForge without requiring it, keep every reference to `settingsforge.*` in one small class and only touch that class when the library is present:

```java
try {
    MyOptionsBridge.init();   // the only class that imports settingsforge.api
} catch (NoClassDefFoundError e) {
    // library not installed, use your own fallback
}
```

---

## Building

Gradle (`SettingsForge/gradlew build`) works the usual way for Necesse mods. Where Gradle cannot start, `tools/build.sh` does the same job by hand with `javac` and `jar`:

```bash
bash tools/build.sh           # compile, check, jar
bash tools/build.sh install   # also copy the jar into the Necesse mods folder
```

The script also runs two offline checks from `tools/`:

- `WeaveCheck` weaves every patch class into the real `Necesse.jar` with ByteBuddy and runs the JVM verifier on the result, so a patch that no longer applies fails the build instead of the game
- `OptionsCheck` round trips the settings through the game's own `SaveData` and `LoadData`

Set `NECESSE_DIR` if the game is not at `D:/SteamLibrary/steamapps/common/Necesse`.

---

## How it works

One ByteBuddy patch runs at the end of the `SettingsForm` constructor. It adds the **Mod Settings** button above Back and registers the page with the form switcher. The hook is wrapped so any failure disables only this page. Everything else is plain game API: `ModSettings` for saving, the game's own form widgets for the page, and the `settingsui` language section for labels.

---

## Changelog

### 1.0
- First release
