package com.estatevault.model;

public class Buyer extends Person {
    public Buyer() { super(); }

    public Buyer(int id, String name, String phone, String email) {
        super(id, name, phone, email);
    }

    @Override
    public String getType() { return "BUYER"; }
}
