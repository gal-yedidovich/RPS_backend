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
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);
			int senderToken = reqJson.getInt("token");
			String name = UserManager.instance.get(senderToken).getName();

			JSONArray arr = new JSONArray();
			for (int t : Network.Lobby.getTokensSet()) { //get users in lobby
				if (t == senderToken) continue; //skip self

				User current = UserManager.instance.get(t);
				if (current != null)
					arr.put(new JSONObject()
							.put("name", current.getName())
							.put("token", t)
					);
				else UserManager.instance.removeUser(t);

				//broadcast new user
				JSONObject json = new JSONObject().put("type", "new_user")
						.put("name", name)
						.put("token", senderToken);

				Network.Lobby.broadcast(senderToken, json.toString());
			}

			CommonHandler.resSuccess(request, new JSONObject().put("player_list", arr));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close(); //clean up
		}
	}
}
