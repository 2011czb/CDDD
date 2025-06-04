package Game;

/**
 * 游戏状态监听器接口
 */
public interface GameStateListener {
    void onGameStateChanged(GameStateManager.EventType eventType, GameStateManager stateManager);
} 