package dao;

import model.Account;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.*;
import java.util.*;

public class AccountDAO {

    public List<Account> getByUser(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Account a = new Account();
                a.setId(rs.getInt("id"));
                a.setUserId(userId);
                a.setName(rs.getString("name"));
                a.setType(rs.getString("type"));
                a.setBalance(rs.getBigDecimal("balance"));
                a.setDescription(rs.getString("description"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Account a) {
        String sql = "INSERT INTO accounts (user_id, name, type, balance, description) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getUserId());
            ps.setString(2, a.getName());
            ps.setString(3, a.getType());
            ps.setBigDecimal(4, a.getBalance());
            ps.setString(5, a.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM accounts WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
 // Trả về: tên tài khoản + tổng thu + tổng chi
    public List<Map<String, Object>> getAccountStats(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT a.name,
              SUM(CASE WHEN t.type='income'  THEN t.amount ELSE 0 END) AS income,
              SUM(CASE WHEN t.type='expense' THEN t.amount ELSE 0 END) AS expense
            FROM accounts a
            LEFT JOIN transactions t ON t.user_id = a.user_id
            WHERE a.user_id = ?
            GROUP BY a.id, a.name
            ORDER BY a.name
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("name",    rs.getString("name"));
                row.put("income",  rs.getDouble("income"));
                row.put("expense", rs.getDouble("expense"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}