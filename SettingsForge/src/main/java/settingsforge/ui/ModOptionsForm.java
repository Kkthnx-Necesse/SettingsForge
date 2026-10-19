package settingsforge.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormDropdownSelectionButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalCheckBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalSlider;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import settingsforge.api.BooleanOption;
import settingsforge.api.IntOption;
import settingsforge.api.ModOptions;
import settingsforge.api.Option;
import settingsforge.api.SelectionOption;

/**
 * The page that lists the settings of every registered mod. Widgets only change a pending copy of the values.
 * Save writes them to the real options and to disk, Cancel and the Escape key throw them away. The page is rebuilt
 * from the stored values each time it is opened, so there is nothing to undo.
 */
public class ModOptionsForm extends Form {

    private static final int WIDTH = 440;
    private static final int CONTENT_Y = 30;
    private static final int BUTTON_ROW = 40;

    private final FormContentBox content;
    private final FormLocalTextButton saveButton;
    private final FormLocalTextButton cancelButton;
    private final List<Runnable> commits = new ArrayList<>();
    private int contentHeight;

    /** @param onClose called after Save or Cancel, should show the previous page again */
    public ModOptionsForm(Runnable onClose) {
        super("settingsforgemods", WIDTH, 200);

        addComponent(new FormLocalLabel(Labels.of("kkthnx.settingsforge.page", "Mod Settings"),
                new FontOptions(20), FormLabel.ALIGN_MID, WIDTH / 2, 5));
        content = addComponent(new FormContentBox(0, CONTENT_Y, WIDTH, 40));

        saveButton = addComponent(new FormLocalTextButton("ui", "savebutton", 4, 0, WIDTH / 2 - 6));
        saveButton.onClicked(e -> {
            save();
            onClose.run();
        });
        cancelButton = addComponent(new FormLocalTextButton("ui", "cancelbutton", WIDTH / 2 + 2, 0, WIDTH / 2 - 6));
        cancelButton.onClicked(e -> onClose.run());
    }

    /** Fills the page from the current stored values. Call it every time before the page is shown. */
    public void rebuild() {
        content.clearComponents();
        commits.clear();

        FormFlow flow = new FormFlow(5);
        int innerWidth = content.getWidth() - 40;

        for (ModOptions mod : ModOptions.all()) {
            List<Option> options = mod.getOptions();
            if (options.isEmpty()) {
                continue;
            }
            content.addComponent(new FormLocalLabel(Labels.modTitle(mod.getModId()), new FontOptions(20),
                    FormLabel.ALIGN_LEFT, 10, flow.next(30)));

            String section = null;
            for (Option option : options) {
                if (option.section != null && !option.section.equals(section)) {
                    section = option.section;
                    content.addComponent(new FormLocalLabel(Labels.section(mod.getModId(), section),
                            new FontOptions(16), FormLabel.ALIGN_LEFT, 10, flow.next(24)));
                }
                GameMessage label = Labels.setting(mod.getModId(), option.id);
                if (option instanceof BooleanOption) {
                    addBoolean(flow, innerWidth, label, (BooleanOption) option);
                } else if (option instanceof IntOption) {
                    addInt(flow, innerWidth, label, (IntOption) option);
                } else if (option instanceof SelectionOption) {
                    addSelection(flow, innerWidth, mod.getModId(), label, (SelectionOption) option);
                }
            }
            flow.next(10);
        }

        contentHeight = flow.next(5);
        content.setScrollY(0);
        updateLayout();
    }

    private void addBoolean(FormFlow flow, int width, GameMessage label, BooleanOption option) {
        FormLocalCheckBox box = new FormLocalCheckBox(label, 10, 0, option.get(), width);
        content.addComponent(flow.nextY(box, 8));
        commits.add(() -> option.set(box.checked));
    }

    private void addInt(FormFlow flow, int width, GameMessage label, IntOption option) {
        FormLocalSlider slider = new FormLocalSlider(label, 10, 0, option.get(), option.min, option.max, width);
        content.addComponent(flow.nextY(slider, 10));
        commits.add(() -> option.set(slider.getValue()));
    }

    private void addSelection(FormFlow flow, int width, String modId, GameMessage label, SelectionOption option) {
        content.addComponent(new FormLocalLabel(label, new FontOptions(16), FormLabel.ALIGN_LEFT, 10, flow.next(22)));
        FormDropdownSelectionButton<Integer> dropdown = new FormDropdownSelectionButton<>(10, flow.next(34),
                FormInputSize.SIZE_24, ButtonColor.BASE, width);
        List<String> choices = option.choices();
        for (int i = 0; i < choices.size(); i++) {
            dropdown.options.add(i, Labels.choice(modId, option.id, choices.get(i)));
        }
        int current = option.getIndex();
        dropdown.setSelected(current, Labels.choice(modId, option.id, choices.get(current)));
        content.addComponent(dropdown);
        commits.add(() -> {
            Integer picked = dropdown.getSelected();
            option.setIndex(picked == null ? option.defaultIndex : picked);
        });
    }

    private void save() {
        try {
            for (Runnable commit : commits) {
                commit.run();
            }
            Settings.saveClientSettings();
        } catch (Throwable t) {
            System.out.println("[SettingsForge] Could not save mod settings: " + t);
        }
    }

    private void updateLayout() {
        GameWindow window = WindowManager.getWindow();
        int height = Math.min(Math.max(100, window.getHudHeight() - 100), contentHeight + BUTTON_ROW + CONTENT_Y);
        setHeight(height);
        content.setHeight(height - BUTTON_ROW - CONTENT_Y);
        content.setContentBox(new Rectangle(getWidth(), contentHeight));
        saveButton.setY(height - BUTTON_ROW);
        cancelButton.setY(height - BUTTON_ROW);
        setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
    }

    @Override
    public void onWindowResized(GameWindow window) {
        super.onWindowResized(window);
        updateLayout();
    }
}
