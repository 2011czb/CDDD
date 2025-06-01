package AI;

import PokerPatterns.generator.CardGroup;
import Players.*;
import cards.Card;
import cards.Suit;
import cards.Rank;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 中等智能策略：出符合规则的随机牌型
 */
public class SmartAIStrategy2 extends AbstractAIStrategy {
    public static final SmartAIStrategy2 INSTANCE = new SmartAIStrategy2();

    private SmartAIStrategy2() {}

    @Override
    public List<Card> playPossiblePattern(Player player) {
        // 获取所有可能的牌型组合
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        if (allPatterns.isEmpty()) {
            return Collections.emptyList();
        }

        // 找出最大的牌型组合
        CardGroup maxGroup = findMaxCardGroup(allPatterns);
        if (maxGroup == null) {
            return Collections.emptyList();
        }

        // 随机选择一个牌型组合
        List<Integer> indices = getIndicesByCards(player.getHand(), maxGroup.getCards());
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

        // 随机选择一个比上一手大的组合
        CardGroup maxLargerGroup = findMaxCardGroup(largerGroups);
        if (maxLargerGroup == null) {
            return Collections.emptyList();
        }

        // 获取牌的索引并出牌
        List<Integer> indices = getIndicesByCards(player.getHand(), maxLargerGroup.getCards());
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

        // 选择一个包含方块三的最大组合
        CardGroup maxGroup = findMaxCardGroup(groupsWithDiamondThree);
        if (maxGroup == null) {
            return Collections.emptyList();
        }

        // 获取牌的索引并出牌
        List<Integer> indices = getIndicesByCards(player.getHand(), maxGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }




} 
