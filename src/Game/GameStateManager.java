package Game;

import Players.Player;
import cards.Card;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 游戏状态管理器
 * 负责管理游戏状态，包括当前玩家、上一手牌、游戏结束状态等
 */
public class GameStateManager {
    // 游戏状态枚举
    public enum GameState {
        WAITING_FOR_START,
        IN_PROGRESS,
        ROUND_END,
        GAME_END
    }
    
    // 事件类型枚举
    public enum EventType {
        PLAYER_CHANGED,
        CARDS_PLAYED,
        GAME_STATE_CHANGED,
        WINNER_DETERMINED
    }
    
    private GameState currentState;
    private int currentPlayerIndex;
    private int lastPlayerIndex;
    private List<Card> lastPlayedCards;
    private Player winner;
    private final List<Player> players;
    private final List<GameStateListener> listeners;
    
    public GameStateManager(List<Player> players) {
        this.players = players;
        this.listeners = new CopyOnWriteArrayList<>();
        reset();
    }
    
    /**
     * 重置游戏状态
     */
    public void reset() {
        this.currentState = GameState.WAITING_FOR_START;
        this.currentPlayerIndex = 0;
        this.lastPlayerIndex = -1;
        this.lastPlayedCards = null;
        this.winner = null;
        notifyListeners(EventType.GAME_STATE_CHANGED);
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        this.currentState = GameState.IN_PROGRESS;
        notifyListeners(EventType.GAME_STATE_CHANGED);
    }
    
    /**
     * 更新游戏状态
     */
    public synchronized void updateState(Player player, List<Card> playedCards) {
        // 使用事务方式更新状态
        GameStateTransaction transaction = new GameStateTransaction();
        
        try {
            if (playedCards != null && !playedCards.isEmpty()) {
                transaction.addChange(() -> {
                    this.lastPlayedCards = new ArrayList<>(playedCards);
                    this.lastPlayerIndex = currentPlayerIndex;
                });
            }
            
            if (player.getHand().isEmpty()) {
                transaction.addChange(() -> {
                    this.winner = player;
                    this.currentState = GameState.GAME_END;
                });
            }
            
            // 提交事务
            transaction.commit();
            
            // 通知监听器
            notifyListeners(EventType.CARDS_PLAYED);
            if (winner != null) {
                notifyListeners(EventType.WINNER_DETERMINED);
            }
            
        } catch (Exception e) {
            // 回滚事务
            transaction.rollback();
            throw new GameException("更新游戏状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换到下一个玩家
     */
    public void nextPlayer() {
        int oldIndex = currentPlayerIndex;
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        if (oldIndex != currentPlayerIndex) {
            notifyListeners(EventType.PLAYER_CHANGED);
        }
    }
    
    /**
     * 返回手中有方块三的玩家索引
     */
    public int selectFirstPlayer() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (Card card : player.getHand()) {
                if (card.getIntValue() == 41) {
                    return i;
                }
            }
        }
        return 0;
    }
    
    // Getters
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public Player getLastPlayer() {
        return lastPlayerIndex == -1 ? null : players.get(lastPlayerIndex);
    }
    
    public List<Card> getLastPlayedCards() {
        return lastPlayedCards;
    }
    
    public boolean isGameEnded() {
        return currentState == GameState.GAME_END;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public int getLastPlayerIndex() {
        return lastPlayerIndex;
    }
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentPlayerIndex(int index) {
        if (index != currentPlayerIndex) {
            this.currentPlayerIndex = index;
            notifyListeners(EventType.PLAYER_CHANGED);
        }
    }
    
    // 事件监听相关方法
    public void addListener(GameStateListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(GameStateListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(EventType eventType) {
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged(eventType, this);
        }
    }
}

/**
 * 游戏状态监听器接口
 */
interface GameStateListener {
    void onGameStateChanged(GameStateManager.EventType eventType, GameStateManager stateManager);
}

/**
 * 游戏状态事务类
 */
class GameStateTransaction {
    private final List<Runnable> changes = new ArrayList<>();
    private final List<Runnable> rollbacks = new ArrayList<>();
    
    public void addChange(Runnable change) {
        changes.add(change);
    }
    
    public void addRollback(Runnable rollback) {
        rollbacks.add(rollback);
    }
    
    public void commit() {
        for (Runnable change : changes) {
            change.run();
        }
    }
    
    public void rollback() {
        for (int i = rollbacks.size() - 1; i >= 0; i--) {
            rollbacks.get(i).run();
        }
    }
}