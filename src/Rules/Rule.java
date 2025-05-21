package Rules;

import java.util.List;
import cards.*;

/**
 * 扑克牌规则接口
 * 定义基本的规则判断方法
 */
public interface Rule {
    /**
     * 判断两组牌是否可以比较大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return 是否可以比较
     */
    boolean canCompare(List<Card> cards1, List<Card> cards2);
    
    /**
     * 比较两组牌的大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return 1: cards1大, -1: cards2大, 0: 相等或无法比较
     */
    int compareCards(List<Card> cards1, List<Card> cards2);
    
    /**
     * 判断一组牌是否为有效牌型
     * @param cards 待判断的牌组
     * @return 是否为有效牌型
     */
    boolean isValidPattern(List<Card> cards);
    
    /**
     * 获取牌型的名称
     * @param cards 牌组
     * @return 牌型名称
     */
    String getPatternName(List<Card> cards);
} 