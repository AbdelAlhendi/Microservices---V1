package API;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.json.JSONTokener;

public class ProductService {
    public int id;


    public String name;


    public String description;


    public double price;

    public int quantity;

    public ProductService(int id, String name, String description, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        JSONTokener tokener = new JSONTokener(new FileReader(args[0]));
        

        JSONObject json = new JSONObject(tokener);


        JSONObject productServ = (JSONObject) json.get("ProductService");

        int port = productServ.getInt("port");


        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
// Example: Set a custom executor with a fixed-size thread pool

        if (connect() == null) {
            createNewDatabase();
        }

        server.setExecutor(Executors.newFixedThreadPool(20)); // Adjust the poo size as needed
// Set up context for /user post and get requests
        server.createContext("/product", new PostHandler());
        server.createContext("/product/", new GetHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class GetHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals((exchange.getRequestMethod()))) {
                String response = "Received Get request for /product";

                String url = exchange.getRequestURI().getPath();
                String[] spliturl = url.split("/");

                if (spliturl[spliturl.length - 1].equals("")) {
                    response = "Missing arguments for Get request for /product";

                    sendResponse(exchange, response, 400);
                } else {
                    int id = 0;
                    try {
                        id = Integer.parseInt(spliturl[spliturl.length - 1]);
                        if (id <= 0) {
                            response = "Invalid arguments";
                            sendResponse(exchange, response, 400);
                        } else {
                    

                            ProductService product = getProduct(id);


                            if (product == null) {
                                response = "Product does not exist";

                                sendResponse(exchange, response, 404);
                            } else {


                                Map<String, String> map = new HashMap<>();
                                map.put("id", String.valueOf(product.id));
                                map.put("name", product.name);
                                map.put("description", product.description);
                                map.put("price", String.valueOf(product.price));
                                map.put("quantity", String.valueOf(product.quantity));

                                JSONObject map2 = new JSONObject(map);

                                sendResponse(exchange, map2.toString(), 200);
                            }
                        }
                    } catch (NumberFormatException e) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } catch (NullPointerException f) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    }
                }
            } else {
                exchange.sendResponseHeaders(405,0);
                exchange.close();
            }
        }
    }

    static class PostHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals((exchange.getRequestMethod()))) {

                String response = "Received Post request for /product";


                JSONObject map = new JSONObject(getRequestBody(exchange));


                if (map.get("command").equals("shutdown")) {
                    System.out.println("SHUTDOWN");
                    response = "shutdown";
                    sendResponse(exchange, response, 200);
                    System.exit(0);
                }


                try {
                    if (map.get("command").equals("delete")) {
                        map.put("price", "1");
                        map.put("quantity", "1");
                        map.put("description", "delete call");
                        map.put("name", "delete call");
                    }

                    if ((Integer.parseInt(map.get("id").toString()) <= 0) || (Double.parseDouble(map.get("price").toString()) <= 0) || (Integer.parseInt(map.get("quantity").toString()) <= 0)) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } else if (map.get("command").equals("update") && map.get("id").equals("")) {
                        response = "Missing arguments for Post request for /product";

                        sendResponse(exchange, response, 400);
                    } else if (!map.get("command").equals("update") && ((map.get("name").equals("") || map.get("description").equals("") || map.get("quantity").equals("") || map.get("price").equals("") || map.get("id").equals("")))) {
                        response = "Missing arguments for Post request for /product";

                        sendResponse(exchange, response, 400);
                    } else {
                        if (map.get("command").equals("create")) {




                            ProductService product = new ProductService(Integer.parseInt(map.get("id").toString()),
                                    map.get("name").toString(), map.get("description").toString(), Double.parseDouble(map.get("price").toString()),
                                    Integer.parseInt(map.get("quantity").toString()));

                            try {
                                createNewTable();
                                insert(product, exchange);
                                response = "Product created";
                                sendResponse(exchange, response, 200);
                            } catch (SQLException e) {
                                response = "Internal Error";
                                sendResponse(exchange, response, 500);
                            }

                            


                        } else if (map.get("command").equals("update")) {
                            // get product with proper id from database
                            
                            ProductService product = getProduct(Integer.parseInt(map.get("id").toString()));

                            if (product == null) {
                                response = "Product does not exist";

                                sendResponse(exchange, response, 404);
                            } else {

                                if (!map.get("name").equals("")) {
                                    product.name = map.get("name").toString();
                                }
                                if (!map.get("description").equals("")) {
                                    product.description = map.get("description").toString();
                                }
                                if (!map.get("price").equals("")) {
                                    product.price = Double.parseDouble(map.get("price").toString());
                                }
                                if (!map.get("quantity").equals("")) {
                                    product.quantity = Integer.parseInt(map.get("quantity").toString());
                                }

                                updateProduct(product);
                                response = "Product updated";
                                sendResponse(exchange, response, 200);
                            }

                        } else if (map.get("command").equals("delete")) {

                            ProductService product = getProduct(Integer.parseInt(map.get("id").toString()));


                            if (product == null) {
                                response = "Product does not exist";

                                sendResponse(exchange, response, 404);
                            } else {

                                    if (product.id == Integer.parseInt(map.get("id").toString())) {
                                        //delete product
                                        deleteProduct(product.id);
                                        response = "Product deleted";
                                        sendResponse(exchange, response, 200);
                                    } else {
                                        response = "Incorrect id";

                                        sendResponse(exchange, response, 400);
                                    }
                            }

                        }
                        sendResponse(exchange, response, 200);
                    }
                } catch (NumberFormatException e) {

                    response = "Invalid arguments";
                    sendResponse(exchange, response, 400);
                } catch (NullPointerException f) {
                    response = "Invalid arguments";
                    sendResponse(exchange, response, 400);
                }

            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }

        }
    }

    private static String getRequestBody(HttpExchange exchange) throws
            IOException {
        try (BufferedReader br = new BufferedReader(new
                InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            return requestBody.toString();
        }
    }


    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws
            IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static void createNewDatabase() throws SQLException {
        String path = "jdbc:sqlite:sqlite/db/products.db";

        try (Connection con = DriverManager.getConnection(path)) {
            if (con != null) {
                DatabaseMetaData metaData = con.getMetaData();
                System.out.println("Driver name: " + metaData.getDriverName());
                System.out.println("New db created!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection connect() throws ClassNotFoundException {
        Connection con = null;
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the SQLite database

            String path = "jdbc:sqlite:sqlite/db/products.db";
            con = DriverManager.getConnection(path);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return con;
    }

    public static void insert(ProductService product, HttpExchange exchange) throws SQLException, IOException {
        String table = "INSERT INTO products(id,name,description,price,quantity) VALUES(?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement preparedStatement = con.prepareStatement(table)) {
            preparedStatement.setInt(1, product.id);
            preparedStatement.setString(2, product.name);
            preparedStatement.setString(3, product.description);
            preparedStatement.setDouble(4, product.price);
            preparedStatement.setInt(5, product.quantity);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {

            String response = "Product already exists";
            sendResponse(exchange, response, 409);
        } catch (ClassNotFoundException e) {
            String response = "Internal Error";
            sendResponse(exchange, response, 500);
        }
    }

    public static void createNewTable() {
        String path = "jdbc:sqlite:sqlite/db/products.db";

        String table = "CREATE TABLE IF NOT EXISTS products (\n"
                + "   id integer PRIMARY KEY,\n"
                + "   name text NOT NULL,\n"
                + "   description text NOT NULL,\n"
                + "   price double,\n"
                + "   quantity integer\n" +
                ");";

        try (Connection con = DriverManager.getConnection(path);
             Statement statement = con.createStatement()) {

            statement.execute(table);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ProductService getProduct(int id) {
        String get = "SELECT id, name, description, price, quantity "
                + "From products WHERE id = ?";

        ProductService returnProduct = null;

        try (Connection con = connect();
             PreparedStatement statement = con.prepareStatement(get)) {

            statement.setInt(1, id);
            ResultSet product = statement.executeQuery();

            returnProduct = new ProductService(product.getInt("id"), product.getString("name"),
                    product.getString("description"), product.getDouble("price"),
                    product.getInt("quantity"));

            if (returnProduct.name == null) {
                returnProduct = null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return returnProduct;
    }

    private static void updateProduct(ProductService product) {
        String updateStr = "UPDATE products SET name = ? , "
                + "description = ? , "
                + "price = ? , "
                + "quantity = ? "
                + "WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement preparedStatement = con.prepareStatement(updateStr)) {


            preparedStatement.setString(1, product.name);
            preparedStatement.setString(2, product.description);
            preparedStatement.setDouble(3, product.price);
            preparedStatement.setInt(4, product.quantity);
            preparedStatement.setInt(5, product.id);

            preparedStatement.executeUpdate();

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteProduct(int id) {
        String delete = "DELETE FROM products WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement statement = con.prepareStatement(delete)) {

            statement.setInt(1, id);

            statement.executeUpdate();


        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}
