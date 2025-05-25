package Network;

import Game.Game;
import Players.Player;
import cards.Card;
import java.util.List;

/**
 * 网络管理器类，负责联机模式的网络通信
 * 注意：这是一个简化的示例实现，实际的联机模式需要更复杂的网络通信功能
 */
public class NetworkManager {
    private static NetworkManager instance;
    private boolean isServer;
    private boolean isConnected;
    private int playerCount = 0;
    private Game currentGame;
    
    // 私有构造函数，使用单例模式
    private NetworkManager() {
        this.isServer = false;
        this.isConnected = false;
        this.playerCount = 0;
    }
    
    /**
     * 获取NetworkManager的单例实例
     */
    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    
    /**
     * 创建服务器
     * @param serverName 服务器名称
     * @return 是否成功创建
     */
    public boolean createServer(String serverName) {
        // 实际实现中，这里会创建一个网络服务器监听连接
        System.out.println("创建服务器: " + serverName);
        this.isServer = true;
        this.isConnected = true;
        this.playerCount = 1; // 包括主机玩家
        return true;
    }
    
    /**
     * 连接到服务器
     * @param serverAddress 服务器地址
     * @param playerName 玩家名称
     * @return 是否成功连接
     */
    public boolean connectToServer(String serverAddress, String playerName) {
        // 实际实现中，这里会连接到指定服务器
        System.out.println("连接到服务器: " + serverAddress + "，玩家: " + playerName);
        this.isServer = false;
        this.isConnected = true;
        this.playerCount++; // 增加玩家计数
        return true;
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        // 实际实现中，这里会断开网络连接
        System.out.println("断开连接");
        this.isConnected = false;
    }
    
    /**
     * 发送游戏开始信号
     * @param game 要开始的游戏
     */
    public void sendGameStartSignal(Game game) {
        // 实际实现中，这里会向所有客户端发送游戏开始信号
        System.out.println("发送游戏开始信号");
        this.currentGame = game;
    }
    
    /**
     * 发送玩家出牌信息
     * @param player 出牌的玩家
     * @param cards 出的牌
     */
    public void sendPlayerAction(Player player, List<Card> cards) {
        // 实际实现中，这里会向所有其他玩家发送出牌信息
        if (cards == null || cards.isEmpty()) {
            System.out.println("发送玩家动作: " + player.getName() + " 选择不出牌");
        } else {
            System.out.println("发送玩家动作: " + player.getName() + " 出牌 " + cardsToString(cards));
        }
    }
    
    /**
     * 接收远程玩家的动作
     * @return 远程玩家的出牌，如果没有则返回null
     */
    public PlayerAction receiveRemoteAction() {
        // 实际实现中，这里会接收来自网络的玩家动作
        // 这里只是一个简化的模拟实现
        System.out.println("等待远程玩家动作...");
        
        // 模拟随机延迟
        try {
            Thread.sleep((long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 返回null表示暂时没有远程动作
        return null;
    }
    
    /**
     * 检查是否所有玩家都已经准备好
     * @return 是否所有玩家都已准备
     */
    public boolean areAllPlayersReady() {
        // 实际实现中，这里会检查所有连接的玩家是否都已准备
        // 这里简化为玩家数量达到4即表示准备好
        return this.playerCount == 4;
    }
    
    /**
     * 发送游戏结束信息
     * @param winner 获胜玩家
     */
    public void sendGameEndSignal(Player winner) {
        // 实际实现中，这里会向所有客户端发送游戏结束信号
        System.out.println("发送游戏结束信号，获胜者: " + (winner != null ? winner.getName() : "无"));
    }
    
    /**
     * 是否是服务器
     */
    public boolean isServer() {
        return isServer;
    }
    
    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * 获取当前连接的玩家数量
     */
    public int getPlayerCount() {
        return playerCount;
    }
    
    /**
     * 获取当前游戏
     */
    public Game getCurrentGame() {
        return currentGame;
    }
    
    /**
     * 辅助方法：将牌列表转换为字符串
     */
    private String cardsToString(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (Card card : cards) {
            sb.append(card.getDisplayName()).append(", ");
        }
        sb.setLength(sb.length() - 2); // 移除最后的", "
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 玩家动作类，表示玩家的出牌动作
     */
    public static class PlayerAction {
        private Player player;
        private List<Card> cards;
        
        public PlayerAction(Player player, List<Card> cards) {
            this.player = player;
            this.cards = cards;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public List<Card> getCards() {
            return cards;
        }
    }
} 