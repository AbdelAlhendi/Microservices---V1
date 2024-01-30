package API;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.Executors;


public class OrderService {
    public static void main(String[] args) throws IOException {

        JSONTokener tokener = new JSONTokener(new FileReader("config.json"));

        

        JSONObject json = new JSONObject(tokener);



        JSONObject orderServ = (JSONObject) json.get("OrderService");

        int port = orderServ.getInt("port");



        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
// Example: Set a custom executor with a fixed-size thread pool

        server.setExecutor(Executors.newFixedThreadPool(20)); // Adjust the poo size as needed
// Set up context for /user post and get requests
        server.createContext("/user", new UserHandler(json));
// Set up context for /product post and get requests
        server.createContext("/product", new ProductHandler(json));
        // Context for /order post and get requests
        server.createContext("/order", new OrderHandler(json));
        // Context for shutdown
        server.createContext("/shutdown", new shutdownHandler(json));
        // Context for restart
        server.createContext("/restart", new restartHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);

    }

    static class shutdownHandler implements HttpHandler {
        JSONObject json;
        public shutdownHandler(JSONObject json) {
            this.json = json;
        }

        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Received Post request for /shutdown";


                JSONObject map = new JSONObject();


                map.put("command", "shutdown");

                HttpClient client = HttpClient.newHttpClient();
                HttpClient client2 = HttpClient.newHttpClient();


                JSONObject userJsonObject = this.json.getJSONObject("UserService");
                int userPort = userJsonObject.getInt("port");
                String userIp = userJsonObject.getString("ip");

                JSONObject productJsonObject = this.json.getJSONObject("ProductService");
                int productPort = productJsonObject.getInt("port");
                String productIp = productJsonObject.getString("ip");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + userIp + ":" + userPort + "/user"))
                        .headers("Content-Type", "application/json")
                        .POST((HttpRequest.BodyPublishers.ofString(map.toString())))
                        .build();

                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + productIp + ":" + productPort + "/product"))
                        .headers("Content-Type", "application/json")
                        .POST((HttpRequest.BodyPublishers.ofString(map.toString())))
                        .build();




                try {

                    HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response1.statusCode() == 200) {

                    } else {

                    }


                    HttpResponse<String> response2 = client2.send(request2, HttpResponse.BodyHandlers.ofString());
                    if (response2.statusCode() == 200) {

                        response = "SHUTDOWN";

                        System.out.println(response);
                        sendResponse(exchange, response, 200);
                        System.exit(0);

                    } else {
                        response = "POST request did not work";
                        System.out.println(response);
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    response = "POST request did not work";
                    System.out.println(response);
                    System.exit(0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }

    static class restartHandler implements HttpHandler {  // STILL NEEDS WORK
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Received Post request for /restart";

                sendResponse(exchange, response, 200);
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    static class UserHandler implements HttpHandler {
        private JSONObject json;

        public UserHandler(JSONObject json) {
            this.json = json;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
// Handle POST request for /user
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Received Post request for /user";


                JSONObject map = new JSONObject(getRequestBody(exchange));



                try {
                    if ((Integer.parseInt(map.get("id").toString()) <= 0)) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    }
                    else if (map.get("command").equals("update") && map.get("id").equals("")) {
                        response = "Missing arguments for Post request for /user";

                        sendResponse(exchange, response, 400);
                    } else if ((map.get("password").equals("") || map.get("email").equals("") || map.get("username").equals("") || map.get("id").equals(""))) {
                        response = "Missing arguments for Post request for /user";

                        sendResponse(exchange, response, 400);
                    } else {



                        JSONObject userJsonObject = this.json.getJSONObject("UserService");
                        int userPort = userJsonObject.getInt("port");
                        String userIp = userJsonObject.getString("ip");

                        HttpClient client = HttpClient.newHttpClient();

                        HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + userIp + ":" + userPort + "/user"))
                        .headers("Content-Type", "application/json")
                        .POST((HttpRequest.BodyPublishers.ofString(map.toString())))
                        .build();

                        


                        try {
                            HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());

                            if (response1.statusCode() == 200) {

                                sendResponse(exchange, response1.body(), 200);

                            } else if (response1.statusCode() == 400) {
                                sendResponse(exchange, response1.body(), 400);

                            } else if (response1.statusCode() == 404) {

                                sendResponse(exchange, response1.body(), 404);

                            } else if (response1.statusCode() == 409) {
                                sendResponse(exchange, response1.body(), 400);
                            } else {
                                response = "POST request did not work";
                                sendResponse(exchange, response1.body(), 500);
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e);
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


            } else if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Received Get request for /user";

                JSONObject map = new JSONObject(getRequestBody(exchange));



                 try {
                    if ((Integer.parseInt(map.get("id").toString()) <= 0)) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } else if (map.get("id").equals("")) {
                        response = "Missing arguments for Get request for /user";
                        // exchange.sendResponseHeaders(400,0);
                        // exchange.close();
                        sendResponse(exchange, response, 400);
                    } else {


                        JSONObject userJsonObject = this.json.getJSONObject("UserService");
                        int userPort = userJsonObject.getInt("port");
                        String userIp = userJsonObject.getString("ip");


                        HttpClient client = HttpClient.newHttpClient();


                        HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + userIp + ":" + userPort + "/user/" + map.get("id")))
                        .headers("Content-Type", "application/json")
                        .GET()
                        .build();


                        try {
                            HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response1.statusCode() == 200) {

                                sendResponse(exchange, response1.body(), 200);
                            } else if (response1.statusCode() == 400) {

                                sendResponse(exchange, response1.body(), 400);
                            } else if (response1.statusCode() == 404) { 
                                sendResponse(exchange, response1.body(), 404);
                            } else {

                                sendResponse(exchange, response1.body(), 500);
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e);
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
// Send a 405 Method Not Allowed response for non-POST requests
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }



    static class ProductHandler implements HttpHandler {
        private JSONObject json;

        public ProductHandler(JSONObject json) {
            this.json = json;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Received Post request for /product";

                JSONObject map = new JSONObject(getRequestBody(exchange));


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

                    } else if ((map.get("name").equals("") || map.get("description").equals("") || map.get("price").equals("") || map.get("id").equals("") || map.get("quantity").equals(""))) {
                        response = "Missing arguments for Post request for /product";

                        sendResponse(exchange, response, 400);

                    } else {



                        JSONObject productJsonObject = this.json.getJSONObject("ProductService");
                        int productPort = productJsonObject.getInt("port");
                        String productIp = productJsonObject.getString("ip");


                        HttpClient client = HttpClient.newHttpClient();

                        HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + productIp + ":" + productPort + "/product"))
                        .headers("Content-Type", "application/json")
                        .POST((HttpRequest.BodyPublishers.ofString(map.toString())))
                        .build();

                        


                        try {

                            HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response1.statusCode() == 200) {

                                sendResponse(exchange, response1.body(), 200);

                            } else if (response1.statusCode() == 400) {
                                sendResponse(exchange, response1.body(), 400);

                            } else if (response1.statusCode() == 404) {

                                sendResponse(exchange, response1.body(), 404);

                            } else if (response1.statusCode() == 409) {
                                sendResponse(exchange, response1.body(), 400);
                            } else {
                                response = "GET request did not work";
                                sendResponse(exchange, response1.body(), 500);
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e);
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

            } else if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Received Get request for /product";

                JSONObject map = new JSONObject(getRequestBody(exchange));


                try {
                    if (Integer.parseInt(map.get("id").toString()) <= 0) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } else if (map.get("id").equals("")) {
                        response = "Missing arguments for Get request for /product";

                        sendResponse(exchange, response, 400);
                    } else {



                        JSONObject productJsonObject = this.json.getJSONObject("ProductService");
                        int productPort = productJsonObject.getInt("port");
                        String productIp = productJsonObject.getString("ip");
                        

                        HttpClient client = HttpClient.newHttpClient();



                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + productIp + ":" + productPort + "/product/" + map.get("id")))
                                .headers("Content-Type", "application/json")
                                .GET()
                                .build();


                        try {
                            HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response1.statusCode() == 200) {

                                sendResponse(exchange, response1.body(), 200);
                            } else if (response1.statusCode() == 400) {
                                sendResponse(exchange, response1.body(), 400);
                            } else if (response1.statusCode() == 404) { 
                                sendResponse(exchange, response1.body(), 404);
                            } else {

                                sendResponse(exchange, response1.body(), 500);
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
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

    static class OrderHandler implements HttpHandler {
        private JSONObject json;

        public OrderHandler(JSONObject json) {
            this.json = json;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Received Post request for /order";

                JSONObject map = new JSONObject(getRequestBody(exchange));

                try {
                    if ((Integer.parseInt(map.get("productid").toString()) <= 0) || (Integer.parseInt(map.get("userid").toString()) <= 0) || (Integer.parseInt(map.get("quantity").toString()) <= 0)) {
                        response = "Invalid arguments";
                        sendResponse(exchange, response, 400);
                    } else if (map.get("productid").equals("") || map.get("userid").equals("") || map.get("quantity").equals("")) {
                        response = "“Invalid Request";

                        sendResponse(exchange, response, 400);
                    } else {



                        JSONObject userJsonObject = this.json.getJSONObject("UserService");
                        int userPort = userJsonObject.getInt("port");
                        String userIp = userJsonObject.getString("ip");


                        HttpClient client = HttpClient.newHttpClient();




                        HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + userIp + ":" + userPort + "/user/" + map.get("userid")))
                        .headers("Content-Type", "application/json")
                        .GET()
                        .build();



                        boolean userCheck = true;


                        try {

                            HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response1.statusCode() == 200) {

                                System.out.println("User GET request did work: " + response1.statusCode());

                            } else if (response1.statusCode() == 400) {
                                userCheck = false;
                                response = "Invalid Request";

                                sendResponse(exchange, response1.body(), 400);
                            } else if (response1.statusCode() == 404) { 
                                sendResponse(exchange, response1.body(), 404);
                            } else {
                                System.out.println("User GET request did not work: " + response1.statusCode());

                                sendResponse(exchange, response1.body(), 500);
                            }
                        } catch (InterruptedException e) {
                            userCheck = false;
                            System.out.println(e);
                        }

                        if (userCheck) {

                            // if user exists, good
                            JSONObject productJsonObject = this.json.getJSONObject("ProductService");
                            int productPort = productJsonObject.getInt("port");
                            String productIp = productJsonObject.getString("ip");
                            

                            HttpClient client3 = HttpClient.newHttpClient();

                            HttpRequest request3 = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + productIp + ":" + productPort + "/product/" + map.get("productid")))
                                .headers("Content-Type", "application/json")
                                .GET()
                                .build();


                            boolean productCheck = true;

                            String product = "";

                            try {

                                HttpResponse<String> response3 = client3.send(request3, HttpResponse.BodyHandlers.ofString());
                                if (response3.statusCode() == 200) {
                                    System.out.println("Product GET request did work: " + response3.statusCode());
                                    product = response3.body();

                                } else if (response3.statusCode() == 400) {
                                    productCheck = false;
                                    response = "Invalid Request";

                                    sendResponse(exchange, response3.body(), 400);
                                } else if (response3.statusCode() == 404) { 
                                    sendResponse(exchange, response3.body(), 404);
                                } else {

                                    sendResponse(exchange, response3.body(), 500);
                                }
                            } catch (InterruptedException e) {
                                productCheck = false;
                                System.out.println(e);
                            }
                            // check if quantity is too low, and then do proper quantity operations and then update product and return success

                            if (productCheck) {
                                JSONObject map2 = new JSONObject(product);

                                int quantity = Integer.parseInt(map2.get("quantity").toString());


                                if (quantity < Integer.parseInt(map.get("quantity").toString())) {
                                    response = "Exceeded Quantity Limit";

                                    sendResponse(exchange, response, 409);
                                } else {



                                    quantity -= Integer.parseInt((map.get("quantity").toString()));
                                    JSONObject map3 = new JSONObject();
                                    map3.put("command", "update");
                                    map3.put("id", map.get("productid"));
                                    map3.put("name", "");
                                    map3.put("description", "");
                                    map3.put("price", map2.get("price"));

                                    
                                    map3.put("quantity", String.valueOf(quantity));



                                    HttpClient client2 = HttpClient.newHttpClient();
                                    

                                    HttpRequest request2 = HttpRequest.newBuilder()
                                    .uri(URI.create("http://" + productIp + ":" + productPort + "/product"))
                                    .headers("Content-Type", "application/json")
                                    .POST((HttpRequest.BodyPublishers.ofString(map3.toString())))
                                    .build();



                                    try {

                                        HttpResponse<String> response2 = client2.send(request2, HttpResponse.BodyHandlers.ofString());
                                        if (response2.statusCode() == 200) {

                                            response = "Success";
                                            sendResponse(exchange, response, 200);
                                        } else if (response2.statusCode() == 404) { 
                                            sendResponse(exchange, response2.body(), 404);
                                        } else if (response2.statusCode() == 400) { 
                                            sendResponse(exchange, response2.body(), 400);
                                        } else {

                                            sendResponse(exchange, response2.body(), 500);
                                        }
                                    } catch (InterruptedException e) {
                                        System.out.println(e);
                                    }

                                }
                            }
                        }
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
}
