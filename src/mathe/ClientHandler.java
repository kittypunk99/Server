package mathe;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClientHandler extends Thread {

    Socket client;

    public ClientHandler(Socket s) {
        client = s;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream())); BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            System.out.println("connected to " + client.getRemoteSocketAddress());
            br.readLine();
            wr.flush();
            wr.write("Willkommen im Mathe-Quiz" + System.lineSeparator());
            wr.write("Wie heftig haettest du es denn gerne ? (1-3)" + System.lineSeparator());
            wr.flush();
            int d = -1;
            while (true) {
                try {
                    d = Integer.parseInt(br.readLine());
                } catch (NumberFormatException ignored) {
                }
                if (d >= 1 && d <= 3) {
                    break;
                }
                wr.write("Bitte eine gültige Zahl eingeben (1-3)" + System.lineSeparator());
                wr.flush();
            }
            wr.write("Wie viele Rechnungen willst du loesen?" + System.lineSeparator());
            wr.flush();
            int n = -1;
            while (true) {
                try {
                    n = Integer.parseInt(br.readLine());
                } catch (NumberFormatException ignored) {
                }
                if (n >= 1) {
                    break;
                }
                wr.write("Bitte eine gültige Zahl eingeben (>=1)" + System.lineSeparator());
                wr.flush();
            }
            for (int i = 0; i < n; i++) {
                doQuiz(d, wr, i, br);
            }
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void doQuiz(int d, BufferedWriter wr, int i, BufferedReader br) throws IOException {
        Random random = new Random();
        String[] operations = {"+", "-", "*", "/"};
        int num1 = random.nextInt((int)Math.pow(10, d)) + 1;
        int num2 = random.nextInt((int)Math.pow(10, d)) + 1;
        String operation = operations[random.nextInt(operations.length)];
        int correctAnswer = 0;

        switch (operation) {
            case "+":
                correctAnswer = num1 + num2;
                break;
            case "-":
                correctAnswer = num1 - num2;
                break;
            case "*":
                correctAnswer = num1 * num2;
                break;
            case "/":
                correctAnswer = num1;
                num1 = num1 * num2;
                break;
        }

        wr.write("Frage "+(i +1)+": "+num1 + " " + operation + " " + num2 + " = ??" + System.lineSeparator());
        wr.flush();
        while (true) {
            String answer = br.readLine();

            if (answer != null && !answer.trim().isEmpty()) {
                try {
                    int clientAnswer = Integer.parseInt(answer.trim());
                    if (clientAnswer == correctAnswer) {
                        wr.write("richtig ;)" + System.lineSeparator());
                        break;
                    } else {
                        wr.write("falsch :( .. versuche es noch einmal" + System.lineSeparator());
                    }
                } catch (NumberFormatException e) {
                    wr.write("Ungültige Eingabe: " + answer + System.lineSeparator());
                }
            } else {
                wr.write("Keine Antwort erhalten" + System.lineSeparator());
            }
            wr.flush();
        }
    }
}