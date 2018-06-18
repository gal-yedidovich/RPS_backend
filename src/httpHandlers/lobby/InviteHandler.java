package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class InviteHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        if (request.getRequestMethod().equals("POST")) {
            try {
                JSONObject reqJson = CommonHandler.readRequestJson(request);
                int senderToken = reqJson.getInt("sender_token"),
                        targetToken = reqJson.getInt("target_token");

                if ("invite".equals(reqJson.optString("req_type"))) { //user invite to play
                    String inviterName = UserManager.instance.get(senderToken).getName();

                    //generate game id
                    int gameNum = new Random().nextInt(1_000_000) + 100_000;
                    //TODO - do something with game num

                    //pass invitation to target receiver
                    JSONObject resJson = new JSONObject()
                            .put("type", "invite")
                            .put("sender_name", inviterName)
                            .put("game_id", gameNum)
                            .put("sender_token", senderToken);

                    Network.Lobby.unicast(targetToken, resJson.toString());

                    //response sender
                    byte[] responseData = new JSONObject()
                            .put("game_id", gameNum)
                            .toString()
                            .getBytes();
                    request.sendResponseHeaders(200, responseData.length);
                    request.getResponseBody().write(responseData);
                } else { //receiver responding to invite

                    //pass json to sender
                    Network.Lobby.unicast(senderToken, reqJson.toString());

                    //response to http request
                    byte[] responseData = new JSONObject()
                            .put("success", true)
                            .toString()
                            .getBytes();

                    request.sendResponseHeaders(200, responseData.length);
                    request.getResponseBody().write(responseData);

                    //init Game Server
                    System.out.println("Init game");
                    DataCache.addGame(reqJson.getInt("game_id"), senderToken, targetToken);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                request.close();
            }

        }
    }
}
