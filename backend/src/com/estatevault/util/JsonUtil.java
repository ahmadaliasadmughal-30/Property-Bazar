package com.estatevault.util;

import com.estatevault.model.Person;
import com.estatevault.model.Property;
import com.estatevault.model.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        String body = json.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        for (String pair : body.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().replaceAll("^\"|\"$", "");
            String val = kv[1].trim().replaceAll("^\"|\"$", "");
            map.put(key, val);
        }
        return map;
    }

    public static String person(Person p) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"type\":\"%s\"}",
            p.getId(), escape(p.getName()), escape(p.getPhone()),
            escape(p.getEmail()), p.getType());
    }

    public static String property(Property p) {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"location\":\"%s\",\"area\":%.2f," +
            "\"price\":%.0f,\"sellerId\":%d,\"status\":\"%s\",\"type\":\"%s\"," +
            "\"commission\":%.0f}",
            p.getId(), escape(p.getTitle()), escape(p.getLocation()), p.getArea(),
            p.getPrice(), p.getSellerId(), p.getStatus(), p.getPropertyType(),
            p.calculateCommission());
    }

    public static String properties(List<Property> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(property(list.get(i)));
        }
        return sb.append("]").toString();
    }

    public static String transaction(Transaction t) {
        String date = t.getDate() != null ? t.getDate().format(FMT) : "";
        return String.format(
            "{\"id\":%d,\"propertyId\":%d,\"buyerId\":%d,\"sellerId\":%d," +
            "\"type\":\"%s\",\"amount\":%.0f,\"commission\":%.0f,\"date\":\"%s\"}",
            t.getId(), t.getPropertyId(), t.getBuyerId(), t.getSellerId(),
            t.getType(), t.getAmount(), t.getCommission(), date);
    }

    public static String transactions(List<Transaction> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(transaction(list.get(i)));
        }
        return sb.append("]").toString();
    }

    public static String stats(int users, int available, int sold, int rented,
                               double totalCommission, int txCount) {
        return String.format(
            "{\"users\":%d,\"available\":%d,\"sold\":%d,\"rented\":%d," +
            "\"totalCommission\":%.0f,\"transactions\":%d}",
            users, available, sold, rented, totalCommission, txCount);
    }

    public static String rental(Map<String, Object> entry) {
        Property p = (Property) entry.get("property");
        Person tenant = (Person) entry.get("tenant");
        java.time.LocalDateTime lastRent = (java.time.LocalDateTime) entry.get("lastRentDate");
        double monthly = (Double) entry.get("monthlyAmount");
        String date = lastRent != null ? lastRent.format(FMT) : "";
        return String.format(
            "{\"property\":%s,\"tenant\":%s,\"lastRentDate\":\"%s\",\"monthlyAmount\":%.0f}",
            property(p), person(tenant), date, monthly);
    }

    public static String rentals(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(rental(list.get(i)));
        }
        return sb.append("]").toString();
    }

    public static String error(String msg) {
        return "{\"error\":\"" + escape(msg) + "\"}";
    }
}
