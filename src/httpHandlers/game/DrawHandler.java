package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.UserManager;
import httpHandlers.CommonHandler;
import javafx.util.Pair;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DrawHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) throws IOException {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);

			int gameId = reqJson.getInt("gameId"),
					token = reqJson.getInt("token");
			String decision = reqJson.getString("decision");

			System.out.println("received data from:\n\t" + reqJson);

			DataCache.putDrawDecision(gameId, token, decision);
			JSONObject draw = DataCache.getDraw(gameId);
			int oppToken = DataCache.getOpponentToken(gameId, token);

			if (draw.length() == 5) { // from , to, 2 decisions & attacker token
				//handle draw
				Pair<Integer, String>[] decisions = new Pair[2];

//                int i = 0;
//                for (Iterator it = draw.keys(); it.hasNext(); i++) {
//                    int t = Integer.parseInt(it.next().toString());
//                    decisions[i] = new Pair<>(t, draw.getString("" + t));
//                }
				int senderIndex = draw.getInt("attacker") == token ? 0 : 1;
				decisions[senderIndex] = new Pair<>(token, decision);
				decisions[1 - senderIndex] = new Pair<>(oppToken, draw.getString(oppToken + ""));

				//int result = DataCache.resolveBattle(decisions[0].getValue(), decisions[1].getValue());


				int result = DataCache.battle(gameId,
						draw.getJSONObject("from").put("type", decisions[0].getValue()),
						draw.getJSONObject("to").put("type", decisions[1].getValue()));

				var otherDecision = token == decisions[0].getKey() ? decisions[1] : decisions[0]; //get other decision from array
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
				Network.Game.unicast(oppToken, new JSONObject().put("msg", UserManager.instance.get(token).getName() + " is ready"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
