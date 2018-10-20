import com.sun.net.httpserver.HttpServer;
import core.Logger;
import httpHandlers.LoggerHandler;
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
		Logger.log("----------------------Server Starts------------------------------");
		//start HTTP servers
		try {
			var lobbyHttpServer = HttpServer.create(new InetSocketAddress(8003), 100);

			lobbyHttpServer.createContext("/login", new LoginHandler());
			lobbyHttpServer.createContext("/logout", new LogoutHandler());
			lobbyHttpServer.createContext("/logs", new LoggerHandler()); //development only

			lobbyHttpServer.createContext("/lobby/players", new AllPlayersHandler());
			lobbyHttpServer.createContext("/lobby/chat", new ChatHandler());
			lobbyHttpServer.createContext("/lobby/invite", new InviteHandler());
			lobbyHttpServer.start();

			HttpServer gameHttpServer = HttpServer.create(new InetSocketAddress(8004), 100);
			gameHttpServer.createContext("/game/flag", SpecialPawnsHandlers.Flag);
			gameHttpServer.createContext("/game/trap", SpecialPawnsHandlers.Trap);
			gameHttpServer.createContext("/game/random", new RandomHandler());
			gameHttpServer.createContext("/game/ready", new ReadyHandler());
			gameHttpServer.createContext("/game/move", new MoveHandler());
			gameHttpServer.createContext("/game/draw", new DrawHandler());
			gameHttpServer.createContext("/game/forfeit", new ForfeitHandler());
			gameHttpServer.createContext("/game/new", new NewGameHandler());
			gameHttpServer.start();


			Logger.log("HTTP servers running");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//start lobby socket server
		new Thread(() -> {
			try {
				ServerSocket lobbyServer = new ServerSocket(15001);
				Logger.log("Lobby socket running");
				while (true) Network.Lobby.registerClient(lobbyServer.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		//start game socket server
		new Thread(() -> {
			try {
				ServerSocket gameServer = new ServerSocket(15002);
				Logger.log("Game socket running");
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
