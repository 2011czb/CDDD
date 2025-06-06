package API.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import Game.Game;
import Players.Player;
import cards.Card;
import cards.Deck;
import cards.Rank;
import cards.Suit;

public class GameServer {
    public static final int PORT = 54555;
    private static final int MAX_PLAYERS = 4;
    private final Server server;
    private final Map<String, Connection> playerConnections;
    private final List<String> waitingPlayers;
    private final Map<String, NetworkPlayer> networkPlayers;
    private NetworkPlayer serverPlayer;  // 添加服务器玩家
    private Game game;
    private boolean gameStarted;
    private String currentPlayerId;
    private List<Card> lastPlayedCards;
    private String lastPlayerId;

    // 添加适配器类
    private static class NetworkPlayerAdapter extends Player {
        private final NetworkPlayer networkPlayer;

        public NetworkPlayerAdapter(NetworkPlayer networkPlayer) {
            super(networkPlayer.getName());
            this.networkPlayer = networkPlayer;
        }

        @Override
        public List<Card> play(List<Card> lastCards) {
            return null; // 网络模式下不使用这个方法
        }

        @Override
        public List<Card> getHand() {
            return networkPlayer.getHand();
        }

        @Override
        public void removeCards(List<Card> cards) {
            networkPlayer.removeCards(cards);
        }
        public void addCard(Card card) {
            networkPlayer.addCards(Collections.singletonList(card));
        }

        @Override
        public void clearHand() {
            networkPlayer.setHand(new ArrayList<>());
        }

        @Override
        public void sortHand() {
            List<Card> hand = networkPlayer.getHand();
            // 使用 Player 类中的排序逻辑
            Collections.sort(hand, (card1, card2) -> card2.getWeight() - card1.getWeight());
            networkPlayer.setHand(hand);
        }

        @Override
        public void drawCard(Deck deck) {
            deck.dealCard().ifPresent(card -> networkPlayer.addCards(Collections.singletonList(card)));
        }

        public NetworkPlayer getNetworkPlayer() {
            return networkPlayer;
        }
    }

    public GameServer() {
        server = new Server();
        playerConnections = new HashMap<>();
        waitingPlayers = new ArrayList<>();
        networkPlayers = new HashMap<>();
        gameStarted = false;
        
        // 注册 Kryo 序列化类
        Kryo kryo = server.getKryo();
        kryo.register(NetworkPacket.JoinGameRequest.class);
        kryo.register(NetworkPacket.GameStarted.class);
        kryo.register(NetworkPacket.PlayCardsRequest.class);
        kryo.register(NetworkPacket.PlayCardsResponse.class);
        kryo.register(NetworkPacket.PassRequest.class);
        kryo.register(NetworkPacket.GameStateUpdate.class);
        kryo.register(NetworkPacket.PlayerDisconnected.class);
        kryo.register(ArrayList.class);
        kryo.register(Arrays.asList().getClass()); // 注册 Arrays.ArrayList
        kryo.register(Card.class);
        kryo.register(Suit.class);
        kryo.register(Rank.class);
        
        // 设置 TCP 端口
        try {
            server.bind(PORT);
            System.out.println("服务器已启动，监听端口：" + PORT);
        } catch (IOException e) {
            System.err.println("服务器启动失败：" + e.getMessage());
        }
        
        // 添加连接监听器
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof NetworkPacket.JoinGameRequest) {
                    handleJoinGame(connection, (NetworkPacket.JoinGameRequest) object);
                } else if (object instanceof NetworkPacket.PlayCardsRequest) {
                    handlePlayCardsRequest(connection, (NetworkPacket.PlayCardsRequest) object);
                } else if (object instanceof NetworkPacket.PassRequest) {
                    handlePassRequest(connection, (NetworkPacket.PassRequest) object);
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
                handleDisconnection(connection);
            }
        });
    }

    public void addServerPlayer(NetworkPlayer player) {
        this.serverPlayer = player;
        networkPlayers.put(player.getId(), player);
        waitingPlayers.add(player.getId());
        System.out.println("服务器玩家已添加：" + player.getName() + " (ID: " + player.getId() + ")");
        System.out.println("当前等待玩家数：" + waitingPlayers.size() + "/" + MAX_PLAYERS);
        System.out.println("等待玩家列表：" + waitingPlayers);
    }

    private void handleJoinGame(Connection connection, NetworkPacket.JoinGameRequest request) {
        if (gameStarted) {
            // 游戏已经开始，拒绝新的加入请求
            return;
        }

        // 生成玩家 ID
        String playerId = UUID.randomUUID().toString();
        
        // 创建网络玩家
        NetworkPlayer networkPlayer = new NetworkPlayer(playerId, request.playerName);
        
        // 存储玩家信息
        playerConnections.put(playerId, connection);
        networkPlayers.put(playerId, networkPlayer);
        waitingPlayers.add(playerId);

        System.out.println("玩家 " + request.playerName + " 已加入游戏");
        int totalPlayers = waitingPlayers.size();
        System.out.println("当前等待玩家数：" + totalPlayers + "/" + MAX_PLAYERS);
        System.out.println("服务器玩家状态：" + (serverPlayer != null ? "已添加" : "未添加"));
        
        // 显示当前等待玩家列表（包含名称）
        System.out.println("等待玩家列表：");
        for (String id : waitingPlayers) {
            NetworkPlayer player = networkPlayers.get(id);
            System.out.println("- " + player.getName() + (id.equals(serverPlayer.getId()) ? " (服务器)" : ""));
        }

        // 如果达到4个玩家，开始游戏
        if (totalPlayers == MAX_PLAYERS) {
            System.out.println("所有玩家已加入，开始游戏！");
            initializeGame();
        } else {
            // 通知玩家等待其他玩家加入
            connection.sendTCP(new NetworkPacket.GameStateUpdate(
                null, null, null, false, null
            ));
        }
    }

    public void initializeGame() {
        // 创建玩家列表
        List<Player> gamePlayers = new ArrayList<>();
        
        // 添加所有玩家（包括服务器玩家）
        for (String playerId : waitingPlayers) {
            NetworkPlayer networkPlayer = networkPlayers.get(playerId);
            gamePlayers.add(new NetworkPlayerAdapter(networkPlayer));
        }

        // 创建游戏实例
        game = new Game.Builder()
            .setPlayers(gamePlayers)
            .setGameMode(Game.MODE_MULTIPLAYER)
            .setRuleType(Game.RULE_NORTH)
            .build();

        // 初始化游戏（这会自动发牌）
        game.initGame();
        game.startGame();

        // 获取每个玩家的手牌
        Map<String, List<Card>> playerHands = new HashMap<>();
        for (int i = 0; i < gamePlayers.size(); i++) {
            Player player = gamePlayers.get(i);
            String playerId = waitingPlayers.get(i);
            playerHands.put(playerId, player.getHand());
        }

        // 通知所有玩家游戏开始
        for (String playerId : waitingPlayers) {
            NetworkPlayer player = networkPlayers.get(playerId);
            if (playerId.equals(serverPlayer.getId())) {
                // 服务器玩家直接显示手牌
                System.out.println("\n游戏开始！");
                System.out.println("您的手牌：" + playerHands.get(playerId));
            } else {
                // 通知客户端玩家
                Connection connection = playerConnections.get(playerId);
                connection.sendTCP(new NetworkPacket.GameStarted(
                    waitingPlayers,
                    playerHands.get(playerId)
                ));
                System.out.println("已发送手牌给玩家：" + player.getName());
            }
        }

        gameStarted = true;
        currentPlayerId = game.getCurrentPlayer().getName();
        lastPlayedCards = null;
        lastPlayerId = null;
        
        broadcastGameState();
    }

    private void handlePlayCardsRequest(Connection connection, NetworkPacket.PlayCardsRequest request) {
        if (!gameStarted) return;
        
        NetworkPlayer player = networkPlayers.get(request.playerId);
        if (player == null || !player.getName().equals(currentPlayerId)) return;
        
        // 找到对应的 Player 实例
        Player gamePlayer = game.getPlayers().stream()
            .filter(p -> p.getName().equals(player.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到对应的游戏玩家"));
        
        // 验证出牌是否合法
        if (game.getPlayManager().isValidPlay(request.cards, lastPlayedCards, gamePlayer)) {
            // 从玩家手牌中移除这些牌
            player.removeCards(request.cards);
            lastPlayedCards = request.cards;
            lastPlayerId = player.getName();
            
            // 如果是服务器玩家，显示出牌信息
            if (player.getId().equals(serverPlayer.getId())) {
                System.out.println("\n您出牌：" + request.cards);
            } else {
                System.out.println("\n玩家 " + player.getName() + " 出牌：" + request.cards);
            }
            
            // 检查游戏是否结束
            if (player.getHand().isEmpty()) {
                handleGameEnd(player);
                return;
            }
            
            // 轮到下一个玩家
            int nextPlayerIndex = (game.getPlayers().indexOf(game.getCurrentPlayer()) + 1) % 4;
            game.getStateManager().setCurrentPlayerIndex(nextPlayerIndex);
            currentPlayerId = game.getCurrentPlayer().getName();
            
            // 广播出牌结果
            NetworkPacket.PlayCardsResponse response = new NetworkPacket.PlayCardsResponse(
                true, player.getId(), request.cards);
            server.sendToAllTCP(response);
            
            // 立即广播游戏状态更新
            NetworkPacket.GameStateUpdate update = new NetworkPacket.GameStateUpdate(
                currentPlayerId,
                lastPlayedCards,
                lastPlayerId,
                game.isGameEnded(),
                game.isGameEnded() ? game.getWinner().getName() : null
            );
            
            // 广播给所有客户端
            for (Connection conn : playerConnections.values()) {
                conn.sendTCP(update);
            }
            
            // 如果是服务器玩家的回合，显示提示
            if (currentPlayerId.equals(serverPlayer.getName())) {
                System.out.println("\n轮到您出牌");
                System.out.println("您的手牌：" + serverPlayer.getHand());
                if (lastPlayedCards != null) {
                    System.out.println("上家出牌：" + lastPlayedCards);
                }
            } else {
                // 显示其他玩家的回合
                NetworkPlayer currentPlayer = networkPlayers.values().stream()
                    .filter(p -> p.getName().equals(currentPlayerId))
                    .findFirst()
                    .orElse(null);
                if (currentPlayer != null) {
                    System.out.println("\n轮到玩家 " + currentPlayer.getName() + " 出牌");
                }
            }
        } else {
            // 发送无效出牌响应
            NetworkPacket.PlayCardsResponse response = new NetworkPacket.PlayCardsResponse(
                false, player.getId(), request.cards);
            server.sendToTCP(connection.getID(), response);
            
            // 如果是服务器玩家，显示错误信息
            if (player.getId().equals(serverPlayer.getId())) {
                System.out.println("\n无效的出牌！");
                // 显示可出的牌型
                List<List<Card>> playablePatterns = game.getPlayablePatterns(gamePlayer, lastPlayedCards);
                if (!playablePatterns.isEmpty()) {
                    System.out.println("可出的牌型：");
                    for (int i = 0; i < playablePatterns.size(); i++) {
                        System.out.println((i + 1) + ". " + playablePatterns.get(i));
                    }
                } else {
                    System.out.println("没有可出的牌，必须过牌！");
                }
            }
        }
    }

    private void handlePassRequest(Connection connection, NetworkPacket.PassRequest request) {
        if (!gameStarted) return;
        
        NetworkPlayer player = networkPlayers.get(request.playerId);
        if (player == null || !player.getName().equals(currentPlayerId)) return;
        
        // 找到对应的 Player 实例
        Player gamePlayer = game.getPlayers().stream()
            .filter(p -> p.getName().equals(player.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到对应的游戏玩家"));
        
        // 检查是否是上一次出牌的玩家
        if (lastPlayerId != null && lastPlayerId.equals(player.getName())) {
            // 如果是上一次出牌的玩家，不允许过牌
            if (player.getId().equals(serverPlayer.getId())) {
                System.out.println("\n您是上一次出牌的玩家，不能过牌！");
            }
            return;
        }
        
        // 轮到下一个玩家
        int nextPlayerIndex = (game.getPlayers().indexOf(game.getCurrentPlayer()) + 1) % 4;
        game.getStateManager().setCurrentPlayerIndex(nextPlayerIndex);
        currentPlayerId = game.getCurrentPlayer().getName();
        
        // 如果是服务器玩家，显示过牌信息
        if (player.getId().equals(serverPlayer.getId())) {
            System.out.println("\n您选择过牌");
        }
        
        broadcastGameState();
    }

    private void handleDisconnection(Connection connection) {
        String disconnectedPlayerId = null;
        for (Map.Entry<String, Connection> entry : playerConnections.entrySet()) {
            if (entry.getValue() == connection) {
                disconnectedPlayerId = entry.getKey();
                break;
            }
        }

        if (disconnectedPlayerId != null) {
            playerConnections.remove(disconnectedPlayerId);
            waitingPlayers.remove(disconnectedPlayerId);
            networkPlayers.remove(disconnectedPlayerId);
            
            if (gameStarted) {
                // 通知其他玩家有人断开连接
                for (Connection conn : playerConnections.values()) {
                    conn.sendTCP(new NetworkPacket.PlayerDisconnected(disconnectedPlayerId));
                }
            }
        }
    }

    private void handleGameEnd(NetworkPlayer winner) {
        NetworkPacket.GameStateUpdate update = new NetworkPacket.GameStateUpdate(
            currentPlayerId, lastPlayedCards, lastPlayerId, true, winner.getId());
        server.sendToAllTCP(update);
        gameStarted = false;
    }

    private void broadcastGameState() {
        if (!gameStarted) return;
        
        NetworkPacket.GameStateUpdate update = new NetworkPacket.GameStateUpdate(
            currentPlayerId,
            lastPlayedCards,
            lastPlayerId,
            game.isGameEnded(),
            game.isGameEnded() ? game.getWinner().getName() : null
        );

        // 如果是服务器玩家的回合，显示提示
        if (currentPlayerId.equals(serverPlayer.getName())) {
            System.out.println("\n轮到您出牌");
            System.out.println("您的手牌：" + serverPlayer.getHand());
            if (lastPlayedCards != null) {
                System.out.println("上家出牌：" + lastPlayedCards);
            }
        } else {
            // 显示其他玩家的回合
            NetworkPlayer currentPlayer = networkPlayers.values().stream()
                .filter(p -> p.getName().equals(currentPlayerId))
                .findFirst()
                .orElse(null);
            if (currentPlayer != null) {
                System.out.println("\n轮到玩家 " + currentPlayer.getName() + " 出牌");
            }
        }

        // 广播状态更新给所有客户端
        for (Connection connection : playerConnections.values()) {
            connection.sendTCP(update);
        }
    }

    public void start() throws IOException {
        server.start();
        // 只使用TCP连接
        server.bind(PORT);
        System.out.println("服务器已启动");
        System.out.println("本地IP地址：");
        for (String ip : getLocalIpAddresses()) {
            System.out.println("  " + ip );
        }
    }

    private List<String> getLocalIpAddresses() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr.isLoopbackAddress()) continue;
                    
                    String ip = addr.getHostAddress();
                    // 只显示IPv4地址
                    if (ip.indexOf(':') == -1) {
                        addresses.add(ip);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getWaitingPlayers() {
        return waitingPlayers;
    }

    public Game getGame() {
        return game;
    }

    public void stop() {
        server.stop();
    }

    public void handleServerPlayerPass(NetworkPlayer player) {
        if (!player.getId().equals(serverPlayer.getId())) return;
        
        // 检查是否是上一次出牌的玩家
        if (lastPlayerId != null && lastPlayerId.equals(player.getName())) {
            System.out.println("\n您是上一次出牌的玩家，不能过牌！");
            return;
        }
        
        int nextPlayerIndex = (game.getPlayers().indexOf(game.getCurrentPlayer()) + 1) % 4;
        game.getStateManager().setCurrentPlayerIndex(nextPlayerIndex);
        currentPlayerId = game.getCurrentPlayer().getName();
        broadcastGameState();
    }

    public void handlePassRequest(NetworkPlayer player) {
        if (!gameStarted) return;
        if (!player.getName().equals(currentPlayerId)) return;
        
        int nextPlayerIndex = (game.getPlayers().indexOf(game.getCurrentPlayer()) + 1) % 4;
        game.getStateManager().setCurrentPlayerIndex(nextPlayerIndex);
        currentPlayerId = game.getCurrentPlayer().getName();
        
        broadcastGameState();
    }

    public void handlePlayCardsRequest(NetworkPlayer player, List<Card> cards) {
        if (!gameStarted) return;
        if (!player.getName().equals(currentPlayerId)) return;
        
        // 找到对应的 Player 实例
        Player gamePlayer = game.getPlayers().stream()
            .filter(p -> p.getName().equals(player.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到对应的游戏玩家"));
        
        // 验证出牌是否合法
        if (game.getPlayManager().isValidPlay(cards, lastPlayedCards, gamePlayer)) {
            game.getPlayManager().playCards(gamePlayer, cards);
            lastPlayedCards = cards;
            lastPlayerId = player.getName();
            
            // 检查游戏是否结束
            if (player.getHand().isEmpty()) {
                handleGameEnd(player);
                return;
            }
            
            // 轮到下一个玩家
            int nextPlayerIndex = (game.getPlayers().indexOf(game.getCurrentPlayer()) + 1) % 4;
            game.getStateManager().setCurrentPlayerIndex(nextPlayerIndex);
            currentPlayerId = game.getCurrentPlayer().getName();
        }
        
        broadcastGameState();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    private static void startServer(Scanner scanner) {
        System.out.print("请输入您的名字：");
        String serverPlayerName = scanner.nextLine();
        
        GameServer server = new GameServer();
        NetworkPlayer serverPlayer = new NetworkPlayer(UUID.randomUUID().toString(), serverPlayerName);
        server.addServerPlayer(serverPlayer);
        
        try {
            server.start();
            System.out.println("服务器已启动，等待其他玩家加入...");
            
            // 等待游戏开始
            while (!server.isGameStarted()) {
                Thread.sleep(1000);
            }
            
            // 游戏主循环
            while (!server.getGame().isGameOver()) {
                Player currentPlayer = server.getGame().getCurrentPlayer();
                String currentPlayerName = currentPlayer.getName();
                if (currentPlayerName.equals(serverPlayer.getName())) {
                    // 服务器玩家回合
                    server.handleServerPlayerTurn(serverPlayer, scanner);
                }
                Thread.sleep(1000);
            }
            
            // 游戏结束
            System.out.println("游戏结束！");
            server.stop();
            
        } catch (Exception e) {
            System.err.println("服务器错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleServerPlayerTurn(NetworkPlayer player, Scanner scanner) {
        System.out.println("\n轮到您出牌！");
        System.out.println("您的手牌：" + player.getHand());
        
        // 获取可出的牌型
        List<Card> lastPlayedCards = this.lastPlayedCards;
        Player gamePlayer = game.getPlayers().stream()
            .filter(p -> p.getName().equals(player.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到对应的游戏玩家"));
        List<List<Card>> playablePatterns = game.getPlayablePatterns(gamePlayer, lastPlayedCards);
        
        if (playablePatterns.isEmpty()) {
            System.out.println("没有可出的牌，必须过牌！");
            handleServerPlayerPass(player);
            return;
        }
        
        System.out.println("可出的牌型：");
        for (int i = 0; i < playablePatterns.size(); i++) {
            System.out.println((i + 1) + ". " + playablePatterns.get(i));
        }
        System.out.println("0. 过牌");
        
        System.out.print("请选择要出的牌（输入序号）：");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符
        
        if (choice == 0) {
            handleServerPlayerPass(player);
        } else if (choice > 0 && choice <= playablePatterns.size()) {
            List<Card> selectedCards = playablePatterns.get(choice - 1);
            handlePlayCardsRequest(player, selectedCards);
        } else {
            System.out.println("无效的选择，自动过牌！");
            handleServerPlayerPass(player);
        }
    }
} 