package httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CommonHandler {
	public static JSONObject readRequestJson(HttpExchange request) throws IOException, JSONException {
		if (!request.getRequestMethod().equalsIgnoreCase("POST")) throw new IOException("invalid method, only POST is allowed");

		int contentLength = Integer.parseInt(request.getRequestHeaders().getFirst("Content-Length"));

		byte[] buffer = new byte[contentLength];
		request.getRequestBody().read(buffer);

		return new JSONObject(new String(buffer));
	}

	public static void resSuccess(HttpExchange request, JSONObject success) throws IOException, JSONException {
		byte[] response = success.put("success", true).toString().getBytes();
		response(request, response);
	}

	public static void resSuccess(HttpExchange request) throws IOException, JSONException {
		resSuccess(request, new JSONObject().put("success", true));
	}

	public static void resError(HttpExchange request, String msg) throws JSONException, IOException {
		byte[] response = new JSONObject()
				.put("success", false)
				.put("msg", msg)
				.toString().getBytes();
		response(request, response);
	}

	private static void response(HttpExchange request, byte[] body) throws IOException {
		request.sendResponseHeaders(200, body.length);
		request.getResponseBody().write(body);
		request.close();
	}
}
