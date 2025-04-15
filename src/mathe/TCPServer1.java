package mathe;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer1 {
    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(22333)) {
            System.out.println("ready to connect");

            while (true) {
                Socket client = server.accept();
                new ClientHandler(client).start();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}