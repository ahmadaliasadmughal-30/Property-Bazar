package com.estatevault.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class HttpUtil {

    private HttpUtil() {}

    public static void json(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    public static void file(HttpExchange ex, Path file) throws IOException {
        String name = file.getFileName().toString().toLowerCase();
        String mime = "application/octet-stream";
        if (name.endsWith(".html")) mime = "text/html; charset=UTF-8";
        else if (name.endsWith(".css"))  mime = "text/css; charset=UTF-8";
        else if (name.endsWith(".js"))   mime = "application/javascript; charset=UTF-8";
        else if (name.endsWith(".svg"))  mime = "image/svg+xml";
        else if (name.endsWith(".png"))  mime = "image/png";

        byte[] bytes = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", mime);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> query(HttpExchange ex) {
        Map<String, String> map = new HashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if (raw == null) return map;
        for (String part : raw.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    public static Double dbl(String s) {
        if (s == null || s.isBlank()) return null;
        return Double.parseDouble(s);
    }
}
