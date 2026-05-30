package model;

import java.math.BigDecimal;

public class Account {
    private int id;
    private int userId;
    private String name;
    private String type;
    private BigDecimal balance;
    private String description;

    public Account() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeLabel() {
        return switch (type) {
            case "cash"    -> "💵 Tiền mặt";
            case "bank"    -> "🏦 Ngân hàng";
            case "ewallet" -> "📱 Ví điện tử";
            default        -> "💼 Khác";
        };
    }
}