package com.estatevault.service;

import com.estatevault.dao.PersonDAO;
import com.estatevault.dao.PropertyDAO;
import com.estatevault.dao.TransactionDAO;
import com.estatevault.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class EstateService {
    private final PersonDAO personDAO;
    private final PropertyDAO propertyDAO;
    private final TransactionDAO transactionDAO;

    public EstateService(Connection conn) {
        this.personDAO = new PersonDAO(conn);
        this.propertyDAO = new PropertyDAO(conn);
        this.transactionDAO = new TransactionDAO(conn);
    }

    public Seller registerSeller(String name, String phone, String email) throws SQLException {
        Seller seller = new Seller(personDAO.nextId(), name, phone, email);
        personDAO.insert(seller);
        return seller;
    }

    public Buyer registerBuyer(String name, String phone, String email) throws SQLException {
        Buyer buyer = new Buyer(personDAO.nextId(), name, phone, email);
        personDAO.insert(buyer);
        return buyer;
    }

    public List<Person> getAllPersons() throws SQLException {
        return personDAO.findAll();
    }

    public Person login(int id, String email, String expectedType) throws SQLException {
        Person person = personDAO.findById(id);
        if (person == null) throw new IllegalArgumentException("Account not found");
        if (!expectedType.equalsIgnoreCase(person.getType())) {
            throw new IllegalArgumentException("Invalid account type for this login");
        }
        if (email == null || !email.equalsIgnoreCase(person.getEmail())) {
            throw new IllegalArgumentException("Email does not match registered account");
        }
        return person;
    }

    public Property addProperty(String type, String title, String location,
                                double area, double price, int sellerId) throws SQLException {
        int id = propertyDAO.nextId();
        Property prop = createProperty(type, id, title, location, area, price, sellerId);
        propertyDAO.insert(prop);
        return prop;
    }

    public List<Property> searchProperties(String location, Double minPrice, Double maxPrice,
                                           Double minArea, Double maxArea, String type) throws SQLException {
        return propertyDAO.search(location, minPrice, maxPrice, minArea, maxArea, type);
    }

    public Transaction recordTransaction(int propertyId, int buyerId, String txType) throws SQLException {
        Property prop = propertyDAO.findById(propertyId);
        if (prop == null) throw new IllegalArgumentException("Property not found");
        if (!"AVAILABLE".equals(prop.getStatus())) {
            throw new IllegalStateException("Property is already " + prop.getStatus());
        }

        String newStatus = "BUY".equalsIgnoreCase(txType) ? "SOLD" : "RENTED";
        propertyDAO.updateStatus(propertyId, newStatus);

        Transaction tx = new Transaction(
            transactionDAO.nextId(),
            propertyId,
            buyerId,
            prop.getSellerId(),
            txType.toUpperCase(),
            prop.getPrice(),
            prop.calculateCommission(),
            LocalDateTime.now()
        );
        transactionDAO.insert(tx);
        return tx;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public List<Map<String, Object>> getActiveRentals() throws SQLException {
        List<Map<String, Object>> rentals = new java.util.ArrayList<>();
        for (Property prop : propertyDAO.findByStatus("RENTED")) {
            Transaction latest = transactionDAO.findLatestRentForProperty(prop.getId());
            if (latest == null) continue;
            Person tenant = personDAO.findById(latest.getBuyerId());
            rentals.add(Map.of(
                "property", prop,
                "tenant", tenant,
                "lastRentDate", latest.getDate(),
                "monthlyAmount", latest.getAmount()
            ));
        }
        return rentals;
    }

    public Transaction renewRent(int propertyId) throws SQLException {
        Property prop = propertyDAO.findById(propertyId);
        if (prop == null) throw new IllegalArgumentException("Property not found");
        if (!"RENTED".equals(prop.getStatus())) {
            throw new IllegalStateException("Property is not currently rented");
        }
        Transaction latest = transactionDAO.findLatestRentForProperty(propertyId);
        if (latest == null) throw new IllegalStateException("No active rental found for this property");

        Transaction tx = new Transaction(
            transactionDAO.nextId(),
            propertyId,
            latest.getBuyerId(),
            prop.getSellerId(),
            "RENT_RENEWAL",
            prop.getPrice(),
            prop.calculateCommission(),
            LocalDateTime.now()
        );
        transactionDAO.insert(tx);
        return tx;
    }

    public Property endRent(int propertyId) throws SQLException {
        Property prop = propertyDAO.findById(propertyId);
        if (prop == null) throw new IllegalArgumentException("Property not found");
        if (!"RENTED".equals(prop.getStatus())) {
            throw new IllegalStateException("Property is not currently rented");
        }
        propertyDAO.updateStatus(propertyId, "AVAILABLE");
        return propertyDAO.findById(propertyId);
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        return Map.of(
            "users", personDAO.count(),
            "available", propertyDAO.countByStatus("AVAILABLE"),
            "sold", propertyDAO.countByStatus("SOLD"),
            "rented", propertyDAO.countByStatus("RENTED"),
            "totalCommission", transactionDAO.totalCommission(),
            "transactions", transactionDAO.count()
        );
    }

    private Property createProperty(String type, int id, String title, String location,
                                    double area, double price, int sellerId) {
        return switch (type.toUpperCase()) {
            case "HOUSE"     -> new House(id, title, location, area, price, sellerId, "AVAILABLE");
            case "APARTMENT" -> new Apartment(id, title, location, area, price, sellerId, "AVAILABLE");
            case "PLOT"      -> new Plot(id, title, location, area, price, sellerId, "AVAILABLE");
            case "SHOP"      -> new Shop(id, title, location, area, price, sellerId, "AVAILABLE");
            default -> throw new IllegalArgumentException("Invalid property type: " + type);
        };
    }
}
