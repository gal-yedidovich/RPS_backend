package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import models.User;
import networking.Network;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * http handler to user first entering the lobby
 * the handler will response with list of all other users
 */
public class AllPlayersHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        if (request.getRequestMethod().equals("POST")) {
//            int contentLength = Integer.parseInt(request.getRequestHeaders().getFirst("Content-Length"));
//
//            byte[] buffer = new byte[contentLength];
//            int actualRead = request.getRequestBody().read(buffer);

            try {
//                if (actualRead <= 0) {
//                    //return error
//                    String jsonError = new JSONObject().put("msg", "invalid data").toString();
//                    request.sendResponseHeaders(400, jsonError.length());
//                    request.getResponseBody().write(jsonError.getBytes());
//                } else {
                JSONObject reqJson = CommonHandler.readRequestJson(request);
                int myToken = reqJson.getInt("token");

                JSONArray arr = new JSONArray();
                for (int t : Network.Lobby.GetTokensSet()) { //get users in lobby
                    if (t == myToken) continue; //skip self

                    User current = UserManager.instance.get(t);
                    if (current != null)
                        arr.put(new JSONObject()
                                .put("name", current.getName())
                                .put("token", t)
                        );
                }

                CommonHandler.resSuccess(request, new JSONObject().put("player_list", arr));

//                String response = new JSONObject().put("player_list", arr).toString();
//                request.sendResponseHeaders(200, response.length());
//                request.getResponseBody().write(response.getBytes());
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                request.close(); //clean up
            }
        }
    }
}
