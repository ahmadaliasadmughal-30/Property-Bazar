package com.estatevault.model;

public class Apartment extends Property {
    public Apartment() { super(); }

    public Apartment(int id, String title, String location, double area,
                     double price, int sellerId, String status) {
        super(id, title, location, area, price, sellerId, status);
    }

    @Override public String getPropertyType() { return "APARTMENT"; }
    @Override public double calculateCommission() { return getPrice() * 0.015; }
}
