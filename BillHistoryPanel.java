package metermind.ui;

import metermind.model.*;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Bill history panel with sortable table, filters, and row actions.
 */
public class BillHistoryPanel extends JPanel {

    private final MeterMindFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> yearFilter;
    private JComboBox<String> typeFilter;
    private JTextField searchField;

    private static final String[] COLUMNS = {
            "Period", "Type", "Provider", "Consumption", "Amount", "Due Date", "Status", "Actions"
    };

    public BillHistoryPanel(MeterMindFrame frame) {
        this.frame = frame;
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout(0, Theme.SPACING));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    public void refresh() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Bill History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SM, 0));
        filterBar.setOpaque(false);

        // Year filter
        yearFilter = createStyledCombo(getYearOptions());
        yearFilter.addActionListener(e -> applyFilters());
        filterBar.add(createFilterLabel("Year:"));
        filterBar.add(yearFilter);

        // Type filter
        typeFilter = createStyledCombo(new String[]{"All", "Electricity", "Water"});
        typeFilter.addActionListener(e -> applyFilters());
        filterBar.add(createFilterLabel("Type:"));
        filterBar.add(typeFilter);

        // Search
        searchField = new JTextField(12);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBackground(Theme.BG_SURFACE);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Theme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { applyFilters(); }
        });
        filterBar.add(createFilterLabel("Search:"));
        filterBar.add(searchField);

        header.add(filterBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(Theme.BG_SURFACE);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(44);
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(Theme.BG_SURFACE);
        tableHeader.setForeground(Theme.TEXT_SECONDARY);
        tableHeader.setFont(Theme.FONT_SUBHEADING);
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.BORDER));
        tableHeader.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);  // Period
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // Type
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Provider
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Consumption
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Amount
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Due Date
        table.getColumnModel().getColumn(6).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(150);  // Actions

        // Custom renderers
        table.setDefaultRenderer(Object.class, new CustomCellRenderer());

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) { // Actions column
                    showRowActions(row);
                } else if (row >= 0 && e.getClickCount() == 2) {
                    editBillAtRow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Theme.BG_PRIMARY);
        scrollPane.getViewport().setBackground(Theme.BG_SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        add(scrollPane, BorderLayout.CENTER);

        // Footer summary
        JPanel footer = createFooter();
        add(footer, BorderLayout.SOUTH);

        // Load data
        populateTable(frame.getHistory().getAllSorted());

        revalidate();
        repaint();
    }

    private void populateTable(List<UtilityBill> bills) {
        tableModel.setRowCount(0);
        double totalAmount = 0;

        for (UtilityBill bill : bills) {
            tableModel.addRow(new Object[]{
                    DateUtil.formatYearMonth(bill.getYear(), bill.getMonth()),
                    bill.getUtilityType().getDisplayName(),
                    bill.getProvider(),
                    CurrencyUtil.formatConsumption(bill.getConsumption(), bill.getUnit()),
                    CurrencyUtil.formatPeso(bill.getAmountDue()),
                    DateUtil.formatDisplay(bill.getDueDate()),
                    bill.isPaid() ? "Paid" : "Unpaid",
                    "Edit | Delete | " + (bill.isPaid() ? "Mark Unpaid" : "Mark Paid")
            });
            totalAmount += bill.getAmountDue();
        }
    }

    private void applyFilters() {
        List<UtilityBill> bills = frame.getHistory().getAllSorted();
        String yearSel = (String) yearFilter.getSelectedItem();
        String typeSel = (String) typeFilter.getSelectedItem();
        String search = searchField.getText().toLowerCase().trim();

        bills.removeIf(b -> {
            // Year filter
            if (!"All".equals(yearSel) && !String.valueOf(b.getYear()).equals(yearSel)) return true;
            // Type filter
            if (!"All".equals(typeSel) && !b.getUtilityType().getDisplayName().equals(typeSel)) return true;
            // Search filter
            if (!search.isEmpty()) {
                String combined = (b.getProvider() + " " + b.getUtilityType().getDisplayName()).toLowerCase();
                if (!combined.contains(search)) return true;
            }
            return false;
        });

        populateTable(bills);
    }

    private void showRowActions(int row) {
        String period = (String) tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);

        String[] options = {"Edit", "Delete", "Toggle Paid", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame, "Action for " + type + " bill (" + period + ")",
                "Bill Actions", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        UtilityBill bill = findBillAtRow(row);
        if (bill == null) return;

        switch (choice) {
            case 0: // Edit
                editBillAtRow(row);
                break;
            case 1: // Delete
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Delete this " + type + " bill for " + period + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    frame.getHistory().removeBill(bill.getId());
                    frame.onDataChanged();
                }
                break;
            case 2: // Toggle Paid
                bill.setPaid(!bill.isPaid());
                frame.onDataChanged();
                break;
        }
    }

    private void editBillAtRow(int row) {
        UtilityBill bill = findBillAtRow(row);
        if (bill != null) {
            frame.showEditBillDialog(bill);
        }
    }

    private UtilityBill findBillAtRow(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return null;
        String period = (String) tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);

        // Parse period back to year/month
        String[] parts = period.split(" ");
        if (parts.length != 2) return null;
        int month = -1;
        for (int i = 0; i < DateUtil.MONTH_SHORT.length; i++) {
            if (DateUtil.MONTH_SHORT[i].equals(parts[0])) { month = i + 1; break; }
        }
        int year = Integer.parseInt(parts[1]);
        UtilityType uType = "Electricity".equals(type) ? UtilityType.ELECTRICITY : UtilityType.WATER;

        return frame.getHistory().getByYearMonth(year, month, uType).orElse(null);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING_LG, 8));
        footer.setOpaque(false);

        BillHistory history = frame.getHistory();
        JLabel countLbl = new JLabel("Total: " + history.size() + " bills");
        countLbl.setFont(Theme.FONT_SMALL);
        countLbl.setForeground(Theme.TEXT_SECONDARY);
        footer.add(countLbl);

        return footer;
    }

    private String[] getYearOptions() {
        List<Integer> years = frame.getHistory().getAvailableYears();
        String[] options = new String[years.size() + 1];
        options[0] = "All";
        for (int i = 0; i < years.size(); i++) {
            options[i + 1] = String.valueOf(years.get(i));
        }
        return options;
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(Theme.FONT_BODY);
        combo.setBackground(Theme.BG_SURFACE);
        combo.setForeground(Theme.TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return combo;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    /**
     * Custom cell renderer for alternating row colors and styled badges.
     */
    private class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            cell.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

            if (!isSelected) {
                cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));
            }

            // Style status column
            if (column == 6) {
                String status = (String) value;
                if (status != null && status.contains("Paid")) {
                    cell.setForeground(Theme.SUCCESS);
                } else {
                    cell.setForeground(Theme.WARNING);
                }
            } else if (column == 7) {
                cell.setForeground(Theme.PRIMARY);
                cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                cell.setForeground(isSelected ? Theme.TEXT_PRIMARY : Theme.TEXT_PRIMARY);
            }

            return cell;
        }
    }
}
