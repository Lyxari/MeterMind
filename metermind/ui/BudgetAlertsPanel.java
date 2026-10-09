package metermind.ui;

import metermind.alerts.*;
import metermind.model.*;
import metermind.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Budget management and alerts center panel.
 * Allows setting budget limits and viewing active/historical alerts.
 */
public class BudgetAlertsPanel extends JPanel {

    private final MeterMindFrame frame;
    private JPanel alertsListPanel;

    // Budget fields
    private JTextField overallLimitField;
    private JTextField elecLimitField;
    private JTextField waterLimitField;

    public BudgetAlertsPanel(MeterMindFrame frame) {
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
        JLabel title = new JLabel("🔔 Budget & Alerts");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Budget configuration section
        content.add(createBudgetSection());
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Budget progress bars
        content.add(createBudgetProgress());
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Active alerts section
        JLabel alertsTitle = new JLabel("Active Alerts");
        alertsTitle.setFont(Theme.FONT_HEADING);
        alertsTitle.setForeground(Theme.TEXT_PRIMARY);
        alertsTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(alertsTitle);
        content.add(Box.createVerticalStrut(Theme.SPACING));

        alertsListPanel = new JPanel();
        alertsListPanel.setLayout(new BoxLayout(alertsListPanel, BoxLayout.Y_AXIS));
        alertsListPanel.setOpaque(false);
        alertsListPanel.setAlignmentX(LEFT_ALIGNMENT);
        updateAlertList(frame.getAlertManager().getActiveAlerts());
        content.add(alertsListPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ========================
    //   Budget Section
    // ========================

    private JPanel createBudgetSection() {
        Budget budget = frame.getBudget();

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("💰 Monthly Budget Limits");
        sectionTitle.setFont(Theme.FONT_HEADING);
        sectionTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(4));

        JLabel sectionDesc = new JLabel("Set 0 to disable a limit");
        sectionDesc.setFont(Theme.FONT_SMALL);
        sectionDesc.setForeground(Theme.TEXT_MUTED);
        card.add(sectionDesc);
        card.add(Box.createVerticalStrut(16));

        JPanel fieldsRow = new JPanel(new GridLayout(1, 3, Theme.SPACING, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        overallLimitField = createBudgetField("Overall Monthly Limit (₱)", budget.getOverallMonthlyLimit());
        elecLimitField = createBudgetField("Electricity Limit (₱)", budget.getElectricityLimit());
        waterLimitField = createBudgetField("Water Limit (₱)", budget.getWaterLimit());

        fieldsRow.add(wrapField("Overall Monthly Limit (₱)", overallLimitField));
        fieldsRow.add(wrapField("Electricity Limit (₱)", elecLimitField));
        fieldsRow.add(wrapField("Water Limit (₱)", waterLimitField));

        card.add(fieldsRow);
        card.add(Box.createVerticalStrut(16));

        // Save button
        JButton saveBtn = new JButton("Save Budget") {
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
        saveBtn.setPreferredSize(new Dimension(140, 38));
        saveBtn.setMaximumSize(new Dimension(140, 38));
        saveBtn.setContentAreaFilled(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveBudget());
        card.add(saveBtn);

        return card;
    }

    private JPanel createBudgetProgress() {
        Budget budget = frame.getBudget();
        if (!budget.hasAnyLimit()) return new JPanel() {{ setOpaque(false); }};

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel progressTitle = new JLabel("📊 Budget Progress (This Month)");
        progressTitle.setFont(Theme.FONT_HEADING);
        progressTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(progressTitle);
        card.add(Box.createVerticalStrut(16));

        java.time.LocalDate now = java.time.LocalDate.now();

        // Electricity progress
        if (budget.getElectricityLimit() > 0) {
            double spent = frame.getHistory().getTotalSpending(now.getYear(), UtilityType.ELECTRICITY);
            card.add(createProgressBar("Electricity", spent, budget.getElectricityLimit(), Theme.PRIMARY));
            card.add(Box.createVerticalStrut(12));
        }

        // Water progress
        if (budget.getWaterLimit() > 0) {
            double spent = frame.getHistory().getTotalSpending(now.getYear(), UtilityType.WATER);
            card.add(createProgressBar("Water", spent, budget.getWaterLimit(), new Color(0x06B6D4)));
            card.add(Box.createVerticalStrut(12));
        }

        // Overall progress
        if (budget.getOverallMonthlyLimit() > 0) {
            double spent = frame.getHistory().getMonthlyTotal(now.getYear(), now.getMonthValue());
            card.add(createProgressBar("Overall", spent, budget.getOverallMonthlyLimit(), Theme.SUCCESS));
        }

        return card;
    }

    private JPanel createProgressBar(String label, double spent, double limit, Color color) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        double percent = (spent / limit) * 100;
        Color barColor = percent > 100 ? Theme.DANGER : (percent > 80 ? Theme.WARNING : color);

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(Theme.FONT_SMALL);
        nameLbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(CurrencyUtil.formatPeso(spent) + " / " + CurrencyUtil.formatPeso(limit)
                + " (" + String.format("%.0f%%", percent) + ")");
        valueLbl.setFont(Theme.FONT_SMALL);
        valueLbl.setForeground(barColor);

        labelRow.add(nameLbl, BorderLayout.WEST);
        labelRow.add(valueLbl, BorderLayout.EAST);
        row.add(labelRow);
        row.add(Box.createVerticalStrut(4));

        // Progress bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.paintGradientBar(g2, 0, 0, getWidth(), getHeight(), barColor, barColor.darker(), percent);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        row.add(bar);

        return row;
    }

    // ========================
    //   Alerts List
    // ========================

    public void updateAlerts(List<Alert> alerts) {
        if (alertsListPanel != null) {
            updateAlertList(alerts);
        }
    }

    private void updateAlertList(List<Alert> alerts) {
        alertsListPanel.removeAll();

        if (alerts.isEmpty()) {
            JLabel noAlerts = new JLabel("✅ No active alerts");
            noAlerts.setFont(Theme.FONT_BODY);
            noAlerts.setForeground(Theme.SUCCESS);
            noAlerts.setAlignmentX(LEFT_ALIGNMENT);
            alertsListPanel.add(noAlerts);
        } else {
            for (Alert alert : alerts) {
                Color sevColor;
                switch (alert.getSeverity()) {
                    case DANGER: sevColor = Theme.DANGER; break;
                    case WARNING: sevColor = Theme.WARNING; break;
                    default: sevColor = Theme.PRIMARY; break;
                }

                JPanel alertCard = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        Theme.enableAntiAliasing(g2);
                        g2.setColor(Theme.BG_SURFACE);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                        g2.setColor(sevColor);
                        g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                    }
                };
                alertCard.setOpaque(false);
                alertCard.setLayout(new BorderLayout(12, 0));
                alertCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
                alertCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
                alertCard.setAlignmentX(LEFT_ALIGNMENT);

                JPanel textPanel = new JPanel();
                textPanel.setOpaque(false);
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

                JLabel titleLbl = new JLabel("[" + alert.getSeverity().getLabel() + "] " + alert.getTitle());
                titleLbl.setFont(Theme.FONT_SUBHEADING);
                titleLbl.setForeground(sevColor);

                JLabel msgLbl = new JLabel(alert.getMessage());
                msgLbl.setFont(Theme.FONT_SMALL);
                msgLbl.setForeground(Theme.TEXT_SECONDARY);

                textPanel.add(titleLbl);
                textPanel.add(Box.createVerticalStrut(2));
                textPanel.add(msgLbl);

                alertCard.add(textPanel, BorderLayout.CENTER);
                alertsListPanel.add(alertCard);
                alertsListPanel.add(Box.createVerticalStrut(Theme.SPACING_SM));
            }
        }

        alertsListPanel.revalidate();
        alertsListPanel.repaint();
    }

    // ========================
    //   Helpers
    // ========================

    private void saveBudget() {
        try {
            Budget budget = frame.getBudget();
            budget.setOverallMonthlyLimit(parseField(overallLimitField));
            budget.setElectricityLimit(parseField(elecLimitField));
            budget.setWaterLimit(parseField(waterLimitField));
            frame.onBudgetChanged();
            refresh();
            JOptionPane.showMessageDialog(frame, "Budget saved successfully!", "MeterMind", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseField(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0;
        return Double.parseDouble(text);
    }

    private JTextField createBudgetField(String placeholder, double value) {
        JTextField field = new JTextField(value > 0 ? String.valueOf(value) : "");
        field.setFont(Theme.FONT_BODY);
        field.setBackground(Theme.BG_HOVER);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JPanel wrapField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }
}
