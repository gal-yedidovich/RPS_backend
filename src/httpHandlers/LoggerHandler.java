package httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import core.Logger;
import org.json.JSONException;

import java.io.IOException;

public class LoggerHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange request) {
		try {
			CommonHandler.resSuccess(request, Logger.getLog());
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			Logger.log("error on requesting logs");
		} finally {
			request.close();
		}
	}
}
