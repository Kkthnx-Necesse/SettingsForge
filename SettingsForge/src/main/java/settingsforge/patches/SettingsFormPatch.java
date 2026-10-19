package settingsforge.patches;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.ContinueComponentManager;
import necesse.gfx.forms.presets.SettingsForm;
import net.bytebuddy.asm.Advice;
import settingsforge.ui.ModsPageHook;

/**
 * Runs once the Settings screen is fully built and hands it to the hook. Suppressing throwables keeps a failure in
 * the hook from ever reaching the game's own constructor.
 */
@ModConstructorPatch(target = SettingsForm.class, arguments = { Client.class, ContinueComponentManager.class })
public class SettingsFormPatch {

    @Advice.OnMethodExit(suppress = Throwable.class)
    static void onExit(@Advice.This SettingsForm self) {
        ModsPageHook.attach(self);
    }
}
