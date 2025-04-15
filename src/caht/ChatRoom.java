package caht;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatRoom {
    private static final ChatRoom instance = new ChatRoom();
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, Integer> messageCount = new ConcurrentHashMap<>();

    private ChatRoom() {
    }

    public static ChatRoom getInstance() {
        return instance;
    }

    public synchronized boolean isNicknameAvailable(String name) {
        return clients.keySet().stream().noneMatch(n -> n.equalsIgnoreCase(name));
    }

    public synchronized void addClient(ClientHandler client) {
        clients.put(client.getNickname(), client);
        messageCount.put(client.getNickname(), 0);
        System.out.println("Client verbunden: " + client.getNickname());
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client.getNickname());
        System.out.println("Client getrennt: " + client.getNickname());
    }

    public synchronized void broadcastMessage(String sender, String message) {
        messageCount.merge(sender, 1, Integer::sum);
        for (ClientHandler client : clients.values()) {
            if (!client.getNickname().equals(sender)) {
                client.sendMessage(sender + ": " + message);
            }
        }
    }

    public synchronized void sendPrivateMessage(String sender, String recipient, String message) {
        ClientHandler client = clients.get(recipient);
        if (client != null) {
            client.sendMessage("[Privat von " + sender + "]: " + message);
            messageCount.merge(sender, 1, Integer::sum);
        }else {
            ClientHandler senderClient = clients.get(sender);
            if (senderClient != null) {
                senderClient.sendMessage("Benutzer \"" + recipient + "\" nicht gefunden.");
            }
        }
    }

    public synchronized void broadcastSystemMessage(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public synchronized String getAllNicknames() {
        return String.join(", ", clients.keySet());
    }

    public synchronized String getStats() {
        return messageCount.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue() + " Nachricht(en)").collect(Collectors.joining(System.lineSeparator()));
    }
}
