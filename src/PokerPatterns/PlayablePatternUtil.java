package PokerPatterns;

import PokerPatterns.basis.*;
import PokerPatterns.generator.CardGroup;
import Rules.Rule;
import cards.Card;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可出牌型工具类
 * 用于根据上一手牌和规则，获取当前可以出的牌型
 */
public class PlayablePatternUtil {
    private static final List<PokerPattern> PATTERNS = Arrays.asList(
        One.getInstance(),           // 单张
        Pair.getInstance(),          // 对子
        Three.getInstance(),         // 三张
        Straight.getInstance(),      // 杂顺
        Flush.getInstance(),         // 同花五
        FullHouse.getInstance(),     // 三带一对
        FourofaKind.getInstance(),   // 四带一
        StraightFlush.getInstance()  // 同花顺
    );

    /**
     * 获取所有可以出的牌型
     * @param hand 手牌
     * @param lastCards 上一手牌
     * @param rule 当前规则
     * @return 可以出的牌型列表，按牌型分组
     */
    public static Map<String, List<CardGroup>> getPlayablePatterns(List<Card> hand, List<Card> lastCards, Rule rule) {
        Map<String, List<CardGroup>> playablePatterns = new TreeMap<>();
        
        // 如果是第一手牌，只需要包含方块3
        if (lastCards == null) {
            for (PokerPattern pattern : PATTERNS) {
                List<CardGroup> groups = pattern.potentialCardGroup(hand);
                for (CardGroup group : groups) {
                    if (group.getCards().stream().anyMatch(card -> card.getIntValue() == 41)) { // 方块3的intValue是41
                        String patternName = pattern.getName();
                        playablePatterns.computeIfAbsent(patternName, k -> new ArrayList<>()).add(group);
                    }
                }
            }
            return playablePatterns;
        }

        // 如果上一手牌是当前玩家出的（其他玩家都过牌），则所有牌型都可以出
        if (lastCards.isEmpty()) {
            for (PokerPattern pattern : PATTERNS) {
                List<CardGroup> groups = pattern.potentialCardGroup(hand);
                if (!groups.isEmpty()) {
                    playablePatterns.put(pattern.getName(), groups);
                }
            }
            return playablePatterns;
        }

        // 根据规则判断哪些牌型可以出
        for (PokerPattern pattern : PATTERNS) {
            List<CardGroup> groups = pattern.potentialCardGroup(hand);
            List<CardGroup> playableGroups = groups.stream()
                .filter(group -> rule.canCompare(group.getCards(), lastCards) && 
                               rule.compareCards(group.getCards(), lastCards) > 0)
                .collect(Collectors.toList());
            
            if (!playableGroups.isEmpty()) {
                playablePatterns.put(pattern.getName(), playableGroups);
            }
        }

        return playablePatterns;
    }

    /**
     * 打印可出的牌型
     * @param playablePatterns 可出的牌型
     * @param lastCards 上一手牌
     * @param lastPlayerIndex 上一个出牌的玩家索引
     * @param currentPlayerIndex 当前玩家索引
     */
    public static void printPlayablePatterns(Map<String, List<CardGroup>> playablePatterns, 
                                           List<Card> lastCards,
                                           int lastPlayerIndex,
                                           int currentPlayerIndex) {
        // 如果是第一个出牌的玩家（lastPlayerIndex == -1）
        if (lastPlayerIndex == -1) {
            if (playablePatterns.isEmpty()) {
                System.out.println("没有包含方块3的牌型");
                return;
            }
        }
        // 如果上一个出牌的是当前玩家（其他玩家都过牌了）
        else if (lastPlayerIndex == currentPlayerIndex) {
            if (playablePatterns.isEmpty()) {
                System.out.println("可以出任意牌型");
                return;
            }
        }
        // 如果是跟牌
        else {
            if (playablePatterns.isEmpty()) {
                System.out.println("没有能大过上一手牌的牌型");
                return;
            }
        }

        System.out.println("可以出的牌型：");
        for (Map.Entry<String, List<CardGroup>> entry : playablePatterns.entrySet()) {
            System.out.println("\n" + entry.getKey() + "：");
            List<CardGroup> groups = entry.getValue();
            for (int i = 0; i < groups.size(); i++) {
                CardGroup group = groups.get(i);
                System.out.printf("  %d. ", i + 1);
                for (Card card : group.getCards()) {
                    System.out.print(card.getDisplayName() + " ");
                }
                System.out.println();
            }
        }
    }
} 