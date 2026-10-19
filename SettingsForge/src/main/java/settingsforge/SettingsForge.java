package settingsforge;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import settingsforge.api.ModOptions;

@ModEntry
public class SettingsForge {

    /** Settings for the demo section on the Mod Settings page. They also double as a template for other mods. */
    public static final ModOptions demo = ModOptions.create("kkthnx.settingsforge")
            .addSection("demo")
            .addBoolean("demoToggle", true)
            .addInt("demoSlider", 50, 0, 100)
            .addSelection("demoChoice", 1, "small", "medium", "large");

    public void init() {
        System.out.println("Thanks for using SettingsForge!");
    }

    public ModSettings initSettings() {
        return demo;
    }
}
