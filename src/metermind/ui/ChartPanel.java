package metermind.ui;

import metermind.model.*;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Custom-painted chart panel for visualizing spending and consumption trends.
 * Supports bar charts and line charts with hover tooltips — no external libraries.
 */
public class ChartPanel extends JPanel {

    private final MeterMindFrame frame;
    private JComboBox<String> typeCombo;
    private JComboBox<String> rangeCombo;
    private JComboBox<String> chartTypeCombo;

    public ChartPanel(MeterMindFrame frame) {
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
        JLabel title = new JLabel("📈 Trends & Charts");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Visualize your spending and consumption over time");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING, 0));
        controls.setOpaque(false);
        controls.setAlignmentX(LEFT_ALIGNMENT);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        chartTypeCombo = new JComboBox<>(new String[]{"Bar Chart", "Line Chart"});
        styleCombo(chartTypeCombo);
        chartTypeCombo.addActionListener(e -> repaintChart());

        typeCombo = new JComboBox<>(new String[]{"Electricity", "Water", "Both"});
        styleCombo(typeCombo);
        typeCombo.addActionListener(e -> repaintChart());

        rangeCombo = new JComboBox<>(new String[]{"Last 12 Months", "Last 6 Months", "All (24 Months)"});
        styleCombo(rangeCombo);
        rangeCombo.addActionListener(e -> repaintChart());

        controls.add(labelFor("Chart:"));
        controls.add(chartTypeCombo);
        controls.add(Box.createHorizontalStrut(Theme.SPACING));
        controls.add(labelFor("Type:"));
        controls.add(typeCombo);
        controls.add(Box.createHorizontalStrut(Theme.SPACING));
        controls.add(labelFor("Range:"));
        controls.add(rangeCombo);

        content.add(controls);
        content.add(Box.createVerticalStrut(Theme.SPACING));

        // Spending Chart
        JPanel spendingChart = new UtilityChart("Monthly Spending (₱)", true);
        spendingChart.setPreferredSize(new Dimension(0, 320));
        spendingChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        spendingChart.setAlignmentX(LEFT_ALIGNMENT);
        content.add(spendingChart);
        content.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Consumption Chart
        JPanel consumptionChart = new UtilityChart("Monthly Consumption", false);
        consumptionChart.setPreferredSize(new Dimension(0, 320));
        consumptionChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        consumptionChart.setAlignmentX(LEFT_ALIGNMENT);
        content.add(consumptionChart);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void repaintChart() {
        refresh();
    }

    // ========================
    //   Custom Chart Component
    // ========================

    private class UtilityChart extends JPanel {
        private final String chartTitle;
        private final boolean showAmount; // true = ₱ amount, false = consumption
        private int hoverIndex = -1;

        UtilityChart(String chartTitle, boolean showAmount) {
            this.chartTitle = chartTitle;
            this.showAmount = showAmount;
            setOpaque(false);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int newIndex = getBarIndex(e.getX());
                    if (newIndex != hoverIndex) {
                        hoverIndex = newIndex;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIndex = -1;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Theme.enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            // Card background
            Theme.paintCard(g2, 0, 0, w - 1, h - 1);

            // Chart title
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_SUBHEADING);
            g2.drawString(chartTitle, Theme.CARD_PADDING, Theme.CARD_PADDING + 14);

            // Chart area
            int chartLeft = Theme.CARD_PADDING + 60;
            int chartTop = Theme.CARD_PADDING + 35;
            int chartRight = w - Theme.CARD_PADDING - 10;
            int chartBottom = h - Theme.CARD_PADDING - 30;
            int chartWidth = chartRight - chartLeft;
            int chartHeight = chartBottom - chartTop;

            if (chartWidth <= 0 || chartHeight <= 0) return;

            // Get data
            String selectedType = (String) typeCombo.getSelectedItem();
            boolean showElec = "Electricity".equals(selectedType) || "Both".equals(selectedType);
            boolean showWater = "Water".equals(selectedType) || "Both".equals(selectedType);

            List<UtilityBill> elecBills = frame.getHistory().getByUtilityType(UtilityType.ELECTRICITY);
            List<UtilityBill> waterBills = frame.getHistory().getByUtilityType(UtilityType.WATER);

            // Apply range filter
            int maxMonths = getMaxMonths();
            if (elecBills.size() > maxMonths) elecBills = elecBills.subList(elecBills.size() - maxMonths, elecBills.size());
            if (waterBills.size() > maxMonths) waterBills = waterBills.subList(waterBills.size() - maxMonths, waterBills.size());

            // Determine max value for scale
            double maxVal = 0;
            if (showElec) for (UtilityBill b : elecBills) maxVal = Math.max(maxVal, showAmount ? b.getAmountDue() : b.getConsumption());
            if (showWater) for (UtilityBill b : waterBills) maxVal = Math.max(maxVal, showAmount ? b.getAmountDue() : b.getConsumption());
            maxVal = maxVal * 1.15; // 15% headroom
            if (maxVal == 0) maxVal = 1;

            // Draw grid lines
            g2.setColor(new Color(255, 255, 255, 15));
            g2.setStroke(new BasicStroke(1));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int y = chartBottom - (int) ((double) i / gridLines * chartHeight);
                g2.drawLine(chartLeft, y, chartRight, y);

                // Y-axis labels
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_SMALL);
                double val = maxVal * i / gridLines;
                String label = showAmount ? CurrencyUtil.formatPeso(val) : String.format("%.0f", val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, chartLeft - fm.stringWidth(label) - 8, y + 4);
                g2.setColor(new Color(255, 255, 255, 15));
            }

            // Determine number of data points
            int dataPoints = Math.max(showElec ? elecBills.size() : 0, showWater ? waterBills.size() : 0);
            if (dataPoints == 0) return;

            boolean isLineChart = "Line Chart".equals(chartTypeCombo.getSelectedItem());

            if (isLineChart) {
                drawLineChart(g2, elecBills, waterBills, showElec, showWater,
                        chartLeft, chartTop, chartWidth, chartHeight, chartBottom, maxVal, dataPoints);
            } else {
                drawBarChart(g2, elecBills, waterBills, showElec, showWater,
                        chartLeft, chartTop, chartWidth, chartHeight, chartBottom, maxVal, dataPoints);
            }

            // X-axis labels
            List<UtilityBill> primaryBills = showElec ? elecBills : waterBills;
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < primaryBills.size(); i++) {
                UtilityBill b = primaryBills.get(i);
                String label = DateUtil.getMonthShort(b.getMonth());
                int x;
                if (isLineChart) {
                    x = chartLeft + (int) ((double) i / Math.max(1, dataPoints - 1) * chartWidth);
                } else {
                    double barWidth = (double) chartWidth / dataPoints;
                    x = chartLeft + (int) (i * barWidth + barWidth / 2);
                }
                g2.drawString(label, x - fm.stringWidth(label) / 2, chartBottom + 18);
            }

            // Tooltip on hover
            if (hoverIndex >= 0 && hoverIndex < primaryBills.size()) {
                drawTooltip(g2, primaryBills, elecBills, waterBills, showElec, showWater,
                        chartLeft, chartWidth, chartBottom, chartHeight, maxVal, dataPoints, isLineChart);
            }
        }

        private void drawBarChart(Graphics2D g2, List<UtilityBill> elec, List<UtilityBill> water,
                                  boolean showElec, boolean showWater,
                                  int chartLeft, int chartTop, int chartWidth, int chartHeight,
                                  int chartBottom, double maxVal, int dataPoints) {

            double barGroupWidth = (double) chartWidth / dataPoints;
            int barCount = (showElec ? 1 : 0) + (showWater ? 1 : 0);
            double barWidth = (barGroupWidth * 0.7) / barCount;
            double barGap = barGroupWidth * 0.15;

            // Average for highlighting outliers
            double elecAvg = showElec ? elec.stream().mapToDouble(b -> showAmount ? b.getAmountDue() : b.getConsumption()).average().orElse(0) : 0;
            double waterAvg = showWater ? water.stream().mapToDouble(b -> showAmount ? b.getAmountDue() : b.getConsumption()).average().orElse(0) : 0;

            for (int i = 0; i < dataPoints; i++) {
                double x = chartLeft + i * barGroupWidth + barGap;
                int barIdx = 0;

                if (showElec && i < elec.size()) {
                    double val = showAmount ? elec.get(i).getAmountDue() : elec.get(i).getConsumption();
                    int barH = (int) (val / maxVal * chartHeight);
                    int barX = (int) (x + barIdx * barWidth);
                    int barY = chartBottom - barH;

                    // Color: highlight outliers
                    Color barColor = val > elecAvg * 1.2 ? Theme.WARNING : Theme.PRIMARY;
                    if (i == hoverIndex) barColor = barColor.brighter();

                    g2.setColor(barColor);
                    g2.fillRoundRect(barX, barY, (int) barWidth - 2, barH, 4, 4);
                    barIdx++;
                }

                if (showWater && i < water.size()) {
                    double val = showAmount ? water.get(i).getAmountDue() : water.get(i).getConsumption();
                    int barH = (int) (val / maxVal * chartHeight);
                    int barX = (int) (x + barIdx * barWidth);
                    int barY = chartBottom - barH;

                    Color barColor = val > waterAvg * 1.2 ? Theme.WARNING : new Color(0x06B6D4);
                    if (i == hoverIndex) barColor = barColor.brighter();

                    g2.setColor(barColor);
                    g2.fillRoundRect(barX, barY, (int) barWidth - 2, barH, 4, 4);
                }
            }
        }

        private void drawLineChart(Graphics2D g2, List<UtilityBill> elec, List<UtilityBill> water,
                                   boolean showElec, boolean showWater,
                                   int chartLeft, int chartTop, int chartWidth, int chartHeight,
                                   int chartBottom, double maxVal, int dataPoints) {

            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (showElec && !elec.isEmpty()) {
                drawLine(g2, elec, Theme.PRIMARY, chartLeft, chartWidth, chartHeight, chartBottom, maxVal, dataPoints);
            }
            if (showWater && !water.isEmpty()) {
                drawLine(g2, water, new Color(0x06B6D4), chartLeft, chartWidth, chartHeight, chartBottom, maxVal, dataPoints);
            }
        }

        private void drawLine(Graphics2D g2, List<UtilityBill> bills, Color color,
                              int chartLeft, int chartWidth, int chartHeight, int chartBottom,
                              double maxVal, int dataPoints) {
            Path2D path = new Path2D.Double();
            int[] xPoints = new int[bills.size()];
            int[] yPoints = new int[bills.size()];

            for (int i = 0; i < bills.size(); i++) {
                double val = showAmount ? bills.get(i).getAmountDue() : bills.get(i).getConsumption();
                xPoints[i] = chartLeft + (int) ((double) i / Math.max(1, dataPoints - 1) * chartWidth);
                yPoints[i] = chartBottom - (int) (val / maxVal * chartHeight);

                if (i == 0) path.moveTo(xPoints[i], yPoints[i]);
                else path.lineTo(xPoints[i], yPoints[i]);
            }

            // Draw gradient fill under line
            Path2D fill = new Path2D.Double(path);
            fill.lineTo(xPoints[bills.size() - 1], chartBottom);
            fill.lineTo(xPoints[0], chartBottom);
            fill.closePath();
            g2.setPaint(new GradientPaint(0, chartBottom - chartHeight, new Color(color.getRed(), color.getGreen(), color.getBlue(), 40),
                    0, chartBottom, new Color(color.getRed(), color.getGreen(), color.getBlue(), 5)));
            g2.fill(fill);

            // Draw line
            g2.setColor(color);
            g2.draw(path);

            // Draw data points
            for (int i = 0; i < bills.size(); i++) {
                int radius = i == hoverIndex ? 6 : 4;
                g2.setColor(color);
                g2.fillOval(xPoints[i] - radius, yPoints[i] - radius, radius * 2, radius * 2);
                g2.setColor(Theme.BG_SURFACE);
                g2.fillOval(xPoints[i] - radius + 2, yPoints[i] - radius + 2, (radius - 2) * 2, (radius - 2) * 2);
            }
        }

        private void drawTooltip(Graphics2D g2, List<UtilityBill> primary,
                                 List<UtilityBill> elec, List<UtilityBill> water,
                                 boolean showElec, boolean showWater,
                                 int chartLeft, int chartWidth, int chartBottom, int chartHeight,
                                 double maxVal, int dataPoints, boolean isLineChart) {
            if (hoverIndex >= primary.size()) return;

            UtilityBill bill = primary.get(hoverIndex);
            String tipText = DateUtil.formatYearMonth(bill.getYear(), bill.getMonth()) + ": ";
            if (showAmount) tipText += CurrencyUtil.formatPeso(bill.getAmountDue());
            else tipText += CurrencyUtil.formatConsumption(bill.getConsumption(), bill.getUnit());

            g2.setFont(Theme.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int tipW = fm.stringWidth(tipText) + 16;
            int tipH = 28;

            int tipX;
            if (isLineChart) {
                tipX = chartLeft + (int) ((double) hoverIndex / Math.max(1, dataPoints - 1) * chartWidth) - tipW / 2;
            } else {
                double barGroupWidth = (double) chartWidth / dataPoints;
                tipX = chartLeft + (int) (hoverIndex * barGroupWidth + barGroupWidth / 2) - tipW / 2;
            }
            int tipY = chartBottom - chartHeight - 10;

            // Clamp to chart bounds
            tipX = Math.max(chartLeft, Math.min(tipX, chartLeft + chartWidth - tipW));

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(tipX, tipY, tipW, tipH, 6, 6);
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(tipText, tipX + 8, tipY + tipH - 8);
        }

        private int getBarIndex(int mouseX) {
            int chartLeft = Theme.CARD_PADDING + 60;
            int chartRight = getWidth() - Theme.CARD_PADDING - 10;
            int chartWidth = chartRight - chartLeft;

            String selectedType = (String) typeCombo.getSelectedItem();
            boolean showElec = "Electricity".equals(selectedType) || "Both".equals(selectedType);
            List<UtilityBill> primary = showElec
                    ? frame.getHistory().getByUtilityType(UtilityType.ELECTRICITY)
                    : frame.getHistory().getByUtilityType(UtilityType.WATER);

            int maxMonths = getMaxMonths();
            if (primary.size() > maxMonths) primary = primary.subList(primary.size() - maxMonths, primary.size());

            int dataPoints = primary.size();
            if (dataPoints == 0 || chartWidth <= 0) return -1;

            double barWidth = (double) chartWidth / dataPoints;
            int index = (int) ((mouseX - chartLeft) / barWidth);
            return (index >= 0 && index < dataPoints) ? index : -1;
        }
    }

    private int getMaxMonths() {
        String range = (String) rangeCombo.getSelectedItem();
        if ("Last 6 Months".equals(range)) return 6;
        if ("Last 12 Months".equals(range)) return 12;
        return 24;
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
