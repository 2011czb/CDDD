package Rules;

import java.util.List;
import cards.Card;

/**
 * 规则接口
 * 定义了规则需要实现的基本方法
 */
public interface Rule {
    /**
     * 获取规则名称
     */
    String getName();
    
    /**
     * 判断是否是有效的牌型
     */
    boolean isValidPattern(List<Card> cards);
    
    /**
     * 获取牌型名称
     */
    String getPatternName(List<Card> cards);
    
    /**
     * 判断两组牌是否可以比较大小
     */
    boolean canCompare(List<Card> cards1, List<Card> cards2);
    
    /**
     * 比较两组牌的大小
     * @return 正数表示cards1大，负数表示cards2大，0表示不能比较
     */
    int compareCards(List<Card> cards1, List<Card> cards2);
    
    /**
     * 获取所有可能的出牌组合
     */
    List<List<Card>> getAllPossiblePlays(List<Card> hand);
    
    /**
     * 获取所有可以大过上一手牌的出牌组合
     */
    List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards);
    
    /**
     * 判断当前出牌是否合法
     */
    boolean isValidPlay(List<Card> cards);
    
    /**
     * 判断当前出牌是否能大过上一手牌
     */
    boolean isValidPlay(List<Card> current, List<Card> last);
    
    /**
     * 比较两组牌的大小
     */
    int compare(List<Card> cards1, List<Card> cards2);

    /**
     * 获取规则的详细说明
     * @return 规则说明文本
     */
    default String getRuleDescription() {
        StringBuilder description = new StringBuilder();
        description.append("规则名称：").append(getName()).append("\n");
        description.append("基本规则：\n");
        description.append("1. 可出的牌型：单张、对子、三张、顺子、同花、葫芦、四带一、同花顺\n");
        description.append("2. 合法的出牌张数：1、2、3、5\n");
        if (getName().equals("NORTH")) {
            description.append("3. 北方规则特点：\n");
            description.append("   - 允许不同牌型之间比较大小\n");
            description.append("   - 牌型大小：同花顺 > 四带一 > 葫芦 > 同花 > 顺子 > 三张 > 对子 > 单张\n");
        } else {
            description.append("3. 南方规则特点：\n");
            description.append("   - 只允许相同牌型之间比较大小\n");
            description.append("   - 同牌型内部按照点数大小比较\n");
        }
        return description.toString();
    }
} 