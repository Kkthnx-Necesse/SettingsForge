package settingsforge.api;

import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public final class BooleanOption extends Option {

    public final boolean defaultValue;
    private volatile boolean value;

    BooleanOption(String id, String section, boolean defaultValue) {
        super(id, section);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    void save(SaveData save) {
        save.addBoolean(id, value);
    }

    @Override
    void load(LoadData load) {
        value = load.getBoolean(id, defaultValue, false);
    }
}
