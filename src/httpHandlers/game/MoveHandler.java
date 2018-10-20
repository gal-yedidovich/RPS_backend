package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
import core.Logger;
import core.UserManager;
import httpHandlers.CommonHandler;
import networking.Network;
import org.json.JSONObject;

public class MoveHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			JSONObject reqJson = CommonHandler.readRequestJson(request);
			int gameId = reqJson.getInt("gameId");
			int token = reqJson.getInt("token");
			String name = UserManager.instance.get(token).getName();
			Logger.log(token + ", " + name + " has moved\n\t" + reqJson);

			JSONObject gameJson = DataCache.getGame(gameId);

			if (gameJson.getInt("turn") == token) { //if player's turn - execute
				int receiverToken = DataCache.getOpponentToken(gameId, token);

				if (receiverToken != -1) { //if valid token process move

					JSONObject req_to = reqJson.getJSONObject("to"), //request coordinates
							req_from = reqJson.getJSONObject("from"); //request coordinates

					int from_row = req_from.getInt("row"),
							from_col = req_from.getInt("col"),
							to_row = req_to.getInt("row"),
							to_col = req_to.getInt("col");

					//if player 1 - rotate positions
					if (gameJson.getInt("player1") == token) {
						int rotation = DataCache.BOARD_SIZE - 1;
						from_row = rotation - from_row;
						from_col = rotation - from_col;
						to_row = rotation - to_row;
						to_col = rotation - to_col;
					}

					JSONObject to = gameJson.getJSONObject("" + to_row + to_col), //DB coordinates
							from = gameJson.getJSONObject("" + from_row + from_col);//DB coordinates
					int targetOwner = to.getInt("owner");

					JSONObject responseBody = new JSONObject().put("success", true);
					if (targetOwner == token) {
						Logger.log(targetOwner + " - " + token);
						throw new RuntimeException("invalid target, can't move on to own RPS");
					} else if (targetOwner == -1) { //empty -> move
						DataCache.move(from, to);
						gameJson.put("turn", receiverToken); //toggle move
					} else { //battle

						if (to.getString("type").equals("flag")) { //game over
							int winner = from.getInt("owner");
							gameJson.put("winner", winner);
							responseBody.put("winner", winner);
							reqJson.put("winner", winner);
							gameJson.put("turn", -1); //no more turns
							Logger.log("game over, " + winner + " won");
						} else {
							gameJson.put("turn", receiverToken); //toggle move
						}

						if (to.getBoolean("isHidden")) {
							to.put("isHidden", false);
							responseBody.put("s_type", to.getString("type")); //add type to response
						}

						if (from.getBoolean("isHidden")) {
							from.put("isHidden", false);
							reqJson.put("s_type", from.getString("type")); //add type before passing data to opponent
						}

						int result = DataCache.battle(gameId, from, to); //resolve battle and put result
						responseBody.put("battle", result);
						reqJson.put("battle", result);

						if (result == 0) { //draw
							DataCache.getDraw(gameId).put("attacker", token); //set sender as attacker
						}
					}

					//pass data & respond
					Network.Game.unicast(receiverToken, reqJson.toString());
					CommonHandler.resSuccess(request, responseBody);


				} else { //invalid token, return with error response
					Logger.log("error - invalid token at game: " + gameId);
					CommonHandler.resError(request, "invalid request");
				}
			} else {
				//not player's turn
				Logger.log("error - token move not at their turn at game: " + gameId);
				CommonHandler.resError(request, "not your turn");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.close();
		}
	}
}
