package Network.client;

import java.io.IOException;
import java.util.List;
import cards.Card;
import Rules.Rule;

/**
 * 游戏客户端接口，定义客户端与服务器通信的所有方法
 */
public interface GameClientInterface {
    
    /**
     * 游戏事件类型
     */
    enum GameEvent {
        CONNECTED,       // 连接成功
        DISCONNECTED,    // 断开连接
        GAME_STARTED,    // 游戏开始
        GAME_ENDED,      // 游戏结束
        CARD_PLAYED,     // 出牌
        TURN_CHANGED,    // 轮到下一个玩家
        PLAYER_JOINED,   // 玩家加入
        PLAYER_LEFT      // 玩家离开
    }
    
    /**
     * 游戏状态监听器接口
     */
    interface GameStateListener {
        /**
         * 当游戏状态发生变化时调用
         * @param event 游戏事件类型
         */
        void onGameStateChanged(GameEvent event);
    }
    
    /**
     * 连接到服务器
     * @param host 服务器地址
     * @throws IOException 连接失败时抛出异常
     */
    void connect(String host) throws IOException;
    
    /**
     * 加入游戏
     * @param playerName 玩家名称
     */
    void joinGame(String playerName);
    
    /**
     * 断开连接
     */
    void disconnect();
    
    /**
     * 检查是否已连接到服务器
     * @return 是否已连接
     */
    boolean isConnected();
    
    /**
     * 检查游戏是否已开始
     * @return 游戏是否已开始
     */
    boolean isGameStarted();
    
    /**
     * 检查游戏是否已结束
     * @return 游戏是否已结束
     */
    boolean isGameEnded();
    
    /**
     * 添加游戏状态监听器
     * @param listener 监听器对象
     */
    void addStateListener(GameStateListener listener);
    
    /**
     * 移除游戏状态监听器
     * @param listener 要移除的监听器对象
     */
    void removeStateListener(GameStateListener listener);
    
    /**
     * 出牌
     * @param cards 要出的牌
     */
    void playCards(List<Card> cards);
    
    /**
     * 获取有效的出牌选择
     * @param hand 手牌
     * @return 有效的出牌列表
     */
    List<List<Card>> getValidPlays(List<Card> hand);
    
    /**
     * 检查是否可以出指定的牌
     * @param cards 要检查的牌
     * @return 是否可以出牌
     */
    boolean canPlay(List<Card> cards);
    
    /**
     * 获取当前游戏规则
     * @return 当前规则
     */
    Rule getCurrentRule();
    
    /**
     * 获取最后出的牌
     * @return 最后出的牌
     */
    List<Card> getLastPlayedCards();
    
    /**
     * 获取当前玩家ID
     * @return 当前玩家ID
     */
    String getCurrentPlayerId();
    
    /**
     * 获取最后出牌玩家ID
     * @return 最后出牌玩家ID
     */
    String getLastPlayerId();
} 