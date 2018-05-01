package httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CommonHandler {
    public static JSONObject readRequestJson(HttpExchange request) throws IOException, JSONException {
        int contentLength = Integer.parseInt(request.getRequestHeaders().getFirst("Content-Length"));

        byte[] buffer = new byte[contentLength];
        request.getRequestBody().read(buffer);

        return new JSONObject(new String(buffer));
    }

    public static void resSuccess(HttpExchange request, JSONObject success) throws IOException {
        byte[] response = success.toString().getBytes();
        request.sendResponseHeaders(200, response.length);
        request.getResponseBody().write(response);
        request.close();
    }

    public static void resSuccess(HttpExchange request) throws IOException, JSONException {
        resSuccess(request, new JSONObject().put("success", true));
    }

    public static void resError(HttpExchange request, String msg) throws JSONException, IOException {
        byte[] response = new JSONObject()
                .put("success", false)
                .put("msg", msg)
                .toString().getBytes();

        request.sendResponseHeaders(400, response.length);
        request.getResponseBody().write(response);
        request.close();
    }
}