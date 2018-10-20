package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.Logger;
import core.UserManager;
import httpHandlers.CommonHandler;
import javafx.util.Pair;
import networking.Network;
import org.json.JSONObject;

public class DrawHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int gameId = reqJson.getInt("gameId"),
					token = reqJson.getInt("token");
			String decision = reqJson.getString("decision");

			Logger.log("received data from:\n\t" + reqJson);

			DataCache.putDrawDecision(gameId, token, decision);
			JSONObject draw = DataCache.getDraw(gameId);
			int oppToken = DataCache.getOpponentToken(gameId, token);

			if (drawReady(draw, token, oppToken)) { // from , to, 2 decisions & attacker token
				//handle draw
				Pair<Integer, String>[] decisions = new Pair[2];

				int senderIndex = draw.getInt("attacker") == token ? 0 : 1;
				decisions[senderIndex] = new Pair<>(token, decision);
				decisions[1 - senderIndex] = new Pair<>(oppToken, draw.getString(oppToken + ""));

				int result = DataCache.battle(gameId,
						draw.getJSONObject("from").put("type", decisions[0].getValue()),
						draw.getJSONObject("to").put("type", decisions[1].getValue()));

				var otherDecision = token == decisions[0].getKey() ? decisions[1] : decisions[0]; //get other decision from array, to return to sender
				JSONObject resBody = new JSONObject()
						.put("result", result)
						.put("opponent", otherDecision.getValue())
						.put("type", "draw")
						.put("success", true);

				CommonHandler.resSuccess(request, resBody);//response to sender
				Network.Game.unicast(oppToken, resBody.put("opponent", decision));//send to other player

				//if result == 0 then prepare for another draw, else clear draw object
				if (result == 0) DataCache.getDraw(gameId).put("attacker", draw.getInt("attacker")); //put attacker from old draw object in new draw object
				else DataCache.clearDraw(gameId); //clear draw cache

			} else {
				CommonHandler.resSuccess(request, new JSONObject().put("type", "draw"));//response success, to wait
				Network.Game.unicast(oppToken, new JSONObject().put("type", "draw").put("msg", UserManager.instance.get(token).getName() + " is ready"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}

	/**
	 * check whether json object has all necessary elements
	 * <p>
	 * the elements required are,
	 * 'attacker' token
	 * 'from' object
	 * 'to' object
	 * two player tokens
	 *
	 * @param draw   json object which we check
	 * @param token1 first player's token
	 * @param token2 second player's token
	 * @return true if all elements are correct, ekse false
	 */
	private boolean drawReady(JSONObject draw, int token1, int token2) {
		return draw.length() == 5
				&& draw.has("attacker")
				&& draw.has("from")
				&& draw.has("to")
				&& draw.has(token1 + "")
				&& draw.has(token2 + "");
	}
}
