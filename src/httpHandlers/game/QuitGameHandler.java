package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class QuitGameHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int senderToken = reqJson.getInt("token"),
					gameId = reqJson.getInt("gameId");

			int targetToken = DataCache.getOpponentToken(gameId, senderToken);
			Network.Game.unicast(targetToken, new JSONObject().put("type", "forfeit"));
			CommonHandler.resSuccess(request);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			request.close();
		}
	}
}
