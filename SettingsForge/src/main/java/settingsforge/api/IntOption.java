package settingsforge.api;

import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public final class IntOption extends Option {

    public final int defaultValue;
    public final int min;
    public final int max;
    private volatile int value;

    IntOption(String id, String section, int defaultValue, int min, int max) {
        super(id, section);
        this.min = min;
        this.max = max;
        this.defaultValue = clamp(defaultValue);
        this.value = this.defaultValue;
    }

    public int get() {
        return value;
    }

    /** Stores the value, clamped to the range given at registration. */
    public void set(int value) {
        this.value = clamp(value);
    }

    private int clamp(int v) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    void save(SaveData save) {
        save.addInt(id, value);
    }

    @Override
    void load(LoadData load) {
        value = clamp(load.getInt(id, defaultValue, false));
    }
}
