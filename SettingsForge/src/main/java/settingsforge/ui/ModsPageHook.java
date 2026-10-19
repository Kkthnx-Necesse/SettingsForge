package settingsforge.ui;

import java.lang.reflect.Field;

import necesse.engine.window.WindowManager;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.presets.SettingsForm;
import settingsforge.api.ModOptions;

/**
 * Adds the Mod Settings button and page to the game's Settings screen. This is the only place that touches the
 * game's own settings form. If anything about it has changed in a game update, the page is skipped, one line is
 * logged and the Settings screen stays as the game built it.
 */
public final class ModsPageHook {

    private static final int BUTTON_ROW = 40;

    private ModsPageHook() {
    }

    public static void attach(SettingsForm settings) {
        try {
            if (ModOptions.all().isEmpty()) {
                return;
            }
            add(settings);
        } catch (Throwable t) {
            System.out.println("[SettingsForge] Mod Settings page disabled, the game's Settings screen has changed: " + t);
        }
    }

    private static void add(SettingsForm settings) throws Exception {
        // Look up everything before changing anything, so a failure leaves the game's form untouched
        Field mainMenuField = SettingsForm.class.getDeclaredField("mainMenu");
        mainMenuField.setAccessible(true);
        Form mainMenu = (Form) mainMenuField.get(settings);

        // The Back button was added last, so it is the button lowest on the menu
        FormLocalTextButton back = null;
        for (Object component : mainMenu.getComponents()) {
            if (component instanceof FormLocalTextButton) {
                FormLocalTextButton button = (FormLocalTextButton) component;
                if (back == null || button.getY() > back.getY()) {
                    back = button;
                }
            }
        }
        if (back == null) {
            throw new IllegalStateException("no buttons found on the main settings menu");
        }

        ModOptionsForm page = new ModOptionsForm(() -> settings.makeCurrent(mainMenu));
        FormLocalTextButton open = new FormLocalTextButton(Labels.of("kkthnx.settingsforge.page", "Mod Settings"),
                4, back.getY(), mainMenu.getWidth() - 8);
        open.onClicked(e -> {
            try {
                page.rebuild();
                settings.makeCurrent(page);
            } catch (Throwable t) {
                System.out.println("[SettingsForge] Could not open the Mod Settings page: " + t);
            }
        });

        settings.addComponent(page);
        mainMenu.addComponent(open);
        back.setY(back.getY() + BUTTON_ROW);
        mainMenu.setHeight(mainMenu.getHeight() + BUTTON_ROW);
        settings.onWindowResized(WindowManager.getWindow());
    }
}
