package com.estatevault.controller;

import com.estatevault.model.Transaction;
import com.estatevault.service.EstateService;
import com.estatevault.util.HttpUtil;
import com.estatevault.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Map;

public class TransactionController implements HttpHandler {
    private final EstateService service;

    public TransactionController(EstateService service) { this.service = service; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                HttpUtil.json(ex, 200, "{}");
                return;
            }

            if ("GET".equals(method) && "/api/transactions".equals(path)) {
                HttpUtil.json(ex, 200, JsonUtil.transactions(service.getAllTransactions()));
                return;
            }

            if ("POST".equals(method) && "/api/transactions".equals(path)) {
                Map<String, String> body = JsonUtil.parseObject(HttpUtil.readBody(ex));
                Transaction tx = service.recordTransaction(
                    Integer.parseInt(body.get("propertyId")),
                    Integer.parseInt(body.get("buyerId")),
                    body.get("type")
                );
                HttpUtil.json(ex, 201, JsonUtil.transaction(tx));
                return;
            }

            HttpUtil.json(ex, 404, JsonUtil.error("Not found"));
        } catch (Exception e) {
            try { HttpUtil.json(ex, 400, JsonUtil.error(e.getMessage())); }
            catch (Exception ignored) {}
        }
    }
}
