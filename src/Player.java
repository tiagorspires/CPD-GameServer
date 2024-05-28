import java.net.Socket;
import java.util.Random;

public class Player {
    private String username;
    private String token;
    private int rank;
    private Socket socket;

    public Player(String username, String token, Socket socket) {
        this.username = username;
        this.token = token;
        this.rank = new Random().nextInt(11); // Assigns a random rank from 0 to 10;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        if(rank < 0) this.rank = 0;
        else if(rank > 10) this.rank = 10;
        else this.rank = rank;
    }

    public Socket getSocket() {
        return socket;
    }
}
