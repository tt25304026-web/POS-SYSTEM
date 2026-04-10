package com.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation for the POS System API.
 * This handles requests to /api/* and interacts with the data/ folder.
 */
@WebServlet("/api/*")
public class PosApiServlet extends HttpServlet {

    private String dataDir;

    @Override
    public void init() throws ServletException {
        // We assume the data folder is in the root of the webapp context or specified by env
        // For local development, we'll try to find it in a fixed path or relative to context
        dataDir = getServletContext().getRealPath("/WEB-INF/data");
        
        // Ensure data directory exists
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        System.out.println("PosApiServlet initialized. Data directory: " + dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo(); // e.g., /products
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing resource name");
            return;
        }

        // Clean up the resource name
        String resource = pathInfo;
        if (resource.startsWith("/")) resource = resource.substring(1);
        if (resource.endsWith("/")) resource = resource.substring(0, resource.length() - 1);

        String fileName = resource + ".json";
        File file = new File(dataDir, fileName);

        // If not in WEB-INF/data, check if it's in a sibling data/ folder (for dev)
        if (!file.exists()) {
            // Try to find it in the project root relative to current working dir
            file = new File("data", fileName);
        }

        if (file.exists() && file.isFile()) {
            byte[] content = Files.readAllBytes(file.toPath());
            response.setContentType("application/json; charset=UTF-8");
            response.setContentLength(content.length);
            response.setStatus(HttpServletResponse.SC_OK);
            try (OutputStream os = response.getOutputStream()) {
                os.write(content);
            }
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found: " + fileName);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.endsWith("/orders")) {
            // Read request body
            String body = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // Log the order
            System.out.println("Received New Order via Tomcat: " + body);
            
            // Log to file
            File logFile = new File(dataDir, "orders_log.txt");
            try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                fos.write((body + "\n---\n").getBytes(StandardCharsets.UTF_8));
            }

            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_CREATED);
            String successMsg = "{\"status\": \"success\", \"message\": \"Order received by Tomcat\"}";
            response.getWriter().write(successMsg);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
