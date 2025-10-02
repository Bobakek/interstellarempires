package io.lonelyrobot.empires.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTest {
  public static void main(String[] args) throws Exception {
    String host = "localhost";
    int port = 51234;
    System.out.println("Connecting to " + host + ":" + port);
    try (Socket s = new Socket(host, port);
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

      // read welcome
      String welcome = in.readLine();
      System.out.println("Server: " + welcome);

      // send a message
      out.println("hello server");
      System.out.println("Client -> hello server");

      // read response
      String resp = in.readLine();
      System.out.println("Server: " + resp);

      // send quit
      out.println("quit");
      System.out.println("Client -> quit");

      // read final
      String finalResp = in.readLine();
      System.out.println("Server: " + finalResp);
    }
  }
}
