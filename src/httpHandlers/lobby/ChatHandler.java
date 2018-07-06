package httpHandlers.lobby;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class ChatHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			var reqJson = CommonHandler.readRequestJson(request);
			var token = reqJson.getInt("token");
			var msg = reqJson.getString("msg");
			var time = reqJson.getLong("time");
			var name = UserManager.instance.get(token).getName();

			CommonHandler.resSuccess(request);
			Network.Lobby.broadcast(token, new JSONObject()
					.put("type", "msg")
					.put("time", time)
					.put("name", name)
					.put("msg", msg).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
