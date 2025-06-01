// SmartAIStrategy3.java
package AI;

import PokerPatterns.generator.CardGroup;
import Players.*;
import PokerPatterns.basis.*;
import cards.Card;
import cards.Suit;
import cards.Rank;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能策略3
 * 通过分析玩家手牌来制定策略
 */
public class SmartAIStrategy3 extends AbstractAIStrategy {
    public static final SmartAIStrategy3 INSTANCE = new SmartAIStrategy3();
    private HumanPlayer humanPlayer;

    private SmartAIStrategy3() {}

    public void setPlayer(HumanPlayer player) {
        this.humanPlayer = player;
    }

    @Override
    public List<Card> playPossiblePattern(Player player) {
        System.out.println("//250529 调试信息：AI需要出牌（上一手是自己出的）");
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        if (allPatterns.isEmpty()) {
            return Collections.emptyList();
        }

        // 分析玩家手牌
        PlayerHandAnalysis analysis = analyzePlayerHand();

        // 新增：优先考虑出废牌
        List<Card> wasteCards = identifyWasteCards(player.getHand());
        if (!wasteCards.isEmpty()) {
            System.out.println("//250529 调试信息：发现 " + wasteCards.size() + " 张废牌，优先处理");
            // 尝试出最小的废牌（单张）
            Optional<CardGroup> wasteGroup = allPatterns.stream()
                    .filter(group -> {
                        // 判断是否为单张牌型
                        PokerPattern pattern = findPatternForCards(group.getCards());
                        return pattern instanceof One && wasteCards.containsAll(group.getCards());
                    })
                    .min((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()));

            if (wasteGroup.isPresent()) {
                System.out.println("//250529 调试信息：选择出废牌: " +
                        wasteGroup.get().getCards().stream()
                                .map(Card::getDisplayName)
                                .collect(Collectors.joining(" ")));
                List<Integer> indices = getIndicesByCards(player.getHand(), wasteGroup.get().getCards());
                return playCardsAndDisplay(player, indices);
            }
        }

        // 选择最优出牌策略
        CardGroup optimalGroup = chooseFirstPlayStrategy(allPatterns, analysis);
        if (optimalGroup == null) {
            return Collections.emptyList();
        }

        List<Integer> indices = getIndicesByCards(player.getHand(), optimalGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public List<Card> tryToMatchLastPlay(Player player, List<Card> lastCards) {
        System.out.println("//250529 调试信息：AI尝试跟牌，上一手牌是：" +
                lastCards.stream().map(Card::getDisplayName).collect(Collectors.joining(" ")));

        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        PlayerHandAnalysis analysis = analyzePlayerHand();
        List<CardGroup> largerGroups = allPatterns.stream()
                .filter(group -> isLargerThanLastPlay(group.getCards(), lastCards))
                .collect(Collectors.toList());

        if (largerGroups.isEmpty()) {
            System.out.println("//250529 调试信息：AI没有找到能大过上一手牌的组合");
            return Collections.emptyList();
        }

        // 新增：优先考虑出废牌（如果能大过上一手）
        List<Card> wasteCards = identifyWasteCards(player.getHand());
        if (!wasteCards.isEmpty() && lastCards.size() == 1) {
            System.out.println("//250529 调试信息：发现 " + wasteCards.size() + " 张废牌，尝试用于跟牌");
            // 找出能大过上一手的最小废牌（单张）
            Optional<Card> smallestWaste = wasteCards.stream()
                    .filter(card -> currentRule.compareCards(Arrays.asList(card), lastCards) > 0)
                    .min(Comparator.comparingInt(Card::getIntValue));

            if (smallestWaste.isPresent()) {
                List<Card> wasteCardList = Arrays.asList(smallestWaste.get());
                System.out.println("//250529 调试信息：选择出废牌: " + smallestWaste.get().getDisplayName());
                List<Integer> indices = getIndicesByCards(player.getHand(), wasteCardList);
                return playCardsAndDisplay(player, indices);
            }
        }

        // 选择最优跟牌策略
        CardGroup optimalGroup = chooseFollowPlayStrategy(largerGroups, analysis, lastCards);
        if (optimalGroup == null) {
            return Collections.emptyList();
        }

        List<Integer> indices = getIndicesByCards(player.getHand(), optimalGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public List<Card> playFirstHandWithDiamondThree(Player player) {
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        List<CardGroup> groupsWithDiamondThree = allPatterns.stream()
                .filter(group -> group.getCards().stream()
                        .anyMatch(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE))
                .collect(Collectors.toList());

        if (groupsWithDiamondThree.isEmpty()) {
            List<Card> diamondThree = player.getHand().stream()
                    .filter(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE)
                    .collect(Collectors.toList());

            if (!diamondThree.isEmpty()) {
                List<Integer> indices = getIndicesByCards(player.getHand(), diamondThree);
                return playCardsAndDisplay(player, indices);
            }
            return Collections.emptyList();
        }

        PlayerHandAnalysis analysis = analyzePlayerHand();
        CardGroup optimalGroup = chooseFirstPlayStrategy(groupsWithDiamondThree, analysis);
        if (optimalGroup == null) {
            return Collections.emptyList();
        }

        List<Integer> indices = getIndicesByCards(player.getHand(), optimalGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    /**
     * 分析玩家手牌
     */
    private PlayerHandAnalysis analyzePlayerHand() {
        if (humanPlayer.getHand() == null || humanPlayer.getHand().isEmpty()) {
            return new PlayerHandAnalysis();
        }

        PlayerHandAnalysis analysis = new PlayerHandAnalysis();
        analysis.playerCardCount = humanPlayer.getHand().size();
        analysis.playerPatterns = new ArrayList<>();
        for (PokerPattern pattern : PATTERNS) {
            analysis.playerPatterns.addAll(pattern.potentialCardGroup(humanPlayer.getHand()));
        }
        analysis.hasBigCards = humanPlayer.getHand().stream()
                .anyMatch(card -> card.getRank() == Rank.ACE || card.getRank() == Rank.TWO);
        analysis.pairs = analysis.playerPatterns.stream()
                .filter(group -> group.getCards().size() == 2)
                .collect(Collectors.toList());
        analysis.triples = analysis.playerPatterns.stream()
                .filter(group -> group.getCards().size() == 3)
                .collect(Collectors.toList());
        analysis.fiveCardPatterns = analysis.playerPatterns.stream()
                .filter(group -> group.getCards().size() == 5)
                .collect(Collectors.toList());
        return analysis;
    }

    /**
     * 选择首出策略
     */
    private CardGroup chooseFirstPlayStrategy(List<CardGroup> availableGroups,
                                              PlayerHandAnalysis analysis) {
        // 如果玩家没有大牌，AI可以用大牌抢到牌权
        if (!analysis.hasBigCards) {
            System.out.println("//250529 调试信息：玩家没有大牌（A或2），AI出大牌抢牌权");
            return findMaxCardGroup(availableGroups);
        }

        // 如果玩家没有比我更大的五张牌型，优先出五张牌型
        if (!analysis.fiveCardPatterns.isEmpty()) {
            List<CardGroup> fiveCardGroups = availableGroups.stream()
                    .filter(group -> group.getCards().size() == 5)
                    .collect(Collectors.toList());

            if (!fiveCardGroups.isEmpty()) {
                CardGroup aiLargestFive = findMaxCardGroup(fiveCardGroups);
                boolean playerHasLargerFive = analysis.fiveCardPatterns.stream()
                        .anyMatch(group -> currentRule.compareCards(group.getCards(), aiLargestFive.getCards()) > 0);

                if (!playerHasLargerFive) {
                    System.out.println("//250529 调试信息：玩家没有比AI更大的五张牌型，AI出五张牌型");
                    return findMinCardGroup(fiveCardGroups);
                } else {
                    System.out.println("//250529 调试信息：玩家有比AI更大的五张牌型，AI不出五张牌型");
                }
            }

            // 如果玩家没有三张，优先出三张
            if (analysis.triples.isEmpty()) {
                System.out.println("//250529 调试信息：玩家没有三张，AI优先出三张");
                List<CardGroup> triples = availableGroups.stream()
                        .filter(group -> group.getCards().size() == 3)
                        .collect(Collectors.toList());
                return triples.isEmpty() ? findMinCardGroup(availableGroups) : findMinCardGroup(triples);
            }

            // 如果玩家没有对子，优先出对子
            if (analysis.pairs.isEmpty()) {
                System.out.println("//250529 调试信息：玩家没有对子，AI优先出对子");
                List<CardGroup> pairs = availableGroups.stream()
                        .filter(group -> group.getCards().size() == 2)
                        .collect(Collectors.toList());
                return pairs.isEmpty() ? findMinCardGroup(availableGroups) : findMinCardGroup(pairs);
            }
        }

        // 新增：优先选择包含废牌的牌型
        List<Card> wasteCards = identifyWasteCards(humanPlayer.getHand());
        if (!wasteCards.isEmpty()) {
            System.out.println("//250529 调试信息：尝试选择包含废牌的牌型");
            Optional<CardGroup> wasteGroup = availableGroups.stream()
                    .filter(group -> group.getCards().stream().anyMatch(wasteCards::contains))
                    .min((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()));

            if (wasteGroup.isPresent()) {
                System.out.println("//250529 调试信息：选择包含废牌的牌型");
                return wasteGroup.get();
            }
        }

        System.out.println("//250529 调试信息：使用默认策略，选择最小的牌组");
        return findMinCardGroup(availableGroups);
    }

    /**
     * 判断玩家是否能压制指定牌组
     */
    private boolean playerCanBeat(PlayerHandAnalysis analysis, List<Card> cards) {
        for (CardGroup group : analysis.playerPatterns) {
            if (currentRule.compareCards(group.getCards(), cards) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选择跟牌策略
     */
    private CardGroup chooseFollowPlayStrategy(List<CardGroup> availableGroups,
                                               PlayerHandAnalysis analysis,
                                               List<Card> lastCards) {
        List<CardGroup> sortedGroups = availableGroups.stream()
                .sorted((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
                .collect(Collectors.toList());

        // 新增：优先选择包含废牌的牌型
        List<Card> wasteCards = identifyWasteCards(humanPlayer.getHand());
        if (!wasteCards.isEmpty()) {
            System.out.println("//250529 调试信息：尝试选择包含废牌的跟牌组合");
            Optional<CardGroup> wasteGroup = sortedGroups.stream()
                    .filter(group -> group.getCards().stream().anyMatch(wasteCards::contains))
                    .findFirst();

            if (wasteGroup.isPresent()) {
                System.out.println("//250529 调试信息：选择包含废牌的跟牌组合");
                return wasteGroup.get();
            }
        }

        // 寻找最小的玩家无法压制的牌组
        for (CardGroup group : sortedGroups) {
            if (!playerCanBeat(analysis, group.getCards())) {
                System.out.println("//250529 调试信息：选择玩家无法压制的牌组");
                return group;
            }
        }

        // 玩家能压制所有牌组时，出最大牌组消耗对手
        CardGroup maxGroup = findMaxCardGroup(sortedGroups);
        System.out.println("//250529 调试信息：玩家能压制所有牌组，选择最大牌组消耗对手");
        return maxGroup;
    }

    /**
     * 玩家手牌分析结果
     */
    private static class PlayerHandAnalysis {
        int playerCardCount;
        List<CardGroup> playerPatterns;
        boolean hasBigCards;
        List<CardGroup> pairs;
        List<CardGroup> triples;
        List<CardGroup> fiveCardPatterns;
    }
}