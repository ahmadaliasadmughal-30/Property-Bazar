package com.estatevault.model;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private int propertyId;
    private int buyerId;
    private int sellerId;
    private String type;
    private double amount;
    private double commission;
    private LocalDateTime date;

    public Transaction() {}

    public Transaction(int id, int propertyId, int buyerId, int sellerId,
                       String type, double amount, double commission, LocalDateTime date) {
        this.id = id;
        this.propertyId = propertyId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.type = type;
        this.amount = amount;
        this.commission = commission;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }
    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getCommission() { return commission; }
    public void setCommission(double commission) { this.commission = commission; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
