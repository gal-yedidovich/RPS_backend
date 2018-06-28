package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

import java.io.IOException;

public class ReadyHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int token = reqJson.getInt("token"),
					gameId = reqJson.getInt("gameId");

			int r = DataCache.getOpponentToken(gameId, token);
			JSONObject d = new JSONObject().put("type", "opponent ready");
			Network.Game.unicast(r, d);

			CommonHandler.resSuccess(request, new JSONObject().put("success", true).put("turn", DataCache.getGame(gameId).getInt("turn")));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
