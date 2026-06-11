package com.estatevault.model;

public class Plot extends Property {
    public Plot() { super(); }

    public Plot(int id, String title, String location, double area,
                double price, int sellerId, String status) {
        super(id, title, location, area, price, sellerId, status);
    }

    @Override public String getPropertyType() { return "PLOT"; }
    @Override public double calculateCommission() { return getPrice() * 0.03; }
}
