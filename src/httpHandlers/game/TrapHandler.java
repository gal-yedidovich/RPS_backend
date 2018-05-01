package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import files.DataCache;
import httpHandlers.CommonHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TrapHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        try {
            JSONObject reqJson = CommonHandler.readRequestJson(request);

            int token = reqJson.getInt("token"),
                    gameId = reqJson.getInt("gameId"),
                    row = reqJson.getInt("row"),
                    col = reqJson.getInt("col");

            JSONObject game = DataCache.getGame(gameId);
            if (game.getInt("player1") == token) {
                row = DataCache.BOARD_SIZE - row - 1;
                col = DataCache.BOARD_SIZE - col - 1;
            }

            JSONObject square = game.getJSONObject("" + row + col);
            if (square.getString("type").equals("none") && square.getInt("owner") == token) { //not flag & valid owner to position
                square.put("type", "trap");
                CommonHandler.resSuccess(request);
                System.out.println("player " + token + " selected trap at " + row + ":" + col);
            } else {
                CommonHandler.resError(request, "invalid position");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            request.close();
        }
    }
}
