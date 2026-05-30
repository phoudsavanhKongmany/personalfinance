package dao;

import model.Investment;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDAO {

    public List<Investment> getByUser(int userId) {
        List<Investment> list = new ArrayList<>();
        String sql = "SELECT * FROM investments WHERE user_id = ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Investment inv = new Investment();
                inv.setId(rs.getInt("id"));
                inv.setUserId(userId);
                inv.setName(rs.getString("name"));
                inv.setType(rs.getString("type"));
                inv.setInvestedAmount(rs.getBigDecimal("invested_amount"));
                inv.setCurrentValue(rs.getBigDecimal("current_value"));
                inv.setNote(rs.getString("note"));
                Date sd = rs.getDate("start_date");
                if (sd != null) inv.setStartDate(sd.toLocalDate());
                list.add(inv);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Investment inv) {
        String sql = "INSERT INTO investments (user_id, name, type, invested_amount, current_value, start_date, note) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inv.getUserId());
            ps.setString(2, inv.getName());
            ps.setString(3, inv.getType());
            ps.setBigDecimal(4, inv.getInvestedAmount());
            ps.setBigDecimal(5, inv.getCurrentValue());
            ps.setDate(6, inv.getStartDate() != null ? Date.valueOf(inv.getStartDate()) : null);
            ps.setString(7, inv.getNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM investments WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
 // Trả về tổng đầu tư và giá trị hiện tại theo loại
    public Map<String, Double> getInvestStats(int userId) {
        Map<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = """
            SELECT type,
              SUM(invested_amount) AS invested,
              SUM(current_value)   AS current
            FROM investments WHERE user_id = ?
            GROUP BY type
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                map.put(type + "_invested", rs.getDouble("invested"));
                map.put(type + "_current",  rs.getDouble("current"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}