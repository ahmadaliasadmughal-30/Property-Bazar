package com.estatevault.model;

public abstract class Property {
    private int id;
    private String title;
    private String location;
    private double area;
    private double price;
    private int sellerId;
    private String status;

    protected Property() {}

    protected Property(int id, String title, String location, double area,
                       double price, int sellerId, String status) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.area = area;
        this.price = price;
        this.sellerId = sellerId;
        this.status = status;
    }

    public abstract String getPropertyType();
    public abstract double calculateCommission();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
