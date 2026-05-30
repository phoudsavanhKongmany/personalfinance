package dao;

import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;          // ✅ Chỉ import java.sql.Date, không dùng java.sql.*
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDate;
public class TransactionDAO {

    public List<Transaction> getByUserAndMonth(int userId, int month, int year) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name AS category_name FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ? AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ?
            ORDER BY t.transaction_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setCategoryName(rs.getString("category_name"));
                t.setCategoryId(rs.getInt("category_id"));
                t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean add(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, type, description, transaction_date) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getType());
            ps.setString(5, t.getDescription());
            ps.setDate(6, Date.valueOf(t.getTransactionDate())); // ✅ Rõ ràng là java.sql.Date
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getSummary(int userId, int month, int year) {
        String sql = """
            SELECT
              SUM(CASE WHEN type='income' THEN amount ELSE 0 END) AS total_income,
              SUM(CASE WHEN type='expense' THEN amount ELSE 0 END) AS total_expense
            FROM transactions
            WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?
            """;
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                map.put("income", rs.getBigDecimal("total_income"));
                map.put("expense", rs.getBigDecimal("total_expense"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    public Transaction getById(int id, int userId) {
        String sql = "SELECT * FROM transactions WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setCategoryId(rs.getInt("category_id"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Transaction t) {
        String sql = "UPDATE transactions SET category_id=?, amount=?, type=?, description=?, transaction_date=? WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getCategoryId());
            ps.setBigDecimal(2, t.getAmount());
            ps.setString(3, t.getType());
            ps.setString(4, t.getDescription());
            ps.setDate(5, Date.valueOf(t.getTransactionDate()));
            ps.setInt(6, t.getId());
            ps.setInt(7, t.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Map<String, Double> getExpenseByCategory(int userId, int month, int year) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
            SELECT c.name, SUM(t.amount) AS total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ? AND t.type = 'expense'
              AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ?
            GROUP BY c.name ORDER BY total DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Double> getIncomeByCategory(int userId, int month, int year) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
            SELECT c.name, SUM(t.amount) AS total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ? AND t.type = 'income'
              AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ?
            GROUP BY c.name ORDER BY total DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    public List<Transaction> getRecent(int userId, int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name AS category_name FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            ORDER BY t.transaction_date DESC, t.id DESC
            LIMIT ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setCategoryName(rs.getString("category_name"));
                t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Map<String, Object> getLast6Months(int userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> labels  = new ArrayList<>();
        List<Double> incomes  = new ArrayList<>();
        List<Double> expenses = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusMonths(i);
            int m = d.getMonthValue(), y = d.getYear();
            labels.add("T" + m + "/" + y);
            Map<String, Object> s = getSummary(userId, m, y);
            incomes.add(s.get("income")  != null ? ((java.math.BigDecimal) s.get("income")).doubleValue()  : 0.0);
            expenses.add(s.get("expense") != null ? ((java.math.BigDecimal) s.get("expense")).doubleValue() : 0.0);
        }

        result.put("labels",   labels);
        result.put("incomes",  incomes);
        result.put("expenses", expenses);
        return result;
    }
}