package Game;

import Players.Player;
import java.util.List;
import cards.Card;

/**
 * 游戏状态监听器接口
 */
public interface GameStateListener {
    void onGameStateChanged(GameStateManager.EventType eventType, GameStateManager stateManager);
    void onCardPlayed(Player player, List<Card> cards);
    void onRoundEnd();
} 