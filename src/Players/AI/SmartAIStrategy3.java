package Players.AI;

import PokerPatterns.generator.CardGroup;
import Players.*;
import PokerPatterns.basis.*;
import cards.Card;
import cards.Suit;
import cards.Rank;
import Rules.Rule;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能策略3
 * 通过分析玩家手牌来制定策略
 * 1. 分析玩家手牌中各种牌型的分布
 * 2. 根据当前规则（南方/北方）选择最优策略
 * 3. 在合适的时机使用特殊牌型
 */
public class SmartAIStrategy3 extends AbstractAIStrategy {
    public static final SmartAIStrategy3 INSTANCE = new SmartAIStrategy3();
    private Rule currentRule;
    private List<Card> playerCards; // 玩家的手牌

    private SmartAIStrategy3() {}

    /**
     * 设置当前使用的规则和玩家手牌
     */
    public void setRule(Rule rule) {
        this.currentRule = rule;
    }

    public void setPlayerCards(List<Card> cards) {
        this.playerCards = new ArrayList<>(cards);
    }

    @Override
    public List<Card> playPossiblePattern(Player player) {
        //250529 调试信息：AI需要出牌（上一手是自己出的）
        System.out.println("//250529 调试信息：AI需要出牌（上一手是自己出的）");
        
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        if (allPatterns.isEmpty()) {
            return Collections.emptyList();
        }

        // 分析玩家手牌
        PlayerHandAnalysis analysis = analyzePlayerHand();
        
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
        //250529 调试信息：AI尝试跟牌
        System.out.println("//250529 调试信息：AI尝试跟牌，上一手牌是：" + lastCards.stream()
            .map(Card::getDisplayName)
            .collect(Collectors.joining(" ")));
        
        List<CardGroup> allPatterns = getAllPossiblePatterns(player);
        
        // 分析玩家手牌
        PlayerHandAnalysis analysis = analyzePlayerHand();
        
        // 找出所有能大过上一手牌的组合
        List<CardGroup> largerGroups = allPatterns.stream()
            .filter(group -> isLargerThanLastPlay(group.getCards(), lastCards))
            .collect(Collectors.toList());

        if (largerGroups.isEmpty()) {
            //250529 调试信息：AI没有找到能大过上一手牌的组合
            System.out.println("//250529 调试信息：AI没有找到能大过上一手牌的组合");
            return Collections.emptyList();
        }

        // 选择最优出牌策略
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
        
        // 找出所有包含方块三的组合
        // 注意：即使AI自己拿到方块三，也需要分析人类玩家的手牌
        // 因为AI的目标是阻止人类玩家获胜，而不是自己获胜
        List<CardGroup> groupsWithDiamondThree = allPatterns.stream()
            .filter(group -> group.getCards().stream()
                .anyMatch(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE))
            .collect(Collectors.toList());

        if (groupsWithDiamondThree.isEmpty()) {
            // 如果没有找到包含方块三的组合，就单出方块三
            List<Card> diamondThree = player.getHand().stream()
                .filter(card -> card.getSuit() == Suit.DIAMONDS && card.getRank() == Rank.THREE)
                .collect(Collectors.toList());
            
            if (!diamondThree.isEmpty()) {
                List<Integer> indices = getIndicesByCards(player.getHand(), diamondThree);
                return playCardsAndDisplay(player, indices);
            }
            return Collections.emptyList();
        }

        // 分析人类玩家的手牌
        // 目的是找出人类玩家没有的牌型，如果AI有包含方块三的这种牌型，就出这种牌型
        PlayerHandAnalysis analysis = analyzePlayerHand();
        
        // 选择最优出牌策略
        // 优先选择人类玩家没有的牌型，以限制人类玩家的出牌选择
        CardGroup optimalGroup = chooseOptimalGroup(groupsWithDiamondThree, analysis, null);
        if (optimalGroup == null) {
            return Collections.emptyList();
        }

        List<Integer> indices = getIndicesByCards(player.getHand(), optimalGroup.getCards());
        return playCardsAndDisplay(player, indices);
    }

    @Override
    public boolean isLargerThanLastPlay(List<Card> cards, List<Card> lastCards) {
        return currentRule.compareCards(cards, lastCards) > 0;
    }

    /**
     * 分析玩家手牌
     */
    private PlayerHandAnalysis analyzePlayerHand() {
        if (playerCards == null || playerCards.isEmpty()) {
            return new PlayerHandAnalysis();
        }

        PlayerHandAnalysis analysis = new PlayerHandAnalysis();
        analysis.playerCardCount = playerCards.size();
        
        // 分析每种牌型
        analysis.playerPatterns = new ArrayList<>();
        for (PokerPattern pattern : PATTERNS) {
            analysis.playerPatterns.addAll(pattern.potentialCardGroup(playerCards));
        }
        
        // 分析玩家手牌中的各种牌型
        analysis.hasBigCards = playerCards.stream()
            .anyMatch(card -> card.getRank() == Rank.ACE || card.getRank() == Rank.TWO);
        
        // 分析玩家手牌中的对子
        analysis.pairs = analysis.playerPatterns.stream()
            .filter(group -> group.getCards().size() == 2)
            .collect(Collectors.toList());
        
        // 分析玩家手牌中的三张
        analysis.triples = analysis.playerPatterns.stream()
            .filter(group -> group.getCards().size() == 3)
            .collect(Collectors.toList());
        
        // 分析玩家手牌中的五张牌型
        analysis.fiveCardPatterns = analysis.playerPatterns.stream()
            .filter(group -> group.getCards().size() == 5)
            .collect(Collectors.toList());
        
        return analysis;
    }

    /**
     * 根据分析结果选择最优出牌策略
     */
    private CardGroup chooseOptimalGroup(List<CardGroup> availableGroups, 
                                       PlayerHandAnalysis analysis, 
                                       List<Card> lastCards) {
        if (availableGroups.isEmpty()) {
            return null;
        }

        // 如果是首出，选择最优的首出策略
        if (lastCards == null) {
            return chooseFirstPlayStrategy(availableGroups, analysis);
        }

        // 如果是跟牌，选择最优的跟牌策略
        return chooseFollowPlayStrategy(availableGroups, analysis, lastCards);
    }

    /**
     * 选择首出策略
     */
    private CardGroup chooseFirstPlayStrategy(List<CardGroup> availableGroups, 
                                            PlayerHandAnalysis analysis) {
        // 如果玩家手牌很少（<=3张），优先出大牌
        if (analysis.playerCardCount <= 3) {
            //250529 调试信息：玩家手牌数量较少，优先出大牌
            System.out.println("//250529 调试信息：玩家手牌数量较少（" + analysis.playerCardCount + "张），优先出大牌");
            return findCardGroup(availableGroups);
        }

        // 如果玩家没有大牌，AI可以用大牌抢到牌权
        if (!analysis.hasBigCards) {
            //250529 调试信息：玩家没有大牌，AI出大牌抢牌权
            System.out.println("//250529 调试信息：玩家没有大牌（A或2），AI出大牌抢牌权");
            return findCardGroup(availableGroups);
        }

        // 如果玩家没有对子，优先出对子
        if (analysis.pairs.isEmpty()) {
            //250529 调试信息：玩家没有对子，AI优先出对子
            System.out.println("//250529 调试信息：玩家没有对子，AI优先出对子");
            List<CardGroup> pairs = availableGroups.stream()
                .filter(group -> group.getCards().size() == 2)
                .collect(Collectors.toList());
            return pairs.isEmpty() ? findCardGroup(availableGroups) : findminCardGroup(pairs);
        }

        // 如果玩家没有三张，优先出三张
        if (analysis.triples.isEmpty()) {
            //250529 调试信息：玩家没有三张，AI优先出三张
            System.out.println("//250529 调试信息：玩家没有三张，AI优先出三张");
            List<CardGroup> triples = availableGroups.stream()
                .filter(group -> group.getCards().size() == 3)
                .collect(Collectors.toList());
            return triples.isEmpty() ? findCardGroup(availableGroups) : findminCardGroup(triples);
        }

        // 如果玩家没有比我更大的五张牌型，优先出五张牌型
        if (!analysis.fiveCardPatterns.isEmpty()) {
            // 找出AI手中最大的五张牌型
            List<CardGroup> fiveCardGroups = availableGroups.stream()
                .filter(group -> group.getCards().size() == 5)
                .collect(Collectors.toList());
            
            if (!fiveCardGroups.isEmpty()) {
                CardGroup aiLargestFive = findCardGroup(fiveCardGroups);
                // 检查玩家是否有更大的五张牌型
                boolean playerHasLargerFive = analysis.fiveCardPatterns.stream()
                    .anyMatch(group -> currentRule.compareCards(group.getCards(), aiLargestFive.getCards()) > 0);
                
                if (!playerHasLargerFive) {
                    //250529 调试信息：玩家没有比AI更大的五张牌型，AI出五张牌型
                    System.out.println("//250529 调试信息：玩家没有比AI更大的五张牌型，AI出五张牌型");
                    return findminCardGroup(fiveCardGroups);
                } else {
                    //250529 调试信息：玩家有比AI更大的五张牌型，AI不出五张牌型
                    System.out.println("//250529 调试信息：玩家有比AI更大的五张牌型，AI不出五张牌型");
                }
            }
        }

        //250529 调试信息：使用默认策略，选择最大的牌组
        System.out.println("//250529 调试信息：使用默认策略，选择最大的牌组");
        return findCardGroup(availableGroups);
    }

    /**
     * 选择跟牌策略
     */
    private CardGroup chooseFollowPlayStrategy(List<CardGroup> availableGroups,
                                             PlayerHandAnalysis analysis,
                                             List<Card> lastCards) {
        int lastCardsSize = lastCards.size();
        
        // 找出所有与上一手牌相同牌型的组合
        List<CardGroup> samePatternGroups = availableGroups.stream()
            .filter(group -> group.getCards().size() == lastCardsSize)
            .collect(Collectors.toList());

        if (!samePatternGroups.isEmpty()) {
            // 分析玩家是否有更大的同类型牌
            boolean playerHasLarger = analysis.playerPatterns.stream()
                .filter(group -> group.getCards().size() == lastCardsSize)
                .anyMatch(group -> currentRule.compareCards(group.getCards(), lastCards) > 0);

            if (!playerHasLarger) {
                //250529 调试信息：玩家没有更大的同类型牌，AI用最小的牌抢牌权
                System.out.println("//250529 调试信息：玩家没有更大的同类型牌，AI用最小的牌抢牌权");
                return findminCardGroup(samePatternGroups);
            } else {
                //250529 调试信息：玩家有更大的同类型牌，AI出最大的牌
                System.out.println("//250529 调试信息：玩家有更大的同类型牌，AI出最大的牌");
                return findCardGroup(samePatternGroups);
            }
        }

        // 如果上一手是五张牌型，分析玩家是否有更大的五张牌型
        if (lastCardsSize == 5) {
            List<CardGroup> fiveCardGroups = availableGroups.stream()
                .filter(group -> group.getCards().size() == 5)
                .collect(Collectors.toList());
            
            if (!fiveCardGroups.isEmpty()) {
                boolean playerHasLargerFive = analysis.fiveCardPatterns.stream()
                    .anyMatch(group -> currentRule.compareCards(group.getCards(), lastCards) > 0);

                if (!playerHasLargerFive) {
                    //250529 调试信息：玩家没有更大的五张牌型，AI用最小的五张牌抢牌权
                    System.out.println("//250529 调试信息：玩家没有更大的五张牌型，AI用最小的五张牌抢牌权");
                    return findminCardGroup(fiveCardGroups);
                } else {
                    //250529 调试信息：玩家有更大的五张牌型，AI不出五张牌型
                    System.out.println("//250529 调试信息：玩家有更大的五张牌型，AI不出五张牌型");
                }
            }
        }

        // 如果玩家没有某种牌型，保留这种牌型（在北方规则下更有意义，因为不同牌型有大小关系）
        if (lastCardsSize == 2 && analysis.pairs.isEmpty()) {
            //250529 调试信息：玩家没有对子，AI保留对子
            System.out.println("//250529 调试信息：玩家没有对子，AI保留对子");
            List<CardGroup> pairs = availableGroups.stream()
                .filter(group -> group.getCards().size() == 2)
                .collect(Collectors.toList());
            return pairs.isEmpty() ? findCardGroup(availableGroups) : findminCardGroup(pairs);
        }
        if (lastCardsSize == 3 && analysis.triples.isEmpty()) {
            //250529 调试信息：玩家没有三张，AI保留三张
            System.out.println("//250529 调试信息：玩家没有三张，AI保留三张");
            List<CardGroup> triples = availableGroups.stream()
                .filter(group -> group.getCards().size() == 3)
                .collect(Collectors.toList());
            return triples.isEmpty() ? findCardGroup(availableGroups) : findminCardGroup(triples);
        }
        if (lastCardsSize == 5 && analysis.fiveCardPatterns.isEmpty()) {
            //250529 调试信息：玩家没有五张牌型，AI保留五张牌型
            System.out.println("//250529 调试信息：玩家没有五张牌型，AI保留五张牌型");
            List<CardGroup> fiveCardGroups = availableGroups.stream()
                .filter(group -> group.getCards().size() == 5)
                .collect(Collectors.toList());
            return fiveCardGroups.isEmpty() ? findCardGroup(availableGroups) : findminCardGroup(fiveCardGroups);
        }

        //250529 调试信息：使用默认策略，选择最大的牌组
        System.out.println("//250529 调试信息：使用默认策略，选择最大的牌组");
        return findCardGroup(availableGroups);
    }

    @Override
    public CardGroup findCardGroup(List<CardGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }
        return groups.stream()
            .max((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
            .orElse(null);
    }

    public CardGroup findminCardGroup(List<CardGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }
        return groups.stream()
            .min((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
            .orElse(null);
    }

    /**
     * 玩家手牌分析结果
     */
    private static class PlayerHandAnalysis {
        int playerCardCount;
        List<CardGroup> playerPatterns;
        boolean hasBigCards;
        List<CardGroup> pairs;      // 对子列表
        List<CardGroup> triples;    // 三张列表
        List<CardGroup> fiveCardPatterns;  // 五张牌型列表
    }
}
