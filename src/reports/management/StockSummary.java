package reports.management;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class StockSummary {
    private int reportId;
    private String generatedBy;
    private String dateAndTime;

    // Directly Generate Stock Report PDF
    public StockSummary(String from_date, String to_date) {

        String sql = "SELECT report_id FROM reports ORDER BY report_id DESC LIMIT 1";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                this.reportId = rs.getInt("report_id") + 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.dateAndTime = LocalDateTime.now().toString();
        this.generatedBy = "Adithya"; // need a static method like currentUser(); which return the current active admin of the system

        GeneratePDF.generateStockSummary(from_date, to_date, reportId, generatedBy, dateAndTime);

        saveReport();
    }

    // Display all fish_stocks details
    public static void displayAllStock() {

        String sql = "SELECT * FROM fish_stock";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {

                // This is just only showing result on the terminal map this into Reports UI

                // .....................Display logic here.........................
                //
                //
                // ................................................................
                System.out.println("Boat ID: " + rs.getInt("boat_id")
                        + " | Fish Type: " + rs.getString("fish_type")
                        + " | Quantity: " + rs.getString("quantity")
                        + " | Catch Date: " + rs.getString("catch_date") );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Private function no need to expose to the outside
    private void saveReport() {
        String sql = "INSERT INTO reports (generated_on, category, export_type, content) VALUES (?, ?, ?, ?)";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            Timestamp ts = new Timestamp(new Date().getTime());

            statement.setTimestamp(1, ts);
            statement.setString(2, "Summary");
            statement.setString(3, "PDF");
            statement.setString(4, "Fish Stock");


            int rows = statement.executeUpdate();
            if (rows < 0) {
                throw new RuntimeException("No rows were affected.");
            } else {
                System.out.println("Report Saved.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
