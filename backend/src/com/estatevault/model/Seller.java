package com.estatevault.model;

public class Seller extends Person {
    public Seller() { super(); }

    public Seller(int id, String name, String phone, String email) {
        super(id, name, phone, email);
    }

    @Override
    public String getType() { return "SELLER"; }
}
