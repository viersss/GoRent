// File: src/RentalService.java
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RentalService {
    private static RentalService instance;

    // Constructor private untuk Singleton Pattern
    private RentalService() {}

    // Method untuk mendapatkan satu-satunya instance dari kelas ini
    public static synchronized RentalService getInstance() {
        if (instance == null) {
            instance = new RentalService();
        }
        return instance;
    }

    private int getNextId(String tableName) {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, 2) AS UNSIGNED)) FROM " + tableName;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // --- LOGIKA MANAJEMEN PENGGUNA ---
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(rs.getString("username"), rs.getString("password"), Role.valueOf(rs.getString("role")), rs.getString("avatar_url"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        recordLoginAttempt(username, user != null);
        return user;
    }

    public boolean register(String username, String password, Role role) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        String insertPelangganSql = "INSERT INTO pelanggan (id, nama, no_telepon) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql)) {
                    userStmt.setString(1, username);
                    userStmt.setString(2, password);
                    userStmt.setString(3, role.name());
                    userStmt.executeUpdate();
                }
                if (role == Role.USER) {
                    try (PreparedStatement pelangganStmt = conn.prepareStatement(insertPelangganSql)) {
                        String pelangganId = "P" + String.format("%03d", getNextId("pelanggan"));
                        pelangganStmt.setString(1, pelangganId);
                        pelangganStmt.setString(2, username);
                        pelangganStmt.setString(3, "N/A");
                        pelangganStmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- FITUR PROFIL & KEAMANAN ---
    public boolean updateProfil(String username, String noTelepon) {
        String sql = "UPDATE pelanggan SET no_telepon = ? WHERE nama = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, noTelepon);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateAvatar(String username, String avatarUrl) {
        String sql = "UPDATE users SET avatar_url = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, avatarUrl);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean ubahPassword(String username, String oldPassword, String newPassword) {
        String sqlLogin = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlLogin)) {
            pstmt.setString(1, username);
            pstmt.setString(2, oldPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(rs.getString("username"), rs.getString("password"), Role.valueOf(rs.getString("role")), rs.getString("avatar_url"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (user == null) return false;

        String sqlUpdate = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteAccount(String username, String password) {
        // Panggil login TANPA mencatat riwayat agar tidak duplikat
        String sqlLogin = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlLogin)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(rs.getString("username"), rs.getString("password"), Role.valueOf(rs.getString("role")), rs.getString("avatar_url"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (user == null) return false;

        String deleteTransaksi = "DELETE FROM transaksi WHERE id_pelanggan = (SELECT id FROM pelanggan WHERE nama = ?)";
        String deletePelanggan = "DELETE FROM pelanggan WHERE nama = ?";
        String deleteHistory = "DELETE FROM login_history WHERE username = ?";
        String deleteUser = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteTransaksi);
                 PreparedStatement stmt2 = conn.prepareStatement(deletePelanggan);
                 PreparedStatement stmt3 = conn.prepareStatement(deleteHistory);
                 PreparedStatement stmt4 = conn.prepareStatement(deleteUser)) {

                stmt1.setString(1, username); stmt1.executeUpdate();
                stmt2.setString(1, username); stmt2.executeUpdate();
                stmt3.setString(1, username); stmt3.executeUpdate();
                stmt4.setString(1, username); stmt4.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<LoginHistory> getLoginHistory(String username) {
        List<LoginHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM login_history WHERE username = ? ORDER BY login_time DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    history.add(new LoginHistory(
                            rs.getTimestamp("login_time").toLocalDateTime(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    private void recordLoginAttempt(String username, boolean success) {
        String sql = "INSERT INTO login_history (username, status, login_time) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, success ? "Berhasil" : "Gagal");
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

            pstmt.executeUpdate();

        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "DEBUG: Gagal merekam aktivitas login ke database.\n\n" +
                            "Pesan Error SQL:\n" + e.getMessage(),
                    "Kesalahan Database Kritis",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }


    // --- METHOD GET & FIND DATA ---
    public List<Kendaraan> getAllKendaraan() {
        List<Kendaraan> list = new ArrayList<>();
        String sql = "SELECT * FROM kendaraan ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Kendaraan k = new Kendaraan(rs.getString("id"), rs.getString("merk"), rs.getString("tipe"), rs.getInt("tahun"), rs.getString("transmisi"), rs.getString("bahan_bakar"), rs.getInt("harga_sewa"), rs.getString("image_url"));
                k.setStatus(rs.getString("status"));
                list.add(k);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Pelanggan> getAllPelanggan() {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Pelanggan(rs.getString("id"), rs.getString("nama"), rs.getString("no_telepon")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, k.*, p.nama as nama_pelanggan FROM transaksi t " +
                "JOIN kendaraan k ON t.id_kendaraan = k.id " +
                "JOIN pelanggan p ON t.id_pelanggan = p.id ORDER BY t.tgl_pinjam DESC, t.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Kendaraan k = new Kendaraan(rs.getString("id_kendaraan"), rs.getString("merk"), rs.getString("tipe"), rs.getInt("tahun"), rs.getString("transmisi"), rs.getString("bahan_bakar"), rs.getInt("harga_sewa"), rs.getString("image_url"));
                Pelanggan p = new Pelanggan(rs.getString("id_pelanggan"), rs.getString("nama_pelanggan"), null);
                Transaksi tx = new Transaksi(rs.getString("id"), k, p, rs.getDate("tgl_pinjam").toLocalDate());
                tx.setStatus(rs.getString("status"));
                tx.setDenda(rs.getDouble("denda"));
                list.add(tx);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean tambahKendaraan(String merk, String tipe, int tahun, String transmisi, String bahanBakar, int hargaSewa, String imageUrl) {
        String sql = "INSERT INTO kendaraan (id, merk, tipe, tahun, transmisi, bahan_bakar, harga_sewa, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "K" + String.format("%03d", getNextId("kendaraan")));
            pstmt.setString(2, merk); pstmt.setString(3, tipe); pstmt.setInt(4, tahun);
            pstmt.setString(5, transmisi); pstmt.setString(6, bahanBakar);
            pstmt.setInt(7, hargaSewa); pstmt.setString(8, imageUrl);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean hapusKendaraan(String id) {
        if (isKendaraanDisewa(id)) return false;
        String sql = "DELETE FROM kendaraan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean tambahPelanggan(String nama, String noTelepon) {
        String sql = "INSERT INTO pelanggan (id, nama, no_telepon) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "P" + String.format("%03d", getNextId("pelanggan")));
            pstmt.setString(2, nama);
            pstmt.setString(3, noTelepon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean hapusPelanggan(String id) {
        if (pelangganPunyaSewaAktif(id)) return false;
        Pelanggan p = findPelangganById(id);
        if (p != null && findUserByUsername(p.getNama()) != null) return false;
        String sql = "DELETE FROM pelanggan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Transaksi tambahTransaksi(String idKendaraan, String idPelanggan, LocalDate tglPinjam) {
        String sql = "INSERT INTO transaksi (id, id_kendaraan, id_pelanggan, tgl_pinjam) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE kendaraan SET status = 'Disewa' WHERE id = ?";
        String transaksiId = "T" + String.format("%03d", getNextId("transaksi"));
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(sql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                insertStmt.setString(1, transaksiId);
                insertStmt.setString(2, idKendaraan);
                insertStmt.setString(3, idPelanggan);
                insertStmt.setDate(4, Date.valueOf(tglPinjam));
                insertStmt.executeUpdate();
                updateStmt.setString(1, idKendaraan);
                updateStmt.executeUpdate();
                conn.commit();
                return findTransaksiById(transaksiId);
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public boolean kembalikanKendaraan(String idTransaksi, double dendaTambahan) {
        String sql = "UPDATE transaksi SET status = 'Selesai', denda = denda + ? WHERE id = ?";
        String updateSql = "UPDATE kendaraan SET status = 'Tersedia' WHERE id = ?";
        Transaksi transaksi = findTransaksiById(idTransaksi);
        if (transaksi == null) return false;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement transStmt = conn.prepareStatement(sql);
                 PreparedStatement kendStmt = conn.prepareStatement(updateSql)) {
                transStmt.setDouble(1, dendaTambahan);
                transStmt.setString(2, idTransaksi);
                transStmt.executeUpdate();
                kendStmt.setString(1, transaksi.getIdKendaraan());
                kendStmt.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        String[] queries = {"SELECT COUNT(*) FROM kendaraan", "SELECT COUNT(*) FROM kendaraan WHERE status = 'Tersedia'", "SELECT COUNT(*) FROM kendaraan WHERE status = 'Disewa'", "SELECT COUNT(*) FROM pelanggan", "SELECT COUNT(*) FROM transaksi WHERE status = 'Disewa'", "SELECT COUNT(*) FROM transaksi WHERE status = 'Selesai'"};
        String[] keys = {"totalKendaraan", "kendaraanTersedia", "kendaraanDisewa", "totalPelanggan", "transaksiAktif", "transaksiSelesai"};
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            for (int i = 0; i < queries.length; i++) {
                try (ResultSet rs = stmt.executeQuery(queries[i])) {
                    if (rs.next()) stats.put(keys[i], rs.getLong(1));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    public List<Transaksi> getTransaksiByUser(User user) {
        Pelanggan pelanggan = findPelangganByUsername(user.getUsername());
        if (pelanggan == null) return new ArrayList<>();
        return getAllTransaksi().stream().filter(t -> t.getIdPelanggan().equals(pelanggan.getId())).collect(Collectors.toList());
    }

    public List<Transaksi> getRecentTransactions(int limit) {
        return getAllTransaksi().stream().limit(limit).collect(Collectors.toList());
    }

    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return new User(rs.getString("username"), rs.getString("password"), Role.valueOf(rs.getString("role")), rs.getString("avatar_url"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Pelanggan findPelangganById(String id) {
        String sql = "SELECT * FROM pelanggan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return new Pelanggan(rs.getString("id"), rs.getString("nama"), rs.getString("no_telepon"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Pelanggan findPelangganByUsername(String username) {
        String sql = "SELECT * FROM pelanggan WHERE nama = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Pelanggan(rs.getString("id"), rs.getString("nama"), rs.getString("no_telepon"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Transaksi findTransaksiById(String id) {
        String sql = "SELECT t.*, k.*, p.nama as nama_pelanggan FROM transaksi t " +
                "JOIN kendaraan k ON t.id_kendaraan = k.id " +
                "JOIN pelanggan p ON t.id_pelanggan = p.id WHERE t.id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Kendaraan k = new Kendaraan(rs.getString("id_kendaraan"), rs.getString("merk"), rs.getString("tipe"), rs.getInt("tahun"), rs.getString("transmisi"), rs.getString("bahan_bakar"), rs.getInt("harga_sewa"), rs.getString("image_url"));
                    Pelanggan p = new Pelanggan(rs.getString("id_pelanggan"), rs.getString("nama_pelanggan"), null);
                    Transaksi tx = new Transaksi(rs.getString("id"), k, p, rs.getDate("tgl_pinjam").toLocalDate());
                    tx.setStatus(rs.getString("status"));
                    tx.setDenda(rs.getDouble("denda"));
                    return tx;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private boolean isKendaraanDisewa(String id) {
        String sql = "SELECT status FROM kendaraan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return "Disewa".equals(rs.getString("status"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean pelangganPunyaSewaAktif(String id) {
        String sql = "SELECT COUNT(*) FROM transaksi WHERE id_pelanggan = ? AND status = 'Disewa'";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}