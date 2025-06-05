package Game;

import Players.Player;
import cards.Card;
import java.util.List;

/**
 * 游戏状态管理器
 * 负责管理游戏状态，包括当前玩家、上一手牌、游戏结束状态等
 */
public class GameStateManager {
    private int currentPlayerIndex; // 当前玩家索引
    private int lastPlayerIndex; // 上一个实际出牌的玩家索引
    private List<Card> lastPlayedCards; // 上一次出的牌
    private boolean gameEnded; // 游戏是否结束
    private Player winner; // 获胜者
    private final List<Player> players; // 玩家列表

    public GameStateManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex =0;
        this.lastPlayerIndex = -1;
        this.lastPlayedCards = null;
        this.gameEnded = false;
        this.winner = null;
    }

    /**
     * 返回手中有方块三的玩家索引
     * @return 持有方块三的玩家索引，如果没有玩家持有方块三则返回0
     */
    public int selectFirstPlayer() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (Card card : player.getHand()) {
                // 方块三的intValue为41
                if (card.getIntValue() == 41) {
                    return i;
                }
            }
        }
        return 0; // 如果没有找到持有方块三的玩家，返回0
    }

    /**
     * 获取当前玩家
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * 获取上一个出牌的玩家
     */
    public Player getLastPlayer() {
        return lastPlayerIndex == -1 ? null : players.get(lastPlayerIndex);
    }

    /**
     * 获取上一手牌
     */
    public List<Card> getLastPlayedCards() {
        return lastPlayedCards;
    }

    /**
     * 更新游戏状态
     */
    public void updateState(Player player, List<Card> playedCards) {
        if (playedCards != null && !playedCards.isEmpty()) {
            lastPlayedCards = playedCards;
            lastPlayerIndex = currentPlayerIndex;
        }

        // 检查游戏是否结束
        if (player.getHand().isEmpty()) {
            gameEnded = true;
            winner = player;
        }
    }

    public void setCurrentPlayerIndex(int index){
        this.currentPlayerIndex = index;
    }

    /**
     * 切换到下一个玩家
     */
    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * 重置游戏状态
     */
    public void reset() {
        currentPlayerIndex = 0;
        lastPlayerIndex = -1;
        lastPlayedCards = null;
        gameEnded = false;
        winner = null;
    }

    /**
     * 获取游戏是否结束
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * 获取获胜者
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * 获取当前玩家索引
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * 获取上一个出牌玩家索引
     */
    public int getLastPlayerIndex() {
        return lastPlayerIndex;
    }
}