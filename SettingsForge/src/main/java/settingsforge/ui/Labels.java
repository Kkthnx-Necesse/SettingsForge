package settingsforge.ui;

import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.modLoader.ModLoader;

/** Resolves labels from the {@code settingsui} language section and falls back to a plain string. */
final class Labels {

    static final String SECTION = "settingsui";

    private Labels() {
    }

    /** The translated message when the key exists in the current or default language, else the fallback text. */
    static GameMessage of(String key, String fallback) {
        try {
            if (Localization.getCurrentLang().translationExists(SECTION, key)
                    || Localization.defaultLang.translationExists(SECTION, key)) {
                return new LocalMessage(SECTION, key);
            }
        } catch (Throwable ignored) {
            // Fall through to the fallback text
        }
        return new StaticMessage(fallback);
    }

    static GameMessage modTitle(String modId) {
        String name = modId;
        try {
            String found = ModLoader.getModName(modId);
            if (found != null && !found.isEmpty()) {
                name = found;
            }
        } catch (Throwable ignored) {
            // Keep the id
        }
        return of(modId + ".title", name);
    }

    static GameMessage section(String modId, String key) {
        return of(modId + ".section." + key, key);
    }

    static GameMessage setting(String modId, String id) {
        return of(modId + "." + id, id);
    }

    static GameMessage choice(String modId, String id, String choice) {
        return of(modId + "." + id + "." + choice, choice);
    }
}
