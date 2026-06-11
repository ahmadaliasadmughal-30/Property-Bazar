package com.estatevault.model;

public class Shop extends Property {
    public Shop() { super(); }

    public Shop(int id, String title, String location, double area,
                double price, int sellerId, String status) {
        super(id, title, location, area, price, sellerId, status);
    }

    @Override public String getPropertyType() { return "SHOP"; }
    @Override public double calculateCommission() { return getPrice() * 0.025; }
}
