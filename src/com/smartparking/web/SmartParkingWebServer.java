package com.smartparking.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Optional Lightweight Built-in Java Web Server.
 * Uses core standard JDK com.sun.net.httpserver.HttpServer (Zero external dependencies).
 * Serves the Visual Parking Dashboard on http://localhost:8080 without requiring
 * Spring Boot, Tomcat, or external libraries.
 */
public class SmartParkingWebServer {

    public static final int PORT = 8080;
    public static final String WEB_ROOT = "web";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null); // default executor

        System.out.println("==================================================");
        System.out.println("   SMART PARKING - LIGHTWEIGHT JAVA WEB SERVER    ");
        System.out.println("==================================================");
        System.out.println(" [✓] Server started successfully on standard JDK!");
        System.out.printf(" [✓] Open your browser at: http://localhost:%d%n", PORT);
        System.out.println(" [!] Press Ctrl+C in terminal to stop server.");
        System.out.println("==================================================");

        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(WEB_ROOT, path.replace("/", File.separator));
            if (!file.exists() || file.isDirectory()) {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
