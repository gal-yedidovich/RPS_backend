package networking;


import core.UserManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;

public enum Network {
	Lobby {
		@Override
		public void unRegisterClient(int token) {
			super.unRegisterClient(token);
			//broadcast to lobby
			try {
				String msg = new JSONObject().put("type", "user_left").put("token", token).toString();
				broadcast(-1, msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}, Game;

	private HashMap<Integer, Socket> clients = new HashMap<>();

	public HashSet<Integer> getTokensSet() {
		return new HashSet<>(clients.keySet());
	}

	public void registerClient(Socket client) {
		try {
			var buffer = new byte[4];
			client.getInputStream().read(buffer);

			int token = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
			if (UserManager.instance.tokenExists(token)) {
				synchronized (this) {
					clients.put(token, client);
				}
				System.out.println("socket registered for " + token + " at " + this.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unRegisterClient(int token) {
		var s = clients.get(token);
		if (s != null && !s.isClosed()) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		synchronized (this) {
			clients.remove(token);
			UserManager.instance.removeUser(token);
		}
		System.out.println("socket removed from " + this.toString());
	}

	public void unicast(int token, String msg) {
		try {
			var data = msg.getBytes();
			var size = data.length;
			OutputStream out = clients.get(token).getOutputStream();
			System.out.println("uni-casting data, size: " + size);
			out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array()); //send size
			out.write(data); //send data
		} catch (IOException ignored) {
			System.out.println("error broadcasting to a socket client " + token + "- removing");
			unRegisterClient(token);
		}
	}

	public void unicast(int token, JSONObject json) {
		unicast(token, json.toString());
	}

	public void broadcast(int token, String msg) {
		synchronized (this) {
			for (int t : clients.keySet()) {
				if (t == token) continue; //skip sender
				unicast(t, msg);
			}
		}
	}

	public void sendHeartbeat() {
//		clients.forEach((t, s) -> {
//
//		});
		getTokensSet().forEach(token -> unicast(token, "{\"type\": \"heartbeat\"}"));
	}
}
