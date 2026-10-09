package metermind.ui;

import metermind.alerts.Alert;
import metermind.analysis.*;
import metermind.model.*;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Dashboard panel showing KPI cards, alerts summary, and forecast banner.
 */
public class DashboardPanel extends JPanel {

    private final MeterMindFrame frame;
    private JPanel alertsContainer;

    public DashboardPanel(MeterMindFrame frame) {
        this.frame = frame;
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    public void refresh() {
        removeAll();

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Header
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Your utility spending at a glance");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        mainContent.add(title);
        mainContent.add(Box.createVerticalStrut(4));
        mainContent.add(subtitle);
        mainContent.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, Theme.SPACING, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        kpiRow.setAlignmentX(LEFT_ALIGNMENT);

        BillHistory history = frame.getHistory();

        // Card 1: Latest Electricity Bill
        Optional<UtilityBill> latestElec = history.getLatestBill(UtilityType.ELECTRICITY);
        kpiRow.add(createKPICard("Latest Electricity",
                latestElec.map(b -> CurrencyUtil.formatPeso(b.getAmountDue())).orElse("—"),
                getElecMoMChange(history),
                Theme.PRIMARY));

        // Card 2: Latest Water Bill
        Optional<UtilityBill> latestWater = history.getLatestBill(UtilityType.WATER);
        kpiRow.add(createKPICard("Latest Water",
                latestWater.map(b -> CurrencyUtil.formatPeso(b.getAmountDue())).orElse("—"),
                getWaterMoMChange(history),
                Theme.SERIES_2));

        // Card 3: Monthly Total
        LocalDate now = LocalDate.now();
        double monthlyTotal = history.getMonthlyTotal(now.getYear(), now.getMonthValue());
        kpiRow.add(createKPICard("This Month Total",
                monthlyTotal > 0 ? CurrencyUtil.formatPeso(monthlyTotal) : "—",
                "", Theme.SUCCESS));

        // Card 4: Active Alerts
        int alertCount = frame.getAlertManager().getActiveAlertCount();
        kpiRow.add(createKPICard("Active Alerts",
                String.valueOf(alertCount),
                alertCount > 0 ? "Needs attention" : "All clear",
                alertCount > 0 ? Theme.WARNING : Theme.SUCCESS));

        mainContent.add(kpiRow);
        mainContent.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Forecast Banner
        JPanel forecastPanel = createForecastBanner(history);
        forecastPanel.setAlignmentX(LEFT_ALIGNMENT);
        mainContent.add(forecastPanel);
        mainContent.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Alerts Section
        JLabel alertsTitle = new JLabel("• Recent Alerts");
        alertsTitle.setFont(Theme.FONT_HEADING);
        alertsTitle.setForeground(Theme.TEXT_PRIMARY);
        alertsTitle.setAlignmentX(LEFT_ALIGNMENT);
        mainContent.add(alertsTitle);
        mainContent.add(Box.createVerticalStrut(Theme.SPACING));

        alertsContainer = new JPanel();
        alertsContainer.setLayout(new BoxLayout(alertsContainer, BoxLayout.Y_AXIS));
        alertsContainer.setOpaque(false);
        alertsContainer.setAlignmentX(LEFT_ALIGNMENT);
        updateAlertCards(frame.getAlertManager().getActiveAlerts());
        mainContent.add(alertsContainer);

        mainContent.add(Box.createVerticalStrut(Theme.SPACING_LG));

        // Quick Actions
        JLabel actionsTitle = new JLabel("Quick Actions");
        actionsTitle.setFont(Theme.FONT_HEADING);
        actionsTitle.setForeground(Theme.TEXT_PRIMARY);
        actionsTitle.setAlignmentX(LEFT_ALIGNMENT);
        mainContent.add(actionsTitle);
        mainContent.add(Box.createVerticalStrut(Theme.SPACING));

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING, 0));
        actionsRow.setOpaque(false);
        actionsRow.setAlignmentX(LEFT_ALIGNMENT);
        actionsRow.add(createActionButton("Add Bill", () -> frame.showAddBillDialog()));
        actionsRow.add(createActionButton("View History", () -> frame.switchTo("Bill History")));
        actionsRow.add(createActionButton("Gap Analysis", () -> frame.switchTo("Gap Analyzer")));
        actionsRow.add(createActionButton("Trends", () -> frame.switchTo("Trends")));
        mainContent.add(actionsRow);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ========================
    //   KPI Card
    // ========================

    private JPanel createKPICard(String label, String value, String subtitle, Color accentColor) {
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

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(Theme.FONT_KPI_LABEL);
        labelLbl.setForeground(Theme.TEXT_SECONDARY);
        labelLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(Theme.FONT_KPI_VALUE);
        valueLbl.setForeground(Theme.TEXT_PRIMARY);
        valueLbl.setAlignmentX(LEFT_ALIGNMENT);

        card.add(labelLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLbl);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(Theme.FONT_SMALL);
            // Color the subtitle based on content
            if (subtitle.startsWith("+")) {
                subLbl.setForeground(Theme.DANGER);
            } else if (subtitle.startsWith("-")) {
                subLbl.setForeground(Theme.SUCCESS);
            } else if (subtitle.equals("Needs attention")) {
                subLbl.setForeground(Theme.WARNING);
            } else {
                subLbl.setForeground(Theme.TEXT_MUTED);
            }
            subLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(subLbl);
        }

        return card;
    }

    // ========================
    //   Forecast Banner
    // ========================

    private JPanel createForecastBanner(BillHistory history) {
        JPanel banner = new JPanel() {
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
        banner.setOpaque(false);
        banner.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.CARD_PADDING, 16));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        AverageForecaster forecaster = new AverageForecaster();
        double elecForecast = forecaster.forecast(history, UtilityType.ELECTRICITY);
        double waterForecast = forecaster.forecast(history, UtilityType.WATER);

        StringBuilder sb = new StringBuilder("Next Month Forecast:  ");
        if (elecForecast > 0) {
            sb.append("Electricity ~").append(CurrencyUtil.formatPeso(elecForecast));
        }
        if (waterForecast > 0) {
            sb.append("    |    Water ~").append(CurrencyUtil.formatPeso(waterForecast));
        }
        if (elecForecast <= 0 && waterForecast <= 0) {
            sb.append("Insufficient data for forecast");
        }

        JLabel forecastLabel = new JLabel(sb.toString());
        forecastLabel.setFont(Theme.FONT_SUBHEADING);
        forecastLabel.setForeground(Theme.TEXT_PRIMARY);
        banner.add(forecastLabel);

        return banner;
    }

    // ========================
    //   Alerts
    // ========================

    public void updateAlerts(List<Alert> alerts) {
        if (alertsContainer != null) {
            updateAlertCards(alerts);
        }
    }

    private void updateAlertCards(List<Alert> alerts) {
        alertsContainer.removeAll();

        if (alerts.isEmpty()) {
            JLabel noAlerts = new JLabel("• No active alerts — everything looks good!");
            noAlerts.setFont(Theme.FONT_BODY);
            noAlerts.setForeground(Theme.SUCCESS);
            noAlerts.setAlignmentX(LEFT_ALIGNMENT);
            alertsContainer.add(noAlerts);
        } else {
            for (Alert alert : alerts) {
                alertsContainer.add(createAlertCard(alert));
                alertsContainer.add(Box.createVerticalStrut(Theme.SPACING_SM));
            }
        }

        alertsContainer.revalidate();
        alertsContainer.repaint();
    }

    private JPanel createAlertCard(Alert alert) {
        Color severityColor;
        Color bgColor;
        switch (alert.getSeverity()) {
            case DANGER: severityColor = Theme.DANGER; bgColor = Theme.DANGER_BG; break;
            case WARNING: severityColor = Theme.WARNING; bgColor = Theme.WARNING_BG; break;
            default: severityColor = Theme.TEXT_SECONDARY; bgColor = Theme.BG_SURFACE; break;
        }

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(Theme.BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(alert.getTitle());
        titleLbl.setFont(Theme.FONT_SUBHEADING);
        titleLbl.setForeground(severityColor);

        JLabel msgLbl = new JLabel(alert.getMessage());
        msgLbl.setFont(Theme.FONT_SMALL);
        msgLbl.setForeground(Theme.TEXT_SECONDARY);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(msgLbl);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // ========================
    //   Action Buttons
    // ========================

    private JPanel createActionButton(String text, Runnable action) {
        JPanel btn = new JPanel() {
            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) { action.run(); }
                });
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(hovered ? Theme.BG_HOVER : Theme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);

                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.BUTTON_RADIUS, Theme.BUTTON_RADIUS);
            }
        };
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(160, 42));
        btn.setLayout(new GridBagLayout());

        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_SUBHEADING);
        label.setForeground(Theme.TEXT_PRIMARY);
        btn.add(label);

        return btn;
    }

    // ========================
    //   Helpers
    // ========================

    private String getElecMoMChange(BillHistory history) {
        MonthOverMonth mom = new MonthOverMonth();
        Optional<GapResult> result = mom.compare(history, UtilityType.ELECTRICITY);
        return result.map(r -> CurrencyUtil.formatPercentChange(r.getPercentChange())).orElse("");
    }

    private String getWaterMoMChange(BillHistory history) {
        MonthOverMonth mom = new MonthOverMonth();
        Optional<GapResult> result = mom.compare(history, UtilityType.WATER);
        return result.map(r -> CurrencyUtil.formatPercentChange(r.getPercentChange())).orElse("");
    }
}
