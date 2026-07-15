package com.shivansh.org.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonObject;

import com.shivansh.org.controller.BookController;
import com.shivansh.org.controller.MemberController;
import com.shivansh.org.controller.TransactionController;
import com.shivansh.org.dto.Book;
import com.shivansh.org.dto.Member;
import com.shivansh.org.dto.Transaction;
import com.shivansh.org.util.DbConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Embedded HTTP server to expose REST APIs for the React Frontend.
 * Integrates with existing Book, Member, and Transaction controllers.
 * Uses Gson for snake_case JSON serialization/deserialization.
 */
public class LibraryHttpServer {

    private static HttpServer server;
    private static final BookController bookController = new BookController();
    private static final MemberController memberController = new MemberController();
    private static final TransactionController transactionController = new TransactionController();

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    public static void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            // Set up endpoints
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/books", new BooksHandler());
            server.createContext("/api/members", new MembersHandler());
            server.createContext("/api/transactions", new TransactionsHandler());
            server.createContext("/api/setup-db", new SetupDbHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            System.out.println("  " + "\u001B[32m" + "✔ Web API Server started successfully on http://localhost:8080" + "\u001B[0m");
        } catch (IOException e) {
            System.err.println("Failed to start Web API Server: " + e.getMessage());
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    // Helper to send HTTP responses
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Helper to handle OPTIONS preflight request
    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // Helper to read request body as String
    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    // Helper to extract a query parameter by name
    private static String getQueryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 0 && entry[0].equals(name)) {
                    return entry.length > 1 ? entry[1] : "";
                }
            }
        }
        return null;
    }

    // ── 1. LOGIN HANDLER ───────────────────────────────────────────
    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                String body = readBody(exchange);
                JsonObject req = gson.fromJson(body, JsonObject.class);
                String email = req.has("email") ? req.get("email").getAsString().trim() : "";
                String password = req.has("password") ? req.get("password").getAsString() : "";

                // Check admin
                if (memberController.authenticateAdmin(email, password)) {
                    JsonObject res = new JsonObject();
                    JsonObject userObj = new JsonObject();
                    userObj.addProperty("role", "ADMIN");
                    userObj.addProperty("username", "admin");
                    userObj.addProperty("name", "Administrator");
                    res.add("user", userObj);
                    res.addProperty("token", "admin-jwt-token");
                    sendResponse(exchange, 200, gson.toJson(res));
                    return;
                }

                // Check member
                Member m = memberController.authenticateMember(email, password);
                if (m != null) {
                    JsonObject res = new JsonObject();
                    JsonObject userObj = new JsonObject();
                    userObj.addProperty("role", "MEMBER");
                    userObj.addProperty("member_id", m.getMemberId());
                    userObj.addProperty("first_name", m.getFirstName());
                    userObj.addProperty("last_name", m.getLastName());
                    userObj.addProperty("email", m.getEmail());
                    userObj.addProperty("phone", m.getPhone());
                    userObj.addProperty("membership_type", m.getMembershipType());
                    userObj.addProperty("name", m.getFirstName() + " " + m.getLastName());
                    res.add("user", userObj);
                    res.addProperty("token", "member-jwt-token-" + m.getMemberId());
                    sendResponse(exchange, 200, gson.toJson(res));
                } else {
                    sendResponse(exchange, 401, "{\"error\":\"Invalid email or password\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ── 2. BOOKS HANDLER ───────────────────────────────────────────
    private static class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Book> books = bookController.getAllBooks();
                    sendResponse(exchange, 200, gson.toJson(books));
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    JsonObject bookObj = gson.fromJson(body, JsonObject.class);
                    String title = bookObj.get("title").getAsString();
                    String author = bookObj.get("author").getAsString();
                    String isbn = bookObj.has("isbn") && !bookObj.get("isbn").isJsonNull() ? bookObj.get("isbn").getAsString() : null;
                    String genre = bookObj.has("genre") && !bookObj.get("genre").isJsonNull() ? bookObj.get("genre").getAsString() : "";
                    int totalCopies = bookObj.get("total_copies").getAsInt();

                    bookController.addBook(title, author, isbn, genre, totalCopies);

                    // Fetch and return the latest book created
                    List<Book> all = bookController.getAllBooks();
                    Book created = all.get(all.size() - 1);
                    sendResponse(exchange, 200, gson.toJson(created));
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    JsonObject bookObj = gson.fromJson(body, JsonObject.class);
                    String idStr = getQueryParam(exchange, "id");
                    if (idStr == null) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing id parameter\"}");
                        return;
                    }
                    int bookId = Integer.parseInt(idStr);

                    String newTitle = bookObj.has("title") ? bookObj.get("title").getAsString() : null;
                    String newAuthor = bookObj.has("author") ? bookObj.get("author").getAsString() : null;
                    String newIsbn = bookObj.has("isbn") && !bookObj.get("isbn").isJsonNull() ? bookObj.get("isbn").getAsString() : null;
                    String newGenre = bookObj.has("genre") && !bookObj.get("genre").isJsonNull() ? bookObj.get("genre").getAsString() : null;
                    String newCopies = bookObj.has("total_copies") ? String.valueOf(bookObj.get("total_copies").getAsInt()) : null;

                    bookController.updateBook(bookId, newTitle, newAuthor, newIsbn, newGenre, newCopies);

                    Book updated = bookController.getBookById(bookId);
                    sendResponse(exchange, 200, gson.toJson(updated));
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String idStr = getQueryParam(exchange, "id");
                    if (idStr == null) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing id parameter\"}");
                        return;
                    }
                    int bookId = Integer.parseInt(idStr);
                    bookController.deleteBook(bookId);
                    sendResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ── 3. MEMBERS HANDLER ─────────────────────────────────────────
    private static class MembersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Member> members = memberController.getAllMembers();
                    sendResponse(exchange, 200, gson.toJson(members));
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    JsonObject memObj = gson.fromJson(body, JsonObject.class);
                    String firstName = memObj.get("first_name").getAsString();
                    String lastName = memObj.get("last_name").getAsString();
                    String email = memObj.get("email").getAsString();
                    String phone = memObj.has("phone") && !memObj.get("phone").isJsonNull() ? memObj.get("phone").getAsString() : null;
                    String password = memObj.has("password") ? memObj.get("password").getAsString() : "password123";
                    String membershipType = memObj.has("membership_type") ? memObj.get("membership_type").getAsString() : "STUDENT";

                    memberController.registerMember(firstName, lastName, email, phone, password, membershipType);

                    Member created = memberController.getMemberByEmail(email);
                    sendResponse(exchange, 200, gson.toJson(created));
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    JsonObject memObj = gson.fromJson(body, JsonObject.class);
                    String idStr = getQueryParam(exchange, "id");
                    if (idStr == null) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing id parameter\"}");
                        return;
                    }
                    int memberId = Integer.parseInt(idStr);
                    Member existing = memberController.getMemberById(memberId);

                    if (memObj.has("first_name")) existing.setFirstName(memObj.get("first_name").getAsString());
                    if (memObj.has("last_name")) existing.setLastName(memObj.get("last_name").getAsString());
                    if (memObj.has("email")) existing.setEmail(memObj.get("email").getAsString());
                    if (memObj.has("phone")) existing.setPhone(memObj.get("phone").isJsonNull() ? null : memObj.get("phone").getAsString());
                    if (memObj.has("membership_type")) existing.setMembershipType(memObj.get("membership_type").getAsString());
                    if (memObj.has("password") && !memObj.get("password").getAsString().isEmpty()) {
                        existing.setPassword(com.shivansh.org.util.PasswordUtil.hashPassword(memObj.get("password").getAsString()));
                    }

                    memberController.updateMemberProfile(existing);
                    Member updated = memberController.getMemberById(memberId);
                    sendResponse(exchange, 200, gson.toJson(updated));
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String idStr = getQueryParam(exchange, "id");
                    if (idStr == null) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing id parameter\"}");
                        return;
                    }
                    int memberId = Integer.parseInt(idStr);
                    memberController.deleteMember(memberId);
                    sendResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ── 4. TRANSACTIONS HANDLER ────────────────────────────────────
    private static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    String memberIdStr = getQueryParam(exchange, "memberId");
                    if (memberIdStr != null) {
                        int memberId = Integer.parseInt(memberIdStr);
                        List<Transaction> txs = transactionController.getMemberTransactions(memberId);
                        sendResponse(exchange, 200, gson.toJson(txs));
                    } else {
                        List<Transaction> txs = transactionController.getAllTransactions();
                        sendResponse(exchange, 200, gson.toJson(txs));
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    JsonObject req = gson.fromJson(body, JsonObject.class);

                    if (path.endsWith("/issue")) {
                        int memberId = req.get("member_id").getAsInt();
                        int bookId = req.get("book_id").getAsInt();
                        transactionController.borrowBook(memberId, bookId);

                        // Return the newly created transaction
                        List<Transaction> all = transactionController.getMemberTransactions(memberId);
                        Transaction created = all.get(all.size() - 1);
                        sendResponse(exchange, 200, gson.toJson(created));
                    } else if (path.endsWith("/return")) {
                        String idStr = getQueryParam(exchange, "id");
                        if (idStr == null) {
                            sendResponse(exchange, 400, "{\"error\":\"Missing id parameter\"}");
                            return;
                        }
                        int transactionId = Integer.parseInt(idStr);

                        // Find memberId and bookId
                        Transaction tx = transactionController.getAllTransactions().stream()
                                .filter(t -> t.getTransactionId() == transactionId)
                                .findFirst()
                                .orElse(null);

                        if (tx == null) {
                            sendResponse(exchange, 404, "{\"error\":\"Transaction not found\"}");
                            return;
                        }

                        double fine = transactionController.returnBook(tx.getMemberId(), tx.getBookId());

                        // Fetch updated transaction
                        Transaction updated = transactionController.getAllTransactions().stream()
                                .filter(t -> t.getTransactionId() == transactionId)
                                .findFirst()
                                .orElse(tx);

                        sendResponse(exchange, 200, gson.toJson(updated));
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    // ── 5. SETUP DB HANDLER ────────────────────────────────────────
    private static class SetupDbHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                DbConnection.initializeDatabase();
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Database initialized successfully.\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
