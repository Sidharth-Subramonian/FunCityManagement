package com.funcity.ui.components;

import javax.swing.*;
import java.awt.*;

public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subTextLabel;

    public StatCard(String title, String value, String subText, Color accentColor) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        setBackground(Color.WHITE);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 116, 139)); // Slate 500

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor != null ? accentColor : new Color(15, 23, 42));

        subTextLabel = new JLabel(subText);
        subTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTextLabel.setForeground(new Color(148, 163, 184)); // Slate 400

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subTextLabel);

        add(textPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubText(String subText) {
        subTextLabel.setText(subText);
    }
}

