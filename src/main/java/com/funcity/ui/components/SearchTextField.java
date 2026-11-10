package com.funcity.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class SearchTextField extends JTextField implements FocusListener {

    private final String placeholder;
    private boolean showingPlaceholder;

    public SearchTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        super.setText(placeholder);
        super.setForeground(Color.GRAY);
        super.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (showingPlaceholder) {
            super.setText("");
            super.setForeground(Color.BLACK);
            showingPlaceholder = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (super.getText().trim().isEmpty()) {
            super.setText(placeholder);
            super.setForeground(Color.GRAY);
            showingPlaceholder = true;
        }
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }
}

