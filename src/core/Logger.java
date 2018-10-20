package core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;

public class Logger {
	private static Queue<String> lines = new ArrayDeque<>();

	public static synchronized void log(String msg) {
		try (var fos = new FileOutputStream("logs.txt", true);
		     var fos2 = new FileOutputStream("roller.txt")) {
			Calendar.getInstance().setTimeInMillis(System.currentTimeMillis());
			String date = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss").format(new Date()) + "\r\n";
			var dateBytes = date.getBytes();
			var log = msg + "\r\n\r\n";
			fos.write(dateBytes);
			fos.write(log.getBytes());

			lines.add(date);
			lines.add(log);
			while (lines.size() > 100) lines.remove();

			StringBuilder sb = new StringBuilder();
			for (String l : lines) sb.append(l);
			fos2.write(sb.toString().getBytes());

			System.out.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized JSONObject getLog() throws IOException, JSONException {
		try (var input = new BufferedReader(new InputStreamReader(new FileInputStream("roller.txt")))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = input.readLine()) != null) sb.append(line).append("\n");

			return new JSONObject().put("logs", sb.toString());
		}
	}
}
