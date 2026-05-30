package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Debt {
    private int id;
    private int userId;
    private String name;
    private String type; // borrow | lend
    private BigDecimal amount;
    private BigDecimal remaining;
    private String person;
    private LocalDate dueDate;
    private String note;
    private String status;

    public Debt() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }
    public String getPerson() { return person; }
    public void setPerson(String person) { this.person = person; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}