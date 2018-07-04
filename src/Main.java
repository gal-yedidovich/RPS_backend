import com.sun.net.httpserver.HttpServer;
import httpHandlers.game.*;
import httpHandlers.lobby.AllPlayersHandler;
import httpHandlers.lobby.ChatHandler;
import httpHandlers.lobby.InviteHandler;
import httpHandlers.login.LoginHandler;
import httpHandlers.login.LogoutHandler;
import networking.Network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Main {
	public static void main(String[] args) {
		//start HTTP servers
		try {
			var lobbyHttpServer = HttpServer.create(new InetSocketAddress(8003), 100);

			lobbyHttpServer.createContext("/login", new LoginHandler());
			lobbyHttpServer.createContext("/logout", new LogoutHandler());
			lobbyHttpServer.createContext("/lobby/players", new AllPlayersHandler());
			lobbyHttpServer.createContext("/lobby/chat", new ChatHandler());
			lobbyHttpServer.createContext("/lobby/invite", new InviteHandler());
			lobbyHttpServer.start();

			HttpServer gameServer = HttpServer.create(new InetSocketAddress(8004), 100);
			gameServer.createContext("/game/flag", SpecialPawnsHandlers.Flag);
			gameServer.createContext("/game/trap", SpecialPawnsHandlers.Trap);
			gameServer.createContext("/game/random", new RandomHandler());
			gameServer.createContext("/game/ready", new ReadyHandler());
			gameServer.createContext("/game/move", new MoveHandler());
			gameServer.createContext("/game/draw", new DrawHandler());
			gameServer.createContext("/game/draw", new QuitGameHandler());
			gameServer.start();

			System.out.println("HTTP servers running");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//start lobby socket server
		new Thread(() -> {
			try {
				ServerSocket lobbyServer = new ServerSocket(15001);
				System.out.println("Lobby socket running");
				while (true) Network.Lobby.registerClient(lobbyServer.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		//start game socket server
		new Thread(() -> {
			try {
				ServerSocket gameServer = new ServerSocket(15002);
				System.out.println("Game socket running");
				while (true) Network.Game.registerClient(gameServer.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

//		manager thread
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);

					Network.Lobby.sendHeartbeat();
					Network.Game.sendHeartbeat();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
