package serverhttp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

/**
 *
 * @author will
 */
public class Serverhttp {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // server.createContext("/test", new MyHandler());
        // server.setExecutor(null); // creates a default executor
        // server.start();
        Scanner sc = new Scanner(System.in);

        int port = 8001;
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/test", new MyHttpHandler());
        server.createContext("/discountBalance", new MyHttpHandler());
        //server.setExecutor(threadPoolExecutor);
        server.setExecutor(null);
        //esta instruccion crea un nuevo hilo en Background para el manejo de la peticiones entrantes
        server.start();
        //logger.info(" Server started on port 8001");
        System.out.println("Sever corriendo en el puerto " + port);

        int decition;
        while (true) {
            System.out.println("Digitar 1 para entrada de un nuevo tag");
            decition = sc.nextInt();
            if (decition == 1) {
                sendRequest("http://127.0.0.1:5000/billing");
                System.out.println("Request sended!!!!!");
            }
        }
    }

    public static void sendRequest(String urlString) throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String jsonInputString = "{\"tag\":\"123456789\",\"plate\":\"rfd123\" }";
        
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        // get the response
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }

    private static class MyHttpHandler implements HttpHandler {

        /*
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestParamValue = null;
            if ("GET".equals(httpExchange.getRequestMethod())) {
                requestParamValue = handleGetRequest(httpExchange);
            } else if ("POST".equals(httpExchange)) {
                //requestParamValue = handlePostRequest(httpExchange);
                System.out.println("Entro por Post");
            }
            handleResponse(httpExchange, requestParamValue);
        }*/
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            String routes = httpExchange.getRequestURI().toString();

            switch (routes) {
                case "/test":
                    handleRouteTest(httpExchange);
                    break;
                case "/discountBalance":
                    handleRouteDiscountBalance(httpExchange);
                    break;
                default:
                    handleRouteNotDefined(httpExchange);
            }
        }
        
        public void sendResponseJson(HttpExchange httpExchange, String response) throws IOException{
            httpExchange.getResponseHeaders().set("Content-Type", "appication/json");
            httpExchange.sendResponseHeaders(200, response.length());
            System.out.println(httpExchange.getResponseBody());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        public void handleRouteTest(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
            System.out.println("llamado por la ruta" + httpExchange.getRequestURI().toString());

            //TODO Aqui se deveria sacar lso parametros del Get
            String response = "{\"msg\":\"respuesta para la ruta get test\"}";

            httpExchange.getResponseHeaders().set("Content-Type", "appication/json");
            httpExchange.sendResponseHeaders(200, response.length());
            System.out.println(httpExchange.getResponseBody());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        public void handleRouteDiscountBalance(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
            System.out.println("llamado por la ruta" + httpExchange.getRequestURI().toString());

            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String bodyJson = br.readLine();

            System.out.println("this is the Json recived: " + bodyJson);
            
            // TODO some process for get out the tag of the list and write the balance
            
            //if there is no error send a goot response

            String response = "{\"tag\":\"123456789\",\"plate\":\"rfd123\" }";

            sendResponseJson(httpExchange, response);
        }

        public void handleRouteNotDefined(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
            System.out.println("llamado por la ruta" + httpExchange.getRequestURI().toString());

            //TODO Aqui se deveria sacar lso parametros del Get
            String response = "{\"msg\":\"error 404 ruta no definida\"}";

            httpExchange.getResponseHeaders().set("Content-Type", "appication/json");
            httpExchange.sendResponseHeaders(404, response.length());
            System.out.println(httpExchange.getResponseBody());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        /*
        private String handleGetRequest(HttpExchange httpExchange) {
            String context = httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
            if(context == null){
                return context;
            }
            return httpExchange.getRequestURI().toString();
        }*/
        //private String handlePostRequest(HttpExchange httpExchange){
        //}

        /*
        private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").append("<body>").append("<h1>").append("Hello ").append(requestParamValue)
                    .append("</h1>").append("</body>").append("</html>");
            
            // encode HTML content
            //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            String str = "<h1>HOLA</h1>";
            // this line is a must
            httpExchange.sendResponseHeaders(200, str.length());
            outputStream.write(str.getBytes());
            outputStream.flush();
            outputStream.close();
        }*/
    }
}
