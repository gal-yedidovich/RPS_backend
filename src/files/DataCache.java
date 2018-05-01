package files;

import javafx.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class DataCache {
    public static final int BOARD_SIZE = 7;
    private static final JSONObject games = new JSONObject();

    public static void addGame(int gameId, int token1, int token2) throws JSONException {
        JSONObject game;
        games.put(gameId + "", game = new JSONObject()
                .put("player1", token1)
                .put("player2", token2)
                .put("turn", token1) //token1 play's first
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
                if (!type.equals("flag") && !type.equals("trap")) {
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
                getGame(gameId).put("draw", new JSONObject()); //init draw object
                break;
        }

        return result;
    }

    public static int resolveBattle(String type1, String type2) {
        Types t1 = Types.valueOf(type1);
        if (t1 == Types.flag || t1 == Types.trap) throw new RuntimeException("flag/trap cannot attack");

        return t1.attack(Types.valueOf(type2));
    }

    enum Types {
        flag, trap, rock, paper, scissors; //used from Types.valueOf(target);

        int attack(Types t) {
            if (t == flag) return 1;
            if (t == trap) return -1;

            int diff = this.ordinal() - t.ordinal();

            if (diff % 2 == 0) return diff / -2;
            else return diff;
        }
    }
}
