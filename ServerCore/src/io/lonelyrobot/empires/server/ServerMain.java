package io.lonelyrobot.empires.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class ServerMain {
  public static void main(String[] args) throws Exception {
    int port = 51234;
    System.out.println("Starting simple server on port " + port + " — " + LocalDateTime.now());
    try (ServerSocket server = new ServerSocket(port)) {
      while (true) {
        Socket client = server.accept();
        System.out.println("Accepted connection from " + client.getRemoteSocketAddress());
        handleClient(client);
      }
    }
  }

  private static void handleClient(Socket socket) {
    new Thread(() -> {
      try (Socket s = socket;
           BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
           PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

        out.println("Welcome to Interstellar Empires test server");
        String line;
        while ((line = in.readLine()) != null) {
          System.out.println("<- " + line);
          if (line.trim().equalsIgnoreCase("quit")) {
            out.println("bye");
            break;
          }
          out.println("echo: " + line);
        }
      } catch (Exception e) {
        System.err.println("Client handler error: " + e.getMessage());
      }
    }).start();
  }
}
