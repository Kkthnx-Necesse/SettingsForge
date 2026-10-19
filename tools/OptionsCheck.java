import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import settingsforge.api.ModOptions;

/** Offline round trip check for ModOptions: save, load into a fresh instance, late registration, clamping. */
public class OptionsCheck {

    static int failures;

    static void check(String what, boolean ok) {
        System.out.println((ok ? "OK   " : "FAIL ") + what);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        ModOptions a = ModOptions.create("test.a")
                .addSection("main")
                .addBoolean("flag", true)
                .addInt("size", 50, 0, 100)
                .addSelection("mode", 1, "low", "mid", "high");
        check("defaults", a.getBoolean("flag") && a.getInt("size") == 50 && a.getSelection("mode") == 1);

        a.setBoolean("flag", false);
        a.setInt("size", 500);
        a.setSelection("mode", 2);
        check("int clamps high", a.getInt("size") == 100);

        SaveData save = new SaveData("SETTINGS");
        a.addSaveData(save);
        String script = save.getScript();
        System.out.println(script.replace("\n", " | "));
        LoadData load = new LoadData(script);

        // Registered before the game loads the file
        ModOptions b = ModOptions.create("test.b")
                .addBoolean("flag", true).addInt("size", 50, 0, 100).addSelection("mode", 1, "low", "mid", "high");
        b.applyLoadData(load);
        check("round trip before registration done", !b.getBoolean("flag") && b.getInt("size") == 100 && b.getSelectionChoice("mode").equals("high"));

        // Registered after the game loaded the file
        ModOptions c = ModOptions.create("test.c");
        c.applyLoadData(load);
        c.addBoolean("flag", true).addInt("size", 50, 0, 100).addSelection("mode", 1, "low", "mid", "high");
        check("late registration picks up saved values", !c.getBoolean("flag") && c.getInt("size") == 100 && c.getSelection("mode") == 2);

        // Reordered choices keep the saved choice by name
        ModOptions d = ModOptions.create("test.d").addSelection("mode", 0, "high", "low", "mid");
        d.applyLoadData(load);
        check("selection survives reordering", d.getSelectionChoice("mode").equals("high") && d.getSelection("mode") == 0);

        // Unknown or removed saved choice falls back to the default
        ModOptions e = ModOptions.create("test.e").addSelection("mode", 2, "x", "y", "z");
        e.applyLoadData(load);
        check("unknown choice falls back to default", e.getSelection("mode") == 2);

        // Missing data falls back to defaults
        ModOptions f = ModOptions.create("test.f").addBoolean("flag", true).addInt("size", 7, 0, 10);
        f.applyLoadData(new LoadData(new SaveData("SETTINGS").getScript()));
        check("empty file gives defaults", f.getBoolean("flag") && f.getInt("size") == 7);

        // Misuse never throws
        ModOptions g = ModOptions.create("test.g")
                .addBoolean("bad id", true).addBoolean("ok", true).addBoolean("ok", false)
                .addInt("rev", 5, 10, 1).addSelection("none", 0);
        check("bad registrations skipped", g.getOptions().size() == 1 && g.getBoolean("ok"));
        check("wrong type read is safe", g.getInt("ok") == 0 && !g.getBoolean("missing"));
        check("create returns same instance", ModOptions.create("test.g") == g);

        System.exit(failures == 0 ? 0 : 1);
    }
}
