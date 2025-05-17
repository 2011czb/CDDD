import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import Card.*;

/**
 * 杂顺牌型实现
 * */

public class Straight extends PokerPattern {
    private static final Straight INSTANCE = new Straight();
    
    private Straight() {
        super("杂顺", 4);
    }
    
    public static Straight getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean match(List<Card> cards) {
        if (cards.size() != 5) return false;
        
        // 检查是否包含A
        boolean hasAce = false;
        boolean hasTen = false;
        boolean hasJack = false;
        boolean hasQueen = false;
        boolean hasKing = false;
        
        for (Card card : cards) {
            if (card == null) return false;
            switch (card.getRank().getValue()) {
                case 0: hasAce = true; break;
                case 9: hasTen = true; break;
                case 10: hasJack = true; break;
                case 11: hasQueen = true; break;
                case 12: hasKing = true; break;
            }
        }
        
        // 检查是否是10JQKA，唯一特殊情况
        if (hasAce && hasTen && hasJack && hasQueen && hasKing) {
            return true;
        }
        
        // 其他情况，检查是否构成连续序列
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((a, b) -> a.getRank().getValue() - b.getRank().getValue());
        return checkConsecutive(sortedCards);
    }
    
    private boolean checkConsecutive(List<Card> sortedCards) {
        // 检查是否连续
        for (int i = 0; i < 4; i++) {
            // 检查相邻两张牌的值是否相差1
            if (sortedCards.get(i + 1).getRank().getValue() - sortedCards.get(i).getRank().getValue() != 1) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getPatternWeight() {
        return this.weight;
    }
    
    @Override
    public int getCritical(List<Card> cards) {

        // 先对牌进行排序
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((a, b) -> a.getRank().getValue() - b.getRank().getValue());
        
        // 根据第一张牌的值判断特殊情况
        int firstRank = sortedCards.get(0).getRank().getValue();
        
        // 如果是A开头，判断是A2345还是10JQKA
        if (firstRank == 0) {
            // 检查是否是A2345
            if (sortedCards.get(1).getRank().getValue() == 1 && 
                sortedCards.get(2).getRank().getValue() == 2 && 
                sortedCards.get(3).getRank().getValue() == 3 && 
                sortedCards.get(4).getRank().getValue() == 4) {
                return sortedCards.get(4).getWeight(); // 返回5的getWeight
            }
            // 检查是否是10JQKA
            if (sortedCards.get(1).getRank().getValue() == 9 && 
                sortedCards.get(2).getRank().getValue() == 10 && 
                sortedCards.get(3).getRank().getValue() == 11 && 
                sortedCards.get(4).getRank().getValue() == 12) {
                return sortedCards.get(0).getWeight(); // 返回A的getWeight
            }
        }
        
        // 如果是2开头，判断是否是23456
        if (firstRank == 1) {
            if (sortedCards.get(1).getRank().getValue() == 2 && 
                sortedCards.get(2).getRank().getValue() == 3 && 
                sortedCards.get(3).getRank().getValue() == 4 && 
                sortedCards.get(4).getRank().getValue() == 5) {
                return sortedCards.get(4).getWeight(); // 返回6的getWeight
            }
        }
        
        // 其他情况，返回最大点数的牌的权重
        return sortedCards.get(4).getWeight();
    }
}