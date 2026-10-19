package settingsforge.api;

import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

/**
 * One registered setting. The concrete types are {@link BooleanOption}, {@link IntOption} and
 * {@link SelectionOption}. Mods do not create these directly, they use the add methods on {@link ModOptions}.
 */
public abstract class Option {

    /** Id used as the save key and as the lookup key for reads. */
    public final String id;

    /** Section this option was added under, or null when no section was started yet. */
    public final String section;

    Option(String id, String section) {
        this.id = id;
        this.section = section;
    }

    abstract void save(SaveData save);

    abstract void load(LoadData load);
}
