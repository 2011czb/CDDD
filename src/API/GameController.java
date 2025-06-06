package API;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import Game.Game;
import Game.GamePlayManager;
import Game.GameStateManager;
import Players.AIPlayer;
import Players.HumanPlayer;
import Players.Player;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import cards.Card;

/**
 * 游戏控制器类
 * 负责处理前端和后端之间的通信，管理单人模式的游戏流程
 */
public class GameController implements GameAPI{
    private final Game game;
    private final GameStateManager stateManager;
    private final GamePlayManager playManager;

    public GameController(Game game) {
        this.game = game;
        this.stateManager = game.getStateManager();
        this.playManager = game.getPlayManager();
    }

    @Override
    public GameResponse<Void> processPlayerPlay(List<Card> cards) {
        Player currentPlayer = stateManager.getCurrentPlayer();
        if (!(currentPlayer instanceof HumanPlayer)) {
            throw new GameException("当前不是人类玩家的回合");
        }

        // 验证并处理出牌
        if (!playManager.isValidPlay(cards, stateManager.getLastPlayedCards(), currentPlayer)) {
            throw new GameException("无效的出牌");
        }

        // 更新游戏状态
        if (cards != null && !cards.isEmpty()) {
            currentPlayer.removeCards(cards);
            stateManager.updateState(currentPlayer, cards);
        }

        // 移动到下一个玩家
        stateManager.nextPlayer();

        return new GameResponse<>(true, "出牌成功", null, false, null);
    }

    @Override
    public GameResponse<Void> processPlayerPass() {
        return processPlayerPlay(Collections.emptyList());
    }

    @Override
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

    @Override
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

    @Override
    public CompletableFuture<GameResponse<AIPlayResult>> advanceGameState() {
        return CompletableFuture.supplyAsync(() -> {
            Player currentPlayer = stateManager.getCurrentPlayer();
            if (currentPlayer instanceof HumanPlayer) {
                return new GameResponse<>(true, "等待人类玩家操作",
                        new AIPlayResult(true, null, null), false, null);
            }

            if (currentPlayer instanceof AIPlayer) {
                try {
                    Thread.sleep(1000); // AI思考时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                List<Card> playedCards = playManager.handlePlayerPlay(currentPlayer);
                stateManager.updateState(currentPlayer, playedCards);
                stateManager.nextPlayer();

                AIPlayResult result = new AIPlayResult(
                        false,
                        currentPlayer.getName(),
                        playedCards
                );

                return new GameResponse<>(true, "AI回合处理成功", result,
                        stateManager.isGameEnded(),
                        stateManager.isGameEnded() ? stateManager.getWinner().getName() : null);
            }

            throw new GameException("无效的玩家类型");
        });
    }

    @Override
    public GameResponse<Map<String, List<CardGroup>>> getPlayableCards() {
        try {
            Player humanPlayer = findHumanPlayer();
            if (humanPlayer == null) {
                throw new GameException("未找到人类玩家");
            }

            Map<String, List<CardGroup>> patterns = PlayablePatternUtil.getPlayablePatterns(
                humanPlayer.getHand(),
                game.getLastPlayedCards(),
                game.getGameRule(),
                stateManager.getLastPlayerIndex(),
                stateManager.getCurrentPlayerIndex()
            );
            return new GameResponse<>(true, "获取可出牌型成功", patterns, false, null);
        } catch (Exception e) {
            return new GameResponse<>(false, "获取可出牌型失败: " + e.getMessage(), null, false, null);
        }
    }

    @Override
    public GameResponse<RuleInfo> getGameRuleInfo() {
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setRuleName(game.getRuleName());
        ruleInfo.setRuleType(game.getRuleType());
        return new GameResponse<>(true, "获取规则信息成功", ruleInfo, false, null);
    }

    @Override
    public GameResponse<Map<String, Integer>> getPlayerScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (Player player : game.getPlayers()) {
            scores.put(player.getName(), player.getScore());
        }
        return new GameResponse<>(true, "获取玩家得分成功", scores, false, null);
    }

    private Player findHumanPlayer() {
        for (Player player : game.getPlayers()) {
            if (player instanceof HumanPlayer) {
                return player;
            }
        }
        return null;
    }

    public List<List<Card>> getPlayablePatterns(Player player) {
        return game.getPlayablePatterns(player, game.getLastPlayedCards());
    }
}

class RuleInfo {
    private String ruleName;
    private int ruleType;

    // Getters & Setters
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public int getRuleType() { return ruleType; }
    public void setRuleType(int ruleType) { this.ruleType = ruleType; }
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