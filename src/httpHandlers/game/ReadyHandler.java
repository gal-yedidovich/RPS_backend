package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ReadyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        try {
            JSONObject reqJson = CommonHandler.readRequestJson(request);

            int token = reqJson.getInt("token"),
                    gameId = reqJson.getInt("gameId");

            Network.Game.unicast(DataCache.getOpponentToken(gameId, token), new JSONObject().put("type", "opponent ready"));

            CommonHandler.resSuccess(request, new JSONObject().put("success", true).put("turn", DataCache.getGame(gameId).getInt("turn")));
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            request.close();
        }
    }
}
