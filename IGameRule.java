package API.rules;

import java.util.List;

import cards.Card;

/**
 * 游戏规则接口
 * 定义前端需要的规则判断方法
 */
public interface IGameRule {
    /**
     * 判断当前出牌是否有效
     * @param cards 要出的牌
     * @param lastCards 上一轮出的牌
     * @return 是否为有效出牌
     */
    boolean isValidPlay(List<Card> cards, List<Card> lastCards);
    
    /**
     * 获取所有可能的有效出牌
     * @param hand 手牌
     * @param lastCards 上一轮出的牌
     * @return 所有可能的有效出牌列表
     */
    List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards);
    
    /**
     * 获取牌型名称
     * @param cards 牌组
     * @return 牌型名称
     */
    String getPatternName(List<Card> cards);
} 