package mine.block.spotify.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class NotImplementedHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(501, -1);
        exchange.close();
    }
}
