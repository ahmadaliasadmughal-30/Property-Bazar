package com.estatevault.model;

public class House extends Property {
    public House() { super(); }

    public House(int id, String title, String location, double area,
                 double price, int sellerId, String status) {
        super(id, title, location, area, price, sellerId, status);
    }

    @Override public String getPropertyType() { return "HOUSE"; }
    @Override public double calculateCommission() { return getPrice() * 0.02; }
}
