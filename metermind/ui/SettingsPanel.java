package metermind.ui;

import metermind.model.*;
import metermind.storage.DataManager;
import metermind.storage.SampleData;
import metermind.util.RegexParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

/**
 * Settings panel for configuring thresholds, importing/exporting data,
 * and quick-paste bill entry from SMS/email text.
 */
public class SettingsPanel extends JPanel {

    private final MeterMindFrame frame;
    private JTextField highBillField;
    private JTextField reminderDaysField;
    private JTextArea quickPasteArea;
    private JTextArea parseResultArea;

    public SettingsPanel(MeterMindFrame frame) {
        this.frame = frame;
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    public void refresh() {
        removeAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Title
        JLabel title = new JLabel("⚙ Settings");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Alert Thresholds
        content.add(createThresholdsSection());
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Quick Paste
        content.add(createQuickPasteSection());
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Data Management
        content.add(createDataSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ========================
    //   Thresholds Section
    // ========================

    private JPanel createThresholdsSection() {
        JPanel card = createCard();

        JLabel sectionTitle = new JLabel("📏 Alert Thresholds");
        sectionTitle.setFont(Theme.FONT_HEADING);
        sectionTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(16));

        JPanel fieldsRow = new JPanel(new GridLayout(1, 2, Theme.SPACING_LG, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // High bill threshold
        highBillField = createField(String.valueOf(frame.getAlertManager().getHighBillThreshold()));
        fieldsRow.add(wrapField("High Bill Threshold (%)", highBillField,
                "Triggers alert when bill exceeds avg by this %"));

        // Reminder days
        reminderDaysField = createField(String.valueOf(frame.getAlertManager().getReminderDays()));
        fieldsRow.add(wrapField("Reminder Days Before Due", reminderDaysField,
                "Days before due date to show reminder"));

        card.add(fieldsRow);
        card.add(Box.createVerticalStrut(16));

        JButton saveBtn = createPrimaryButton("Save Thresholds");
        saveBtn.addActionListener(e -> saveThresholds());
        card.add(saveBtn);

        return card;
    }

    private void saveThresholds() {
        try {
            double threshold = Double.parseDouble(highBillField.getText().trim());
            int days = Integer.parseInt(reminderDaysField.getText().trim());

            frame.getAlertManager().setHighBillThreshold(threshold);
            frame.getAlertManager().setReminderDays(days);
            frame.getDataManager().saveHighBillThreshold(threshold);
            frame.getDataManager().saveReminderDays(days);
            frame.getAlertManager().checkAlerts();

            JOptionPane.showMessageDialog(frame, "Thresholds saved!", "MeterMind", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================
    //   Quick Paste Section
    // ========================

    private JPanel createQuickPasteSection() {
        JPanel card = createCard();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        JLabel sectionTitle = new JLabel("📋 Quick Paste Bill Entry");
        sectionTitle.setFont(Theme.FONT_HEADING);
        sectionTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(4));

        JLabel desc = new JLabel("Paste SMS or email notification text to auto-extract bill data");
        desc.setFont(Theme.FONT_SMALL);
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(12));

        quickPasteArea = new JTextArea(5, 40);
        quickPasteArea.setFont(Theme.FONT_MONO);
        quickPasteArea.setBackground(Theme.BG_HOVER);
        quickPasteArea.setForeground(Theme.TEXT_PRIMARY);
        quickPasteArea.setCaretColor(Theme.TEXT_PRIMARY);
        quickPasteArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        quickPasteArea.setLineWrap(true);
        quickPasteArea.setWrapStyleWord(true);

        JScrollPane pasteScroll = new JScrollPane(quickPasteArea);
        pasteScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        pasteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pasteScroll.setAlignmentX(LEFT_ALIGNMENT);
        card.add(pasteScroll);
        card.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton parseBtn = createPrimaryButton("Parse Text");
        parseBtn.addActionListener(e -> parseQuickPaste());
        btnRow.add(parseBtn);

        JButton addBtn = createPrimaryButton("Add as Bill");
        addBtn.addActionListener(e -> addParsedBill());
        btnRow.add(addBtn);

        card.add(btnRow);
        card.add(Box.createVerticalStrut(8));

        parseResultArea = new JTextArea(3, 40);
        parseResultArea.setFont(Theme.FONT_MONO);
        parseResultArea.setBackground(new Color(16, 185, 129, 15));
        parseResultArea.setForeground(Theme.SUCCESS);
        parseResultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        parseResultArea.setEditable(false);
        parseResultArea.setLineWrap(true);

        JScrollPane resultScroll = new JScrollPane(parseResultArea);
        resultScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        resultScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        resultScroll.setAlignmentX(LEFT_ALIGNMENT);
        card.add(resultScroll);

        return card;
    }

    private Map<String, String> lastParsed;

    private void parseQuickPaste() {
        String text = quickPasteArea.getText().trim();
        if (text.isEmpty()) {
            parseResultArea.setText("No text to parse.");
            return;
        }

        lastParsed = RegexParser.parse(text);
        StringBuilder sb = new StringBuilder("Extracted fields:\n");
        lastParsed.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));

        if (lastParsed.isEmpty()) {
            sb.append("  (no fields detected — try a different format)");
        }

        parseResultArea.setText(sb.toString());
    }

    private void addParsedBill() {
        if (lastParsed == null || lastParsed.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Parse text first!", "MeterMind", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pre-populate the add bill dialog would be ideal, but for now show a message
        // with extracted data and open the dialog
        frame.showAddBillDialog();
    }

    // ========================
    //   Data Section
    // ========================

    private JPanel createDataSection() {
        JPanel card = createCard();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel sectionTitle = new JLabel("💾 Data Management");
        sectionTitle.setFont(Theme.FONT_HEADING);
        sectionTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton loadSampleBtn = createPrimaryButton("Load Sample Data");
        loadSampleBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "This will replace all current data with sample data. Continue?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    BillHistory sample = SampleData.generate();
                    // Replace the current history
                    BillHistory current = frame.getHistory();
                    // Clear and re-add
                    for (UtilityBill bill : current.getAllSorted()) {
                        current.removeBill(bill.getId());
                    }
                    for (UtilityBill bill : sample.getAllSorted()) {
                        try { current.addBill(bill); } catch (DuplicateBillException ignored) {}
                    }
                    frame.onDataChanged();
                    JOptionPane.showMessageDialog(frame, "Sample data loaded!", "MeterMind", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnRow.add(loadSampleBtn);

        JButton exportBtn = createPrimaryButton("Export CSV");
        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "Data is saved to: " + frame.getDataManager().getBillsFile().getAbsolutePath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        });
        btnRow.add(exportBtn);

        card.add(btnRow);
        card.add(Box.createVerticalStrut(12));

        JLabel pathLabel = new JLabel("Data file: " + frame.getDataManager().getBillsFile().getAbsolutePath());
        pathLabel.setFont(Theme.FONT_SMALL);
        pathLabel.setForeground(Theme.TEXT_MUTED);
        pathLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(pathLabel);

        JLabel settingsLabel = new JLabel("Settings file: " + frame.getDataManager().getSettingsFile().getAbsolutePath());
        settingsLabel.setFont(Theme.FONT_SMALL);
        settingsLabel.setForeground(Theme.TEXT_MUTED);
        settingsLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(settingsLabel);

        return card;
    }

    // ========================
    //   Helpers
    // ========================

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.paintCard(g2, 0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(
                Theme.CARD_PADDING, Theme.CARD_PADDING, Theme.CARD_PADDING, Theme.CARD_PADDING));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JTextField createField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(Theme.FONT_BODY);
        field.setBackground(Theme.BG_HOVER);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JPanel wrapField(String labelText, JTextField field, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(Theme.TEXT_MUTED);
        hintLabel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(2));
        panel.add(hintLabel);

        return panel;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(getModel().isRollover() ? Theme.PRIMARY_HOVER : Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_SUBHEADING);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setPreferredSize(new Dimension(160, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
