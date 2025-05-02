package reports.management;

public class TestClass {
    public static void main(String[] args) {
        StockSummary.displayAllStock();

        ComplianceSummary.displayAllCompliance();

        new StockSummary("2025-04-14", "2025-04-18");


        new ComplianceSummary("2025-04-14", "2025-04-18");
    }
}
