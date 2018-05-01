package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import files.DataCache;
import httpHandlers.CommonHandler;
import javafx.util.Pair;
import networking.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class DrawHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange request) throws IOException {
        try {
            JSONObject reqJson = CommonHandler.readRequestJson(request);

            int gameId = reqJson.getInt("gameId"),
                    token = reqJson.getInt("token");
            String decision = reqJson.getString("decision");

            DataCache.putDrawDecision(gameId, token, decision);
            JSONObject draw = DataCache.getDraw(gameId);
            if (draw.length() == 2) {
                //handle draw
                Pair<Integer, String>[] decisions = new Pair[2];

                int i = 0;
                for (Iterator it = draw.keys(); it.hasNext(); i++) {
                    int t = Integer.parseInt(it.next().toString());
                    decisions[i] = new Pair<>(t, draw.getString("" + t));
                }


                int result = DataCache.resolveBattle(decisions[0].getValue(), decisions[1].getValue());

                if (result == 0) result = -1; //another draw
                else if (result == 1) result = decisions[0].getKey();
                else result = decisions[1].getKey();

                JSONObject resBody = new JSONObject().put("draw", result).put("success", true);

                //response to sender
                CommonHandler.resSuccess(request, resBody);

                //send to other player
                int otherToken = token == decisions[0].getKey() ? decisions[0].getKey() : decisions[1].getKey();
                Network.Game.unicast(otherToken, resBody);

                DataCache.clearDraw(gameId);
            } else {
                //response to wait
                CommonHandler.resSuccess(request);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            request.close();
        }
    }
}
