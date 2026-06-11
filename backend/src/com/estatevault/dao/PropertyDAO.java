package com.estatevault.dao;

import com.estatevault.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {
    private final Connection conn;

    public PropertyDAO(Connection conn) { this.conn = conn; }

    public int nextId() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 AS n FROM properties")) {
            rs.next();
            return rs.getInt("n");
        }
    }

    public void insert(Property p) throws SQLException {
        String sql = "INSERT INTO properties (id,title,location,area,price,seller_id,status,type) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getTitle());
            ps.setString(3, p.getLocation());
            ps.setDouble(4, p.getArea());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6, p.getSellerId());
            ps.setString(7, p.getStatus());
            ps.setString(8, p.getPropertyType());
            ps.executeUpdate();
        }
    }

    public Property findById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM properties WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE properties SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Property> search(String location, Double minPrice, Double maxPrice,
                                 Double minArea, Double maxArea, String type) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM properties WHERE status='AVAILABLE'");
        List<Object> params = new ArrayList<>();

        if (location != null && !location.isBlank()) {
            sql.append(" AND location LIKE ?");
            params.add("%" + location + "%");
        }
        if (minPrice != null) { sql.append(" AND price >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND price <= ?"); params.add(maxPrice); }
        if (minArea  != null) { sql.append(" AND area >= ?");  params.add(minArea);  }
        if (maxArea  != null) { sql.append(" AND area <= ?");  params.add(maxArea);  }
        if (type != null && !type.isBlank()) {
            sql.append(" AND type = ?");
            params.add(type.toUpperCase());
        }
        sql.append(" ORDER BY id DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<Property> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

    public List<Property> findByStatus(String status) throws SQLException {
        List<Property> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM properties WHERE status=? ORDER BY id DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countByStatus(String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS c FROM properties WHERE status=?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("c");
            }
        }
    }

    private Property map(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String location = rs.getString("location");
        double area = rs.getDouble("area");
        double price = rs.getDouble("price");
        int sellerId = rs.getInt("seller_id");
        String status = rs.getString("status");

        return switch (type) {
            case "HOUSE"     -> new House(id, title, location, area, price, sellerId, status);
            case "APARTMENT" -> new Apartment(id, title, location, area, price, sellerId, status);
            case "PLOT"      -> new Plot(id, title, location, area, price, sellerId, status);
            case "SHOP"      -> new Shop(id, title, location, area, price, sellerId, status);
            default -> throw new SQLException("Unknown property type: " + type);
        };
    }
}
