package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Investment {
    private int id;
    private int userId;
    private String name;
    private String type;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private LocalDate startDate;
    private String note;

    public Investment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) { this.investedAmount = investedAmount; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getProfit() {
        return currentValue.subtract(investedAmount);
    }
}