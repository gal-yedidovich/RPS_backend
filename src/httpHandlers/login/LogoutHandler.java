package httpHandlers.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        if (request.getRequestMethod().equals("POST")) {
            try {
                JSONObject reqJson = CommonHandler.readRequestJson(request);
                int token = reqJson.optInt("token");
                UserManager.instance.removeUser(token);
                Network.Lobby.unRegisterClient(token);

                byte[] msg = new JSONObject().put("msg", "goodbye").toString().getBytes();
                request.sendResponseHeaders(200, msg.length);
                request.getResponseBody().write(msg);

            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                request.close();
            }
        }
    }
}
