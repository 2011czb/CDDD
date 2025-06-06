package API.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import Rules.NorthRule;
import Rules.Rule;
import cards.Card;
import cards.Rank;
import cards.Suit;

public class GameClient {
    private static final int TIMEOUT = 5000;
    private static final int TCP_PORT = GameServer.PORT;  // 使用服务器端口
    
    private final Client client;
    private NetworkPlayer localPlayer;
    private final Map<String, NetworkPlayer> players;
    private final List<GameStateListener> listeners;
    private boolean gameStarted;
    private String currentPlayerId;
    private List<Card> lastPlayedCards;
    private String lastPlayerId;
    private Rule currentRule;
    private String playerName;
    private String serverIp;
    private int serverPort;
    private int localPort;
    private NetworkPacket.GameStateUpdate pendingStateUpdate = null;
    
    public GameClient() {
        this.client = new Client();
        this.players = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.gameStarted = false;
        
        // 注册 Kryo 序列化类
        Kryo kryo = client.getKryo();
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
        
        // 添加连接监听器
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                handleReceivedObject(object);
            }
            
            @Override
            public void disconnected(Connection connection) {
                System.out.println("与服务器断开连接");
            }
        });
    }
    
    public void initialize(String serverIp, int serverPort, int localPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.localPort = localPort;
    }
    
    private void setupListeners() {
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("已连接到服务器");
                notifyListeners(GameEvent.CONNECTED);
                // 连接成功后自动发送加入游戏请求
                if (playerName != null && !playerName.isEmpty()) {
                    joinGame(playerName);
                    System.out.println("已发送加入游戏请求");
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
                notifyListeners(GameEvent.DISCONNECTED);
            }
            
            @Override
            public void received(Connection connection, Object object) {
                handleReceivedObject(object);
            }
        });
    }
    
    public void connect(String serverIp, int serverPort, String playerName) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.playerName = playerName;
        
        try {
            System.out.println("正在连接到服务器...");
            client.start();
            client.connect(5000, serverIp, serverPort);
            System.out.println("已连接到服务器");
            
            // 连接成功后自动发送加入游戏请求
            if (playerName != null && !playerName.isEmpty()) {
                joinGame(playerName);
                System.out.println("已发送加入游戏请求");
            }
        } catch (IOException e) {
            System.err.println("连接服务器失败：" + e.getMessage());
        }
    }
    
    public void disconnect() {
        client.stop();
    }
    
    public void joinGame(String playerName) {
        // 发送加入游戏请求，等待服务器分配 ID
        this.playerName = playerName;
        NetworkPacket.JoinGameRequest request = new NetworkPacket.JoinGameRequest(playerName);
        client.sendTCP(request);
    }
    
    public boolean isConnected() {
        return client.isConnected();
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public void playCards(List<Card> cards) {
        if (!gameStarted || !currentPlayerId.equals(localPlayer.getId())) return;
        
        NetworkPacket.PlayCardsRequest request = new NetworkPacket.PlayCardsRequest(
            localPlayer.getId(), cards);
        client.sendTCP(request);
    }
    
    public void pass() {
        if (!gameStarted || !currentPlayerId.equals(localPlayer.getId())) return;
        
        NetworkPacket.PassRequest request = new NetworkPacket.PassRequest(localPlayer.getId());
        client.sendTCP(request);
    }
    
    public boolean canPlay(List<Card> cards) {
        if (!gameStarted || !currentPlayerId.equals(localPlayer.getId())) return false;
        
        // 使用当前规则检查是否可以出牌
        return currentRule != null && currentRule.isValidPlay(cards, lastPlayedCards);
    }
    
    public List<List<Card>> getValidPlays(List<Card> hand) {
        if (!gameStarted || currentRule == null) return new ArrayList<>();
        
        // 如果是第一手牌，只返回包含方块3的牌型
        if (lastPlayedCards == null) {
            return hand.stream()
                .filter(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE)
                .map(Collections::singletonList)
                .collect(Collectors.toList());
        }
        
        return currentRule.getValidPlays(hand, lastPlayedCards);
    }
    
    public Rule getCurrentRule() {
        return currentRule;
    }
    
    public List<Card> getLastPlayedCards() {
        return lastPlayedCards != null ? new ArrayList<>(lastPlayedCards) : null;
    }
    
    public String getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public String getLastPlayerId() {
        return lastPlayerId;
    }
    
    public NetworkPlayer getLocalPlayer() {
        return localPlayer;
    }
    
    public void addStateListener(GameStateListener listener) {
        listeners.add(listener);
    }
    
    public void removeStateListener(GameStateListener listener) {
        listeners.remove(listener);
    }
    
    public void addGameStateListener(GameStateListener listener) {
        listeners.add(listener);
    }
    
    private void handleReceivedObject(Object object) {
        if (object instanceof NetworkPacket.GameStarted) {
            handleGameStarted((NetworkPacket.GameStarted) object);
        } else if (object instanceof NetworkPacket.PlayCardsResponse) {
            handlePlayCardsResponse((NetworkPacket.PlayCardsResponse) object);
        } else if (object instanceof NetworkPacket.GameStateUpdate) {
            handleGameStateUpdate((NetworkPacket.GameStateUpdate) object);
        } else if (object instanceof NetworkPacket.PlayerDisconnected) {
            handlePlayerDisconnected((NetworkPacket.PlayerDisconnected) object);
        }
    }
    
    private void handleGameStarted(NetworkPacket.GameStarted packet) {
        if (!gameStarted) {
            gameStarted = true;
            
            // 使用服务器分配的 ID 创建本地玩家
            String serverPlayerId = packet.playerIds.get(0); // 假设第一个 ID 是本地玩家
            localPlayer = new NetworkPlayer(serverPlayerId, playerName);
            players.put(serverPlayerId, localPlayer);
            
            localPlayer.setHand(packet.initialHand);
            currentRule = NorthRule.getInstance(); // 默认使用北方规则
            
            // 初始化游戏状态
            currentPlayerId = playerName;  // 使用玩家名字作为当前玩家ID
            lastPlayedCards = null;
            lastPlayerId = null;
            
            System.out.println("\n游戏开始！");
            System.out.println("您的手牌：" + localPlayer.getHand());
            
            // 如果有待处理的状态更新，现在处理它
            if (pendingStateUpdate != null) {
                processGameStateUpdate(pendingStateUpdate);
                pendingStateUpdate = null;
            }
            
            notifyListeners(GameEvent.GAME_STARTED);
        }
    }
    
    private void handlePlayCardsResponse(NetworkPacket.PlayCardsResponse response) {
        if (response.isValid) {
            if (response.playerId.equals(localPlayer.getId())) {
                localPlayer.removeCards(response.cards);
                System.out.println("\n您出牌：" + response.cards);
            } else {
                NetworkPlayer player = players.get(response.playerId);
                if (player != null) {
                    System.out.println("\n" + player.getName() + " 出牌：" + response.cards);
                }
            }
            notifyListeners(GameEvent.CARD_PLAYED);
        } else {
            if (response.playerId.equals(localPlayer.getId())) {
                System.out.println("\n无效的出牌！");
                // 显示可出的牌型
                List<List<Card>> playablePatterns = getValidPlays(localPlayer.getHand());
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
    
    private void handleGameStateUpdate(NetworkPacket.GameStateUpdate update) {
        // 如果本地玩家还未初始化，保存状态更新
        if (localPlayer == null) {
            pendingStateUpdate = update;
            return;
        }

        processGameStateUpdate(update);
    }
    
    private void processGameStateUpdate(NetworkPacket.GameStateUpdate update) {
        // 更新状态
        if (update.currentPlayerId != null) {
            currentPlayerId = update.currentPlayerId;
        }
        if (update.lastPlayedCards != null) {
            lastPlayedCards = update.lastPlayedCards;
        }
        if (update.lastPlayerId != null) {
            lastPlayerId = update.lastPlayerId;
        }
        
        if (update.gameEnded) {
            gameStarted = false;
            if (update.winnerId.equals(localPlayer.getId())) {
                System.out.println("\n恭喜您获胜！");
            } else {
                NetworkPlayer winner = players.get(update.winnerId);
                if (winner != null) {
                    System.out.println("\n" + winner.getName() + " 获胜！");
                }
            }
        }
        
        notifyListeners(GameEvent.STATE_UPDATED);
    }
    
    private void handlePlayerDisconnected(NetworkPacket.PlayerDisconnected packet) {
        players.remove(packet.playerId);
    }
    
    private void notifyListeners(GameEvent event) {
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged(event, this);
        }
    }
    
    public enum GameEvent {
        CONNECTED,
        DISCONNECTED,
        GAME_STARTED,
        CARD_PLAYED,
        GAME_ENDED,
        STATE_UPDATED
    }
    
    public interface GameStateListener {
        void onGameStateChanged(GameEvent event, GameClient client);
    }
    
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    
    public boolean isGameOver() {
        return !gameStarted;
    }
    
    public boolean isMyTurn() {
        return localPlayer != null && currentPlayerId != null && 
               currentPlayerId.equals(localPlayer.getName());
    }
    
    public List<List<Card>> getPlayablePatterns(List<Card> lastPlayedCards) {
        if (localPlayer == null) {
            return new ArrayList<>();
        }
        Map<String, List<CardGroup>> patterns = PlayablePatternUtil.getPlayablePatterns(
            localPlayer.getHand(),
            lastPlayedCards,
            currentRule,
            lastPlayerId != null && lastPlayerId.equals(localPlayer.getId()) ? 0 : -1,
            currentPlayerId != null && currentPlayerId.equals(localPlayer.getId()) ? 0 : -1
        );
        
        List<List<Card>> result = new ArrayList<>();
        patterns.values().forEach(cardGroups -> 
            cardGroups.forEach(group -> result.add(group.getCards()))
        );
        return result;
    }

    public void sendPassRequest() {
        if (client != null && client.isConnected()) {
            client.sendTCP(new NetworkPacket.PassRequest(localPlayer.getId()));
        }
    }

    public void sendPlayCardsRequest(List<Card> cards) {
        if (client != null && client.isConnected()) {
            client.sendTCP(new NetworkPacket.PlayCardsRequest(localPlayer.getId(), cards));
        }
    }

    public Map<String, NetworkPlayer> getPlayers() {
        return new HashMap<>(players);
    }
} 