package Players.AI;

import PokerPatterns.generator.CardGroup;
import Players.*;
import cards.Card;
import cards.Suit;
import cards.Rank;
import Rules.Rule;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能策略
 * 根据游戏规则动态选择比较策略
 * 南方规则：只有相同牌型才能比较大小
 * 北方规则：不同牌型也可以比较大小，按照牌型权重比较
 */
//出符合规则的最大牌
public class SmartAIStrategy2 extends AbstractAIStrategy {
    public static final SmartAIStrategy2 INSTANCE = new SmartAIStrategy2();
    private Rule currentRule; // 当前使用的规则

    private SmartAIStrategy2() {}

    /**
     * 设置当前使用的规则
     */
    public void setRule(Rule rule) {
        this.currentRule = rule;
    }

    @Override
    public List<Card> playPossiblePattern(Player player) {
        // 获取所有可能的牌型组合
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        if (allPatterns.isEmpty()) {
            return Collections.emptyList();
        }

        // 找出最大的牌型组合
        CardGroup biggestGroup = findCardGroup(allPatterns);
        if (biggestGroup == null) {
            return Collections.emptyList();
        }

        // 获取牌的索引并出牌
        List<Integer> indices = getIndicesByCards(player.getHand(), biggestGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public List<Card> tryToMatchLastPlay(Player player, List<Card> lastCards) {
        // 获取所有可能的牌型组合
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        
        // 找出所有比上一手牌大的组合
        List<CardGroup> largerGroups = allPatterns.stream()
            .filter(group -> isLargerThanLastPlay(group.getCards(), lastCards))
            .collect(Collectors.toList());

        if (largerGroups.isEmpty()) {
            return Collections.emptyList();
        }

        // 找出最大的组合
        CardGroup biggestLargerGroup = findCardGroup(largerGroups);
        if (biggestLargerGroup == null) {
            return Collections.emptyList();
        }

        // 获取牌的索引并出牌
        List<Integer> indices = getIndicesByCards(player.getHand(), biggestLargerGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public List<Card> playFirstHandWithDiamondThree(Player player) {
        // 获取所有可能的牌型组合
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        
        // 找出所有包含方块三的组合
        List<CardGroup> groupsWithDiamondThree = allPatterns.stream()
            .filter(group -> group.getCards().stream()
                .anyMatch(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE))
            .collect(Collectors.toList());

        if (groupsWithDiamondThree.isEmpty()) {
            // 如果没有找到包含方块三的组合，就出单张方块三
            List<Card> diamondThree = player.getHand().stream()
                .filter(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE)
                .collect(Collectors.toList());
            
            if (!diamondThree.isEmpty()) {
                List<Integer> indices = getIndicesByCards(player.getHand(), diamondThree);
                return playCardsAndDisplay(player, indices);
            }
            return Collections.emptyList();
        }

        // 找出最小的包含方块三的组合
        CardGroup biggestGroup = findCardGroup(groupsWithDiamondThree);
        if (biggestGroup == null) {
            return Collections.emptyList();
        }

        // 获取牌的索引并出牌
        List<Integer> indices = getIndicesByCards(player.getHand(), biggestGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public boolean isLargerThanLastPlay(List<Card> cards, List<Card> lastCards) {
        // 使用当前规则的compareCards方法比较牌型大小
        return currentRule.compareCards(cards, lastCards) > 0;
    }

    @Override
    public CardGroup findCardGroup(List<CardGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }

        // 使用max找出最大的牌组
        return groups.stream()
            .max((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
            .orElse(null);
    }
} 
