package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.Logger;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

import java.util.Random;

public class InviteHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);
			int senderToken = reqJson.getInt("sender_token"),
					targetToken = reqJson.getInt("target_token");

			if ("invite".equals(reqJson.optString("req_type"))) { //user invite to play
				String senderName = UserManager.instance.get(senderToken).getName();

				//generate game id
				int gameNum = new Random().nextInt(1_000_000) + 100_000;

				//pass invitation to target receiver
				JSONObject resJson = new JSONObject()
						.put("type", "invite")
						.put("sender_name", senderName)
						.put("game_id", gameNum)
						.put("sender_token", senderToken);

				Network.Lobby.unicast(targetToken, resJson);

				//response sender
				CommonHandler.resSuccess(request, new JSONObject().put("game_id", gameNum));
			} else { //receiver responding to invite
				if (reqJson.optBoolean("accept")) {
					//init Game Server
					Logger.log("Init game");
					DataCache.addGame(reqJson.getInt("game_id"), senderToken, targetToken);
				}

				//pass json to sender
				reqJson.put("name", UserManager.instance.get(targetToken).getName());
				Network.Lobby.unicast(senderToken, reqJson.toString());

				//response to http request
				CommonHandler.resSuccess(request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}

	}
}
