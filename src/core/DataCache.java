package core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DataCache {
	public static final int BOARD_SIZE = 7;
	private static final JSONObject games = new JSONObject();
	private static final HashMap<Integer,Integer> tokensGames = new HashMap<>();

	public static void addGame(int gameId, int token1, int token2) throws JSONException {
		JSONObject game;
		games.put(gameId + "", game = new JSONObject()
				.put("player1", token1)
				.put("player2", token2)
				.put("turn", token1) //token1 plays first
		);

		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				game.put("" + row + col, new JSONObject()
						.put("type", "none") //all squares start without type
						.put("owner", row < 2 ? token1 : row >= BOARD_SIZE - 2 ? token2 : -1) //token of owner or -1 if none
						.put("isHidden", row < 2 || row >= BOARD_SIZE - 2) //2 first & last rows are hidden at beginning
				);
			}
		}

		tokensGames.put(token1, gameId);
		tokensGames.put(token2, gameId);
	}

	public static void removeGame(int gameID) throws JSONException {
		JSONObject game = getGame(gameID);
		tokensGames.remove(game.getInt("player1"));
		tokensGames.remove(game.getInt("player2"));

		games.remove(gameID + "");
	}

	public static JSONObject getDraw(int gameId) throws JSONException {
		JSONObject game = getGame(gameId),
				draw = game.optJSONObject("draw");

		if (draw == null) draw = game.put("draw", new JSONObject()).getJSONObject("draw");
		return draw;
	}

	public static void putDrawDecision(int gameId, int token, String decision) throws JSONException {
		getDraw(gameId).put(token + "", decision);
	}

	public static void clearDraw(int gameId) throws JSONException {
		getGame(gameId).remove("draw");
	}

	public static JSONObject getGame(int gameId) throws JSONException {
		return games.getJSONObject(gameId + "");
	}

	public static int getOpponentToken(int gameId, int token) throws JSONException {
		JSONObject game = getGame(gameId);
		int p1 = game.getInt("player1"), p2 = game.getInt("player2");
		if (p1 == token) return p2;
		else if (p2 == token) return p1;
		return -1;
	}

	public static void move(JSONObject from, JSONObject to) throws JSONException {
		to.put("type", from.getString("type"))
				.put("owner", from.getInt("owner"))
				.put("isHidden", from.getBoolean("isHidden"));
		from.put("type", "none")
				.put("owner", -1)
				.put("isHidden", false);
	}

	public static JSONObject randomRPS1(int gameId) throws JSONException {
		return randomRps(getGame(gameId), true);
	}

	public static JSONObject randomRPS2(int gameId) throws JSONException {
		return randomRps(getGame(gameId), false);
	}

	private static JSONObject randomRps(JSONObject game, boolean up) throws JSONException {
		int row = up ? 0 : BOARD_SIZE - 2;

		ArrayList<JSONObject> rps = new ArrayList<>((BOARD_SIZE - 1) * 2); //number of RPS

		JSONObject result = new JSONObject(); //json object to return to caller - it will hold all rps

		//get rps to random (all object that are not trap/flag)
		for (int i = row; i < row + 2; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				JSONObject s = game.getJSONObject("" + i + j);
				String type = s.getString("type");
				if (!type.equalsIgnoreCase("flag") && !type.equalsIgnoreCase("trap")) {
					rps.add(s);
					int r = i, c = j; //copy values to avoid ruining loops
					if (up) { //if up -> rotate position
						r = BOARD_SIZE - i - 1;
						c = BOARD_SIZE - j - 1;
					}
					result.put("" + r + c, s);
				}
			}
		}

		//put random RPS in json
		String[] types = {"rock", "paper", "scissors"};
		Random rnd = new Random();
		for (String type : types) {
			for (int i = 0; i < 4; i++) {
				int index = rnd.nextInt(rps.size());
				rps.get(index).put("type", type); //this will put in reference the new type - result object will be updated too (reference type)
				rps.remove(index);
			}
		}

		return result; //return final result
	}

	public static int battle(int gameId, JSONObject from, JSONObject to) throws JSONException {
		int result = resolveBattle(from.getString("type"), to.getString("type"));

		switch (result) {
			case 1: //won - move to target
				move(from, to);
				break;
			case -1: //lost - kill attacker
				from.put("type", "none")
						.put("owner", -1)
						.put("isHidden", false);
				break;
			case 0://tie - draw
				getGame(gameId).put("draw", new JSONObject().put("to", to).put("from", from)); //init draw object
				break;
		}

		return result;
	}

	private static int resolveBattle(String attacker, String target) {
		if (attacker.equalsIgnoreCase("flag")
				|| attacker.equalsIgnoreCase("trap")) throw new RuntimeException("flag/trap cannot attack");

		return battleResults.get(attacker.toLowerCase() + "-" + target.toLowerCase());
	}

	public static int userInAGame(int token){
		return tokensGames.get(token);
	}

	private static final HashMap<String, Integer> battleResults = new HashMap<>(15);

	static {
		//flag
		battleResults.put("rock-flag", 1);
		battleResults.put("paper-flag", 1);
		battleResults.put("scissors-flag", 1);
		//trap
		battleResults.put("rock-trap", -1);
		battleResults.put("paper-trap", -1);
		battleResults.put("scissors-trap", -1);
		//RPS
		//draw
		battleResults.put("rock-rock", 0);
		battleResults.put("paper-paper", 0);
		battleResults.put("scissors-scissors", 0);
		//win
		battleResults.put("rock-scissors", 1);
		battleResults.put("paper-rock", 1);
		battleResults.put("scissors-paper", 1);
		//lose
		battleResults.put("rock-paper", -1);
		battleResults.put("paper-scissors", -1);
		battleResults.put("scissors-rock", -1);
	}
}
