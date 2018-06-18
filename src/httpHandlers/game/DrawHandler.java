package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.DataCache;
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

            System.out.println("received data from:\n\t" + reqJson);

            DataCache.putDrawDecision(gameId, token, decision);
            JSONObject draw = DataCache.getDraw(gameId);
            if (draw.length() == 2) {
                //handle draw
                Pair<Integer, String>[] decisions = new Pair[2];

//                int i = 0;
//                for (Iterator it = draw.keys(); it.hasNext(); i++) {
//                    int t = Integer.parseInt(it.next().toString());
//                    decisions[i] = new Pair<>(t, draw.getString("" + t));
//                }
                int senderIndex = draw.getInt("attacker") == token ? 0 : 1;
                int oppToken = DataCache.getOpponentToken(gameId,token);
                decisions[senderIndex] = new Pair<>(token, decision);
                decisions[1-senderIndex] = new Pair<>(oppToken, draw.getString(oppToken+""));

                int result = DataCache.resolveBattle(decisions[0].getValue(), decisions[1].getValue());


//                if (result == 0) result = -1; //another draw
//                else if (result == 1) result = decisions[0].getKey();
//                else result = decisions[1].getKey();

                var otherDecision = token == decisions[0].getKey() ? decisions[1] : decisions[0]; //get other decision from array
                JSONObject resBody = new JSONObject()
                        .put("result", result)
                        .put("opponent", otherDecision.getValue())
                        .put("success", true);

                CommonHandler.resSuccess(request, resBody);//response to sender
                Network.Game.unicast(otherDecision.getKey(), resBody.put("opponent", decision));//send to other player
                DataCache.clearDraw(gameId); //clear draw cache
            } else {
                CommonHandler.resSuccess(request);//response to wait
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            request.close();
        }
    }
}
