package PokerPatterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import PokerPatterns.basis.Flush;
import PokerPatterns.basis.FourofaKind;
import PokerPatterns.basis.FullHouse;
import PokerPatterns.basis.One;
import PokerPatterns.basis.Pair;
import PokerPatterns.basis.PokerPattern;
import PokerPatterns.basis.Straight;
import PokerPatterns.basis.StraightFlush;
import PokerPatterns.basis.Three;
import PokerPatterns.generator.CardGroup;
import Rules.NorthRule;
import Rules.Rule;
import Rules.SouthRule;
import cards.Card;

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
     * @param lastPlayerIndex 上一个出牌的玩家索引
     * @param currentPlayerIndex 当前玩家索引
     * @return 可以出的牌型列表，按牌型分组
     */
    public static Map<String, List<CardGroup>> getPlayablePatterns(List<Card> hand, List<Card> lastCards, Rule rule, int lastPlayerIndex, int currentPlayerIndex) {
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

        // 如果上一个出牌的是当前玩家（其他玩家都过牌），则所有牌型都可以出
        if (lastPlayerIndex == currentPlayerIndex) {
            for (PokerPattern pattern : PATTERNS) {
                List<CardGroup> groups = pattern.potentialCardGroup(hand);
                if (!groups.isEmpty()) {
                    playablePatterns.put(pattern.getName(), groups);
                }
            }
            return playablePatterns;
        }

        // 根据规则判断哪些牌型可以出
        if (rule instanceof NorthRule) {
            // 北方规则：任何牌型都可以比较
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
        } else if (rule instanceof SouthRule) {
            // 南方规则：只能出相同牌型
            PokerPattern lastPattern = getPattern(lastCards);
            if (lastPattern != null) {
                List<CardGroup> groups = lastPattern.potentialCardGroup(hand);
                List<CardGroup> playableGroups = groups.stream()
                        .filter(group -> rule.canCompare(group.getCards(), lastCards) &&
                                rule.compareCards(group.getCards(), lastCards) > 0)
                        .collect(Collectors.toList());

                if (!playableGroups.isEmpty()) {
                    playablePatterns.put(lastPattern.getName(), playableGroups);
                }
            }
        }

        return playablePatterns;
    }

    /**
     * 获取牌组对应的牌型
     * @param cards 牌组
     * @return 牌型，如果不是有效牌型则返回null
     */
    private static PokerPattern getPattern(List<Card> cards) {
        for (PokerPattern pattern : PATTERNS) {
            if (pattern.match(cards)) {
                return pattern;
            }
        }
        return null;
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

    /**
     * 获取所有可能的有效出牌组合
     * @param hand 手牌
     * @param lastCards 上一手牌
     * @param rule 当前规则
     * @return 所有可能的有效出牌组合
     */
    public static List<List<Card>> getAllValidPlays(List<Card> hand, List<Card> lastCards, Rule rule) {
        List<List<Card>> validPlays = new ArrayList<>();
        Map<String, List<CardGroup>> playablePatterns = getPlayablePatterns(hand, lastCards, rule, -1, 0);
        
        for (List<CardGroup> groups : playablePatterns.values()) {
            for (CardGroup group : groups) {
                validPlays.add(group.getCards());
            }
        }
        
        return validPlays;
    }

    public static boolean isValidPattern(List<Card> cards, PokerPattern pattern) {
        return pattern.match(cards);
    }
} 