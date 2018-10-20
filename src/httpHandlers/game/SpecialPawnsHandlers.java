package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.Logger;
import httpHandlers.CommonHandler;
import org.json.JSONObject;

public enum SpecialPawnsHandlers implements HttpHandler {
	Flag, Trap;

	@Override
	public void handle(HttpExchange request) {
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
			if (square.getString("type").equals("none") && square.getInt("owner") == token) { //not flag & valid owner to position
				String pawnName = this.toString().toLowerCase();
				square.put("type", pawnName); //flag or trap
				CommonHandler.resSuccess(request);
				Logger.log("player " + token + " selected " + pawnName + " at " + row + ":" + col);
			} else {
				CommonHandler.resError(request, "invalid position");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
