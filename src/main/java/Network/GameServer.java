package Network;

import Network.model.NetworkPlayer;
import Network.model.NetworkCard;
import Network.packets.*;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;
import Network.PacketRegister;
import cards.Card;
import cards.Rank;
import cards.Suit;
import cards.Deck;
import PokerPatterns.generator.*;
import Rules.Rule;
import Rules.RuleManager;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.basis.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameServer {
    private static final int TCP_PORT = 54555;
    private static final int UDP_PORT = 54777;
    private static final int DISCOVERY_PORT = 54556;
    private static final int HEARTBEAT_INTERVAL = 5000; // 5秒发送一次心跳

    private final Server server;
    private final Map<Connection, NetworkPlayer> connectedPlayers;
    private final List<NetworkPlayer> playerList;
    private NetworkPlayer currentPlayer;
    private boolean gameStarted;
    private List<NetworkCard> lastPlayedCards;
    private int lastPlayerIndex;
    private String hostName;
    private Rule currentRule;  // 改为Rule类型
    private AtomicBoolean gameRunning;
    private Timer heartbeatTimer;
    private String currentPlayerId;
    private Map<String, List<Card>> playerHands;
    private Deck deck;
    private final Map<String, GameStateUpdate> pendingUpdates;
    private static final long UPDATE_TIMEOUT = 5000;
    
    // 添加日志系统
    private static final Logger logger = Logger.getLogger(GameServer.class.getName());
    private FileHandler fileHandler;
    private ConsoleHandler consoleHandler;
    private RuleManager ruleManager;

    public GameServer() {
        server = new Server();
        connectedPlayers = new ConcurrentHashMap<>();
        playerList = new ArrayList<>();
        gameStarted = false;
        lastPlayedCards = null;
        lastPlayerIndex = -1;
        hostName = "房主";
        this.ruleManager = RuleManager.getInstance(); // Initialize RuleManager
        this.currentRule = this.ruleManager.getCurrentRule(); // Get initial rule from RuleManager
        gameRunning = new AtomicBoolean(false);
        playerHands = new ConcurrentHashMap<String, List<Card>>();
        pendingUpdates = new ConcurrentHashMap<>();

        setupLogging();
        registerPackets();
        setupServerListener();
    }

    private void setupLogging() {
        try {
            // 创建日志目录
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            // 清理旧日志文件
            cleanOldLogs(logDir);

            // 设置文件处理器
            fileHandler = new FileHandler("logs/game_server_%g.log", 5 * 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // 设置控制台处理器
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            // 设置日志级别
            logger.setLevel(Level.INFO);
            
            // 添加日志清理定时任务
            startLogCleanupTask();
        } catch (IOException e) {
            System.err.println("无法设置日志系统: " + e.getMessage());
        }
    }

    private void cleanOldLogs(java.io.File logDir) {
        // 获取所有日志文件
        java.io.File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("game_server_") && name.endsWith(".log"));
        if (logFiles == null) return;

        // 按最后修改时间排序
        Arrays.sort(logFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // 保留最新的10个文件，删除其他文件
        for (int i = 10; i < logFiles.length; i++) {
            logFiles[i].delete();
        }

        // 压缩超过7天的日志文件
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        for (java.io.File logFile : logFiles) {
            if (logFile.lastModified() < sevenDaysAgo && !logFile.getName().endsWith(".gz")) {
                compressLogFile(logFile);
            }
        }
    }

    private void compressLogFile(java.io.File logFile) {
        try {
            java.io.File gzFile = new java.io.File(logFile.getAbsolutePath() + ".gz");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(logFile);
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(gzFile);
                 java.util.zip.GZIPOutputStream gzipOut = new java.util.zip.GZIPOutputStream(fos)) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, len);
                }
            }
            // 压缩成功后删除原文件
            logFile.delete();
        } catch (IOException e) {
            logger.warning("压缩日志文件失败: " + logFile.getName() + " - " + e.getMessage());
        }
    }

    private void startLogCleanupTask() {
        Timer cleanupTimer = new Timer();
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    cleanOldLogs(new java.io.File("logs"));
                } catch (Exception e) {
                    logger.warning("清理日志文件失败: " + e.getMessage());
                }
            }
        }, 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000); // 每24小时执行一次
    }

    public void setHostName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.hostName = name.trim();
        }
    }

    public void setGameRule(String gameRuleName) {
        if (this.ruleManager == null) { // Defensive check, should be initialized in constructor
            this.ruleManager = RuleManager.getInstance();
        }
        this.ruleManager.switchRule(gameRuleName); // Switch rule in RuleManager
        this.currentRule = this.ruleManager.getCurrentRule(); // Update local currentRule
        logger.info("游戏规则已切换为: " + (this.currentRule != null ? this.currentRule.getName() : "未知"));
        // Optionally, broadcast this rule change to clients if not already handled elsewhere
        // broadcastRuleChange(this.currentRule.getName()); 
    }

    private void registerPackets() {
        // 使用PacketRegister统一注册所有网络包
        PacketRegister.registerPacketsFor(server);
    }

    private void setupServerListener() {
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                logger.info("新的客户端连接：" + connection.getRemoteAddressTCP());
                // 创建新玩家并加入游戏
                String playerId = java.util.UUID.randomUUID().toString();
                String playerName = "玩家" + (playerList.size() + 1);
                NetworkPlayer player = new NetworkPlayer(playerId, playerName);
                connectedPlayers.put(connection, player);
                playerList.add(player);
                logger.info("当前玩家数：" + playerList.size() + "/4");
                logger.info("新玩家ID：" + playerId);
                
                // 发送当前游戏状态给新连接的玩家
                GameStateUpdate update = new GameStateUpdate(playerList, currentPlayer != null ? currentPlayer.getId() : null);
                update.setTargetPlayerId(playerId);
                connection.sendTCP(update);
            }

            @Override
            public void received(Connection connection, Object object) {
                logger.info("收到消息类型：" + object.getClass().getSimpleName());
                if (object instanceof GameStateUpdate) {
                    GameStateUpdate update = (GameStateUpdate) object;
                    if (update.isConfirmation()) {
                        // 处理确认包
                        pendingUpdates.remove(update.getUpdateId());
                        return;
                    }
                } else if (object instanceof PlayerAction) {
                    handlePlayerAction((PlayerAction) object);
                } else if (object instanceof EndTurn) {
                    handleEndTurn(connection, (EndTurn) object);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                logger.info("客户端断开连接：" + connection.getRemoteAddressTCP());
                handleDisconnection(connection);
            }
        });
    }

    public void start() throws IOException {
        server.start();
        server.bind(TCP_PORT, UDP_PORT);
        gameRunning.set(true);
        startHeartbeat();
        logger.info("服务器已启动，监听端口: TCP=" + TCP_PORT + ", UDP=" + UDP_PORT);
        
        // 创建服务器玩家（房主）
        String hostId = java.util.UUID.randomUUID().toString();
        NetworkPlayer hostPlayer = new NetworkPlayer(hostId, hostName);
        playerList.add(hostPlayer);
        
        // 只有在游戏未开始时才设置currentPlayer
        if (!gameStarted) {
            currentPlayer = hostPlayer;
        }
        
        logger.info(hostName + "已加入，当前玩家数：" + playerList.size() + "/4");
        
        // 获取并显示客户端连接需要的IP地址
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            logger.info("客户端连接地址：" + ipAddress);
        } catch (Exception e) {
            logger.severe("获取IP地址失败：" + e.getMessage());
        }
    }

    public void stop() {
        // 广播服务器关闭消息
        GameEnd gameEnd = new GameEnd(null, "服务器已关闭");
        server.sendToAllTCP(gameEnd);
        server.stop();
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }
        gameRunning.set(false);
        logger.info("服务器已停止");
    }

    private void handlePlayerAction(PlayerAction action) {
        NetworkPlayer player = getPlayerByConnection(action.getConnection());
        if (player == null) return;

        switch (action.getActionType()) {
            case PLAY_CARD:
                List<Card> playedCards = action.getPlayedCards().stream()
                    .map(NetworkCard::getCard)
                    .collect(Collectors.toList());
                if (isValidPlay(player, playedCards, ActionType.PLAY_CARD)) {
                    handlePlayCard(player, action.getPlayedCards());
                }
                break;
            case PASS:
                handlePass(player);
                break;
            case END_TURN:
                handleEndTurn(action.getConnection(), new EndTurn());
                break;
            case READY:
                handleReady(player);
                break;
            case UNREADY:
                handleUnready(player);
                break;
            case JOIN:
                handleJoinGame(action.getConnection(), action);
                break;
            case LEAVE_GAME:
                handleLeaveGame(player);
                break;
            default:
                logger.warning("未知的动作类型：" + action.getActionType());
                break;
        }
    }

    private void handlePlayCard(NetworkPlayer player, List<NetworkCard> networkCards) {
        List<Card> cards = networkCards.stream()
            .map(NetworkCard::getCard)
            .collect(Collectors.toList());
            
        if (!gameStarted || !player.getId().equals(currentPlayer.getId())) {
            logger.warning(String.format("玩家 %s 不能出牌：不是当前玩家或游戏未开始", player.getName()));
            return;
        }

        // 检查是否是有效的出牌
        if (!isValidPlay(player, cards, ActionType.PLAY_CARD)) {
            logger.warning(String.format("玩家 %s 出牌不符合规则", player.getName()));
            return;
        }

        // 从玩家手牌中移除卡牌
        for (Card card : cards) {
            player.removeCard(card);
        }

        lastPlayedCards = new ArrayList<>(networkCards);
        lastPlayerIndex = playerList.indexOf(player);

        StringBuilder playInfo = new StringBuilder();
        playInfo.append("\n=== 玩家出牌 ===\n");
        playInfo.append("玩家：").append(player.getName()).append("\n");
        playInfo.append("出的牌：");
        for (NetworkCard card : networkCards) {
            playInfo.append(card.getDisplayName()).append(" ");
        }
        logger.info(playInfo.toString());

        // 检查是否获胜
        if (player.getHand().isEmpty()) {
            endGame(player, "玩家获胜");
            return;
        }

        // 切换到下一个玩家
        nextPlayer();
        logger.info("轮到玩家：" + currentPlayer.getName());

        // 广播游戏状态更新
        broadcastGameState();
    }

    private boolean canPass(NetworkPlayer player) {
        // 检查是否是当前玩家
        if (!player.getId().equals(currentPlayer.getId())) {
            return false;
        }

        // 检查是否是第一轮
        if (lastPlayedCards == null) {
            logger.warning("你是第一个出牌的玩家，必须出牌");
            return false;
        }

        // 检查是否是最后一个出牌的玩家
        if (lastPlayerIndex == playerList.indexOf(player)) {
            logger.warning("其他玩家都过牌了，你必须出牌");
            return false;
        }

        return true;
    }

    private void handlePass(NetworkPlayer player) {
        if (!gameStarted || !player.getId().equals(currentPlayer.getId())) {
            logger.warning(String.format("玩家 %s 不能过牌：不是当前玩家或游戏未开始", player.getName()));
            return;
        }

        // 检查是否可以过牌
        if (!canPass(player)) {
            logger.warning(String.format("玩家 %s 不能过牌", player.getName()));
            return;
        }

        logger.info("\n=== 玩家过牌 ===");
        logger.info("玩家：" + player.getName() + " 选择过牌");

        // 切换到下一个玩家
        nextPlayer();
        logger.info("轮到玩家：" + currentPlayer.getName());

        // 广播游戏状态更新
        broadcastGameState();
    }

    private void handleEndTurn(Connection connection, EndTurn endTurn) {
        String playerId = connection.getRemoteAddressTCP().toString();
        if (currentPlayerId.equals(playerId)) {
            // 处理回合结束逻辑
            endCurrentTurn();
        }
    }

    private void handleReady(NetworkPlayer player) {
        player.setReady(true);
        broadcastGameState();

        // 检查是否可以开始游戏
        if (canStartGame()) {
            startGame();
        }
    }

    private void handleUnready(NetworkPlayer player) {
        player.setReady(false);
        broadcastGameState();
    }

    private void handleJoinGame(Connection connection, PlayerAction action) {
        logger.info("\n=== 处理玩家加入请求 ===");
        logger.info("收到玩家加入请求：" + action.getPlayerName());
        logger.info("客户端临时ID：" + action.getPlayerId());
        logger.info("连接ID：" + connection.getID());
        
        // 检查玩家是否已存在
        NetworkPlayer existingPlayer = connectedPlayers.get(connection);
        if (existingPlayer != null) {
            logger.info("玩家已存在，更新信息");
            logger.info("玩家ID分配情况：");
            logger.info("  玩家名称：" + action.getPlayerName());
            logger.info("  客户端临时ID：" + action.getPlayerId());
            logger.info("  服务器分配ID：" + existingPlayer.getId());
            logger.info("  连接ID：" + connection.getID());
            
            // 更新玩家名称
            existingPlayer.setName(action.getPlayerName());
            
            // 发送确认消息给客户端，使用临时ID
            GameStateUpdate update = new GameStateUpdate(playerList, currentPlayer != null ? currentPlayer.getId() : null);
            update.setTargetPlayerId(action.getPlayerId());  // 使用临时ID
            update.setPlayerCount(playerList.size());
            
            // 确保玩家列表包含正确的玩家信息
            boolean found = false;
            for (NetworkPlayer p : update.getPlayers()) {
                if (p.getId().equals(existingPlayer.getId())) {
                    p.setName(action.getPlayerName());
                    found = true;
                    break;
                }
            }
            if (!found) {
                update.getPlayers().add(existingPlayer);
            }
            
            connection.sendTCP(update);
            logger.info("已发送确认消息给玩家：" + action.getPlayerName());
            logger.info("使用临时ID：" + action.getPlayerId());
            logger.info("服务器分配ID：" + existingPlayer.getId());
            logger.info("当前玩家数：" + playerList.size() + "/4");
            
            // 如果玩家数量达到4人，自动开始游戏
            if (playerList.size() == 4) {
                logger.info("玩家数量已满，自动开始游戏");
                startGame();
            } else {
                // 广播游戏状态给所有玩家
                broadcastGameState();
            }
        } else {
            logger.warning("警告：找不到玩家连接信息");
        }
        logger.info("=== 处理玩家加入请求结束 ===\n");
    }

    private void handleLeaveGame(NetworkPlayer player) {
        playerList.remove(player);
        if (gameStarted) {
            endGame();
        }
        broadcastGameState();
    }

    private void handleDisconnection(Connection connection) {
        NetworkPlayer player = connectedPlayers.remove(connection);
        if (player != null) {
            logger.info("玩家 " + player.getName() + " 断开连接");
            playerList.remove(player);
            
            // 如果断开的玩家是当前玩家，选择新的当前玩家
            if (currentPlayer != null && currentPlayer.getId().equals(player.getId())) {
                if (!playerList.isEmpty()) {
                    currentPlayer = playerList.get(0);
                } else {
                    currentPlayer = null;
                }
            }
            
            if (gameStarted) {
                endGame();
            }
            broadcastGameState();
        }
    }

    private void nextPlayer() {
        int currentIndex = playerList.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % playerList.size();
        currentPlayer = playerList.get(nextIndex);
    }

    private boolean canStartGame() {
        return playerList.size() >= 2 && playerList.size() <= 4 &&
               playerList.stream().allMatch(NetworkPlayer::isReady);
    }

    private void startGame() {
        if (gameStarted) {
            return;
        }
        
        logger.info("\n=== 开始游戏 ===");
        gameStarted = true;
        currentPlayer = playerList.get(0);
        lastPlayedCards = null;
        lastPlayerIndex = -1;

        // 初始化牌组并给每个玩家发牌
        deck = new Deck();
        deck.shuffle();
        
        // 检查牌堆容量
        int cardsPerPlayer = 13;
        int totalCardsNeeded = playerList.size() * cardsPerPlayer;
        
        if (deck.cardsRemaining() < totalCardsNeeded) {
            logger.warning("警告：牌堆容量不足（" + deck.cardsRemaining() + "张），标准游戏需要" + totalCardsNeeded + "张牌");
            cardsPerPlayer = deck.cardsRemaining() / playerList.size();
            logger.warning("调整为每位玩家发" + cardsPerPlayer + "张牌");
        }
        
        // 给每个玩家发牌
        List<NetworkPlayer> playersCopy = new ArrayList<>(playerList);
        for (NetworkPlayer player : playersCopy) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < cardsPerPlayer; i++) {
                if (deck.cardsRemaining() > 0) {
                    Optional<Card> cardOpt = deck.dealCard();
                    if (cardOpt.isPresent()) {
                        hand.add(cardOpt.get());
                    }
                }
            }
            
            // 对手牌进行排序
            Collections.sort(hand, (card1, card2) -> card2.getWeight() - card1.getWeight());
            player.setHand(hand);
            
            // 如果是服务器玩家（房主），直接显示手牌
            if (player.getName().equals(hostName)) {
                logger.info("\n=== 服务器玩家视图 ===");
                logger.info("你的手牌：");
                for (Card card : hand) {
                    logger.info(card.getDisplayName());
                }
                logger.info("===================\n");
            } else {
                logger.info("玩家 " + player.getName() + " 获得 " + hand.size() + " 张牌");
            }
        }

        // 广播游戏开始
        GameStart startPacket = new GameStart(playersCopy, currentPlayer, currentRule.getName());
        server.sendToAllTCP(startPacket);
        logger.info("游戏开始！当前玩家：" + currentPlayer.getName());

        // 广播游戏状态
        broadcastGameState();
        logger.info("=== 开始游戏结束 ===\n");
    }

    private Connection getConnectionByPlayerId(String playerId) {
        for (Map.Entry<Connection, NetworkPlayer> entry : connectedPlayers.entrySet()) {
            if (entry.getValue().getId().equals(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void endGame() {
        gameStarted = false;
        // 找出获胜者
        NetworkPlayer winner = null;
        for (NetworkPlayer player : playerList) {
            if (player.getHand().isEmpty()) {
                winner = player;
                break;
            }
        }
        // 广播游戏结束
        GameEnd gameEnd = new GameEnd(winner, "游戏结束");
        server.sendToAllTCP(gameEnd);
    }

    private boolean checkGameEnd() {
        // 检查是否有玩家手牌为空
        for (NetworkPlayer player : playerList) {
            if (player.getHand().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void broadcastGameState() {
        if (playerList.isEmpty()) {
            return;
        }
        
        logger.info("=== 广播游戏状态 ===");
        if (currentPlayer == null) {
            currentPlayer = playerList.get(0);
        }
        
        if (!gameStarted) {
            logger.info("游戏未开始，发送玩家数量");
            for (NetworkPlayer player : playerList) {
                GameStateUpdate update = new GameStateUpdate(playerList, null);
                String targetId = player.getTempId() != null ? player.getTempId() : player.getId();
                update.setTargetPlayerId(targetId);
                update.setPlayerCount(playerList.size());
                
                if (player.getName().equals(hostName)) {
                    displayServerPlayerView();
                } else {
                    Connection connection = getConnectionByPlayerId(player.getId());
                    if (connection != null) {
                        connection.sendTCP(update);
                        pendingUpdates.put(update.getUpdateId(), update);
                        logger.info(String.format("已发送状态更新给玩家：%s (ID: %s, 更新ID: %s)", 
                            player.getName(), targetId, update.getUpdateId()));
                    } else {
                        logger.warning("找不到玩家 " + player.getName() + " 的连接");
                    }
                }
            }
            logger.info("=== 广播游戏状态结束 ===\n");
            return;
        }
        
        // 游戏开始后的状态广播
        for (NetworkPlayer player : playerList) {
            logger.info(String.format("\n处理玩家：%s (ID: %s)", player.getName(), player.getId()));
            
            GameStateUpdate update = new GameStateUpdate(playerList, currentPlayer.getId());
            String targetId = player.getTempId() != null ? player.getTempId() : player.getId();
            update.setTargetPlayerId(targetId);
            
            if (lastPlayedCards != null) {
                update.setLastPlayedCards(lastPlayedCards);
                StringBuilder lastPlayedCardsStr = new StringBuilder("最后出的牌：");
                for (NetworkCard card : lastPlayedCards) {
                    lastPlayedCardsStr.append(card.getDisplayName()).append(" ");
                }
                logger.info(lastPlayedCardsStr.toString());
            }
            
            if (player.getName().equals(hostName)) {
                displayServerPlayerView();
            } else {
                Connection connection = getConnectionByPlayerId(player.getId());
                if (connection != null) {
                    if (player.getId().equals(update.getTargetPlayerId())) {
                        update.setPlayerHand(NetworkCard.fromCardList(player.getHand()));
                        logger.info(String.format("发送手牌给玩家：%s (手牌数量：%d)", 
                            player.getName(), player.getHand().size()));
                    }
                    connection.sendTCP(update);
                    pendingUpdates.put(update.getUpdateId(), update);
                    logger.info(String.format("已发送状态更新给玩家：%s (ID: %s, 更新ID: %s)", 
                        player.getName(), targetId, update.getUpdateId()));
                } else {
                    logger.warning("找不到玩家 " + player.getName() + " 的连接");
                }
            }
        }
        logger.info("=== 广播游戏状态结束 ===\n");
    }

    private NetworkPlayer getPlayerByConnection(Connection connection) {
        return connectedPlayers.get(connection);
    }

    protected boolean isValidPlay(NetworkPlayer player, List<Card> cards, ActionType actionType) {
        if (actionType != ActionType.PLAY_CARD) {
            return false;
        }

        // 检查是否是空牌
        if (cards == null || cards.isEmpty()) {
            return canPass(player);
        }

        // 检查玩家是否拥有这些牌
        for (Card card : cards) {
            if (!player.hasCard(card)) {
                logger.warning("玩家没有这些牌");
                return false;
            }
        }

        // 获取牌型
        PokerPattern pattern = PokerPattern.getPattern(cards);
        if (pattern == null) {
            logger.warning("不是有效的牌型");
            return false;
        }

        // 如果是第一手牌，检查是否包含方块三
        if (lastPlayedCards == null) {
            boolean hasDiamondThree = false;
            for (Card card : cards) {
                if (card.getSuit() == Suit.DIAMONDS && 
                    card.getRank() == Rank.THREE) {
                    hasDiamondThree = true;
                    break;
                }
            }
            if (!hasDiamondThree) {
                logger.warning("第一手牌必须包含方块三");
                return false;
            }
            return true;
        }

        // 如果是跟牌
        if (lastPlayedCards != null && !lastPlayedCards.isEmpty()) {
            // 如果上一个出牌的是自己（其他人都过牌），可以出任意牌型
            if (lastPlayerIndex == playerList.indexOf(player)) {
                return true;
            }

            // 将上一手牌转换为Card列表
            List<Card> lastCards = NetworkCard.toCardList(lastPlayedCards);

            // 获取上一手牌的牌型
            PokerPattern lastPattern = PokerPattern.getPattern(lastCards);
            if (lastPattern == null) {
                logger.warning("上一手牌型无效");
                return false;
            }

            // 检查是否是相同的牌型
            if (!pattern.getClass().equals(lastPattern.getClass())) {
                logger.warning("牌型不匹配");
                return false;
            }

            // 比较牌型大小
            if (pattern.getPatternWeight() <= lastPattern.getPatternWeight()) {
                logger.warning("出的牌没有大过上家的牌");
                return false;
            }
        }

        return true;
    }

    private void endGame(NetworkPlayer winner, String reason) {
        GameEnd gameEnd = new GameEnd(winner, reason);
        server.sendToAllTCP(gameEnd);
        gameStarted = false;
    }

    private void updateGameState() {
        GameStateUpdate update = new GameStateUpdate(playerList, currentPlayer.getId());
        server.sendToAllTCP(update);
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    public boolean areAllPlayersReady() {
        return playerList.size() == 4 && playerList.stream().allMatch(NetworkPlayer::isReady);
    }

    public List<String> getPlayerNames() {
        return playerList.stream().map(NetworkPlayer::getName).collect(java.util.stream.Collectors.toList());
    }

    private void startHeartbeat() {
        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameRunning.get()) {
                    server.sendToAllTCP(new Heartbeat());
                    checkPendingUpdates();  // 检查超时的更新包
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
    }

    public boolean isGameRunning() {
        return gameRunning.get();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    private void endCurrentTurn() {
        // 实现回合结束逻辑
        nextPlayer();
        broadcastGameState();
    }

    private void initializeGame() {
        // 初始化游戏状态
        gameStarted = true;
        currentPlayerId = playerList.get(0).getId();
        deck = new Deck();
        deck.shuffle();
        
        // 发牌给每个玩家
        for (NetworkPlayer player : playerList) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Optional<Card> card = deck.dealCard();
                if (card.isPresent()) {
                    hand.add(card.get());
                }
            }
            playerHands.put(player.getId(), hand);
        }
        
        // 通知所有玩家游戏开始
        broadcastGameStart();
        ruleManager = RuleManager.getInstance();
        ruleManager.switchRule("NORTH"); // 默认使用北方规则
    }

    private void broadcastGameStart() {
        // 实现广播游戏开始逻辑
    }

    // 添加检查超时更新的方法
    private void checkPendingUpdates() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, GameStateUpdate> entry : pendingUpdates.entrySet()) {
            GameStateUpdate update = entry.getValue();
            String targetPlayerId = update.getTargetPlayerId();
            Connection connection = getConnectionByPlayerId(targetPlayerId);
            
            if (connection != null) {
                logger.warning("警告：更新包 " + entry.getKey() + " 超时未收到确认，重新发送给玩家 " + targetPlayerId);
                connection.sendTCP(update);
            } else {
                logger.warning("警告：找不到玩家 " + targetPlayerId + " 的连接，无法重发更新包");
                pendingUpdates.remove(entry.getKey());
            }
        }
    }

    private void displayServerPlayerView() {
        StringBuilder view = new StringBuilder();
        view.append("\n=== 服务器玩家视图 ===\n");
        view.append("当前玩家：").append(currentPlayer != null ? currentPlayer.getName() : "无").append("\n");
        view.append("玩家列表：\n");
        
        for (NetworkPlayer player : playerList) {
            view.append("  ").append(player.getName())
                .append(player.isReady() ? " (已准备)" : " (未准备)")
                .append(player.getId().equals(currentPlayer.getId()) ? " [当前玩家]" : "")
                .append("\n");
        }
        
        NetworkPlayer serverPlayer = playerList.stream()
            .filter(p -> p.getName().equals(hostName))
            .findFirst()
            .orElse(null);
            
        if (serverPlayer != null) {
            view.append("\n你的手牌：\n");
            List<Card> hand = serverPlayer.getHand();
            for (int i = 0; i < hand.size(); i++) {
                view.append((i + 1)).append(".").append(hand.get(i).getDisplayName()).append(" ");
                if ((i + 1) % 4 == 0) {
                    view.append("\n");
                }
            }
            view.append("\n");

            // 显示可出的牌型
            List<Card> lastCards = lastPlayedCards != null ? 
                lastPlayedCards.stream()
                    .map(NetworkCard::getCard)
                    .collect(java.util.stream.Collectors.toList()) : 
                null;

            // 获取可出的牌型
            Map<String, List<PokerPatterns.generator.CardGroup>> playablePatterns = 
                PokerPatterns.PlayablePatternUtil.getPlayablePatterns(hand, lastCards, currentRule);
            
            // 打印可出的牌型
            view.append("\n=== 可出的牌型 ===\n");
            if (playablePatterns.isEmpty()) {
                if (lastCards == null) {
                    view.append("没有包含方块三的牌型\n");
                } else if (lastPlayerIndex != -1 && lastPlayerIndex == playerList.indexOf(serverPlayer)) {
                    view.append("可以出任意牌型\n");
                } else {
                    view.append("没有能大过上一手牌的牌型\n");
                }
            } else {
                for (Map.Entry<String, List<PokerPatterns.generator.CardGroup>> entry : playablePatterns.entrySet()) {
                    view.append("\n").append(entry.getKey()).append("：\n");
                    List<PokerPatterns.generator.CardGroup> groups = entry.getValue();
                    for (int i = 0; i < groups.size(); i++) {
                        PokerPatterns.generator.CardGroup group = groups.get(i);
                        view.append(String.format("  %d. ", i + 1));
                        for (Card card : group.getCards()) {
                            view.append(card.getDisplayName()).append(" ");
                        }
                        view.append("\n");
                    }
                }
            }
        }
        
        if (lastPlayedCards != null && !lastPlayedCards.isEmpty()) {
            view.append("\n最后出的牌：");
            for (NetworkCard card : lastPlayedCards) {
                view.append(card.getDisplayName()).append(" ");
            }
            view.append("\n");
        }
        
        view.append("===================\n");
        logger.info(view.toString());
    }

    // 添加一个方法来显示服务器玩家的手牌和可出牌型
    public void displayServerPlayerHand() {
        if (!gameStarted) {
            logger.warning("游戏未开始");
            return;
        }

        NetworkPlayer serverPlayer = playerList.stream()
            .filter(p -> p.getName().equals(hostName))
            .findFirst()
            .orElse(null);

        if (serverPlayer == null) {
            logger.warning("找不到服务器玩家");
            return;
        }

        displayServerPlayerView();
    }

    // 修改serverPlayerPlayCards方法，添加输入提示
    public void serverPlayerPlayCards(String input) {
        if (!gameStarted) {
            logger.warning("游戏未开始，无法出牌");
            return;
        }

        NetworkPlayer serverPlayer = playerList.stream()
            .filter(p -> p.getName().equals(hostName))
            .findFirst()
            .orElse(null);

        if (serverPlayer == null) {
            logger.warning("找不到服务器玩家");
            return;
        }

        if (!serverPlayer.getId().equals(currentPlayer.getId())) {
            logger.warning("还没轮到服务器玩家出牌");
            return;
        }

        // 如果输入为"P"或"p"，则过牌
        if (input.equalsIgnoreCase("P")) {
            serverPlayerPass();
            return;
        }

        List<Card> selectedCards = new ArrayList<>();
        List<Card> hand = serverPlayer.getHand();

        try {
            // 解析输入的卡牌编号
            String[] indexStrings = input.split("\\s+");
            for (String indexStr : indexStrings) {
                int index = Integer.parseInt(indexStr) - 1; // 转换为0基索引
                if (index < 0 || index >= hand.size()) {
                    logger.warning("无效的卡牌索引: " + (index + 1));
                    return;
                }
                selectedCards.add(hand.get(index));
            }
        } catch (NumberFormatException e) {
            logger.warning("输入格式错误，请输入数字或'P'");
            return;
        }

        // 转换为NetworkCard并处理出牌
        List<NetworkCard> networkCards = NetworkCard.fromCardList(selectedCards);
        handlePlayCard(serverPlayer, networkCards);
    }

    public void serverPlayerPass() {
        if (!gameStarted) {
            logger.warning("游戏未开始，无法过牌");
            return;
        }

        NetworkPlayer serverPlayer = playerList.stream()
            .filter(p -> p.getName().equals(hostName))
            .findFirst()
            .orElse(null);

        if (serverPlayer == null) {
            logger.warning("找不到服务器玩家");
            return;
        }

        if (!serverPlayer.getId().equals(currentPlayer.getId())) {
            logger.warning("还没轮到服务器玩家过牌");
            return;
        }

        // 处理过牌
        handlePass(serverPlayer);
    }

    private void handleRuleChange(String ruleName) {
        ruleManager.switchRule(ruleName);
        // 通知所有客户端规则变更
        broadcastRuleChange(ruleName);
    }

    private void broadcastRuleChange(String ruleName) {
        // ... existing code ...
    }

    private void drawCard(NetworkPlayer player) {
        if (deck.cardsRemaining() > 0) {
            Optional<Card> cardOpt = deck.dealCard();
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();
                player.addCard(NetworkCard.fromCard(card));
            }
        }
    }
} 