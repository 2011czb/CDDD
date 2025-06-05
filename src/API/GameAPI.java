package API;

import PokerPatterns.generator.CardGroup;
import cards.Card;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface GameAPI {
    /**
     * 处理玩家出牌请求
     * @param cards 玩家想要出的牌列表
     * @return 游戏操作响应
     * @throws GameException 当出牌不合法或不是玩家回合时
     */
    public GameResponse<Void> processPlayerPlay(List<Card> cards);

    /**
     * 处理玩家过牌请求
     * @return 游戏操作响应
     * @throws GameException 当过牌不合法时
     */
    public GameResponse<Void> processPlayerPass();

    /**
     * 获取当前回合中每个玩家出的牌
     * @return 玩家出牌记录
     */
    public GameResponse<Map<String, PlayRecord>> getPlayHistory();

    /**
     * 获取所有玩家的详细信息
     * @return 玩家信息列表
     */
    public GameResponse<List<PlayerInfo>> getPlayersInfo();

    /**
     * 推进游戏状态并处理AI回合
     * @return 游戏状态更新响应
     */
    public CompletableFuture<GameResponse<AIPlayResult>> advanceGameState();

    /**
     * 获取可出牌提示
     * @return 可出牌型及其组合
     */
    GameResponse<Map<String, List<CardGroup>>> getPlayableCards();

    /**
     * 获取游戏规则信息
     * @return 规则名称和类型
     */
    GameResponse<RuleInfo> getGameRuleInfo();

    /**
     * 获取玩家得分
     * @return 玩家得分映射表
     */
    GameResponse<Map<String, Integer>> getPlayerScores();
}
