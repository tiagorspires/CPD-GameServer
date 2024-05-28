import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameInstance extends Thread {
    private Player[] players;
    private char[][] board;
    private int currentPlayerIndex;
    private boolean gameRunning;
    private final Lock lock = new ReentrantLock();
    private boolean ranked;

    public GameInstance(Player[] players, boolean ranked) {
        this.players = players;
        this.board = new char[3][3];
        this.currentPlayerIndex = 0;
        this.gameRunning = true;
        this.ranked = ranked;
        initializeBoard();
    }

    @Override
    public void run() {
        try {
            for (Player player : players) {
                PrintWriter out = new PrintWriter(player.getSocket().getOutputStream(), true);
                out.println("Game is starting! You are playing Tic Tac Toe.");
                updateBoard(out);
            }
            while (gameRunning) {
                lock.lock();
                try {
                    Player currentPlayer = players[currentPlayerIndex];
                    PrintWriter currentOut = new PrintWriter(currentPlayer.getSocket().getOutputStream(), true);
                    currentOut.println("Your turn. Enter your move (row and column):");

                    boolean validMove = false;
                    while (!validMove) {
                        Scanner in = new Scanner(currentPlayer.getSocket().getInputStream());
                        if (in.hasNextLine()) {
                            String move = in.nextLine();
                            System.out.println("Received move from player: " + move); // Debug log
                            String[] tokens = move.split(" ");
                            if (tokens.length == 2) {
                                int row = Integer.parseInt(tokens[0]);
                                int col = Integer.parseInt(tokens[1]);
                                if (makeMove(currentPlayerIndex, row, col)) {
                                    validMove = true;
                                    updateAllClients(); // Update board for all clients
                                    if (isWin()) {
                                        gameRunning = false;
                                        updateAllClients(); // Show final board before result
                                        for (Player player : players) {
                                            PrintWriter out = new PrintWriter(player.getSocket().getOutputStream(), true);
                                            if (player == currentPlayer) {
                                                out.println("Game over. You win!");
                                                if(this.ranked) {
                                                    player.setRank(player.getRank() + 1);
                                                    out.println("You won 1 rank point. Your new score is " + player.getRank() + ".");
                                                }
                                            } else {
                                                out.println("Game over. You lose!");
                                                if(this.ranked) {
                                                    player.setRank(player.getRank() - 1);
                                                    out.println("You won 1 rank point. Your new score is " + player.getRank() + ".");
                                                }
                                            }
                                        }
                                    } else if (isDraw()) {
                                        gameRunning = false;
                                        updateAllClients(); // Show final board before result
                                        for (Player player : players) {
                                            PrintWriter out = new PrintWriter(player.getSocket().getOutputStream(), true);
                                            out.println("Game over. Game draw!");
                                        }
                                    } else {
                                        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
                                    }
                                } else {
                                    currentOut.println("Invalid move. Try again:");
                                }
                            } else {
                                currentOut.println("Invalid input. Enter row and column numbers:");
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    private void updateAllClients() throws IOException {
        lock.lock();
        try {
            for (Player player : players) {
                PrintWriter out = new PrintWriter(player.getSocket().getOutputStream(), true);
                updateBoard(out);
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateBoard(PrintWriter out) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                out.print(board[i][j] + " ");
            }
            out.println();
        }
        out.println(); // Adding an extra line for better readability
    }

    private boolean makeMove(int playerIndex, int row, int col) {
        lock.lock();
        try {
            if (row < 0 || col < 0 || row >= 3 || col >= 3 || board[row][col] != '-') {
                return false;
            }
            board[row][col] = (playerIndex == 0) ? 'X' : 'O';
            return true;
        } finally {
            lock.unlock();
        }
    }

    private boolean isWin() {
        lock.lock();
        try {
            // Check rows, columns, and diagonals
            for (int i = 0; i < 3; i++) {
                if (board[i][0] != '-' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                    return true;
                }
                if (board[0][i] != '-' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                    return true;
                }
            }
            if (board[0][0] != '-' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
                return true;
            }
            if (board[0][2] != '-' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private boolean isDraw() {
        lock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == '-') {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
}
