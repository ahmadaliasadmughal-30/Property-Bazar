package com.estatevault.controller;

import com.estatevault.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class StaticController implements HttpHandler {
    private final Path webRoot;

    public StaticController(Path webRoot) { this.webRoot = webRoot; }

    @Override
    public void handle(HttpExchange ex) {
        try {
            String uri = ex.getRequestURI().getPath();
            if ("/".equals(uri) || uri.isEmpty()) uri = "/index.html";

            Path file = webRoot.resolve(uri.substring(1)).normalize();
            if (!file.startsWith(webRoot) || !Files.exists(file) || Files.isDirectory(file)) {
                ex.sendResponseHeaders(404, -1);
                ex.close();
                return;
            }
            HttpUtil.file(ex, file);
        } catch (Exception e) {
            try { ex.sendResponseHeaders(500, -1); ex.close(); }
            catch (Exception ignored) {}
        }
    }
}
