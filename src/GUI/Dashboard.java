package GUI;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.border.AbstractBorder;
import model.ComplianceNotice;
import model.FishCatch;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ValueAxis;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

class RoundedBorder extends AbstractBorder {
    private final int radius;
    private final Color backgroundColor;

    public RoundedBorder(int radius, Color bgColor) {
        this.radius = radius;
        this.backgroundColor = bgColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor.darker());
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = radius / 2;
        return insets;
    }
}

public class Dashboard extends JFrame {

    private JPanel sidebar, contentPanel;
    private JPanel contentContainer;
private CardLayout cardLayout;
private java.util.List<FishCatch> fishCatches = new ArrayList<>();
private JPanel cardListboats;
private JPanel cardList;
private JPanel complianceCardList;
private java.util.List<ComplianceNotice> complianceList = new ArrayList<>();


    public Dashboard() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
        
    }
private void loadBoats(String query) {
    cardListboats.removeAll();
    try {
        ResultSet rs = model.Mysql.search(query);
        while (rs.next()) {
            String id = String.valueOf(rs.getInt("boat_id"));
            String name = rs.getString("name");
            String status = rs.getString("status");
            String signal = rs.getString("last_signal");

            cardListboats.add(createBoatCard(id, name, status, signal));
            cardListboats.add(Box.createVerticalStrut(10));
        }
        cardListboats.revalidate();
        cardListboats.repaint();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
private void loadComplianceData() {
    if (complianceList == null) complianceList = new ArrayList<>();
    complianceList.clear();
    complianceCardList.removeAll();

    try {
        ResultSet rs = model.Mysql.search(
            "SELECT c.notice_id, b.name AS boat_name, c.violation, c.violation_date, c.status " +
            "FROM compliance c JOIN boats b ON c.boat_id = b.boat_id ORDER BY c.violation_date DESC"
        );

        while (rs.next()) {
            ComplianceNotice notice = new ComplianceNotice(
                rs.getString("notice_id"),
                rs.getString("boat_name"),
                rs.getString("violation"),
                rs.getString("violation_date"),
                rs.getString("status")
            );
            complianceList.add(notice);
            complianceCardList.add(createComplianceCard(notice));
            complianceCardList.add(Box.createVerticalStrut(10));
        }

        complianceCardList.revalidate();
        complianceCardList.repaint();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to load compliance data", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private void loadBoats() {
    cardListboats.removeAll();
    try {
        ResultSet rs = model.Mysql.search("SELECT * FROM boats");
        while (rs.next()) {
            String id = String.valueOf(rs.getInt("boat_id"));
            String name = rs.getString("name");
            String status = rs.getString("status");
            String signal = rs.getString("last_signal");

            cardListboats.add(createBoatCard(id, name, status, signal));
            cardListboats.add(Box.createVerticalStrut(10));
        }
        cardListboats.revalidate();
        cardListboats.repaint();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
private void loadFishStockFromDB() {
    fishCatches.clear();
    try {
        ResultSet rs = model.Mysql.search(
            "SELECT fs.catch_id, b.name AS boat, ft.name AS fish, fs.catch_date, fs.quantity_kg " +
            "FROM fish_stock fs " +
            "JOIN boats b ON fs.boat_id = b.boat_id " +
            "JOIN fish_types ft ON fs.fish_type_id = ft.type_id"
        );

        while (rs.next()) {
            String id = rs.getString("catch_id");
            String boat = rs.getString("boat");
            String fish = rs.getString("fish");
            String date = rs.getString("catch_date");
            String qty = rs.getString("quantity_kg");

            fishCatches.add(new FishCatch(id, boat, fish, date, qty));
        }

        refreshFishStockView();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private JPanel createSummaryChartPanel() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
Map<String, Double> dateToQty = new LinkedHashMap<>();

// Generate last 7 days (including today)
LocalDate today = LocalDate.now();
for (int i = 6; i >= 0; i--) {
    String dateStr = today.minusDays(i).toString();
    dateToQty.put(dateStr, 0.0);
}

try {
    ResultSet rs = model.Mysql.search(
        "SELECT catch_date, SUM(quantity_kg) as total " +
        "FROM fish_stock " +
        "WHERE catch_date >= CURDATE() - INTERVAL 7 DAY " +
        "GROUP BY catch_date ORDER BY catch_date"
    );

    while (rs.next()) {
        String date = rs.getString("catch_date");
        double total = rs.getDouble("total");
        if (dateToQty.containsKey(date)) {
            dateToQty.put(date, total);
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}

for (Map.Entry<String, Double> entry : dateToQty.entrySet()) {
    dataset.addValue(entry.getValue(), "Fish (kg)", entry.getKey());
}


    // Create Bar Chart
    JFreeChart chart = ChartFactory.createBarChart(
        "", // No chart title for cleaner look
        "", // X-axis label hidden
        "", // Y-axis label hidden
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false
    );

    // === Styling ===
    chart.setBackgroundPaint(new Color(17, 36, 51)); // Outer background
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(23, 43, 64));  // Chart panel background
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinePaint(new Color(80, 80, 80));

    // Bar Colors
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(45, 156, 219));
    renderer.setBarPainter(new StandardBarPainter()); // Flat style
    renderer.setShadowVisible(false); // Remove bar shadows

    // Axis Styling
        org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setTickLabelPaint(Color.WHITE);
    domainAxis.setAxisLinePaint(Color.WHITE);
    domainAxis.setTickMarksVisible(false);

        org.jfree.chart.axis.ValueAxis rangeAxis = plot.getRangeAxis();
    rangeAxis.setTickLabelPaint(Color.WHITE);
    rangeAxis.setAxisLinePaint(Color.WHITE);
    rangeAxis.setTickMarksVisible(false);

    // Panel
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(800, 300));
    chartPanel.setBackground(new Color(17, 36, 51));
    chartPanel.setBorder(BorderFactory.createEmptyBorder());

    return chartPanel;
}


private JPanel createFishTypePieChartPanel() {
    DefaultPieDataset dataset = new DefaultPieDataset();

    try {
        ResultSet rs = model.Mysql.search(
            "SELECT ft.name, SUM(fs.quantity_kg) as total " +
            "FROM fish_stock fs " +
            "JOIN fish_types ft ON fs.fish_type_id = ft.type_id " +
            "WHERE fs.catch_date >= CURDATE() - INTERVAL 7 DAY " +
            "GROUP BY ft.name"
        );

        while (rs.next()) {
            String fishType = rs.getString("name");
            double total = rs.getDouble("total");
            dataset.setValue(fishType, total);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    JFreeChart chart = ChartFactory.createPieChart(
        "Fish Type % (Last 7 Days)",
        dataset,
        true,
        true,
        false
    );

    chart.setBackgroundPaint(new Color(17, 36, 51)); // Dark dashboard background
    chart.getTitle().setPaint(Color.WHITE);
    chart.getLegend().setItemPaint(Color.WHITE);

    // Custom pie slice styling
    PiePlot plot = (PiePlot) chart.getPlot();
    plot.setBackgroundPaint(new Color(17, 36, 51));
    plot.setOutlineVisible(false);
    plot.setLabelBackgroundPaint(new Color(30, 50, 70));
    plot.setLabelOutlinePaint(null);
    plot.setLabelShadowPaint(null);
    plot.setLabelPaint(Color.WHITE);
    plot.setLabelFont(new Font("Inter", Font.PLAIN, 12));
    plot.setShadowPaint(null); // Remove drop shadow

    // Optional: Set custom colors for known fish types
    Map<String, Color> fishColors = new HashMap<>();
    fishColors.put("Tuna", new Color(45, 156, 219));
    fishColors.put("Mackerel", new Color(74, 144, 226));
    fishColors.put("Sardine", new Color(135, 206, 250));
    fishColors.put("Salmon", new Color(255, 99, 132));
    fishColors.put("Haddock", new Color(255, 195, 0));

   for (Object keyObj : dataset.getKeys()) {
    Comparable key = (Comparable) keyObj;
    String keyStr = key.toString();
    Color color = fishColors.getOrDefault(keyStr, new Color(100, 100, 255));
    plot.setSectionPaint(key, color);
}


    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(400, 250));
    chartPanel.setBackground(new Color(17, 36, 51));
    return chartPanel;
}
   private void initUI() {
    setTitle("OceanOps - Dashboard");

    // Set fullscreen with no title bar
    
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setDividerLocation(220);
    splitPane.setDividerSize(2);
    splitPane.setResizeWeight(0);

    sidebar = createSidebar();
    splitPane.setLeftComponent(sidebar);

    // CardLayout container for switching views
    cardLayout = new CardLayout();
    contentContainer = new JPanel(cardLayout);
    
    contentContainer.setBackground(new Color(17, 36, 51));

    
    // Add views
    contentContainer.add(createDashboardPanel(), "Dashboard");
    contentContainer.add(createBoatsPanel(), "Boats");

    contentContainer.add(createFishStockPanel(), "Fish Stock");

    contentContainer.add(createCompliancePanel(), "Compliance");

    contentContainer.add(createReportsPanel(), "Reports");

    splitPane.setRightComponent(contentContainer);

    setContentPane(splitPane);
    setVisible(true);
}


private JPanel createBoatsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(17, 36, 51));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Header
    JLabel title = new JLabel("Boat Management");
    title.setFont(new Font("Inter", Font.BOLD, 22));
    title.setForeground(Color.WHITE);

    // Search bar
    JTextField searchField = new JTextField();
    searchField.setPreferredSize(new Dimension(200, 35));
    searchField.setFont(new Font("Inter", Font.PLAIN, 14));
    searchField.setBackground(new Color(21, 44, 62));
    searchField.setForeground(Color.WHITE);
    searchField.setCaretColor(Color.WHITE);
    searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    JButton searchButton = new JButton("Search");
    searchButton.addActionListener(e -> {
    String keyword = searchField.getText().trim();
    String q = "SELECT * FROM boats WHERE name LIKE '%" + keyword + "%'";
    loadBoats(q);
});
    styleButton(searchButton);

    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setOpaque(false);
    topBar.add(title, BorderLayout.WEST);

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    searchPanel.setOpaque(false);
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    topBar.add(searchPanel, BorderLayout.EAST);

    panel.add(topBar, BorderLayout.NORTH);

    // Table
  // === Card List Panel ===
 cardListboats = new JPanel();
cardListboats.setLayout(new BoxLayout(cardListboats, BoxLayout.Y_AXIS));
cardListboats.setBackground(new Color(17, 36, 51));

cardListboats.removeAll();
try {
    ResultSet rs = model.Mysql.search("SELECT * FROM boats");
    while (rs.next()) {
        String id = String.valueOf(rs.getInt("boat_id"));
        String name = rs.getString("name");
        String status = rs.getString("status");
        String signal = rs.getString("last_signal");

        cardListboats.add(createBoatCard(id, name, status, signal));
        cardListboats.add(Box.createVerticalStrut(10));
    }
    cardListboats.revalidate();
    cardListboats.repaint();
} catch (Exception ex) {
    ex.printStackTrace();
}


JScrollPane scrollPane = new JScrollPane(cardListboats);
scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
scrollPane.getVerticalScrollBar().setUnitIncrement(16);
scrollPane.getViewport().setBackground(new Color(17, 36, 51));

panel.add(scrollPane, BorderLayout.CENTER);


    // Bottom bar with action buttons
    JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomBar.setOpaque(false);

    JButton addButton = new JButton("Add New Boat");
    addButton.addActionListener(e -> {
    String name = JOptionPane.showInputDialog("Enter Boat Name:");
    if (name != null && !name.trim().isEmpty()) {
        try {
            String q = "INSERT INTO boats (name, status, last_signal) VALUES ('" + name + "', 'Active', NOW())";
            model.Mysql.iud(q);
            String searchq="Select * from boats";
            loadBoats(searchq);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Failed to add boat.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
});

    JButton updateButton = new JButton("Update Selected");
    JButton deleteButton = new JButton("Delete Selected");
    deleteButton.addActionListener(e -> {
    String id = JOptionPane.showInputDialog("Enter Boat ID to delete:");
    if (id != null && !id.trim().isEmpty()) {
        try {
            model.Mysql.iud("DELETE FROM boats WHERE boat_id = " + id);
            String searchq="Select * from boats";
            loadBoats(searchq);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Failed to delete boat.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
});

    styleButton(addButton);
    styleButton(updateButton);
    styleButton(deleteButton);

    bottomBar.add(addButton);
    bottomBar.add(updateButton);
    bottomBar.add(deleteButton);

    panel.add(bottomBar, BorderLayout.SOUTH);

    // TODO: Add logic for each button (search, update, delete)

    return panel;
}


private JPanel createBoatCard(String id, String name, String status, String lastSignal) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(new Color(21, 44, 62));
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 100, 120), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
    ));
    card.setPreferredSize(new Dimension(600, 80));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    // Left section: name & last signal
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setOpaque(false);

    JLabel nameLabel = new JLabel("🛥 " + name);
    nameLabel.setFont(new Font("Inter", Font.BOLD, 16));
    nameLabel.setForeground(Color.WHITE);

    JLabel signalLabel = new JLabel("Last Signal: " + lastSignal);
    signalLabel.setFont(new Font("Inter", Font.PLAIN, 13));
    signalLabel.setForeground(new Color(180, 180, 180));

    left.add(nameLabel);
    left.add(signalLabel);

    // Right section: status and buttons
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    right.setOpaque(false);

    JLabel statusLabel = new JLabel(status);
    statusLabel.setOpaque(true);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    statusLabel.setForeground(Color.WHITE);
    statusLabel.setFont(new Font("Inter", Font.BOLD, 12));
    statusLabel.setBackground(status.equalsIgnoreCase("Active") ?
            new Color(39, 174, 96) : new Color(235, 87, 87));

    JButton editBtn = new JButton("Edit");
    JButton delBtn = new JButton("Delete");
    styleButton(editBtn);
    styleButton(delBtn);

    // ===== DELETE BOAT LOGIC =====
    delBtn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(card, "Delete boat: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                model.Mysql.iud("DELETE FROM boats WHERE boat_id = " + id);
                loadBoats(); // refresh
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(card, "Failed to delete boat.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    // ===== EDIT BOAT LOGIC =====
    editBtn.addActionListener(e -> {
        JTextField nameField = new JTextField(name);
        String[] statusOptions = {"Active", "Docked", "Offline"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        statusBox.setSelectedItem(status);

        JPanel form = new JPanel(new GridLayout(0, 1));
        form.add(new JLabel("Boat Name:"));
        form.add(nameField);
        form.add(new JLabel("Status:"));
        form.add(statusBox);

        int result = JOptionPane.showConfirmDialog(card, form, "Edit Boat", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newStatus = (String) statusBox.getSelectedItem();

            if (!newName.isEmpty()) {
                try {
                    String q = "UPDATE boats SET name='" + newName + "', status='" + newStatus + "' WHERE boat_id=" + id;
                    model.Mysql.iud(q);
                    loadBoats(); // refresh
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(card, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    });

    right.add(statusLabel);
    right.add(Box.createHorizontalStrut(10));
    right.add(editBtn);
    right.add(delBtn);

    card.add(left, BorderLayout.WEST);
    card.add(right, BorderLayout.EAST);

    return card;
}


private JPanel createFishStockPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(17, 36, 51));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Title and Search Bar
    JLabel title = new JLabel("Fish Stock Management");
    title.setFont(new Font("Inter", Font.BOLD, 22));
    title.setForeground(Color.WHITE);

    JTextField searchField = new JTextField();
    searchField.setPreferredSize(new Dimension(200, 35));
    searchField.setFont(new Font("Inter", Font.PLAIN, 14));
    searchField.setBackground(new Color(21, 44, 62));
    searchField.setForeground(Color.WHITE);
    searchField.setCaretColor(Color.WHITE);
    searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    JButton searchButton = new JButton("Search");
    styleButton(searchButton);

    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setOpaque(false);
    topBar.add(title, BorderLayout.WEST);

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    searchPanel.setOpaque(false);
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    topBar.add(searchPanel, BorderLayout.EAST);
    panel.add(topBar, BorderLayout.NORTH);

    // === Card List Panel ===
     cardList = new JPanel();
    cardList.setLayout(new BoxLayout(cardList, BoxLayout.Y_AXIS));
    cardList.setBackground(new Color(17, 36, 51));

    // Dummy data
    fishCatches = new ArrayList<>();
try {
    ResultSet rs = model.Mysql.search(
        "SELECT fs.catch_id, b.name AS boat, ft.name AS fish, fs.catch_date, fs.quantity_kg " +
        "FROM fish_stock fs " +
        "JOIN boats b ON fs.boat_id = b.boat_id " +
        "JOIN fish_types ft ON fs.fish_type_id = ft.type_id"
    );

    while (rs.next()) {
        String id = rs.getString("catch_id");
        String boat = rs.getString("boat");
        String fish = rs.getString("fish");
        String date = rs.getString("catch_date");
        String qty = rs.getString("quantity_kg");

        fishCatches.add(new FishCatch(id, boat, fish, date, qty));
    }
    loadFishStockFromDB();
} catch (Exception ex) {
    ex.printStackTrace();
}


    JScrollPane scrollPane = new JScrollPane(cardList);
    scrollPane.getViewport().setBackground(new Color(17, 36, 51));
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    panel.add(scrollPane, BorderLayout.CENTER);

    // === Bottom Action Buttons ===
    JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomBar.setOpaque(false);

    JButton addButton = new JButton("Add Catch");
    addButton.addActionListener(e->{showAddCatchDialog();});
    JButton updateButton = new JButton("Update Selected");
  

    JButton deleteButton = new JButton("Delete Selected");

    styleButton(addButton);
    styleButton(updateButton);
    styleButton(deleteButton);

    bottomBar.add(addButton);
    bottomBar.add(updateButton);
    bottomBar.add(deleteButton);

    panel.add(bottomBar, BorderLayout.SOUTH);

    return panel;
}
private JPanel createFishCard(FishCatch fishCatch) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(new Color(21, 44, 62));
    card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    // Left info
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setOpaque(false);

    JLabel label1 = new JLabel("🐟 " + fishCatch.getFishType() + " - " + fishCatch.getQuantity() + " kg");
    label1.setFont(new Font("Inter", Font.BOLD, 16));
    label1.setForeground(Color.WHITE);

    JLabel label2 = new JLabel("Boat: " + fishCatch.getBoat() + " | Date: " + fishCatch.getDate());
    label2.setFont(new Font("Inter", Font.PLAIN, 13));
    label2.setForeground(new Color(180, 180, 180));

    left.add(label1);
    left.add(label2);

    // Right buttons
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    right.setOpaque(false);

    JButton editBtn = new JButton("Edit");
    JButton delBtn = new JButton("Delete");
    delBtn.addActionListener(e -> {
    int confirm = JOptionPane.showConfirmDialog(card, "Delete this catch?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            model.Mysql.iud("DELETE FROM fish_stock WHERE catch_id = " + fishCatch.getId());
            loadFishStockFromDB();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(card, "Delete failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
});

    styleButton(editBtn);
    styleButton(delBtn);

    // ✅ Edit handler
    editBtn.addActionListener(e -> showEditCatchDialog(fishCatch));

    // (Optional: delBtn can remove from list + UI)

    right.add(editBtn);
    right.add(delBtn);

    card.add(left, BorderLayout.WEST);
    card.add(right, BorderLayout.EAST);

    return card;
}

private void showEditCatchDialog(FishCatch fishCatch) {
    JDialog dialog = new JDialog(this, "Edit Catch", true);
    dialog.setSize(400, 350);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(new Color(17, 36, 51));

    JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
    form.setBackground(new Color(17, 36, 51));
    form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel boatLabel = new JLabel("Boat:");
    JLabel fishLabel = new JLabel("Fish Type:");
    JLabel dateLabel = new JLabel("Date:");
    JLabel qtyLabel = new JLabel("Quantity (kg):");

    for (JLabel l : new JLabel[]{boatLabel, fishLabel, dateLabel, qtyLabel})
        l.setForeground(Color.WHITE);

    JComboBox<String> boatDropdown = new JComboBox<>();
JComboBox<String> fishDropdown = new JComboBox<>();

try {
    ResultSet rs = model.Mysql.search("SELECT name FROM boats");
    while (rs.next()) {
        boatDropdown.addItem(rs.getString("name"));
    }
    boatDropdown.setSelectedItem(fishCatch.getBoat());
} catch (Exception ex) {
    ex.printStackTrace();
}

try {
    ResultSet rs = model.Mysql.search("SELECT name FROM fish_types");
    while (rs.next()) {
        fishDropdown.addItem(rs.getString("name"));
    }
    fishDropdown.setSelectedItem(fishCatch.getFishType());
} catch (Exception ex) {
    ex.printStackTrace();
}

    JTextField dateField = new JTextField(fishCatch.getDate());
    JTextField qtyField = new JTextField(fishCatch.getQuantity());

    boatDropdown.setSelectedItem(fishCatch.getBoat());
    fishDropdown.setSelectedItem(fishCatch.getFishType());

    styleComboBox(boatDropdown);
    styleComboBox(fishDropdown);
    styleFormField(dateField);
    styleFormField(qtyField);

    form.add(boatLabel); form.add(boatDropdown);
    form.add(fishLabel); form.add(fishDropdown);
    form.add(dateLabel); form.add(dateField);
    form.add(qtyLabel); form.add(qtyField);

    dialog.add(form, BorderLayout.CENTER);

    // Buttons
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.setBackground(new Color(17, 36, 51));
    JButton updateBtn = new JButton("Update");
    JButton cancelBtn = new JButton("Cancel");

    styleButton(updateBtn);
    styleButton(cancelBtn);

    updateBtn.addActionListener(e -> {
    try {
        String boat = boatDropdown.getSelectedItem().toString();
        String fish = fishDropdown.getSelectedItem().toString();
        String date = dateField.getText();
        String qty = qtyField.getText();

        ResultSet boatRS = model.Mysql.search("SELECT boat_id FROM boats WHERE name = '" + boat + "'");
        boatRS.next();
        int boatId = boatRS.getInt("boat_id");

        ResultSet fishRS = model.Mysql.search("SELECT type_id FROM fish_types WHERE name = '" + fish + "'");
        fishRS.next();
        int fishId = fishRS.getInt("type_id");

        String q = "UPDATE fish_stock SET boat_id=" + boatId + ", fish_type_id=" + fishId +
                   ", catch_date='" + date + "', quantity_kg='" + qty + "' WHERE catch_id=" + fishCatch.getId();
        model.Mysql.iud(q);

        loadFishStockFromDB();
        dialog.dispose();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(dialog, "Update failed", "Error", JOptionPane.ERROR_MESSAGE);
    }
});

    cancelBtn.addActionListener(e -> dialog.dispose());
    bottom.add(updateBtn);
    bottom.add(cancelBtn);
    dialog.add(bottom, BorderLayout.SOUTH);

    dialog.setVisible(true);
}
private void refreshFishStockView() {
    cardList.removeAll();
    for (FishCatch catchData : fishCatches) {
        cardList.add(createFishCard(catchData));
        cardList.add(Box.createVerticalStrut(10));
    }
    cardList.revalidate();
    cardList.repaint();
}


private void showAddCatchDialog() {
    JDialog dialog = new JDialog(this, "Add New Catch", true);
    dialog.setSize(400, 350);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(new Color(17, 36, 51));

    JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
    form.setBackground(new Color(17, 36, 51));
    form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel boatLabel = new JLabel("Boat:");
    JLabel fishLabel = new JLabel("Fish Type:");
    JLabel dateLabel = new JLabel("Date:");
    JLabel qtyLabel = new JLabel("Quantity (kg):");

    for (JLabel label : new JLabel[]{boatLabel, fishLabel, dateLabel, qtyLabel}) {
        label.setForeground(Color.WHITE);
    }

    JComboBox<String> boatDropdown = new JComboBox<>();
JComboBox<String> fishDropdown = new JComboBox<>();

// Load boats
try {
    ResultSet rs = model.Mysql.search("SELECT name FROM boats");
    while (rs.next()) {
        boatDropdown.addItem(rs.getString("name"));
    }
} catch (Exception e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(dialog, "Failed to load boats.", "Error", JOptionPane.ERROR_MESSAGE);
}

// Load fish types
try {
    ResultSet rs = model.Mysql.search("SELECT name FROM fish_types");
    while (rs.next()) {
        fishDropdown.addItem(rs.getString("name"));
    }
} catch (Exception e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(dialog, "Failed to load fish types.", "Error", JOptionPane.ERROR_MESSAGE);
}


    JTextField dateField = new JTextField(LocalDate.now().toString());
    JTextField qtyField = new JTextField();

    styleComboBox(boatDropdown);
    styleComboBox(fishDropdown);
    styleFormField(dateField);
    styleFormField(qtyField);

    form.add(boatLabel);
    form.add(boatDropdown);
    form.add(fishLabel);
    form.add(fishDropdown);
    form.add(dateLabel);
    form.add(dateField);
    form.add(qtyLabel);
    form.add(qtyField);

    dialog.add(form, BorderLayout.CENTER);

    // Buttons
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.setBackground(new Color(17, 36, 51));
    JButton addBtn = new JButton("Add Catch");
    addBtn.addActionListener(e -> {
    String boat = boatDropdown.getSelectedItem().toString();
    String fish = fishDropdown.getSelectedItem().toString();
    String date = dateField.getText();
    String qty = qtyField.getText();

    if (date.isEmpty() || qty.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Get IDs from names
        ResultSet boatRS = model.Mysql.search("SELECT boat_id FROM boats WHERE name = '" + boat + "'");
        boatRS.next();
        int boatId = boatRS.getInt("boat_id");

        ResultSet fishRS = model.Mysql.search("SELECT type_id FROM fish_types WHERE name = '" + fish + "'");
        fishRS.next();
        int fishId = fishRS.getInt("type_id");

        String q = "INSERT INTO fish_stock (boat_id, fish_type_id, quantity_kg, catch_date) VALUES (" +
                   boatId + ", " + fishId + ", '" + qty + "', '" + date + "')";
        model.Mysql.iud(q);

        loadFishStockFromDB(); // reload cards
        dialog.dispose();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(dialog, "Error adding record", "Error", JOptionPane.ERROR_MESSAGE);
    }
});

    JButton cancelBtn = new JButton("Cancel");

    styleButton(addBtn);
    styleButton(cancelBtn);

    addBtn.addActionListener(e -> {
        String boat = boatDropdown.getSelectedItem().toString();
        String fish = fishDropdown.getSelectedItem().toString();
        String date = dateField.getText();
        String qty = qtyField.getText();

        if (date.isEmpty() || qty.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: Insert to data model / UI
        System.out.println("Added: " + boat + ", " + fish + ", " + date + ", " + qty);
        dialog.dispose();
    });

    cancelBtn.addActionListener(e -> dialog.dispose());

    bottom.add(addBtn);
    bottom.add(cancelBtn);
    dialog.add(bottom, BorderLayout.SOUTH);

    dialog.setVisible(true);
}
private void styleButton(JButton button) {
    button.setPreferredSize(new Dimension(150, 40));
    button.setBackground(new Color(45, 156, 219)); // OceanOps Blue
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Inter", Font.BOLD, 14));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}
private void styleComboBox(JComboBox<String> comboBox) {
    comboBox.setBackground(new Color(21, 44, 62));
    comboBox.setForeground(Color.WHITE);
    comboBox.setFont(new Font("Inter", Font.PLAIN, 14));
    comboBox.setFocusable(false);
    comboBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
}

private void styleFormField(JTextField field) {
    field.setBackground(new Color(21, 44, 62));
    field.setForeground(Color.WHITE);
    field.setCaretColor(Color.WHITE);
    field.setFont(new Font("Inter", Font.PLAIN, 14));
    field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
}

// Declare globally:


private JPanel createCompliancePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(17, 36, 51));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // === Title and Search ===
    JLabel title = new JLabel("Compliance Management");
    title.setFont(new Font("Inter", Font.BOLD, 22));
    title.setForeground(Color.WHITE);

    JTextField searchField = new JTextField();
    searchField.setPreferredSize(new Dimension(200, 35));
    styleFormField(searchField);

    JButton searchButton = new JButton("Search");
    styleButton(searchButton);

    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setOpaque(false);
    topBar.add(title, BorderLayout.WEST);

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    searchPanel.setOpaque(false);
    searchPanel.add(searchField);
    searchPanel.add(searchButton);
    topBar.add(searchPanel, BorderLayout.EAST);

    panel.add(topBar, BorderLayout.NORTH);

    // === Card List Panel Initialization ===
    complianceCardList = new JPanel();
    complianceCardList.setLayout(new BoxLayout(complianceCardList, BoxLayout.Y_AXIS));
    complianceCardList.setBackground(new Color(17, 36, 51));

    // === Load Data from DB ===
    loadComplianceData();

    JScrollPane scrollPane = new JScrollPane(complianceCardList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    scrollPane.getViewport().setBackground(new Color(17, 36, 51));
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scrollPane, BorderLayout.CENTER);

    // === Bottom Buttons ===
    JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomBar.setOpaque(false);

    JButton addButton = new JButton("Add Notice");
    JButton updateButton = new JButton("Update Selected");
    JButton deleteButton = new JButton("Delete Selected");

    addButton.addActionListener(e -> showAddComplianceDialog());
    // Optional: add listeners for update and delete

    styleButton(addButton);
    styleButton(updateButton);
    styleButton(deleteButton);

    bottomBar.add(addButton);
    bottomBar.add(updateButton);
    bottomBar.add(deleteButton);

    panel.add(bottomBar, BorderLayout.SOUTH);

    return panel;
}


private void showAddComplianceDialog() {
    JDialog dialog = new JDialog(this, "Add Compliance Notice", true);
    dialog.setSize(400, 300);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(new Color(17, 36, 51));

    JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
    form.setBackground(new Color(17, 36, 51));
    form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel boatLabel = new JLabel("Boat:");
    JLabel issueLabel = new JLabel("Violation:");
    JLabel dateLabel = new JLabel("Date:");
    JLabel statusLabel = new JLabel("Status:");

    for (JLabel l : new JLabel[]{boatLabel, issueLabel, dateLabel, statusLabel})
        l.setForeground(Color.WHITE);

    JComboBox<String> boatDropdown = new JComboBox<>(getBoatNamesFromDB());
    JTextField issueField = new JTextField();
    JTextField dateField = new JTextField(LocalDate.now().toString());
    JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Open", "Resolved"});

    styleComboBox(boatDropdown);
    styleComboBox(statusDropdown);
    styleFormField(issueField);
    styleFormField(dateField);

    form.add(boatLabel); form.add(boatDropdown);
    form.add(issueLabel); form.add(issueField);
    form.add(dateLabel); form.add(dateField);
    form.add(statusLabel); form.add(statusDropdown);

    dialog.add(form, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.setBackground(new Color(17, 36, 51));
    JButton addBtn = new JButton("Add");
    JButton cancelBtn = new JButton("Cancel");
    styleButton(addBtn);
    styleButton(cancelBtn);

    addBtn.addActionListener(e -> {
        String boat = boatDropdown.getSelectedItem().toString();
        String issue = issueField.getText();
        String date = dateField.getText();
        String status = statusDropdown.getSelectedItem().toString();

        try {
            ResultSet rs = model.Mysql.search("SELECT boat_id FROM boats WHERE name='" + boat + "'");
            rs.next();
            int boatId = rs.getInt("boat_id");

            String q = "INSERT INTO compliance (boat_id, violation, violation_date, status) VALUES (" +
                    boatId + ", '" + issue + "', '" + date + "', '" + status + "')";
            model.Mysql.iud(q);
            loadComplianceData();
            dialog.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Failed to add notice", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelBtn.addActionListener(e -> dialog.dispose());

    bottom.add(addBtn);
    bottom.add(cancelBtn);
    dialog.add(bottom, BorderLayout.SOUTH);
    dialog.setVisible(true);
}
private void showUpdateComplianceDialog(ComplianceNotice notice) {
    JDialog dialog = new JDialog(this, "Update Compliance Notice", true);
    dialog.setSize(400, 300);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(new Color(17, 36, 51));

    JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
    form.setBackground(new Color(17, 36, 51));
    form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JComboBox<String> boatDropdown = new JComboBox<>(getBoatNamesFromDB());
    boatDropdown.setSelectedItem(notice.getBoat());

    JTextField issueField = new JTextField(notice.getViolation());
    JTextField dateField = new JTextField(notice.getViolation_date());
    JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"Open", "Resolved"});
    statusDropdown.setSelectedItem(notice.getStatus());

    styleComboBox(boatDropdown);
    styleComboBox(statusDropdown);
    styleFormField(issueField);
    styleFormField(dateField);

    form.add(new JLabel("Boat:")); form.add(boatDropdown);
    form.add(new JLabel("Violation:")); form.add(issueField);
    form.add(new JLabel("Date:")); form.add(dateField);
    form.add(new JLabel("Status:")); form.add(statusDropdown);

    dialog.add(form, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.setBackground(new Color(17, 36, 51));
    JButton updateBtn = new JButton("Update");
    JButton cancelBtn = new JButton("Cancel");
    styleButton(updateBtn);
    styleButton(cancelBtn);

    updateBtn.addActionListener(e -> {
        try {
            ResultSet rs = model.Mysql.search("SELECT boat_id FROM boats WHERE name='" + boatDropdown.getSelectedItem() + "'");
            rs.next();
            int boatId = rs.getInt("boat_id");

            String q = "UPDATE compliance SET boat_id=" + boatId + ", violation='" + issueField.getText() +
                    "', violation_date='" + dateField.getText() + "', status='" + statusDropdown.getSelectedItem() +
                    "' WHERE notice_id='" + notice.getId() + "'";
            model.Mysql.iud(q);
            loadComplianceData();
            dialog.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Failed to update", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelBtn.addActionListener(e -> dialog.dispose());

    bottom.add(updateBtn);
    bottom.add(cancelBtn);
    dialog.add(bottom, BorderLayout.SOUTH);
    dialog.setVisible(true);
}
private void showDeleteComplianceDialog(String noticeId) {
    int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this notice?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            model.Mysql.iud("DELETE FROM compliance WHERE notice_id='" + noticeId + "'");
            loadComplianceData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
private String[] getBoatNamesFromDB() {
    ArrayList<String> names = new ArrayList<>();
    try {
        ResultSet rs = model.Mysql.search("SELECT name FROM boats");
        while (rs.next()) names.add(rs.getString("name"));
    } catch (Exception e) {
        e.printStackTrace();
    }
    return names.toArray(new String[0]);
}


private JPanel createComplianceCard(ComplianceNotice c) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(new Color(21, 44, 62));
    card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    // LEFT info
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setOpaque(false);

    JLabel title = new JLabel("⚠️ " + c.getViolation());
    title.setFont(new Font("Inter", Font.BOLD, 16));
    title.setForeground(Color.WHITE);

    JLabel subtitle = new JLabel("Boat: " + c.getBoat() + " | Date: " + c.getViolation_date());
    subtitle.setFont(new Font("Inter", Font.PLAIN, 13));
    subtitle.setForeground(new Color(180, 180, 180));

    left.add(title);
    left.add(subtitle);

    // RIGHT actions
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    right.setOpaque(false);

    JLabel statusBadge = new JLabel(c.getStatus());
    statusBadge.setOpaque(true);
    statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    statusBadge.setForeground(Color.WHITE);
    statusBadge.setFont(new Font("Inter", Font.BOLD, 12));
    statusBadge.setBackground(c.getStatus().equals("Resolved") ? new Color(39, 174, 96) : new Color(235, 87, 87));

    JButton edit = new JButton("Edit");
    edit.addActionListener(e->{
        showUpdateComplianceDialog(c);
    });
    
    JButton delete = new JButton("Delete");
    delete.addActionListener(e->{
        showDeleteComplianceDialog(c.getId());
    });
    styleButton(edit);
    styleButton(delete);

    right.add(statusBadge);
    right.add(Box.createHorizontalStrut(10));
    right.add(edit);
    right.add(delete);

    card.add(left, BorderLayout.WEST);
    card.add(right, BorderLayout.EAST);

    return card;
}
private JPanel createReportsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(17, 36, 51));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // === Title & Export ===
    JLabel title = new JLabel("📊 Reports & Insights");
    title.setFont(new Font("Inter", Font.BOLD, 24));
    title.setForeground(Color.WHITE);

    JButton exportBtn = new JButton("Export PDF");
    
    styleButton(exportBtn);

    JPanel titleBar = new JPanel(new BorderLayout());
    titleBar.setOpaque(false);
    titleBar.add(title, BorderLayout.WEST);
    titleBar.add(exportBtn, BorderLayout.EAST);
    panel.add(titleBar, BorderLayout.NORTH);

    // === Filter Bar ===
    JTextField fromDate = new JTextField("2025-04-01");
    JTextField toDate = new JTextField(LocalDate.now().toString());
    
    JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All", "Boats", "Fish Stock", "Compliance"});
    
    JButton generateBtn = new JButton("Generate");

    styleFormField(fromDate);
    styleFormField(toDate);
    styleComboBox(categoryFilter);
    styleButton(generateBtn);

    JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filterBar.setOpaque(false);

    JLabel fromLabel = new JLabel("From:");
    fromLabel.setForeground(Color.WHITE);
    JLabel toLabel = new JLabel("To:");
    toLabel.setForeground(Color.WHITE);

    filterBar.add(fromLabel); filterBar.add(fromDate);
    filterBar.add(toLabel); filterBar.add(toDate);
    filterBar.add(categoryFilter);
    filterBar.add(generateBtn);
    panel.add(filterBar, BorderLayout.BEFORE_FIRST_LINE);

    // === Center Section (cards + chart) ===
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setOpaque(false);

    JPanel cardRow = new JPanel(new GridLayout(1, 4, 20, 20));
    cardRow.setOpaque(false);

    // === Load Summary Data ===
    int boatCount = 0, violationCount = 0;
    double totalFish = 0;
    double avgPerBoat = 0;

    try {
        ResultSet boats = model.Mysql.search("SELECT COUNT(*) AS total FROM boats WHERE status = 'Active'");
        if (boats.next()) boatCount = boats.getInt("total");

        ResultSet fish = model.Mysql.search("SELECT SUM(quantity_kg) AS total FROM fish_stock WHERE catch_date >= CURDATE() - INTERVAL 30 DAY");
        if (fish.next()) totalFish = fish.getDouble("total");

        ResultSet violations = model.Mysql.search("SELECT COUNT(*) AS total FROM compliance WHERE status = 'Open'");
        if (violations.next()) violationCount = violations.getInt("total");

        ResultSet avg = model.Mysql.search("SELECT AVG(total) AS avgCatch FROM (SELECT SUM(quantity_kg) AS total FROM fish_stock GROUP BY boat_id) AS sub");
        if (avg.next()) avgPerBoat = avg.getDouble("avgCatch");

    } catch (Exception ex) {
        ex.printStackTrace();
    }

    // === Summary Cards ===
    cardRow.add(createReportCard("Boats Active", String.valueOf(boatCount), new Color(45, 156, 219)));
    cardRow.add(createReportCard("Catches", String.format("%.0f kg", totalFish), new Color(74, 144, 226)));
    cardRow.add(createReportCard("Violations", String.valueOf(violationCount), new Color(235, 87, 87)));
    cardRow.add(createReportCard("Avg Catch/Boat", String.format("%.1f kg", avgPerBoat), new Color(39, 174, 96)));

    centerPanel.add(cardRow);
    centerPanel.add(Box.createVerticalStrut(20));

    // === Chart Placeholder ===
    JPanel chartPanel = createSummaryChartPanel();
centerPanel.add(chartPanel);
    centerPanel.add(chartPanel);

    panel.add(centerPanel, BorderLayout.CENTER);

    // === Report Table ===
    String[] columns = {"ID", "Type", "Date", "Details"};
    ArrayList<Object[]> rowList = new ArrayList<Object[]>();


    try {
        ResultSet logs = model.Mysql.search(
            "SELECT 'Catch' AS type, catch_id AS id, catch_date AS date, CONCAT('Boat ID: ', boat_id, ', ', quantity_kg, 'kg') AS detail FROM fish_stock " +
            "UNION " +
            "SELECT 'Compliance', notice_id, violation_date, CONCAT('Boat ID: ', boat_id, ', ', violation) FROM compliance " +
            "UNION " +
            "SELECT 'Boat', boat_id, NOW(), CONCAT('Boat: ', name, ', Status: ', status) FROM boats LIMIT 15"
        );

        while (logs.next()) {
            rowList.add(new Object[]{
                logs.getString("id"),
                logs.getString("type"),
                logs.getString("date"),
                logs.getString("detail")
            });
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    Object[][] tableData = rowList.toArray(new Object[0][]);
    JTable reportTable = new JTable(tableData, columns);
    reportTable.setFont(new Font("Inter", Font.PLAIN, 13));
    reportTable.setRowHeight(26);
    reportTable.setForeground(Color.WHITE);
    reportTable.setBackground(new Color(21, 44, 62));
    reportTable.setSelectionBackground(new Color(74, 144, 226));
    reportTable.setGridColor(new Color(60, 60, 60));

    JScrollPane tableScroll = new JScrollPane(reportTable);
    tableScroll.getViewport().setBackground(new Color(21, 44, 62));
    tableScroll.setBorder(BorderFactory.createTitledBorder(null, "Detailed Logs", 0, 0, new Font("Inter", Font.BOLD, 16), Color.WHITE));

generateBtn.addActionListener(e -> exportReportAsPDF(fromDate.getText(), toDate.getText(), categoryFilter.getSelectedItem().toString(), tableData));
    panel.add(tableScroll, BorderLayout.SOUTH);
    return panel;
}

private void exportReportAsPDF(String from, String to, String category, Object[][] data) {
    System.out.println("file");
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setSelectedFile(new java.io.File("OceanOps_Report.pdf"));
    int option = fileChooser.showSaveDialog(this);

    if (option != JFileChooser.APPROVE_OPTION) return;

    File file = fileChooser.getSelectedFile();

    try {
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Fonts
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12);

        // Title
        document.add(new com.lowagie.text.Paragraph("OceanOps Report", titleFont));
        document.add(new com.lowagie.text.Paragraph("Date Range: " + from + " to " + to));
        document.add(new com.lowagie.text.Paragraph("Category: " + category));
        document.add(new com.lowagie.text.Paragraph("Generated on: " + java.time.LocalDateTime.now()));
        document.add(new com.lowagie.text.Paragraph(" ")); // Spacer

        // Table
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 4});

        String[] headers = {"ID", "Type", "Date", "Details"};
        for (String h : headers) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(h, titleFont));
            cell.setBackgroundColor(new Color(45, 156, 219));
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (Object[] row : data) {
            for (Object val : row) {
                table.addCell(new com.lowagie.text.Phrase(val.toString(), normalFont));
            }
        }

        document.add(table);
        document.close();

        JOptionPane.showMessageDialog(this, "Report exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to export report", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private JPanel createSidebar() {
    // Outer sidebar panel using BorderLayout
    JPanel sidebar = new JPanel(new BorderLayout());
    sidebar.setPreferredSize(new Dimension(100, getHeight()));
    sidebar.setBackground(new Color(11, 30, 45));
    sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

    // === TOP LOGO PANEL ===
    JPanel logoPanel = new JPanel();
    logoPanel.setBackground(new Color(11, 30, 45));
    logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

    JLabel logoLabel = new JLabel();
    logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    try {
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/oceanOpsLogo (1).png")); // adjust path
        Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaled));
    } catch (Exception e) {
        logoLabel.setText("OceanOps");
        logoLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        logoLabel.setForeground(Color.WHITE);
    }

    logoPanel.add(Box.createVerticalStrut(10));
    logoPanel.add(logoLabel);
    logoPanel.add(Box.createVerticalStrut(20));
    sidebar.add(logoPanel, BorderLayout.NORTH);

    // === CENTER BUTTONS PANEL ===
    String[] buttons = {"Dashboard", "Boats", "Fish Stock", "Compliance", "Reports", "Logout"};

    JPanel buttonGrid = new JPanel(new GridLayout(buttons.length, 1, 0, 10)); // evenly spaced rows
    buttonGrid.setBackground(new Color(11, 30, 45));

    for (String name : buttons) {
        JButton btn = new JButton(name);
        styleSidebarButton(btn);
        btn.addActionListener(e -> {
            if (name.equals("Logout")) {
                dispose();
                new Login();
            } else {
                contentContainer.add(createDashboardPanel(), "Dashboard");
                contentContainer.add(createReportsPanel(), "Reports");
                cardLayout.show(contentContainer, name);
                
            }
        });
        buttonGrid.add(btn);
    }

    sidebar.add(buttonGrid, BorderLayout.CENTER);
    return sidebar;
}
private JPanel createReportCard(String title, String value, Color color) {
    JPanel card = new JPanel(new BorderLayout());
    card.setPreferredSize(new Dimension(200, 100));
    card.setBackground(color);
    card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
    titleLabel.setForeground(Color.WHITE);

    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(new Font("Inter", Font.BOLD, 28));
    valueLabel.setForeground(Color.WHITE);

    card.add(titleLabel, BorderLayout.NORTH);
    card.add(valueLabel, BorderLayout.CENTER);

    return card;
}


private JPanel createDashboardPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(14, 27, 41));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15, 15, 15, 15);
    gbc.fill = GridBagConstraints.BOTH;

    // Header
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);

    JLabel title = new JLabel("Dashboard");
    title.setFont(new Font("Segoe UI", Font.BOLD, 28));
    title.setForeground(Color.WHITE);

    JLabel subTitle = new JLabel("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    subTitle.setFont(new Font("Inter", Font.PLAIN, 14));
    subTitle.setForeground(new Color(180, 180, 180));

    headerPanel.add(title, BorderLayout.WEST);
    headerPanel.add(subTitle, BorderLayout.EAST);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.weightx = 1;
    gbc.weighty = 0;
    panel.add(headerPanel, gbc);

    // === Fetch live stats ===
    String totalBoats = "0";
    String fishToday = "0";
    String alerts = "0";

    try {
        ResultSet rs1 = model.Mysql.search("SELECT COUNT(*) FROM boats WHERE status = 'Active'");
        System.out.println("search");
        if (rs1.next()) totalBoats = rs1.getString(1);

        ResultSet rs2 = model.Mysql.search("SELECT SUM(quantity_kg) FROM fish_stock WHERE catch_date = CURDATE()");
        if (rs2.next()) fishToday = rs2.getString(1) != null ? rs2.getString(1) : "0";

        ResultSet rs3 = model.Mysql.search("SELECT COUNT(*) FROM compliance WHERE status = 'Open'");
        if (rs3.next()) alerts = rs3.getString(1);
    } catch (Exception e) {
        e.printStackTrace();
    }

    // === KPI Cards ===
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.weighty = 0.2;
    gbc.gridx = 0;

    panel.add(createModernCard("🛥 Total Active Boats", totalBoats, "", new Color(45, 156, 219)), gbc);

    gbc.gridx = 1;
    panel.add(createModernCard("🐟 Fish Caught Today", fishToday, "kg", new Color(74, 144, 226)), gbc);

    gbc.gridy = 2;
    gbc.gridx = 0;
    panel.add(createModernCard("⚠️ Compliance Alerts", alerts, "", new Color(255, 182, 0)), gbc);

    gbc.gridx = 1;
    panel.add(createModernCard("🌊 Sea Conditions", "Calm", "22°C", new Color(135, 206, 250)), gbc);

    // === Bottom Placeholder Panels ===
    gbc.gridy = 3;
    gbc.gridx = 0;
    gbc.weighty = 0.5;

    JPanel chartPanel = createFishTypePieChartPanel();
    chartPanel.setPreferredSize(new Dimension(400, 250));
    panel.add(chartPanel, gbc);

    gbc.gridx = 1;

    JPanel activityPanel = new JPanel();
    activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
    activityPanel.setBackground(new Color(21, 44, 62));
    activityPanel.setBorder(BorderFactory.createTitledBorder(null, "Recent Activity", 0, 0, new Font("Inter", Font.BOLD, 16), Color.WHITE));

    String[] logs = {
        "✅ Ocean Rider docked at 10:45",
        "⚠️ Sea Voyager violated restricted zone",
        "🐟 Tuna stock updated from Wave Breaker",
        "✅ Compliance notice resolved"
    };

    for (String log : logs) {
        JLabel logLabel = new JLabel(log);
        logLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        logLabel.setForeground(Color.WHITE);
        logLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        activityPanel.add(logLabel);
    }

    panel.add(activityPanel, gbc);

    return panel;
}


private JPanel createModernCard(String title, String value, String suffix, Color accent) {
    JPanel card = new JPanel(new BorderLayout());
    card.setPreferredSize(new Dimension(200, 100));
    card.setBackground(new Color(23, 43, 64));
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15),
            new RoundedBorder(20, new Color(23, 43, 64))
    ));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    titleLabel.setForeground(new Color(200, 200, 200));

    JLabel valueLabel = new JLabel(value + (suffix.isEmpty() ? "" : " " + suffix));
    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
    valueLabel.setForeground(accent != null ? accent : Color.WHITE);

    card.add(titleLabel, BorderLayout.NORTH);
    card.add(valueLabel, BorderLayout.CENTER);

    return card;
}




private JPanel createPlaceholderPanel(String text) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(17, 36, 51));
    JLabel label = new JLabel(text, SwingConstants.CENTER);
    label.setFont(new Font("Inter", Font.BOLD, 24));
    label.setForeground(Color.WHITE);
    panel.add(label, BorderLayout.CENTER);
    return panel;
}


    private void styleSidebarButton(JButton button) {
    button.setBackground(new Color(21, 44, 62));
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Inter", Font.PLAIN, 14));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}

    

    private JPanel createInfoCard(String title, String value, Color color) {
    JPanel card = new JPanel();
    card.setLayout(new BorderLayout());
    card.setPreferredSize(new Dimension(250, 120));
    card.setBackground(color);
    card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    card.setOpaque(true);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1)
    ));

    // Rounded corners
    card.setBorder(new RoundedBorder(20, color));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
    titleLabel.setForeground(Color.WHITE);

    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(new Font("Inter", Font.PLAIN, 22));
    valueLabel.setForeground(Color.WHITE);

    card.add(titleLabel, BorderLayout.NORTH);
    card.add(valueLabel, BorderLayout.CENTER);

    return card;
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Dashboard::new);
    }
}
