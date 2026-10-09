package metermind.ui;

import metermind.alerts.Alert;
import metermind.alerts.AlertManager;
import metermind.analysis.*;
import metermind.model.*;
import metermind.storage.DataManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main application frame with sidebar navigation and card panel switching.
 * This is the shell that hosts all MeterMind panels.
 */
public class MeterMindFrame extends JFrame {

    // Core data
    private BillHistory history;
    private Budget budget;
    private DataManager dataManager;
    private AlertManager alertManager;

    // UI panels
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DashboardPanel dashboardPanel;
    private BillHistoryPanel billHistoryPanel;
    private GapAnalyzerPanel gapAnalyzerPanel;
    private ChartPanel chartPanel;
    private BudgetAlertsPanel budgetAlertsPanel;
    private SettingsPanel settingsPanel;
    private AddBillPanel addBillPanel;

    // Sidebar buttons
    private JPanel sidebarPanel;
    private String activeNav = "Dashboard";

    // Navigation items
    private static final String[] NAV_ITEMS = {
            "Dashboard", "Add Bill", "Bill History",
            "Gap Analyzer", "Trends", "Budget & Alerts", "Settings"
    };

    public MeterMindFrame() {
        super("MeterMind - Utility Bill Tracker");
        initData();
        initUI();
        initAlerts();
    }

    private void initData() {
        dataManager = new DataManager();

        // Load saved data (starts empty on first run)
        try {
            if (dataManager.billsFileExists()) {
                history = dataManager.loadBills();
            } else {
                history = new BillHistory();
                dataManager.saveBills(history);
            }
        } catch (Exception e) {
            System.err.println("[MeterMind] Error loading data: " + e.getMessage());
            history = new BillHistory();
        }

        budget = dataManager.loadBudget();

        // Create the alert manager before the UI so panels can read the alert count while being built
        alertManager = new AlertManager(history, budget);
        alertManager.setHighBillThreshold(dataManager.loadHighBillThreshold());
        alertManager.setReminderDays(dataManager.loadReminderDays());
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));

        // Sidebar
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // Content area with CardLayout for panel switching
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_PRIMARY);

        // Create all panels
        dashboardPanel = new DashboardPanel(this);
        billHistoryPanel = new BillHistoryPanel(this);
        gapAnalyzerPanel = new GapAnalyzerPanel(this);
        chartPanel = new ChartPanel(this);
        budgetAlertsPanel = new BudgetAlertsPanel(this);
        settingsPanel = new SettingsPanel(this);
        addBillPanel = new AddBillPanel(this);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(addBillPanel, "Add Bill");
        contentPanel.add(billHistoryPanel, "Bill History");
        contentPanel.add(gapAnalyzerPanel, "Gap Analyzer");
        contentPanel.add(chartPanel, "Trends");
        contentPanel.add(budgetAlertsPanel, "Budget & Alerts");
        contentPanel.add(settingsPanel, "Settings");

        add(contentPanel, BorderLayout.CENTER);

        // Show dashboard by default
        switchTo("Dashboard");
    }

    private void initAlerts() {
        alertManager.addListener(alerts -> {
            dashboardPanel.updateAlerts(alerts);
            budgetAlertsPanel.updateAlerts(alerts);
        });

        alertManager.start();
    }

    // ========================
    //   Sidebar
    // ========================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Right border line
                g2.setColor(Theme.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Logo / Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);
        logoPanel.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 60));
        JLabel logoLabel = new JLabel("MeterMind");
        logoLabel.setFont(Theme.FONT_HEADING);
        logoLabel.setForeground(Theme.TEXT_PRIMARY);
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel);

        sidebar.add(Box.createVerticalStrut(30));

        // Nav items
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            String name = NAV_ITEMS[i];
            JPanel navButton = createNavButton(name);
            sidebar.add(navButton);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Version label at bottom
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(Theme.FONT_SMALL);
        versionLabel.setForeground(Theme.TEXT_MUTED);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(versionLabel);

        return sidebar;
    }

    private JPanel createNavButton(String name) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Theme.enableAntiAliasing(g2);

                if (activeNav.equals(name)) {
                    // Active state - plain light gray background
                    g2.setColor(Theme.BG_HOVER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));


        JLabel textLabel = new JLabel(name);
        textLabel.setFont(name.equals(activeNav) ? Theme.FONT_NAV_ACTIVE : Theme.FONT_NAV);
        textLabel.setForeground(name.equals(activeNav) ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);

        btn.add(textLabel);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (name.equals("Add Bill")) {
                    addBillPanel.setEditingBill(null);
                    switchTo(name);
                } else {
                    switchTo(name);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!activeNav.equals(name)) {
                    btn.setBackground(Theme.BG_HOVER);
                    btn.setOpaque(true);
                    textLabel.setForeground(Theme.TEXT_PRIMARY);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!activeNav.equals(name)) {
                    btn.setOpaque(false);
                    textLabel.setForeground(Theme.TEXT_SECONDARY);
                }
            }
        });

        return btn;
    }

    // ========================
    //   Navigation
    // ========================

    public void switchTo(String panelName) {
        activeNav = panelName;
        cardLayout.show(contentPanel, panelName);

        // Refresh the target panel
        switch (panelName) {
            case "Dashboard": dashboardPanel.refresh(); break;
            case "Bill History": billHistoryPanel.refresh(); break;
            case "Gap Analyzer": gapAnalyzerPanel.refresh(); break;
            case "Trends": chartPanel.refresh(); break;
            case "Budget & Alerts": budgetAlertsPanel.refresh(); break;
            case "Settings": settingsPanel.refresh(); break;
        }

        // Update sidebar text styling and repaint
        refreshSidebar();
        sidebarPanel.repaint();
    }

    private void refreshSidebar() {
        for (Component c : sidebarPanel.getComponents()) {
            if (!(c instanceof JPanel)) continue;
            for (Component child : ((JPanel) c).getComponents()) {
                if (child instanceof JLabel) {
                    JLabel label = (JLabel) child;
                    for (String item : NAV_ITEMS) {
                        if (item.equals(label.getText())) {
                            boolean active = item.equals(activeNav);
                            label.setFont(active ? Theme.FONT_NAV_ACTIVE : Theme.FONT_NAV);
                            label.setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                        }
                    }
                }
            }
        }
    }

    // ========================
    //   Edit Bill
    // ========================

    public void showAddBillDialog() {
        addBillPanel.setEditingBill(null);
        switchTo("Add Bill");
    }

    public void showEditBillDialog(UtilityBill bill) {
        addBillPanel.setEditingBill(bill);
        switchTo("Add Bill");
    }

    // ========================
    //   Data Access
    // ========================

    public BillHistory getHistory() { return history; }
    public Budget getBudget() { return budget; }
    public DataManager getDataManager() { return dataManager; }
    public AlertManager getAlertManager() { return alertManager; }

    /**
     * Called after data changes to persist and refresh all panels.
     */
    public void onDataChanged() {
        try {
            dataManager.saveBills(history);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save data: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }

        // Trigger alert recheck
        alertManager.checkAlerts();

        // Refresh the currently visible panel
        switchTo(activeNav);
    }

    /**
     * Saves budget changes and refreshes alerts.
     */
    public void onBudgetChanged() {
        try {
            dataManager.saveBudget(budget);
        } catch (Exception e) {
            System.err.println("[MeterMind] Failed to save budget: " + e.getMessage());
        }
        alertManager.checkAlerts();
    }
}
