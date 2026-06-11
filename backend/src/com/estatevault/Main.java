package com.estatevault;

import com.estatevault.config.DatabaseConfig;
import com.estatevault.controller.*;
import com.estatevault.service.EstateService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.sql.Connection;

/**
 * EstateVault — Lahore Property Marketplace
 *
 * MVC Architecture:
 *   Model      → com.estatevault.model   (Person, Property, Transaction)
 *   View       → web/                    (HTML, CSS, JavaScript)
 *   Controller → com.estatevault.controller (REST API handlers)
 *   DAO        → com.estatevault.dao      (MySQL data access)
 *   Service    → com.estatevault.service  (business logic)
 *
 * Run: java com.estatevault.Main
 * Open: http://localhost:8080
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Connection conn = DatabaseConfig.connect();
        EstateService service = new EstateService(conn);

        Path webRoot = Path.of("frontend").toAbsolutePath();
        if (!webRoot.toFile().exists()) {
            webRoot = Path.of(System.getProperty("user.dir"), "EstateVault", "web");
        }

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticController(webRoot));
        server.createContext("/api/persons", new PersonController(service));
        server.createContext("/api/persons/seller", new PersonController(service));
        server.createContext("/api/persons/buyer", new PersonController(service));
        server.createContext("/api/properties", new PropertyController(service));
        server.createContext("/api/properties/search", new PropertyController(service));
        server.createContext("/api/transactions", new TransactionController(service));
        server.createContext("/api/rentals", new RentalController(service));
        server.createContext("/api/rentals/renew", new RentalController(service));
        server.createContext("/api/rentals/end", new RentalController(service));
        server.createContext("/api/persons/login/seller", new PersonController(service));
        server.createContext("/api/persons/login/buyer", new PersonController(service));
        server.createContext("/api/stats", new StatsController(service));
        server.setExecutor(null);
        server.start();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   EstateVault — Property Marketplace         ║");
        System.out.println("║   http://localhost:" + port + "                      ║");
        System.out.println("║   Press Ctrl+C to stop                       ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { conn.close(); } catch (Exception ignored) {}
        }));
    }
}
