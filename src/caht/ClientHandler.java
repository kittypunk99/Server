package caht;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {
    private static final String STZ_DAVOR = new String(new byte[]{0x0b, 0x1b, '[', '1', 'A', 0x1b, '7', 0x1b, '[', '1', 'L', '\r'});
    private static final String STZ_DANACH = new String(new byte[]{0x1b, '8', 0x1b, '[', '1', 'B'});
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            handleClient();
        } catch (IOException e) {
            System.err.println("Fehler: " + e.getMessage());
        } finally {
            ChatRoom.getInstance().removeClient(this);
        }
    }

    private void handleClient() throws IOException {
        sendLine("Willkommen beim Chat-Server der 3CI");
        sendLine("Um die Verbindung zu beenden gib quit ein.");
        sendLine("Druecke return um zu starten.");
        in.readLine();
        while (true) {
            send("Welchen Spitznamen moechtest du haben: ");
            String name = in.readLine().trim();
            if (name == null) return;
            if (name.isEmpty()) {
                sendLine("Der Spitzname darf nicht leer sein.");
                continue;}
            if (!ChatRoom.getInstance().isNicknameAvailable(name)) {
                sendLine("Der Spitzname \"" + name + "\" ist leider schon vergeben.");
                continue;
            }

            nickname = name;
            ChatRoom.getInstance().addClient(this);
            break;
        }

        ChatRoom.getInstance().broadcastSystemMessage("\"" + nickname + "\" hat den Raum betreten.");
        prompt();
        String line;
        while ((line = in.readLine()) != null) {

            if (line.equalsIgnoreCase("quit")) break;
            if (line.equalsIgnoreCase("list")) {
                sendLine("Teilnehmer: " + ChatRoom.getInstance().getAllNicknames());
                prompt();
                continue;
            }
            if (line.equalsIgnoreCase("stat")) {
                sendLine(ChatRoom.getInstance().getStats());
                prompt();
                continue;
            }
            if (line.contains(":")) {
                int idx = line.indexOf(":");
                String to = line.substring(0, idx).trim();
                String msg = line.substring(idx + 1).trim();
                ChatRoom.getInstance().sendPrivateMessage(nickname, to, msg);

            } else if (!line.isEmpty()) {
                ChatRoom.getInstance().broadcastMessage(nickname, line);

            }
            prompt();
        }

        ChatRoom.getInstance().removeClient(this);
        ChatRoom.getInstance().broadcastSystemMessage("\"" + nickname + "\" hat den Raum verlassen.");
        socket.close();
    }

    public void sendMessage(String msg) {
        out.print(STZ_DAVOR);
        out.println(msg);
        out.print(STZ_DANACH);
        out.flush();
    }

    public void send(String msg) {
        out.print(msg);
        out.flush();
    }

    public void sendLine(String msg) {
        out.println(msg);
        out.flush();
    }

    public void prompt() {
        send(nickname + "> ");
    }

    public String getNickname() {
        return nickname;
    }
}
