package reports.management;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.sql.Timestamp;

public class ComplianceSummary {
    private int reportId;
    private String generatedBy;
    private String dateAndTime;

    // Directly Generate Stock Report PDF accroding to the given date
    public ComplianceSummary(String from_date, String to_date) {

        String sql = "SELECT report_id FROM reports ORDER BY report_id DESC LIMIT 1";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                this.reportId = rs.getInt("report_id");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.dateAndTime = LocalDateTime.now().toString();
        this.generatedBy = "Adithya"; // need a static method like currentUser(); which return the current active admin of the system

        GeneratePDF.generateComplianceSummary(from_date, to_date, reportId, generatedBy, dateAndTime);

        saveReport();
    }

    // Display all fish_stocks details, use this for display All Compliances
    public static void displayAllCompliance() {
        String sql = "SELECT * FROM compliance";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                // .....................  apply display logic here  .........................
                // This is just only showing result on the terminal map this into Reports UI

                System.out.println("Notice ID: " + rs.getInt("boat_id")
                        + "Boat ID: " + rs.getInt("boat_id")
                        + "Violation: "+ rs.getString("violation")
                        + "Violation Date: " + rs.getString("status")
                        + "Created Date: " + rs.getString("created_at"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Private function no need to expourse to the outside
    private void saveReport() {
        String sql = "INSERT INTO reports (generated_on, category, export_type, content) VALUES (?, ?, ?, ?)";

        try {
            var conn = new DBConnection(sql);
            var statement = conn.getStatement();

            Timestamp ts = new Timestamp(new Date().getTime());

            statement.setTimestamp(1, ts);
            statement.setString(2, "Compliance");
            statement.setString(3, "PDF");
            statement.setString(4, "Violation");


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
