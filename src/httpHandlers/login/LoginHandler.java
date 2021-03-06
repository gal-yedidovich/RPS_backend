package httpHandlers.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.Logger;
import core.UserManager;
import httpHandlers.CommonHandler;
import models.User;
import org.json.JSONObject;

import java.util.Random;

public class LoginHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject json = CommonHandler.readRequestJson(request);
			String name = json.getString("name");
			int token = new Random().nextInt(10_000_000) + 10_000_000; //from 10 mil to 20 mil
			CommonHandler.resSuccess(request, new JSONObject().put("success", true).put("token", token));

			UserManager.instance.putUser(token, new User()
					.setName(name)
					.setToken(token));
			Logger.log("new client - token " + token + ", " + name);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
