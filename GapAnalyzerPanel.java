package metermind.ui;

import metermind.analysis.*;
import metermind.model.*;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Gap Analyzer panel — lets users pick two months and see a detailed
 * breakdown of what drove the cost change (Usage Effect vs Rate Effect).
 */
public class GapAnalyzerPanel extends JPanel {

    private final MeterMindFrame frame;
    private JComboBox<String> typeCombo;
    private JComboBox<String> month1Combo;
    private JComboBox<String> month2Combo;
    private JPanel resultPanel;

    public GapAnalyzerPanel(MeterMindFrame frame) {
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
        JLabel title = new JLabel("• Gap Analyzer");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Compare two billing periods to understand what drove the cost change");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING, 0));
        controls.setOpaque(false);
        controls.setAlignmentX(LEFT_ALIGNMENT);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Utility type selector
        typeCombo = new JComboBox<>(new String[]{"Electricity", "Water"});
        styleCombo(typeCombo);
        typeCombo.addActionListener(e -> {
            updateMonthCombos();
            analyzeGap();
        });

        controls.add(labelFor("Type:"));
        controls.add(typeCombo);

        // Month selectors
        month1Combo = new JComboBox<>();
        month2Combo = new JComboBox<>();
        styleCombo(month1Combo);
        styleCombo(month2Combo);
        month1Combo.addActionListener(e -> analyzeGap());
        month2Combo.addActionListener(e -> analyzeGap());

        controls.add(Box.createHorizontalStrut(Theme.SPACING));
        controls.add(labelFor("From:"));
        controls.add(month1Combo);
        controls.add(labelFor("to"));
        controls.add(labelFor("To:"));
        controls.add(month2Combo);

        // Analyze button
        JButton analyzeBtn = new JButton("Analyze");
        analyzeBtn.setFont(Theme.FONT_SUBHEADING);
        analyzeBtn.setBackground(Theme.PRIMARY);
        analyzeBtn.setForeground(Color.WHITE);
        analyzeBtn.setFocusPainted(false);
        analyzeBtn.setBorderPainted(false);
        analyzeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        analyzeBtn.addActionListener(e -> analyzeGap());
        controls.add(Box.createHorizontalStrut(Theme.SPACING));
        controls.add(analyzeBtn);

        content.add(controls);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Results panel
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setOpaque(false);
        resultPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(resultPanel);

        // Populate month combos
        updateMonthCombos();

        // Auto-analyze if possible
        analyzeGap();

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void updateMonthCombos() {
        UtilityType type = "Electricity".equals(typeCombo.getSelectedItem())
                ? UtilityType.ELECTRICITY : UtilityType.WATER;

        List<UtilityBill> bills = frame.getHistory().getByUtilityType(type);

        month1Combo.removeAllItems();
        month2Combo.removeAllItems();

        for (UtilityBill bill : bills) {
            String label = DateUtil.formatYearMonth(bill.getYear(), bill.getMonth());
            month1Combo.addItem(label);
            month2Combo.addItem(label);
        }

        // Default: select second-to-last and last
        if (month1Combo.getItemCount() >= 2) {
            month1Combo.setSelectedIndex(month1Combo.getItemCount() - 2);
            month2Combo.setSelectedIndex(month2Combo.getItemCount() - 1);
        }
    }

    private void analyzeGap() {
        resultPanel.removeAll();

        if (month1Combo.getSelectedItem() == null || month2Combo.getSelectedItem() == null) {
            showMessage("Select two billing periods to compare.");
            return;
        }

        String sel1 = (String) month1Combo.getSelectedItem();
        String sel2 = (String) month2Combo.getSelectedItem();

        if (sel1.equals(sel2)) {
            showMessage("Please select two different billing periods.");
            return;
        }

        // Parse selections
        int[] ym1 = parseYearMonth(sel1);
        int[] ym2 = parseYearMonth(sel2);
        if (ym1 == null || ym2 == null) return;

        UtilityType type = "Electricity".equals(typeCombo.getSelectedItem())
                ? UtilityType.ELECTRICITY : UtilityType.WATER;

        CustomGap comparison = new CustomGap(ym1[0], ym1[1], ym2[0], ym2[1]);
        Optional<GapResult> result = comparison.compare(frame.getHistory(), type);

        if (!result.isPresent()) {
            showMessage("Bills not found for the selected periods.");
            return;
        }

        GapResult gap = result.get();

        // Delta card
        resultPanel.add(createDeltaCard(gap));
        resultPanel.add(Box.createVerticalStrut(Theme.SPACING));

        // Breakdown row
        JPanel breakdownRow = new JPanel(new GridLayout(1, 2, Theme.SPACING, 0));
        breakdownRow.setOpaque(false);
        breakdownRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        breakdownRow.setAlignmentX(LEFT_ALIGNMENT);

        breakdownRow.add(createEffectCard("Usage Effect",
                CurrencyUtil.formatPeso(Math.abs(gap.getUsageEffect())),
                String.format("Consumption changed by %+.1f %s", gap.getConsumptionDelta(), gap.getBill2().getUnit()),
                gap.getUsageEffect() >= 0 ? Theme.WARNING : Theme.SUCCESS,
                gap));

        breakdownRow.add(createEffectCard("Rate Effect",
                CurrencyUtil.formatPeso(Math.abs(gap.getRateEffect())),
                String.format("Effective rate changed by %+.4f/%s", gap.getRateDelta(), gap.getBill2().getUnit()),
                gap.getRateEffect() >= 0 ? Theme.WARNING : Theme.SUCCESS,
                gap));

        resultPanel.add(breakdownRow);
        resultPanel.add(Box.createVerticalStrut(Theme.SPACING));

        // Explanation
        JPanel explPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.paintCard(g2, 0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        explPanel.setOpaque(false);
        explPanel.setLayout(new BoxLayout(explPanel, BoxLayout.Y_AXIS));
        explPanel.setBorder(BorderFactory.createEmptyBorder(
                Theme.CARD_PADDING, Theme.CARD_PADDING, Theme.CARD_PADDING, Theme.CARD_PADDING));
        explPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        explPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel explTitle = new JLabel("• Explanation");
        explTitle.setFont(Theme.FONT_SUBHEADING);
        explTitle.setForeground(Theme.TEXT_PRIMARY);
        explPanel.add(explTitle);
        explPanel.add(Box.createVerticalStrut(8));

        JTextArea explText = new JTextArea(gap.explain());
        explText.setFont(Theme.FONT_BODY);
        explText.setForeground(Theme.TEXT_SECONDARY);
        explText.setOpaque(false);
        explText.setEditable(false);
        explText.setLineWrap(true);
        explText.setWrapStyleWord(true);
        explPanel.add(explText);

        resultPanel.add(explPanel);

        // Detail comparison table
        resultPanel.add(Box.createVerticalStrut(Theme.SPACING));
        resultPanel.add(createDetailTable(gap));

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    // ========================
    //   Result Cards
    // ========================

    private JPanel createDeltaCard(GapResult gap) {
        Color changeColor = gap.isIncrease() ? Theme.DANGER : Theme.SUCCESS;
        Color bgColor = gap.isIncrease() ? Theme.DANGER_BG : Theme.SUCCESS_BG;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.CARD_RADIUS, Theme.CARD_RADIUS);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.CARD_RADIUS, Theme.CARD_RADIUS);
            }
        };
        card.setOpaque(false);
        card.setLayout(new FlowLayout(FlowLayout.CENTER, Theme.SPACING_LG, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel arrow = new JLabel(gap.isIncrease() ? "Increase" : "Decrease");
        arrow.setFont(Theme.FONT_HEADING);
        arrow.setForeground(changeColor);

        JLabel delta = new JLabel(CurrencyUtil.formatAmountChange(gap.getAmountChange()));
        delta.setFont(new Font("Segoe UI", Font.BOLD, 28));
        delta.setForeground(changeColor);

        JLabel percent = new JLabel("(" + CurrencyUtil.formatPercentChange(gap.getPercentChange()) + ")");
        percent.setFont(Theme.FONT_HEADING);
        percent.setForeground(changeColor);

        card.add(arrow);
        card.add(delta);
        card.add(percent);

        return card;
    }

    private JPanel createEffectCard(String title, String value, String detail, Color accentColor, GapResult gap) {
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

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_KPI_LABEL);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(Theme.FONT_KPI_VALUE);
        valueLbl.setForeground(accentColor);

        JLabel detailLbl = new JLabel(detail);
        detailLbl.setFont(Theme.FONT_SMALL);
        detailLbl.setForeground(Theme.TEXT_MUTED);

        // Proportion bar
        double totalChange = Math.abs(gap.getUsageEffect()) + Math.abs(gap.getRateEffect());
        double effectVal = title.contains("Usage") ? Math.abs(gap.getUsageEffect()) : Math.abs(gap.getRateEffect());
        double proportion = totalChange > 0 ? (effectVal / totalChange) * 100 : 50;

        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.paintGradientBar(g2, 0, 0, getWidth(), getHeight(),
                        accentColor, accentColor.darker(), proportion);
            }
        };
        barPanel.setOpaque(false);
        barPanel.setPreferredSize(new Dimension(0, 6));
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(detailLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(barPanel);

        JLabel pctLbl = new JLabel(String.format("%.0f%% of total change", proportion));
        pctLbl.setFont(Theme.FONT_SMALL);
        pctLbl.setForeground(Theme.TEXT_MUTED);
        card.add(Box.createVerticalStrut(4));
        card.add(pctLbl);

        return card;
    }

    private JPanel createDetailTable(GapResult gap) {
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

        JLabel detailTitle = new JLabel("• Side-by-Side Comparison");
        detailTitle.setFont(Theme.FONT_SUBHEADING);
        detailTitle.setForeground(Theme.TEXT_PRIMARY);
        card.add(detailTitle);
        card.add(Box.createVerticalStrut(12));

        String unit = gap.getBill2().getUnit();
        String[][] rows = {
                {"", DateUtil.formatYearMonth(gap.getBill1().getYear(), gap.getBill1().getMonth()),
                        DateUtil.formatYearMonth(gap.getBill2().getYear(), gap.getBill2().getMonth()), "Change"},
                {"Consumption", CurrencyUtil.formatConsumption(gap.getBill1().getConsumption(), unit),
                        CurrencyUtil.formatConsumption(gap.getBill2().getConsumption(), unit),
                        String.format("%+.1f %s", gap.getConsumptionDelta(), unit)},
                {"Amount Due", CurrencyUtil.formatPeso(gap.getBill1().getAmountDue()),
                        CurrencyUtil.formatPeso(gap.getBill2().getAmountDue()),
                        CurrencyUtil.formatAmountChange(gap.getAmountChange())},
                {"Effective Rate", CurrencyUtil.formatRate(gap.getRate1(), unit),
                        CurrencyUtil.formatRate(gap.getRate2(), unit),
                        String.format("%+.4f", gap.getRateDelta())}
        };

        for (int r = 0; r < rows.length; r++) {
            JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.setAlignmentX(LEFT_ALIGNMENT);

            for (int c = 0; c < rows[r].length; c++) {
                JLabel cell = new JLabel(rows[r][c]);
                cell.setFont(r == 0 ? Theme.FONT_SUBHEADING : Theme.FONT_BODY);
                cell.setForeground(r == 0 ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);
                if (c == 3 && r > 0) {
                    cell.setForeground(Theme.getChangeColor(gap.getAmountChange()));
                }
                row.add(cell);
            }

            card.add(row);
        }

        return card;
    }

    // ========================
    //   Helpers
    // ========================

    private void showMessage(String msg) {
        JLabel label = new JLabel(msg);
        label.setFont(Theme.FONT_BODY);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        resultPanel.add(label);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private int[] parseYearMonth(String label) {
        if (label == null) return null;
        String[] parts = label.split(" ");
        if (parts.length != 2) return null;
        int month = -1;
        for (int i = 0; i < DateUtil.MONTH_SHORT.length; i++) {
            if (DateUtil.MONTH_SHORT[i].equals(parts[0])) { month = i + 1; break; }
        }
        if (month == -1) return null;
        try {
            int year = Integer.parseInt(parts[1]);
            return new int[]{year, month};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private JLabel labelFor(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(Theme.FONT_BODY);
        combo.setBackground(Theme.BG_SURFACE);
        combo.setForeground(Theme.TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }
}
