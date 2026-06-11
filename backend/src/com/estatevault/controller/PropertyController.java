package com.estatevault.controller;

import com.estatevault.model.Property;
import com.estatevault.service.EstateService;
import com.estatevault.util.HttpUtil;
import com.estatevault.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.List;
import java.util.Map;

public class PropertyController implements HttpHandler {
    private final EstateService service;

    public PropertyController(EstateService service) { this.service = service; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                HttpUtil.json(ex, 200, "{}");
                return;
            }

            if ("POST".equals(method) && "/api/properties".equals(path)) {
                Map<String, String> body = JsonUtil.parseObject(HttpUtil.readBody(ex));
                Property p = service.addProperty(
                    body.get("type"),
                    body.get("title"),
                    body.get("location"),
                    Double.parseDouble(body.get("area")),
                    Double.parseDouble(body.get("price")),
                    Integer.parseInt(body.get("sellerId"))
                );
                HttpUtil.json(ex, 201, JsonUtil.property(p));
                return;
            }

            if ("GET".equals(method) && path.startsWith("/api/properties/search")) {
                Map<String, String> q = HttpUtil.query(ex);
                List<Property> results = service.searchProperties(
                    q.get("location"),
                    HttpUtil.dbl(q.get("minPrice")),
                    HttpUtil.dbl(q.get("maxPrice")),
                    HttpUtil.dbl(q.get("minArea")),
                    HttpUtil.dbl(q.get("maxArea")),
                    q.get("type")
                );
                HttpUtil.json(ex, 200, JsonUtil.properties(results));
                return;
            }

            HttpUtil.json(ex, 404, JsonUtil.error("Not found"));
        } catch (Exception e) {
            try { HttpUtil.json(ex, 400, JsonUtil.error(e.getMessage())); }
            catch (Exception ignored) {}
        }
    }
}
