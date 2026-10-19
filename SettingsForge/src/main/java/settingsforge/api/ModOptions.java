package settingsforge.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import necesse.engine.Settings;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

/**
 * The settings of one mod. Create it once with {@link #create(String)}, describe the settings with the add methods,
 * hand the object back from your entry class {@code initSettings()} and read values with the get methods.
 *
 * <pre>
 * public static final ModOptions options = ModOptions.create("my.modid")
 *         .addSection("display")
 *         .addBoolean("showClock", true)
 *         .addInt("clockScale", 100, 50, 200)
 *         .addSelection("clockCorner", 0, "topleft", "topright");
 *
 * public ModSettings initSettings() {
 *     return options;
 * }
 * </pre>
 *
 * This is a normal game {@link ModSettings}, so the game saves and loads it together with the rest of the client
 * settings. Each option is stored under its id, which keeps the file readable and lets a mod move to this library
 * without losing values it already saved under the same names.
 *
 * Labels are looked up in your mod's own language file, section {@code settingsui}:
 * <ul>
 * <li>{@code <modId>.title} heading for your mod, defaults to the mod name</li>
 * <li>{@code <modId>.section.<key>} heading for a section, defaults to the key</li>
 * <li>{@code <modId>.<id>} label of a setting, defaults to the id</li>
 * <li>{@code <modId>.<id>.<choice>} label of a selection choice, defaults to the choice</li>
 * </ul>
 *
 * Ids and section keys may use letters, digits and underscores. Registering anything that breaks a rule is logged
 * and skipped, it never throws, so a typo in one setting cannot stop your mod from loading.
 */
public final class ModOptions extends ModSettings {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]+");
    private static final Map<String, ModOptions> registry = new LinkedHashMap<>();

    private final String modId;
    private final Map<String, Option> options = new LinkedHashMap<>();
    private final Set<String> warned = new HashSet<>();
    private String currentSection;
    private LoadData loaded;

    private ModOptions(String modId) {
        this.modId = modId;
    }

    /**
     * Returns the settings holder for the given mod id, creating it on the first call. Calling it again with the same
     * id returns the same object.
     */
    public static synchronized ModOptions create(String modId) {
        ModOptions existing = registry.get(modId);
        if (existing != null) {
            return existing;
        }
        ModOptions created = new ModOptions(modId);
        registry.put(modId, created);
        return created;
    }

    /** Every mod that has registered settings, in registration order. */
    public static synchronized List<ModOptions> all() {
        return Collections.unmodifiableList(new ArrayList<>(registry.values()));
    }

    public String getModId() {
        return modId;
    }

    /** The registered options in the order they were added. */
    public synchronized List<Option> getOptions() {
        return Collections.unmodifiableList(new ArrayList<>(options.values()));
    }

    // ------------------------------------------------------------------ registering

    /** Starts a new section. Every option added after this call is listed under it. */
    public synchronized ModOptions addSection(String key) {
        if (!valid(key, "section")) {
            return this;
        }
        currentSection = key;
        return this;
    }

    public synchronized ModOptions addBoolean(String id, boolean defaultValue) {
        if (canAdd(id)) {
            add(new BooleanOption(id, currentSection, defaultValue));
        }
        return this;
    }

    public synchronized ModOptions addInt(String id, int defaultValue, int min, int max) {
        if (!canAdd(id)) {
            return this;
        }
        if (min > max) {
            log(id + " has min above max, skipped");
            return this;
        }
        add(new IntOption(id, currentSection, defaultValue, min, max));
        return this;
    }

    /**
     * Adds a pick one setting. The choices are short names, not display text. The display text comes from the language
     * file, see the class comment.
     */
    public synchronized ModOptions addSelection(String id, int defaultIndex, String... choices) {
        if (!canAdd(id)) {
            return this;
        }
        if (choices == null || choices.length == 0) {
            log(id + " has no choices, skipped");
            return this;
        }
        add(new SelectionOption(id, currentSection, defaultIndex, choices.clone()));
        return this;
    }

    private void add(Option option) {
        options.put(option.id, option);
        if (loaded != null) {
            option.load(loaded);
        }
    }

    private boolean canAdd(String id) {
        if (!valid(id, "setting id")) {
            return false;
        }
        if (options.containsKey(id)) {
            log(id + " is already registered, second one ignored");
            return false;
        }
        return true;
    }

    private boolean valid(String name, String what) {
        if (name == null || !NAME.matcher(name).matches()) {
            log("invalid " + what + " \"" + name + "\", use letters digits and underscores");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ reading and writing values

    /** Returns the value, or false when the id is unknown or not a boolean. */
    public synchronized boolean getBoolean(String id) {
        Option o = options.get(id);
        if (o instanceof BooleanOption) {
            return ((BooleanOption) o).get();
        }
        wrongType(id, "boolean");
        return false;
    }

    /** Returns the value, or 0 when the id is unknown or not an int. */
    public synchronized int getInt(String id) {
        Option o = options.get(id);
        if (o instanceof IntOption) {
            return ((IntOption) o).get();
        }
        wrongType(id, "int");
        return 0;
    }

    /** Returns the chosen index, or 0 when the id is unknown or not a selection. */
    public synchronized int getSelection(String id) {
        Option o = options.get(id);
        if (o instanceof SelectionOption) {
            return ((SelectionOption) o).getIndex();
        }
        wrongType(id, "selection");
        return 0;
    }

    /** Returns the chosen choice name, or an empty string when the id is unknown or not a selection. */
    public synchronized String getSelectionChoice(String id) {
        Option o = options.get(id);
        if (o instanceof SelectionOption) {
            return ((SelectionOption) o).getChoice();
        }
        wrongType(id, "selection");
        return "";
    }

    /** Changes a value in memory, for example from a key bind. Call {@link #save()} to write it to disk. */
    public synchronized void setBoolean(String id, boolean value) {
        Option o = options.get(id);
        if (o instanceof BooleanOption) {
            ((BooleanOption) o).set(value);
        } else {
            wrongType(id, "boolean");
        }
    }

    public synchronized void setInt(String id, int value) {
        Option o = options.get(id);
        if (o instanceof IntOption) {
            ((IntOption) o).set(value);
        } else {
            wrongType(id, "int");
        }
    }

    public synchronized void setSelection(String id, int index) {
        Option o = options.get(id);
        if (o instanceof SelectionOption) {
            ((SelectionOption) o).setIndex(index);
        } else {
            wrongType(id, "selection");
        }
    }

    /** Writes all client settings, including every mod, to disk. */
    public void save() {
        Settings.saveClientSettings();
    }

    private void wrongType(String id, String expected) {
        // Reads can happen every frame, so complain once per id
        if (warned.add(id)) {
            log("no " + expected + " setting named " + id);
        }
    }

    // ------------------------------------------------------------------ game persistence

    @Override
    public synchronized void addSaveData(SaveData save) {
        for (Option o : options.values()) {
            o.save(save);
        }
    }

    @Override
    public synchronized void applyLoadData(LoadData load) {
        // Keep the data. A mod may register more options after the game has already loaded the file
        loaded = load;
        for (Option o : options.values()) {
            o.load(load);
        }
    }

    private void log(String message) {
        System.out.println("[SettingsForge] " + modId + ": " + message);
    }
}
