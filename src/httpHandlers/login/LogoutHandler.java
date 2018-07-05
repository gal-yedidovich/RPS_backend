package httpHandlers.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class LogoutHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);
			int token = reqJson.optInt("token");
			UserManager.instance.removeUser(token);
			Network.Lobby.unRegisterClient(token);
			Network.Game.unRegisterClient(token);

			CommonHandler.resSuccess(request, new JSONObject().put("msg", "goodbye"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
