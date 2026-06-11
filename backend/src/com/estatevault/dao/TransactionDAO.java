package com.estatevault.dao;

import com.estatevault.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Connection conn;

    public TransactionDAO(Connection conn) { this.conn = conn; }

    public int nextId() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 AS n FROM transactions")) {
            rs.next();
            return rs.getInt("n");
        }
    }

    public void insert(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (id,property_id,buyer_id,seller_id,type,amount,commission,date) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getId());
            ps.setInt(2, t.getPropertyId());
            ps.setInt(3, t.getBuyerId());
            ps.setInt(4, t.getSellerId());
            ps.setString(5, t.getType());
            ps.setDouble(6, t.getAmount());
            ps.setDouble(7, t.getCommission());
            ps.setString(8, t.getDate().format(FMT));
            ps.executeUpdate();
        }
    }

    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM transactions ORDER BY id DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public double totalCommission() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(commission),0) AS s FROM transactions")) {
            rs.next();
            return rs.getDouble("s");
        }
    }

    public Transaction findLatestRentForProperty(int propertyId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE property_id=? AND type IN ('RENT','RENT_RENEWAL') " +
                     "ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int count() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM transactions")) {
            rs.next();
            return rs.getInt("c");
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("date");
        LocalDateTime date = dateStr != null ? LocalDateTime.parse(dateStr, FMT) : LocalDateTime.now();
        return new Transaction(
            rs.getInt("id"),
            rs.getInt("property_id"),
            rs.getInt("buyer_id"),
            rs.getInt("seller_id"),
            rs.getString("type"),
            rs.getDouble("amount"),
            rs.getDouble("commission"),
            date
        );
    }
}
