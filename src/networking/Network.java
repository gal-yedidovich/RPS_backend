package networking;


import core.DataCache;
import core.DispatchQueue;
import core.Logger;
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
		public void closeSocket(int token) {
			super.closeSocket(token);
			//broadcast to lobby
			try {
				String msg = new JSONObject().put("type", "user_left").put("token", token).toString();
				broadcast(-1, msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}, Game {
		@Override
		public void closeSocket(int token) {
			super.closeSocket(token);
//			try {
//				int gameId = DataCache.userInAGame(token);
//				if (gameId != 0) {
//					int opponent = DataCache.getOpponentToken(gameId, token);
//					String name = UserManager.instance.get(token).getName();
//					unicast(opponent, new JSONObject().put("type", "forfeit").put("name", name));
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
		}
	};

	private HashMap<Integer, Socket> clients = new HashMap<>();
	private DispatchQueue queue = new DispatchQueue();

	public HashSet<Integer> getTokensSet() {
		return new HashSet<>(clients.keySet());
	}

	public void registerClient(Socket client) {
		try {
			var buffer = new byte[4];
			client.getInputStream().read(buffer);

			int token = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
			if (UserManager.instance.tokenExists(token)) {
				clients.put(token, client);
				Logger.log("socket registered for " + token + " at " + this.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * close socket of user token, sed to remove socket when players go to game or back
	 *
	 * @param token key to socket
	 */
	public void closeSocket(int token) {
		var s = clients.remove(token);
		if (s != null && !s.isClosed()) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * remove user client from server, close the socket then remove token from cache
	 *
	 * @param token key to socket & user
	 */
	public void unRegisterClient(int token) {
		closeSocket(token);

		UserManager.instance.removeUser(token);
		Logger.log("socket removed from " + this.toString());
	}

	public void unicast(int token, String msg) {
		queue.add(() -> {
			try {
				var data = msg.getBytes();
				var size = data.length;
				OutputStream out = clients.get(token).getOutputStream();
				out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array()); //send size
				out.write(data); //send data
			} catch (Exception e) {
				Logger.log("error writing to a socket client " + token + " - removing");
				Logger.log('\t' + e.getMessage());
				closeSocket(token);
			}
		});
	}

	public void unicast(int token, JSONObject json) {
		unicast(token, json.toString());
	}

	public void broadcast(int token, String msg) {
		for (int t : getTokensSet()) {
			if (t == token) continue; //skip sender
			unicast(t, msg);
		}
	}

	public void sendHeartbeat() {
		broadcast(-1, "{\"type\": \"heartbeat\"}");
	}
}

