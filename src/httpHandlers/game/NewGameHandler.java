package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class NewGameHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int token = reqJson.getInt("token");
			int gameId = reqJson.getInt("gameId");
			int targetToken = DataCache.getOpponentToken(gameId, token);
			String type = reqJson.optString("req_type");
			String name = UserManager.instance.get(token).getName();

			if ("new_game".equalsIgnoreCase(type)) {
				JSONObject invite = new JSONObject()
						.put("name", name)
						.put("type", "new_game");

				//pass to opponent
				Network.Game.unicast(targetToken, invite);
				CommonHandler.resSuccess(request);
			} else if ("answer".equalsIgnoreCase(type)) {
				boolean accept = reqJson.getBoolean("accept");
				JSONObject answer = new JSONObject()
						.put("type", "new_game_answer")
						.put("name", name)
						.put("accept", accept);

				Network.Game.unicast(targetToken, answer);
				CommonHandler.resSuccess(request);

				DataCache.removeGame(gameId); //remove old game object
				if (accept) DataCache.addGame(gameId, targetToken, token); //create new is accept
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
