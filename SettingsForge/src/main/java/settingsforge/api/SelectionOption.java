package settingsforge.api;

import java.util.Arrays;
import java.util.List;

import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

/**
 * A pick one of several choices setting. The choice is saved by its name rather than its position, so a mod can
 * reorder or insert choices in a later version without scrambling what players already picked.
 */
public final class SelectionOption extends Option {

    public final int defaultIndex;
    private final String[] choices;
    private volatile int index;

    SelectionOption(String id, String section, int defaultIndex, String[] choices) {
        super(id, section);
        this.choices = choices;
        this.defaultIndex = clamp(defaultIndex);
        this.index = this.defaultIndex;
    }

    public List<String> choices() {
        return Arrays.asList(choices);
    }

    public int size() {
        return choices.length;
    }

    public int getIndex() {
        return index;
    }

    public String getChoice() {
        return choices[index];
    }

    /** Stores the index, clamped to the valid range. */
    public void setIndex(int index) {
        this.index = clamp(index);
    }

    private int clamp(int i) {
        return Math.max(0, Math.min(choices.length - 1, i));
    }

    @Override
    void save(SaveData save) {
        save.addSafeString(id, choices[index]);
    }

    @Override
    void load(LoadData load) {
        String saved = load.getSafeString(id, choices[defaultIndex], false);
        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(saved)) {
                index = i;
                return;
            }
        }
        index = defaultIndex;
    }
}
