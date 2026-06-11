package com.estatevault.controller;

import com.estatevault.model.Buyer;
import com.estatevault.model.Person;
import com.estatevault.model.Seller;
import com.estatevault.service.EstateService;
import com.estatevault.util.HttpUtil;
import com.estatevault.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Map;

public class PersonController implements HttpHandler {
    private final EstateService service;

    public PersonController(EstateService service) { this.service = service; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                HttpUtil.json(ex, 200, "{}");
                return;
            }

            if ("GET".equals(method) && "/api/persons".equals(path)) {
                StringBuilder sb = new StringBuilder("[");
                int i = 0;
                for (Person p : service.getAllPersons()) {
                    if (i++ > 0) sb.append(",");
                    sb.append(JsonUtil.person(p));
                }
                HttpUtil.json(ex, 200, sb.append("]").toString());
                return;
            }

            if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(HttpUtil.readBody(ex));
                String name = body.get("name");
                String phone = body.get("phone");
                String email = body.get("email");

                if (path.contains("/login/")) {
                    int id = Integer.parseInt(body.get("id"));
                    String expected = path.endsWith("/seller") ? "SELLER" : "BUYER";
                    Person p = service.login(id, email, expected);
                    HttpUtil.json(ex, 200, JsonUtil.person(p));
                } else if (path.endsWith("/seller")) {
                    Seller s = service.registerSeller(name, phone, email);
                    HttpUtil.json(ex, 201, JsonUtil.person(s));
                } else if (path.endsWith("/buyer")) {
                    Buyer b = service.registerBuyer(name, phone, email);
                    HttpUtil.json(ex, 201, JsonUtil.person(b));
                } else {
                    HttpUtil.json(ex, 404, JsonUtil.error("Not found"));
                }
                return;
            }

            HttpUtil.json(ex, 405, JsonUtil.error("Method not allowed"));
        } catch (Exception e) {
            respondError(ex, e);
        }
    }

    private void respondError(HttpExchange ex, Exception e) {
        try {
            HttpUtil.json(ex, 400, JsonUtil.error(e.getMessage()));
        } catch (Exception ignored) {}
    }
}
