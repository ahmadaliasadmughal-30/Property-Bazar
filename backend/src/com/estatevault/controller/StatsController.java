package com.estatevault.controller;

import com.estatevault.service.EstateService;
import com.estatevault.util.HttpUtil;
import com.estatevault.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Map;

public class StatsController implements HttpHandler {
    private final EstateService service;

    public StatsController(EstateService service) { this.service = service; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                HttpUtil.json(ex, 200, "{}");
                return;
            }
            Map<String, Object> s = service.getDashboardStats();
            HttpUtil.json(ex, 200, JsonUtil.stats(
                (int) s.get("users"),
                (int) s.get("available"),
                (int) s.get("sold"),
                (int) s.get("rented"),
                (double) s.get("totalCommission"),
                (int) s.get("transactions")
            ));
        } catch (Exception e) {
            try { HttpUtil.json(ex, 500, JsonUtil.error(e.getMessage())); }
            catch (Exception ignored) {}
        }
    }
}
