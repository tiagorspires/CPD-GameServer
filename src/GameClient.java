import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;

    public void startConnection(String ip, int port) throws Exception {
        System.out.println("Attempting to connect to server at " + ip + ":" + port);
        clientSocket = new Socket(ip, port);
        System.out.println("Connection established");
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        // Thread to handle server messages
        Thread serverHandler = new Thread(() -> {
            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    if (fromServer.startsWith("Game is starting") || fromServer.startsWith("Your turn") || fromServer.startsWith("Invalid move") || fromServer.startsWith("Invalid input") || fromServer.startsWith("You win") || fromServer.startsWith("You lose") || fromServer.startsWith("Game draw")) {
                        System.out.println(fromServer);
                        if (fromServer.startsWith("Your turn")) {
                            playTurn();
                        }
                        if (fromServer.startsWith("Game over")) {
                            break;
                        }
                    } else {
                        System.out.println(fromServer);
                    }
                }
            } catch (Exception e) {
                System.out.println("Server connection closed unexpectedly.");
            } finally {
                try {
                    stopConnection();
                } catch (Exception e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        });
        serverHandler.start();

        // Allow user to input username and password initially
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
        }
    }

    private void playTurn() {
        try {
            String userInput = stdIn.readLine();
            out.println(userInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() throws Exception {
        if (in != null) in.close();
        if (out != null) out.close();
        if (stdIn != null) stdIn.close();
        if (clientSocket != null) clientSocket.close();
        System.out.println("Disconnected from server.");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java GameClient <serverIP> <port>");
            return;
        }

        GameClient client = new GameClient();
        try {
            client.startConnection(args[0], Integer.parseInt(args[1]));
        } catch (Exception e) {
            System.out.println("Error connecting to server.");
            e.printStackTrace();
        }
    }
}
