package httpHandlers.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import models.User;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        if(request.getRequestMethod().equals("GET")){
            try {
                CommonHandler.resSuccess(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (request.getRequestMethod().equals("POST")) {
            try {
                JSONObject json = CommonHandler.readRequestJson(request);

                if ("login".equals(json.optString("req_type"))) {
                    String name = json.getString("name");

                    int token = new Random().nextInt(10_000_000) + 10_000_000; //from 10 mil to 20 mil

                    String res = new JSONObject()
                            .put("token", token)
                            .toString();
                    request.sendResponseHeaders(200, res.length());
                    request.getResponseBody().write(res.getBytes());
                    request.close();


                    UserManager.instance.putUser(token, new User()
                            .setName(name)
                            .setToken(token));
                    System.out.println("new client - token " + token);

                    //broadcast new user
                    json.remove("req_type");
                    json.put("type", "new_user")
                            .put("name", name)
                            .put("token", token);

                    Network.Lobby.broadcast(token, json.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                request.close();
            }
        }
    }
}
