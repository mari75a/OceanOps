package reports.management;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private String sql;
    private PreparedStatement statement;

    public DBConnection(String sql) {
        this.sql = sql;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            java.sql.Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/oceanops",
                    "root", "adithya123"
//                    "200301403251A"
            );

            this.statement = connection.prepareStatement(sql);

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement getStatement() {
        return statement;
    }
}
