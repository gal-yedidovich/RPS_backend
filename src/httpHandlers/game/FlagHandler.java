package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import httpHandlers.CommonHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FlagHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) throws IOException {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int token = reqJson.getInt("token"),
					gameId = reqJson.getInt("gameId"),
					row = reqJson.getInt("row"),
					col = reqJson.getInt("col");

			JSONObject game = DataCache.getGame(gameId);
			if (game.getInt("player1") == token) { //rotate position on board
				row = DataCache.BOARD_SIZE - row - 1;
				col = DataCache.BOARD_SIZE - col - 1;
			}

			JSONObject square = game.getJSONObject("" + row + col);
			if (square.getInt("owner") == token) { //valid owner to position
				square.put("type", "flag");
				CommonHandler.resSuccess(request);
				System.out.println("player " + token + " selected flag at " + row + ":" + col);
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
