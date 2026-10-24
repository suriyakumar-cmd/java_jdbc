import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Database Connection Manager for KEC R2024 Evaluation System
 * Manages JDBC connections to MySQL database (kec_eval).
 */
public class DBConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "kec_eval";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            String host = props.getProperty("db.host", DEFAULT_HOST);
            String port = props.getProperty("db.port", DEFAULT_PORT);
            String db = props.getProperty("db.name", DEFAULT_DB);
            user = props.getProperty("db.user", DEFAULT_USER);
            password = props.getProperty("db.password", DEFAULT_PASS);
            url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
        } catch (Exception e) {
            // Fallback to default XAMPP MySQL configuration
            user = DEFAULT_USER;
            password = DEFAULT_PASS;
            url = "jdbc:mysql://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found in classpath! Make sure mysql-connector-j jar is included.");
        }
    }

    /**
     * Obtains a new database connection.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether the database connection is alive.
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[Connection Error] " + e.getMessage());
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }
}

