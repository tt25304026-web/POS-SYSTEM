import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        
        server.setExecutor(null); // creates a default executor
        System.out.println("POS System Server started at http://localhost:" + port);
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            Path path = Paths.get(baseDir, requestPath);
            File file = path.toFile();

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
