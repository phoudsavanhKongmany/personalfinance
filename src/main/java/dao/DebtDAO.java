package dao;

import model.Debt;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DebtDAO {

    public List<Debt> getByUser(int userId) {
        List<Debt> list = new ArrayList<>();
        String sql = "SELECT * FROM debts WHERE user_id = ? ORDER BY due_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Debt d = new Debt();
                d.setId(rs.getInt("id"));
                d.setUserId(userId);
                d.setName(rs.getString("name"));
                d.setType(rs.getString("type"));
                d.setAmount(rs.getBigDecimal("amount"));
                d.setRemaining(rs.getBigDecimal("remaining"));
                d.setPerson(rs.getString("person"));
                d.setNote(rs.getString("note"));
                d.setStatus(rs.getString("status"));
                Date due = rs.getDate("due_date");
                if (due != null) d.setDueDate(due.toLocalDate());
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Debt d) {
        String sql = "INSERT INTO debts (user_id, name, type, amount, remaining, person, due_date, note) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getUserId());
            ps.setString(2, d.getName());
            ps.setString(3, d.getType());
            ps.setBigDecimal(4, d.getAmount());
            ps.setBigDecimal(5, d.getAmount());
            ps.setString(6, d.getPerson());
            ps.setDate(7, d.getDueDate() != null ? Date.valueOf(d.getDueDate()) : null);
            ps.setString(8, d.getNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean markDone(int id, int userId) {
        String sql = "UPDATE debts SET status='done', remaining=0 WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM debts WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
 // Trả về tổng đang nợ và đã trả
    public Map<String, Double> getDebtStats(int userId) {
        Map<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = """
            SELECT
              SUM(CASE WHEN status='active' AND type='borrow' THEN remaining ELSE 0 END) AS borrowing,
              SUM(CASE WHEN status='active' AND type='lend'   THEN remaining ELSE 0 END) AS lending,
              SUM(CASE WHEN status='done'                     THEN amount    ELSE 0 END) AS paid
            FROM debts WHERE user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                map.put("borrowing", rs.getDouble("borrowing"));
                map.put("lending",   rs.getDouble("lending"));
                map.put("paid",      rs.getDouble("paid"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}