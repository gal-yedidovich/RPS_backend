package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import models.User;
import networking.Network;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * the handler will response with list of all users (except the sender)
 */
public class AllPlayersHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		if (request.getRequestMethod().equals("POST")) {
			try {
				JSONObject reqJson = CommonHandler.readRequestJson(request);
				int senderToken = reqJson.getInt("token");

				JSONArray arr = new JSONArray();
				for (int t : Network.Lobby.GetTokensSet()) { //get users in lobby
					if (t == senderToken) continue; //skip self

					User current = UserManager.instance.get(t);
					if (current != null)
						arr.put(new JSONObject()
								.put("name", current.getName())
								.put("token", t)
						);
				}

				CommonHandler.resSuccess(request, new JSONObject().put("player_list", arr));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				request.close(); //clean up
			}
		}
	}
}
