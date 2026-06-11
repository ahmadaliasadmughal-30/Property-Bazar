package com.estatevault.controller;

import com.estatevault.model.Property;
import com.estatevault.model.Transaction;
import com.estatevault.service.EstateService;
import com.estatevault.util.HttpUtil;
import com.estatevault.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.List;
import java.util.Map;

public class RentalController implements HttpHandler {
    private final EstateService service;

    public RentalController(EstateService service) { this.service = service; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                HttpUtil.json(ex, 200, "{}");
                return;
            }

            if ("GET".equals(method) && "/api/rentals".equals(path)) {
                List<Map<String, Object>> rentals = service.getActiveRentals();
                HttpUtil.json(ex, 200, JsonUtil.rentals(rentals));
                return;
            }

            if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(HttpUtil.readBody(ex));
                int propertyId = Integer.parseInt(body.get("propertyId"));

                if (path.endsWith("/renew")) {
                    Transaction tx = service.renewRent(propertyId);
                    HttpUtil.json(ex, 201, JsonUtil.transaction(tx));
                    return;
                }
                if (path.endsWith("/end")) {
                    Property p = service.endRent(propertyId);
                    HttpUtil.json(ex, 200, JsonUtil.property(p));
                    return;
                }
            }

            HttpUtil.json(ex, 404, JsonUtil.error("Not found"));
        } catch (Exception e) {
            try { HttpUtil.json(ex, 400, JsonUtil.error(e.getMessage())); }
            catch (Exception ignored) {}
        }
    }
}
