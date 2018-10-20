package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.Logger;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class ForfeitHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int senderToken = reqJson.getInt("token"),
					gameId = reqJson.getInt("gameId");

			int targetToken = DataCache.getOpponentToken(gameId, senderToken);
			String name = UserManager.instance.get(senderToken).getName();
			Network.Game.unicast(targetToken, new JSONObject().put("type", "forfeit").put("name", name));
			CommonHandler.resSuccess(request);
			Logger.log(name + ", " + senderToken + " has forfeited game " + gameId);

			DataCache.removeGame(gameId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
