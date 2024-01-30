package API;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.json.JSONTokener;

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

public class UserService {
    //users id
    public int id;

    //user's username
    public String username;

    //user's email
    public String email;

    //user's password
    public String password;

    /**
     * Constructor
     *
     */
    public UserService(int id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }



    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {


        JSONTokener tokener = new JSONTokener(new FileReader(args[0]));
        

        JSONObject json = new JSONObject(tokener);


        JSONObject userServ = (JSONObject) json.get("UserService");

        int port = userServ.getInt("port");


        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
// Example: Set a custom executor with a fixed-size thread pool

        if (connect() == null) {
            createNewDatabase();
        }


        server.setExecutor(Executors.newFixedThreadPool(20)); // Adjust the poo size as needed
// Set up context for /user post and get requests
        server.createContext("/user", new PostHandler());

        server.createContext("/user/", new GetHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class GetHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals((exchange.getRequestMethod()))) {
                String response = "Received Get request for /user";

                String url = exchange.getRequestURI().getPath();
                String[] spliturl = url.split("/");

                if (spliturl[spliturl.length - 1].equals("")) {
                    response = "Missing arguments for Get request for /user";

                    sendResponse(exchange, response, 400);
                } else {
                    int id = 0;
                    try {
                        id = Integer.parseInt(spliturl[spliturl.length - 1]);
                        if (id <= 0) {
                            response = "Invalid arguments";
                            sendResponse(exchange, response, 400);
                        } else {



                            UserService user = getUser(id);
                            if (user == null) {
                                response = "User does not exist";

                                sendResponse(exchange, response, 404);
                            } else {

                                
                                Map<String, String> map = new HashMap<>();
                                map.put("id", String.valueOf(user.id)); // id
                                map.put("username", user.username); // username
                                map.put("email", user.email); // email
                                map.put("password", user.password); // password

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
                String response = "Received Post request for /user";

                JSONObject map = new JSONObject(getRequestBody(exchange));

                if (map.get("command").equals("shutdown")) {
                    System.out.println("SHUTDOWN");
                    response = (String) map.get("command");
                    sendResponse(exchange, response, 200);
                    System.exit(0);
                }

                try {
                    if ((Integer.parseInt(map.get("id").toString()) <= 0)) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } else if (map.get("command").equals("update") && map.get("id").equals("")) {
                        response = "Missing arguments for Post request for /user";

                        sendResponse(exchange, response, 400);
                    } else if (!map.get("command").equals("update") && ((map.get("username").equals("") || map.get("email").equals("") || map.get("password").equals("") || map.get("id").equals("")))) {
                        response = "Missing arguments for Post request for /user";

                        sendResponse(exchange, response, 400);
                    } else {

                        if (map.get("command").equals("create")) {



                            UserService user = new UserService
                                    (Integer.parseInt((String) map.get("id")), (String) map.get("username"), (String) map.get("email"), (String) map.get("password"));


                            try {
                                createNewTable();
                                insert(user, exchange);

                                response = "User created";
                                sendResponse(exchange, response, 200);

                            } catch (SQLException e) {
                                response = "Internal Error";
                                sendResponse(exchange, response, 500);

                            }


                            

                        } else if (map.get("command").equals("update")) {
                            // get user with proper id from database
                            UserService user = getUser(Integer.parseInt((String) map.get("id")));

                            if (user == null) {

                                
                                response = "User does not exist";

                                sendResponse(exchange, response, 404);

                            } else {

                                if (!map.get("username").equals("")) {
                                    user.username = (String) map.get("username");
                                }
                                if (!map.get("email").equals("")) {
                                    user.email = (String) map.get("email");
                                }
                                if (!map.get("password").equals("")) {
                                    user.password = (String) map.get("password");
                                }

                                updateUser(user);
                                response = "User updated";
                                sendResponse(exchange, response, 200);
                            }

                        } else if (map.get("command").equals("delete")) {
                            // get user with proper id from database
                            UserService user = getUser(Integer.parseInt((String) map.get("id")));


                            if (user == null) {
                                response = "User does not exist";

                                sendResponse(exchange, response, 404);
                            } else {

                                if (user.password.equals(map.get("password")) && (user.email.equals(map.get("email"))) && (user.username.equals(map.get("username")))) {
                                    //delete user
                                    deleteUser(user.id);
                                    response = "User deleted";
                                    sendResponse(exchange, response, 200);
                                } else {

                                    response = "Incorrect username, email or password";

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
                exchange.sendResponseHeaders(405,0);
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
        String path = "jdbc:sqlite:sqlite/db/users.db";
        

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
            String path = "jdbc:sqlite:sqlite/db/users.db";

            con = DriverManager.getConnection(path);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return con;
    }

    public static void insert(UserService user, HttpExchange exchange) throws SQLException, IOException {
        String table = "INSERT INTO users(id,username,email,password) VALUES(?,?,?,?)";
        try (Connection con = connect(); PreparedStatement preparedStatement = con.prepareStatement(table)) {
            preparedStatement.setInt(1, user.id);
            preparedStatement.setString(2, user.username);
            preparedStatement.setString(3, user.email);
            preparedStatement.setString(4, user.password);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            String response = "User already exists";
            sendResponse(exchange, response, 409);
        } catch (ClassNotFoundException e) {
            String response = "Internal Error";
            sendResponse(exchange, response, 500);
            throw new RuntimeException(e);
        }
    }

    public static void createNewTable() {
        String path = "jdbc:sqlite:sqlite/db/users.db";

        String table = "CREATE TABLE IF NOT EXISTS users (\n"
                + "   id integer PRIMARY KEY,\n"
                + "   username text NOT NULL,\n"
                + "   email text NOT NULL,\n"
                + "   password text NOT NULL\n" +
                ");";

        try (Connection con = DriverManager.getConnection(path);
             Statement statement = con.createStatement()) {

            statement.execute(table);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static UserService getUser(int id) {
        String get = "SELECT id, username, email, password "
                + "From users WHERE id = ?";

        UserService returnUser = null;

        try (Connection con = connect();
                PreparedStatement statement = con.prepareStatement(get)) {

            statement.setInt(1, id);
            ResultSet user = statement.executeQuery();



            returnUser = new UserService(user.getInt("id"), user.getString("username"),
                    user.getString("email"), user.getString("password"));

            if (returnUser.email == null) {
                returnUser = null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return returnUser;
    }

    private static void updateUser(UserService user) {
        String updateStr = "UPDATE users SET username = ? , "
                + "email = ? , "
                + "password = ? "
                + "WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement statement = con.prepareStatement(updateStr)) {

            statement.setString(1, user.username);
            statement.setString(2, user.email);
            statement.setString(3, user.password);
            statement.setInt(4, user.id);

            statement.executeUpdate();

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteUser(int id) {
        String delete = "DELETE FROM users WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement statement = con.prepareStatement(delete)) {

            statement.setInt(1, id);

            statement.executeUpdate();


        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}
