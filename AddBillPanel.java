package metermind.ui;

import metermind.model.*;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

/**
 * Dialog for adding a new bill or editing an existing one.
 * Features real-time validation and inline error messages.
 */
public class AddBillPanel extends JPanel {

    private final MeterMindFrame frame;
    private UtilityBill editingBill; // null = add mode

    // Form fields
    private JComboBox<String> typeCombo;
    private JTextField providerField;
    private JSpinner yearSpinner;
    private JComboBox<String> monthCombo;
    private JTextField consumptionField;
    private JTextField amountField;
    private JTextField dueDateField;
    private JCheckBox paidCheck;

    // Error labels
    private JLabel providerError;
    private JLabel consumptionError;
    private JLabel amountError;
    private JLabel dueDateError;
    private JLabel generalError;

    private JLabel title;
    private JButton saveBtn;

    public AddBillPanel(MeterMindFrame frame) {
        this.frame = frame;
        initUI();
    }

    public void setEditingBill(UtilityBill bill) {
        this.editingBill = bill;
        title.setText(editingBill == null ? "• Add New Bill" : "• Edit Bill");
        saveBtn.setText(editingBill == null ? "Add Bill" : "Save Changes");
        if (editingBill != null) {
            populateFields();
        } else {
            clearFields();
        }
    }

    private void clearFields() {
        typeCombo.setSelectedIndex(0);
        providerField.setText("");
        yearSpinner.setValue(LocalDate.now().getYear());
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        consumptionField.setText("");
        amountField.setText("");
        dueDateField.setText("");
        paidCheck.setSelected(false);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Theme.BG_PRIMARY);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // Title
        title = new JLabel("• Add New Bill");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        // Utility Type
        typeCombo = new JComboBox<>(new String[]{"Electricity", "Water"});
        mainPanel.add(createFormField("Utility Type", typeCombo));
        mainPanel.add(Box.createVerticalStrut(12));

        // Provider
        providerField = createStyledTextField("e.g., Meralco, Maynilad");
        providerError = createErrorLabel();
        mainPanel.add(createFormField("Provider", providerField));
        mainPanel.add(providerError);
        mainPanel.add(Box.createVerticalStrut(12));

        // Year & Month row
        JPanel periodPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        periodPanel.setOpaque(false);
        periodPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        periodPanel.setAlignmentX(LEFT_ALIGNMENT);

        yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2030, 1));
        yearSpinner.setFont(Theme.FONT_BODY);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(editor);

        String[] months = DateUtil.MONTH_NAMES;
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        JPanel yearPanel = createFormField("Year", yearSpinner);
        JPanel monthPanel = createFormField("Month", monthCombo);
        periodPanel.add(yearPanel);
        periodPanel.add(monthPanel);
        mainPanel.add(periodPanel);
        mainPanel.add(Box.createVerticalStrut(12));

        // Consumption
        consumptionField = createStyledTextField("e.g., 250");
        consumptionError = createErrorLabel();
        mainPanel.add(createFormField("Consumption", consumptionField));
        mainPanel.add(consumptionError);
        mainPanel.add(Box.createVerticalStrut(12));

        // Amount Due
        amountField = createStyledTextField("e.g., 3500.00");
        amountError = createErrorLabel();
        mainPanel.add(createFormField("Amount Due (₱)", amountField));
        mainPanel.add(amountError);
        mainPanel.add(Box.createVerticalStrut(12));

        // Due Date
        dueDateField = createStyledTextField("YYYY-MM-DD");
        dueDateError = createErrorLabel();
        mainPanel.add(createFormField("Due Date", dueDateField));
        mainPanel.add(dueDateError);
        mainPanel.add(Box.createVerticalStrut(12));

        // Paid checkbox
        paidCheck = new JCheckBox("Already Paid");
        paidCheck.setFont(Theme.FONT_BODY);
        paidCheck.setForeground(Theme.TEXT_PRIMARY);
        paidCheck.setOpaque(false);
        paidCheck.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(paidCheck);
        mainPanel.add(Box.createVerticalStrut(20));

        // General error
        generalError = createErrorLabel();
        mainPanel.add(generalError);
        mainPanel.add(Box.createVerticalStrut(8));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = createButton("Cancel", Theme.BG_SURFACE, Theme.TEXT_PRIMARY);
        cancelBtn.addActionListener(e -> frame.switchTo("Bill History"));

        saveBtn = createButton("Add Bill", Theme.PRIMARY, Color.WHITE);
        saveBtn.addActionListener(e -> saveBill());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        mainPanel.add(buttonPanel);

        // The form follows the window width and scrolls vertically when the window is short
        ScrollableForm wrapper = new ScrollableForm(new BorderLayout());
        wrapper.setBackground(Theme.BG_PRIMARY);
        wrapper.add(mainPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_PRIMARY);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** A panel that always matches the viewport width so the form resizes with the window. */
    private static class ScrollableForm extends JPanel implements Scrollable {
        ScrollableForm(LayoutManager layout) { super(layout); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private void populateFields() {
        typeCombo.setSelectedItem(editingBill.getUtilityType().getDisplayName());
        providerField.setText(editingBill.getProvider());
        yearSpinner.setValue(editingBill.getYear());
        monthCombo.setSelectedIndex(editingBill.getMonth() - 1);
        consumptionField.setText(String.valueOf(editingBill.getConsumption()));
        amountField.setText(String.valueOf(editingBill.getAmountDue()));
        dueDateField.setText(DateUtil.format(editingBill.getDueDate()));
        paidCheck.setSelected(editingBill.isPaid());
    }

    private void saveBill() {
        // Clear errors
        providerError.setText("");
        consumptionError.setText("");
        amountError.setText("");
        dueDateError.setText("");
        generalError.setText("");

        boolean valid = true;

        // Validate provider
        String provider = providerField.getText().trim();
        if (provider.isEmpty()) {
            providerError.setText("Provider is required");
            valid = false;
        }

        // Validate consumption
        double consumption = 0;
        try {
            consumption = Double.parseDouble(consumptionField.getText().trim());
            if (consumption <= 0) {
                consumptionError.setText("Must be greater than 0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            consumptionError.setText("Enter a valid number");
            valid = false;
        }

        // Validate amount
        double amount = 0;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                amountError.setText("Must be greater than 0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            amountError.setText("Enter a valid number");
            valid = false;
        }

        // Validate due date
        LocalDate dueDate = DateUtil.parse(dueDateField.getText().trim());
        if (dueDate == null) {
            dueDateError.setText("Enter date as YYYY-MM-DD");
            valid = false;
        }

        if (!valid) return;

        // Build bill
        int year = (int) yearSpinner.getValue();
        int month = monthCombo.getSelectedIndex() + 1;
        boolean isPaid = paidCheck.isSelected();
        boolean isElectricity = "Electricity".equals(typeCombo.getSelectedItem());

        try {
            if (editingBill != null) {
                // Update existing bill
                editingBill.setProvider(provider);
                editingBill.setYear(year);
                editingBill.setMonth(month);
                editingBill.setConsumption(consumption);
                editingBill.setAmountDue(amount);
                editingBill.setDueDate(dueDate);
                editingBill.setPaid(isPaid);
                frame.getHistory().updateBill(editingBill);
            } else {
                // Create new bill
                UtilityBill newBill;
                if (isElectricity) {
                    newBill = new ElectricityBill(provider, year, month, consumption, amount, dueDate, isPaid);
                } else {
                    newBill = new WaterBill(provider, year, month, consumption, amount, dueDate, isPaid);
                }
                frame.getHistory().addBill(newBill);
            }

            frame.onDataChanged();
            frame.switchTo("Bill History");

        } catch (DuplicateBillException e) {
            generalError.setText(e.getMessage());
        }
    }

    // ========================
    //   UI Helpers
    // ========================

    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(LEFT_ALIGNMENT);

        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setFont(Theme.FONT_BODY);
            field.setBackground(Theme.BG_SURFACE);
            field.setForeground(Theme.TEXT_PRIMARY);
        }

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(Theme.FONT_BODY);
                    g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        field.setFont(Theme.FONT_BODY);
        field.setBackground(Theme.BG_SURFACE);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel("");
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.DANGER);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
                g2.setColor(fg);
                g2.setFont(Theme.FONT_SUBHEADING);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
