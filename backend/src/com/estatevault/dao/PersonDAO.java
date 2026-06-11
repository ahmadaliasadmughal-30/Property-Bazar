package com.estatevault.dao;

import com.estatevault.model.Buyer;
import com.estatevault.model.Person;
import com.estatevault.model.Seller;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonDAO {
    private final Connection conn;

    public PersonDAO(Connection conn) { this.conn = conn; }

    public int nextId() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 AS n FROM persons")) {
            rs.next();
            return rs.getInt("n");
        }
    }

    public void insert(Person p) throws SQLException {
        String sql = "INSERT INTO persons (id,name,phone,email,type) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getPhone());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getType());
            ps.executeUpdate();
        }
    }

    public List<Person> findAll() throws SQLException {
        List<Person> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM persons ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Person findById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM persons WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int count() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM persons")) {
            rs.next();
            return rs.getInt("c");
        }
    }

    private Person map(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        return "SELLER".equals(type)
                ? new Seller(id, name, phone, email)
                : new Buyer(id, name, phone, email);
    }
}
