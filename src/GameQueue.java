import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameQueue {
    private LinkedList<Player> simpleQueue;
    private LinkedList<Player> rankedQueue;
    private LinkedList<String> disconnectedQueue;
    private LinkedList<Map<String, Player>> matchedWhileDisconnected;
    private Lock lock;
    private static final int NUMBER_OF_PLAYERS_PER_TEAM = 2; // Example value, adjust as needed
    private static final int RANK_DIFFERENCE_THRESHOLD = 3; // Maximum rank difference allowed
    private static final int TIMEOUT = 15;

    public GameQueue() {
        this.simpleQueue = new LinkedList<>();
        this.rankedQueue = new LinkedList<>();
        this.disconnectedQueue = new LinkedList<>();
        this.matchedWhileDisconnected = new LinkedList<>();
        this.lock = new ReentrantLock();
    }

    public void enqueueSimple(Player player) {
        lock.lock();
        try {
            if (!isPlayerInQueue(simpleQueue, player.getUsername())) {
                simpleQueue.add(player);
                System.out.println("Player " + player.getUsername() + " has joined the simple queue.");
                if (simpleQueue.size() >= NUMBER_OF_PLAYERS_PER_TEAM) {
                    LinkedList<Player> tempQueue = new LinkedList<>();
                    tempQueue.addAll(simpleQueue);
                    simpleQueue.clear();
                    startGame(tempQueue, false);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void enqueueRanked(Player player) {
        System.out.println(player.getUsername() + player.getRank());
        lock.lock();
        try {
            if (disconnectedQueue.contains(player.getToken())) {
                disconnectedQueue.remove(player.getToken());
                System.out.println("Player " + player.getUsername() + " has rejoined the ranked queue.");
                //check if player was matched while disconnected
                for(Map<String, Player> map : matchedWhileDisconnected) {
                    if(map.containsKey(player.getToken())) {
                        List<Player> team = new LinkedList<>();
                        Player player2 = map.get(player.getToken());
                        team.add(player2);
                        team.add(player);
                        matchedWhileDisconnected.remove(map);
                        startGame(team, true);
                    }
                }
            } else {
                rankedQueue.add(player);
                System.out.println("Player " + player.getUsername() + " has joined the ranked queue.");
            }
            matchRankedPlayers();
        } finally {
            lock.unlock();
        }
    }

    private void matchRankedPlayers() {
        if (rankedQueue.size() < NUMBER_OF_PLAYERS_PER_TEAM) {
            return;
        }

        for (int i = 0; i < rankedQueue.size() - 1; i++) {
            for (int j = i + 1; j < rankedQueue.size(); j++) {
                Player player1 = rankedQueue.get(i);
                Player player2 = rankedQueue.get(j);
                if (Math.abs(player1.getRank() - player2.getRank()) <= RANK_DIFFERENCE_THRESHOLD) {
                    List<Player> team = new LinkedList<>();
                    team.add(player1);
                    team.add(player2);
                    if (disconnectedQueue.contains(player1.getToken())) {
                        System.out.println("Matchmaking is holding for reconnected players.");
                        Map<String, Player> aux = new HashMap<>();
                        aux.put(player1.getToken(), player2);
                        team.clear();
                        rankedQueue.remove(player1);
                        rankedQueue.remove(player2);
                        matchedWhileDisconnected.add(aux);
                    } else if (disconnectedQueue.contains(player2.getToken())) {
                        System.out.println("Matchmaking is holding for reconnected players.");
                        Map<String, Player> aux = new HashMap<>();
                        aux.put(player2.getToken(), player1);
                        team.clear();
                        rankedQueue.remove(player1);
                        rankedQueue.remove(player2);
                        matchedWhileDisconnected.add(aux);
                    } else if (disconnectedQueue.contains(player1.getToken()) && disconnectedQueue.contains(player2.getToken())) {
                        //both players disconnected, do not make match
                        continue;
                    } else {
                        rankedQueue.remove(player1);
                        rankedQueue.remove(player2);
                        startGame(team, true);
                    }
                    return;
                }
            }
        }
    }

    private boolean isPlayerInQueue(LinkedList<Player> queue, String username) {
        return queue.stream().anyMatch(p -> p.getUsername().equals(username));
    }

    private void startGame(List<Player> team, boolean ranked) {
        Player[] players = new Player[team.size()];
        team.toArray(players);
        new GameInstance(players, ranked).start();
    }

    public void handleDisconnect(Player player) {
        lock.lock();
        try {
            if (rankedQueue.contains(player)) {
                disconnectedQueue.add(player.getToken());
                System.out.println("Player " + player.getUsername() + " has disconnected and will be given 15 seconds to reconnect.");

                // Custom scheduler for timeout
                new Thread(() -> {
                    try {
                        Thread.sleep(TIMEOUT * 1000);
                        lock.lock();
                        try {
                            if (disconnectedQueue.contains(player.getToken())) {
                                rankedQueue.remove(player);
                                disconnectedQueue.remove(player.getToken());
                                // Check if player was matched while disconnected
                                for (Map<String, Player> map : matchedWhileDisconnected) {
                                    if (map.containsKey(player.getToken())) {
                                        matchedWhileDisconnected.remove(map);
                                        rankedQueue.add(map.get(player.getToken()));
                                        System.out.println("Matchmaking failed. Returned " + map.get(player.getToken()).getUsername() + " to Queue");
                                    }
                                }
                                System.out.println("Player " + player.getUsername() + " has been removed from the ranked queue due to disconnection.");
                            }
                        } finally {
                            lock.unlock();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                simpleQueue.remove(player);
                System.out.println("Player " + player.getUsername() + " has been removed from the simple queue.");
            }
        } finally {
            lock.unlock();
        }
    }
}
