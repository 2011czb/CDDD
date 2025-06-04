package API.impl;

import API.GameService;
import Game.Game;
import Game.GamePlayManager;
import Game.GameStateManager;
import Players.Player;
import Players.HumanPlayer;
import cards.Card;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import java.util.*;

/**
 * 游戏控制器类
 * 负责处理前端和后端之间的通信，管理单人模式的游戏流程
 */
public class GameServiceImpl implements GameService {
    private final Game game;
    private final GameStateManager stateManager;
    private final GamePlayManager playManager;

    public GameServiceImpl(Game game) {
        this.game = game;
        this.stateManager = game.getStateManager();
        this.playManager = game.getPlayManager();
    }

    @Override
    public GameResponse<Void> playCards(List<Card> cards) {
        Player currentPlayer = stateManager.getCurrentPlayer();
        if (!(currentPlayer instanceof HumanPlayer)) {
            return new GameResponse<>(false, "当前不是你的回合", null, false, null);
        }

        // 验证出牌
        if (!playManager.isValidPlay(cards, stateManager.getLastPlayedCards(), currentPlayer)) {
            return new GameResponse<>(false, "出牌无效，请选择其他牌", null, false, null);
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
    public GameResponse<Void> pass() {
        Player currentPlayer = stateManager.getCurrentPlayer();

        // 检查是否可以过牌
        if (stateManager.getLastPlayerIndex() == -1) {
            return new GameResponse<>(false, "你是第一个出牌的玩家，不能过牌", null, false, null);
        }

        // 如果所有玩家都过牌，当前玩家必须出牌
        if (stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex()) {
            return new GameResponse<>(false, "其他玩家都过牌了，你必须出牌", null, false, null);
        }

        // 处理过牌
        stateManager.updateState(currentPlayer, Collections.emptyList());
        stateManager.nextPlayer();

        return new GameResponse<>(true, "过牌成功", null, false, null);
    }

    @Override
    public GameResponse<PlayRecord> getPlayHistory() {
        List<Card> lastCards = stateManager.getLastPlayedCards();
        if (lastCards == null || lastCards.isEmpty()) {
            return new GameResponse<>(true, "暂无出牌记录", null, false, null);
        }

        int lastPlayerIndex = stateManager.getLastPlayerIndex();
        if (lastPlayerIndex == -1) {
            return new GameResponse<>(true, "游戏刚开始", null, false, null);
        }

        Player lastPlayer = game.getPlayers().get(lastPlayerIndex);
        PlayRecord record = new PlayRecord(
                lastCards,
                lastPlayer.getName(),
                System.currentTimeMillis()
        );

        return new GameResponse<>(true, "获取出牌记录成功", record, false, null);
    }

    @Override
    public GameResponse<List<PlayerInfo>> getPlayersInfo() {
        List<PlayerInfo> playersInfo = new ArrayList<>();
        Player currentPlayer = stateManager.getCurrentPlayer();

        for (Player player : game.getPlayers()) {
            PlayerInfo info = new PlayerInfo();
            info.setName(player.getName());
            info.setHandSize(player.getHand().size());
            info.setHuman(player instanceof HumanPlayer);
            info.setPassed(player == currentPlayer &&
                    stateManager.getLastPlayedCards() != null &&
                    stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex());
            info.setCurrent(player == currentPlayer);

            playersInfo.add(info);
        }

        return new GameResponse<>(true, "获取玩家信息成功", playersInfo, false, null);
    }

    @Override
    public GameResponse<AIPlayResult> advanceGameState() {
        try {
            Player currentPlayer = stateManager.getCurrentPlayer();

            // 如果是人类玩家回合，直接返回等待状态
            if (currentPlayer instanceof HumanPlayer) {
                return new GameResponse<>(true, "等待人类玩家操作",
                        new AIPlayResult(true, null, null), false, null);
            }

            // 处理AI出牌
            List<Card> playedCards = playManager.handlePlayerPlay(currentPlayer);
            stateManager.updateState(currentPlayer, playedCards);

            // 检查游戏是否结束
            boolean gameEnded = stateManager.isGameEnded();
            String winner = gameEnded ? stateManager.getWinner().getName() : null;

            // 移动到下一个玩家
            stateManager.nextPlayer();

            AIPlayResult result = new AIPlayResult(
                    false,
                    currentPlayer.getName(),
                    playedCards
            );

            return new GameResponse<>(true, "AI回合处理成功", result, gameEnded, winner);
        } catch (Exception e) {
            return new GameResponse<>(false, "AI回合处理失败: " + e.getMessage(),
                    null, false, null);
        }
    }

    @Override
    public GameResponse<Map<String, List<CardGroup>>> getPlayablePatterns() {
        Player currentPlayer = stateManager.getCurrentPlayer();

        if (!(currentPlayer instanceof HumanPlayer)) {
            return new GameResponse<>(false, "只有人类玩家可以获取提示", null, false, null);
        }

        Map<String, List<CardGroup>> patterns = PlayablePatternUtil.getPlayablePatterns(
                currentPlayer.getHand(),
                stateManager.getLastPlayedCards(),
                game.getGameRule(),
                stateManager.getLastPlayerIndex(),
                stateManager.getCurrentPlayerIndex()
        );

        return new GameResponse<>(true, "可出牌型获取成功", patterns, false, null);
    }
}

