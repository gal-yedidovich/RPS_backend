import com.sun.net.httpserver.HttpServer;
import files.DataCache;
import httpHandlers.game.*;
import httpHandlers.login.*;
import networking.Network;
import httpHandlers.lobby.*;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;


import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //start HTTP servers
        try {
            var lobbyHttpServer = HttpServer.create(new InetSocketAddress(8003), 100);

            lobbyHttpServer.createContext("/login", new LoginHandler());
            lobbyHttpServer.createContext("/logout", new LogoutHandler());
            lobbyHttpServer.createContext("/lobby/players", new AllPlayersHandler());
            lobbyHttpServer.createContext("/lobby/invite", new InviteHandler());
            lobbyHttpServer.start();

            HttpServer gameServer = HttpServer.create(new InetSocketAddress(8004), 100);
            gameServer.createContext("/game/flag", new FlagHandler());
            gameServer.createContext("/game/trap", new TrapHandler());
            gameServer.createContext("/game/random", new RandomHandler());
            gameServer.createContext("/game/ready", new ReadyHandler());
            gameServer.createContext("/game/move", new MoveHandler());
            gameServer.createContext("/game/draw", new DrawHandler());
            gameServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //start lobby socket server
        new Thread(() -> {
            try {
                ServerSocket lobbyServer = new ServerSocket(15001);
                while (true) Network.Lobby.registerClient(lobbyServer.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //start game socket server
        new Thread(() -> {
            try {
                ServerSocket gameServer = new ServerSocket(15002);
                while (true)
                    Network.Game.registerClient(gameServer.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
