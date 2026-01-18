package com.training.coach.tui.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.RadioBoxList;
import com.googlecode.lanterna.gui2.TextBox;
import java.util.List;

public class TuiRenderer {

    private final MultiWindowTextGUI gui;

    public TuiRenderer(MultiWindowTextGUI gui) {
        this.gui = gui;
    }

    public void render(UiSpec spec) {
        BasicWindow window = new BasicWindow(spec.title());
        Panel panel = new Panel(new LinearLayout());
        panel.setPreferredSize(new TerminalSize(70, 22));

        for (UiComponentSpec component : spec.components()) {
            renderComponent(panel, component);
        }

        window.setComponent(panel.withBorder(Borders.singleLine(spec.title())));
        gui.addWindowAndWait(window);
    }

    private void renderComponent(Panel panel, UiComponentSpec component) {
        if (component instanceof UiLabelSpec label) {
            panel.addComponent(new Label(label.text()));
            return;
        }
        if (component instanceof UiSpacerSpec spacer) {
            panel.addComponent(new EmptySpace(new TerminalSize(1, Math.max(spacer.lines(), 1))));
            return;
        }
        if (component instanceof UiTextInputSpec input) {
            panel.addComponent(new Label(input.label()));
            TextBox textBox = new TextBox(nullToEmpty(input.getter().get()));
            textBox.setTextChangeListener((text, change) -> input.setter().accept(text));
            panel.addComponent(textBox);
            return;
        }
        if (component instanceof UiRadioSpec<?> radioSpec) {
            renderRadio(panel, radioSpec);
            return;
        }
        if (component instanceof UiButtonSpec button) {
            panel.addComponent(new Button(button.label(), button.onClick()));
        }
    }

    private <T> void renderRadio(Panel panel, UiRadioSpec<T> radioSpec) {
        panel.addComponent(new Label(radioSpec.label()));
        RadioBoxList<T> list = new RadioBoxList<>();
        for (T option : radioSpec.options()) {
            list.addItem(option);
        }
        T selected = radioSpec.getter().get();
        if (selected != null) {
            list.setCheckedItem(selected);
        } else if (!radioSpec.options().isEmpty()) {
            list.setCheckedItem(radioSpec.options().get(0));
            radioSpec.setter().accept(radioSpec.options().get(0));
        }
        list.addListener((selectedIndex, previousSelection) -> {
            List<T> options = radioSpec.options();
            if (selectedIndex >= 0 && selectedIndex < options.size()) {
                radioSpec.setter().accept(options.get(selectedIndex));
            }
        });
        panel.addComponent(list);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
