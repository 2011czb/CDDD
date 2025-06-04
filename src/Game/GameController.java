package Game;

import Players.Player;
import Players.HumanPlayer;
import Players.AIPlayer;
import cards.Card;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import java.util.*;

/**
 * 游戏控制器类
 * 负责处理前端和后端之间的通信，管理单人模式的游戏流程
 */
public class GameController {
    private final Game game;
    private final GameStateManager stateManager;
    private final GamePlayManager playManager;

    public GameController(Game game) {
        this.game = game;
        this.stateManager = game.getStateManager();
        this.playManager = game.getPlayManager();
    }

    /**
     * 处理玩家出牌请求
     * @param cards 玩家想要出的牌列表
     * @return 游戏操作响应
     */
    public GameResponse<Void> processPlayerPlay(List<Card> cards) {
        Player currentPlayer = stateManager.getCurrentPlayer();
        if (!(currentPlayer instanceof HumanPlayer)) {
            // 显式指定 Void 类型
            return new GameResponse<Void>(false, "当前不是你的回合，请等待", null, false, null);
        }

        // 验证并处理出牌
        if (!playManager.isValidPlay(cards, stateManager.getLastPlayedCards(), currentPlayer)) {
            // 显式指定 Void 类型
            return new GameResponse<Void>(false, "出牌无效，请选择其他牌", null, false, null);
        }

        // 更新游戏状态
        if (cards != null && !cards.isEmpty()) {
            currentPlayer.removeCards(cards);
            stateManager.updateState(currentPlayer, cards);
        }

        // 移动到下一个玩家
        stateManager.nextPlayer();

        // 显式指定 Void 类型
        return new GameResponse<Void>(true, "出牌成功", null, false, null);
    }

    /**
     * 处理玩家过牌请求
     * @return 游戏操作响应
     */
    public GameResponse<Void> processPlayerPass() {
        return processPlayerPlay(Collections.emptyList());
    }

    /**
     * 获取当前回合中每个玩家出的牌
     * @return 玩家出牌记录
     */
    public GameResponse<Map<String, PlayRecord>> getPlayHistory() {
        Map<String, PlayRecord> playHistory = new HashMap<>();

        List<Card> lastCards = stateManager.getLastPlayedCards();
        if (lastCards != null && !lastCards.isEmpty()) {
            int lastPlayerIndex = stateManager.getLastPlayerIndex();
            if (lastPlayerIndex != -1) {
                Player lastPlayer = game.getPlayers().get(lastPlayerIndex);
                playHistory.put(lastPlayer.getName(),
                        new PlayRecord(lastCards, System.currentTimeMillis()));
            }
        }

        return new GameResponse<>(true, "获取出牌记录成功", playHistory, false, null);
    }

    /**
     * 获取所有玩家的详细信息
     * @return 玩家信息列表
     */
    public GameResponse<List<PlayerInfo>> getPlayersInfo() {
        List<PlayerInfo> playersInfo = new ArrayList<>();

        for (Player player : game.getPlayers()) {
            PlayerInfo info = new PlayerInfo();
            info.setName(player.getName());
            info.setHandSize(player.getHand().size());
            info.setHuman(player instanceof HumanPlayer);
            info.setCurrent(player == stateManager.getCurrentPlayer());

            if (player instanceof HumanPlayer) {
                info.setHand(player.getHand());
                info.setPlayablePatterns(
                        PlayablePatternUtil.getPlayablePatterns(
                                player.getHand(),
                                stateManager.getLastPlayedCards(),
                                game.getGameRule(),
                                stateManager.getLastPlayerIndex(),
                                stateManager.getCurrentPlayerIndex()
                        )
                );
            }

            playersInfo.add(info);
        }

        return new GameResponse<>(true, "获取玩家信息成功", playersInfo, false, null);
    }

    /**
     * 推进游戏状态并处理AI回合
     * @return 游戏状态更新响应
     */
    public GameResponse<AIPlayResult> advanceGameState() {
        try {
            Player currentPlayer = stateManager.getCurrentPlayer();
            if (currentPlayer instanceof HumanPlayer) {
                return new GameResponse<>(true, "等待人类玩家操作",
                        new AIPlayResult(true, null, null), false, null);
            }

            if (currentPlayer instanceof AIPlayer) {
                List<Card> playedCards = playManager.handlePlayerPlay(currentPlayer);

                // 更新AI手牌
                if (playedCards != null && !playedCards.isEmpty()) {
                    currentPlayer.removeCards(playedCards);
                }

                stateManager.updateState(currentPlayer, playedCards);

                // 检查游戏是否结束
                boolean gameEnded = stateManager.isGameEnded();
                String winner = null;
                if (gameEnded && stateManager.getWinner() != null) {
                    winner = stateManager.getWinner().getName();
                }

                // 移动到下一个玩家
                stateManager.nextPlayer();

                AIPlayResult result = new AIPlayResult(
                        false,
                        currentPlayer.getName(),
                        playedCards != null && !playedCards.isEmpty() ? playedCards : null
                );

                return new GameResponse<>(true, "AI回合处理成功", result,
                        gameEnded,
                        winner);
            }

            return new GameResponse<AIPlayResult>(false, "游戏状态错误", null, false, null);
        } catch (Exception e) {
            // 处理异常并继续游戏
            return new GameResponse<AIPlayResult>(false, "AI回合处理失败: " + e.getMessage(),
                    null, false, null);
        }
    }
}

/**
 * 游戏操作响应类
 */
class GameResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final boolean gameEnded;
    private final String winner;

    public GameResponse(boolean success, String message, T data, boolean gameEnded, String winner) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.gameEnded = gameEnded;
        this.winner = winner;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public boolean isGameEnded() { return gameEnded; }
    public String getWinner() { return winner; }
}

/**
 * 玩家信息类
 */
class PlayerInfo {
    private String name;
    private int handSize;
    private boolean isHuman;
    private boolean isCurrent;
    private List<Card> hand;
    private Map<String, List<CardGroup>> playablePatterns;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHandSize() { return handSize; }
    public void setHandSize(int handSize) { this.handSize = handSize; }

    public boolean isHuman() { return isHuman; }
    public void setHuman(boolean human) { isHuman = human; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }

    public List<Card> getHand() { return hand; }
    public void setHand(List<Card> hand) { this.hand = hand; }

    public Map<String, List<CardGroup>> getPlayablePatterns() { return playablePatterns; }
    public void setPlayablePatterns(Map<String, List<CardGroup>> patterns) { this.playablePatterns = patterns; }
}

/**
 * 出牌记录类
 */
class PlayRecord {
    private final List<Card> cards;
    private final long timestamp;

    public PlayRecord(List<Card> cards, long timestamp) {
        this.cards = cards;
        this.timestamp = timestamp;
    }

    public List<Card> getCards() { return cards; }
    public long getTimestamp() { return timestamp; }
}

/**
 * AI出牌结果类
 */
class AIPlayResult {
    private final boolean waitingForHuman;
    private final String aiPlayer;
    private final List<Card> playedCards;

    public AIPlayResult(boolean waitingForHuman, String aiPlayer, List<Card> playedCards) {
        this.waitingForHuman = waitingForHuman;
        this.aiPlayer = aiPlayer;
        this.playedCards = playedCards;
    }

    public boolean isWaitingForHuman() { return waitingForHuman; }
    public String getAiPlayer() { return aiPlayer; }
    public List<Card> getPlayedCards() { return playedCards; }
}