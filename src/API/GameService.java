package API;

import API.impl.AIPlayResult;
import API.impl.GameResponse;
import API.impl.PlayRecord;
import API.impl.PlayerInfo;
import cards.Card;
import PokerPatterns.generator.CardGroup;
import java.util.*;

/**
 * 游戏服务接口
 * 提供前端所需的所有游戏操作功能
 */
public interface GameService {

    /**
     * 处理玩家出牌请求
     * @param cards 玩家想要出的牌列表
     * @return 游戏操作响应
     */
    GameResponse<Void> playCards(List<Card> cards);

    /**
     * 处理玩家过牌请求
     * @return 游戏操作响应
     */
    GameResponse<Void> pass();

    /**
     * 获取场上出牌记录
     * @return 包含上一个玩家出牌记录的响应
     */
    GameResponse<PlayRecord> getPlayHistory();

    /**
     * 获取所有玩家信息
     * @return 包含玩家详细信息的响应
     */
    GameResponse<List<PlayerInfo>> getPlayersInfo();

    /**
     * 推进游戏状态
     * @return 包含AI出牌结果的响应
     */
    GameResponse<AIPlayResult> advanceGameState();

    /**
     * 获取当前玩家可出的牌型提示
     * @return 包含可出牌型提示的响应
     */
    GameResponse<Map<String, List<CardGroup>>> getPlayablePatterns();
}