// =================================================================================
// File 2: src/DatabaseConnection.java
// =================================================================================
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConnection {
    // Konfigurasi koneksi ke database MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/gorent";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Method static untuk mendapatkan koneksi database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}