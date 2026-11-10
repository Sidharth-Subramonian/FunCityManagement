package com.funcity.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIHelper {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue 600
    public static final Color SUCCESS_COLOR = new Color(22, 163, 74); // Green 600
    public static final Color WARNING_COLOR = new Color(217, 119, 6); // Amber 600
    public static final Color DANGER_COLOR = new Color(220, 38, 38);  // Red 600
    public static final Color TEXT_DARK = new Color(30, 41, 59);     // Slate 800
    public static final Color BG_LIGHT = new Color(248, 250, 252);   // Slate 50

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatDate(Date date) {
        if (date == null) return "-";
        return DATE_FORMAT.format(date);
    }

    public static <T> void executeAsync(Component parent, String loadingMsg, Supplier<T> backgroundTask, Consumer<T> onSuccess, Consumer<Exception> onError) {
        JDialog progressDialog = null;
        if (parent != null) {
            Frame frame = JOptionPane.getFrameForComponent(parent);
            progressDialog = new JDialog(frame, "Please Wait", false);
            JPanel p = new JPanel(new BorderLayout(15, 15));
            p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            JProgressBar bar = new JProgressBar();
            bar.setIndeterminate(true);
            p.add(new JLabel(loadingMsg != null ? loadingMsg : "Loading..."), BorderLayout.NORTH);
            p.add(bar, BorderLayout.CENTER);
            progressDialog.getContentPane().add(p);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(parent);
            progressDialog.setVisible(true);
        }

        final JDialog dialogToClose = progressDialog;

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.get();
            }

            @Override
            protected void done() {
                if (dialogToClose != null) {
                    dialogToClose.dispose();
                }
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (onError != null) {
                        onError.accept((Exception) cause);
                    } else {
                        showError(parent, cause.getMessage());
                    }
                }
            }
        };
        worker.execute();
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        int res = JOptionPane.showConfirmDialog(parent, message, title != null ? title : "Confirm", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(SUCCESS_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

