package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChatHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        if (request.getRequestMethod().equals("POST")) {
            try {
                var reqJson = CommonHandler.readRequestJson(request);
                var token = reqJson.getInt("token");
                var msg = reqJson.getString("msg");

                CommonHandler.resSuccess(request);
                Network.Lobby.broadcast(token, new JSONObject()
                        .put("type", "msg")
                        .put("msg", msg).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
