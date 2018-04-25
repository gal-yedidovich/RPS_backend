package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import files.DataCache;
import httpHandlers.CommonHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RandomHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        try {
            JSONObject reqJson = CommonHandler.readRequestJson(request);
            int token = reqJson.getInt("token"),
                    gameId = reqJson.getInt("gameId");

            JSONObject game = DataCache.getGame(gameId);
            JSONObject rps;
            if (game.getInt("player1") == token) { //random RPS for player 1
                rps = DataCache.randomRPS1(gameId);
            } else if (game.getInt("player2") == token) { //random RPS for player 2
                rps = DataCache.randomRPS2(gameId);
            } else {
                CommonHandler.resError(request, "unknown token in game ");
                return;
            }

            CommonHandler.resSuccess(request, rps);

        } catch (JSONException e) {
            e.printStackTrace();
            request.close();
        }
    }
}
