package Network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import Network.model.NetworkCard;
import Network.model.NetworkPlayer;
import Network.packets.*;
import Network.PacketRegister;
import Network.client.GameClientInterface;
import cards.Card;
import cards.Rank;
import cards.Suit;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.stream.Collectors;
import PokerPatterns.*;
import PokerPatterns.basis.*;
import PokerPatterns.generator.*;
import PokerPatterns.PlayablePatternUtil;
import Rules.Rule;
import Rules.RuleManager;
import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.*;
import Rules.NorthRule;
import Rules.SouthRule;

public class GameClient implements GameClientInterface {
    private static final int TCP_PORT = 54555;
    private static final int UDP_PORT = 54777;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int HEARTBEAT_TIMEOUT = 15000; // 15秒没有收到心跳就认为断开
    private static final long UPDATE_TIMEOUT = 5000;  // 5秒超时

    private final Client client;
    private final List<GameStateListener> stateListeners;
    private NetworkPlayer localPlayer;
    private final AtomicBoolean connected;
    private final AtomicBoolean gameStarted;
    private final AtomicBoolean gameEnded;
    private long lastHeartbeatTime;
    private final Map<String, Long> pendingUpdates;  // 等待确认的更新包
    private String currentPlayerId;
    private List<Card> lastPlayedCards;
    private String lastPlayerId;
    private Rule currentRule;  // 改为Rule类型
    private RuleManager ruleManager;

    public GameClient() {
        this.client = new Client();
        this.stateListeners = new ArrayList<>();
        this.connected = new AtomicBoolean(false);
        this.gameStarted = new AtomicBoolean(false);
        this.gameEnded = new AtomicBoolean(false);
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.pendingUpdates = new ConcurrentHashMap<>();
        this.currentRule = NorthRule.getInstance();  // 默认使用北方规则
        this.ruleManager = RuleManager.getInstance();
        
        // 创建本地玩家对象，使用临时ID
        String tempId = java.util.UUID.randomUUID().toString();
        this.localPlayer = new NetworkPlayer(tempId, "");
        this.localPlayer.setTempId(tempId);
        
        registerPackets();
        setupClient();
        initializeGame();
    }

    private void registerPackets() {
        // 使用PacketRegistrar统一注册所有网络包
        PacketRegister.registerPacketsFor(client);
    }

    private void setupClient() {
        client.start();
        
        // 添加网络事件监听器
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                handleConnection(connection);
            }

            @Override
            public void disconnected(Connection connection) {
                handleDisconnection(connection);
            }

            @Override
            public void received(Connection connection, Object object) {
                handleReceivedObject(connection, object);
            }
        });
    }

    private void initializeGame() {
        // 初始化游戏状态
        this.lastPlayedCards = new ArrayList<>();
    }

    public void connect(String host) throws IOException {
        client.connect(CONNECTION_TIMEOUT, host, TCP_PORT, UDP_PORT);
    }

    public void joinGame(String playerName) {
        if (!isConnected()) {
            throw new IllegalStateException("未连接到服务器");
        }
        
        // 设置本地玩家名称
        localPlayer.setName(playerName);
        
        // 发送PlayerJoin消息，包含临时ID和玩家名称
        PlayerJoin joinPacket = new PlayerJoin(localPlayer.getTempId(), playerName);
        client.sendTCP(joinPacket);
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isGameStarted() {
        return gameStarted.get();
    }

    public boolean isGameEnded() {
        return gameEnded.get();
    }

    public void addStateListener(GameStateListener listener) {
        if (listener != null) {
            stateListeners.add(listener);
        }
    }

    public void removeStateListener(GameStateListener listener) {
        stateListeners.remove(listener);
    }

    private void handleConnection(Connection connection) {
        connected.set(true);
        notifyStateListeners(GameEvent.CONNECTED);
    }

    private void handleDisconnection(Connection connection) {
        connected.set(false);
        gameStarted.set(false);
        gameEnded.set(false);
        notifyStateListeners(GameEvent.DISCONNECTED);
    }

    private void handleReceivedObject(Connection connection, Object object) {
        if (object instanceof GameStart) {
            handleGameStart((GameStart) object);
        } else if (object instanceof PlayerAction) {
            handlePlayerAction((PlayerAction) object);
        } else if (object instanceof GameEnd) {
            handleGameEnd((GameEnd) object);
        } else if (object instanceof Heartbeat) {
            handleHeartbeat();
        } else if (object instanceof GameStateUpdate) {
            handleGameStateUpdate((GameStateUpdate) object);
        } else if (object instanceof PlayerIdAssignment) {
            handlePlayerIdAssignment((PlayerIdAssignment) object);
        }
    }

    private void handleGameStart(GameStart gameStart) {
        gameStarted.set(true);
        gameEnded.set(false);
        
        // 设置游戏规则
        String ruleName = gameStart.getGameRule();
        if ("SOUTH".equals(ruleName)) {
            currentRule = SouthRule.getInstance();
        } else {
            currentRule = NorthRule.getInstance();
        }
        
        // 通知所有监听器游戏开始
        notifyStateListeners(GameEvent.GAME_STARTED);
    }

    private void handlePlayerAction(PlayerAction action) {
        if (action.getActionType() == ActionType.PLAY_CARD) {
            // 更新最后出的牌
            if (action.getPlayedCards() != null && !action.getPlayedCards().isEmpty()) {
                lastPlayedCards = NetworkCard.toCardList(action.getPlayedCards());
                notifyStateListeners(GameEvent.CARD_PLAYED);
            }
        }
    }

    private void handleGameEnd(GameEnd gameEnd) {
        gameStarted.set(false);
        gameEnded.set(true);
        notifyStateListeners(GameEvent.GAME_ENDED);
    }

    private void handleHeartbeat() {
        lastHeartbeatTime = System.currentTimeMillis();
    }

    private void handleGameStateUpdate(GameStateUpdate update) {
        // 检查是否是发给本地玩家的更新
        if (update.getTargetPlayerId() != null) {
            // 初始化本地玩家
            if (localPlayer == null) {
                for (NetworkPlayer player : update.getPlayers()) {
                    if (player.getId().equals(update.getTargetPlayerId())) {
                        localPlayer = player;
                        break;
                    }
                }
            }
            
            // 更新当前玩家ID
            currentPlayerId = update.getCurrentPlayerId();
            
            // 更新最后出的牌
            if (update.getLastPlayedCards() != null) {
                lastPlayedCards = NetworkCard.toCardList(update.getLastPlayedCards());
            }
            
            // 如果包含手牌信息，更新本地玩家手牌
            if (update.getPlayerHand() != null && localPlayer != null) {
                List<Card> hand = NetworkCard.toCardList(update.getPlayerHand());
                localPlayer.setHand(hand);
            }
            
            // 发送确认包
            GameStateUpdate confirmation = new GameStateUpdate();
            confirmation.setConfirmation(true);
            confirmation.setUpdateId(update.getUpdateId());
            client.sendTCP(confirmation);
        }
    }

    private void handlePlayerIdAssignment(PlayerIdAssignment idAssignment) {
        // 检查是否是发给本地玩家的ID分配
        if (idAssignment.getClientId().equals(localPlayer.getTempId())) {
            // 更新本地玩家ID
            localPlayer.setId(idAssignment.getServerId());
            logger.info("收到服务器分配的ID: " + idAssignment.getServerId());
            
            // 发送PlayerAction.JOIN消息，使用新分配的ID
            PlayerAction action = new PlayerAction(localPlayer.getId(), ActionType.JOIN);
            action.setPlayerName(localPlayer.getName());
            client.sendTCP(action);
        }
    }

    private void notifyStateListeners(GameEvent event) {
        for (GameStateListener listener : stateListeners) {
            listener.onGameStateChanged(event);
        }
    }

    public void playCards(List<Card> cards) {
        if (!isConnected() || !isGameStarted() || isGameEnded()) {
            return;
        }
        
        // 检查是否是有效的出牌
        if (!isValidPlay(cards)) {
            return;
        }
        
        // 转换为网络传输对象
        List<NetworkCard> networkCards = NetworkCard.fromCardList(cards);
        PlayerAction action = new PlayerAction(localPlayer.getId(), ActionType.PLAY_CARD);
        action.setPlayedCards(networkCards);
        client.sendTCP(action);
    }
    
    /**
     * 发送过牌请求到服务器
     */
    public void pass() {
        if (!isConnected() || !isGameStarted() || isGameEnded()) {
            return;
        }
        
        PlayerAction action = new PlayerAction(localPlayer.getId(), ActionType.PASS);
        client.sendTCP(action);
    }
    
    private boolean isValidPlay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        
        // 如果是第一手牌，只需要检查是否是有效牌型
        if (lastPlayedCards.isEmpty()) {
            return currentRule.isValidPattern(cards);
        }
        
        // 否则需要检查是否能大过上一手牌
        return currentRule.canCompare(cards, lastPlayedCards) && 
               currentRule.compareCards(cards, lastPlayedCards) > 0;
    }
    
    public List<List<Card>> getValidPlays(List<Card> hand) {
        if (hand == null || hand.isEmpty()) {
            return new ArrayList<>();
        }
        return currentRule.getValidPlays(hand, lastPlayedCards);
    }
    
    public boolean canPlay(List<Card> cards) {
        return isValidPlay(cards);
    }

    public Rule getCurrentRule() {
        return currentRule;
    }

    public List<Card> getLastPlayedCards() {
        return new ArrayList<>(lastPlayedCards);
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

    // 使用接口中定义的GameEvent枚举和GameStateListener接口
} 