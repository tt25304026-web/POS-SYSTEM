import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * A simple POS Server in Java using built-in JDK libraries.
 * Serves static files and provides a basic foundation for a POS system.
 */
public class PosServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Context to serve static files from the 'www' directory
        server.createContext("/", new StaticFileHandler("www"));
        // Context to serve API requests from the 'data' directory
        server.createContext("/api", new ApiHandler("data"));
        
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("POS System Server started at http://localhost:" + port);
        System.out.println("Server is running. Press Ctrl+C to stop.");
        
        // Keep the main thread alive with a simple loop
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    static class ApiHandler implements HttpHandler {
        private final String dataDir;

        public ApiHandler(String dataDir) {
            this.dataDir = dataDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            // Allow CORS for development
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (method.equalsIgnoreCase("GET")) {
                handleGet(exchange, path);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePost(exchange, path);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleGet(HttpExchange exchange, String path) throws IOException {
            // Log for debugging
            System.out.println("API GET Request: " + path);
            
            // Clean up the path: remove /api and trailing slashes
            String resource = path;
            if (resource.startsWith("/api")) {
                resource = resource.substring(4);
            }
            if (resource.startsWith("/")) resource = resource.substring(1);
            if (resource.endsWith("/")) resource = resource.substring(0, resource.length() - 1);
            
            if (resource.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing resource name\"}");
                return;
            }

            String fileName = resource + ".json";
            File file = new File(dataDir, fileName);
            System.out.println("Looking for data file: " + file.getAbsolutePath());

            if (file.exists() && file.isFile()) {
                byte[] content = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        }

        private void handlePost(HttpExchange exchange, String path) throws IOException {
            if (path.endsWith("/orders")) {
                // Read request body
                String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                // In a real system, we'd parse JSON and save to a database.
                // Here, we'll append to an orders.json file (simplified).
                File ordersFile = new File(dataDir, "orders.json");
                
                // For simplicity, we just log it and return success. 
                // A real implementation would manage a JSON array of orders.
                System.out.println("Received New Order: " + body);
                
                // Append to a log file
                try (FileOutputStream fos = new FileOutputStream(new File(dataDir, "orders_log.txt"), true)) {
                    fos.write((body + "\n---\n").getBytes(StandardCharsets.UTF_8));
                }

                sendResponse(exchange, 201, "{\"status\": \"success\", \"message\": \"Order received\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            
            // Fix: ensure requestPath doesn't start with / for Paths.get on Windows
            String relativePath = requestPath;
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            
            if (relativePath.isEmpty() || relativePath.equals("/")) {
                relativePath = "index.html";
            } else if (relativePath.endsWith("/")) {
                relativePath += "index.html";
            }

            Path path = Paths.get(baseDir, relativePath);
            File file = path.toFile();

            // Handle directories: if user requests /pos/ and it exists, look for /pos/index.html
            if (file.isDirectory()) {
                file = new File(file, "index.html");
                path = file.toPath();
            }

            if (file.exists() && file.isFile()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    if (requestPath.endsWith(".css")) contentType = "text/css";
                    else if (requestPath.endsWith(".js")) contentType = "application/javascript";
                    else contentType = "text/plain";
                }

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());

                try (OutputStream os = exchange.getResponseBody();
                     FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
            } else {
                String response = "404 (Not Found)\nFile: " + requestPath;
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}
