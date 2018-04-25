package httpHandlers.game;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import files.DataCache;
import httpHandlers.CommonHandler;
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
            if(draw.length() == 2) {
                for (Iterator it = draw.keys(); it.hasNext(); ) {
                    int t = Integer.parseInt(it.next().toString());
                    Network.Game.unicast(t, draw.toString());
                }

                DataCache.clearDraw(gameId);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
