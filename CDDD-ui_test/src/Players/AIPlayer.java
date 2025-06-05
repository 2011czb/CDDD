package Players;

import cards.Card;
import java.util.List;
import AI.*;

/**
 * AI玩家
 * */
public class AIPlayer extends Player {
    private final AIStrategy aiStrategy;

    public AIPlayer(String name, AIStrategy strategy) {
        super(name);
        this.aiStrategy = strategy;
    }

    public AIPlayer(String name) {
        this(name, SmartAIStrategy1.INSTANCE);//默认简单规则
    }

    public AIStrategy getStrategy() {
        return aiStrategy;
    }

    @Override
    public List<Card> play(List<Card> lastCards) {
        return aiStrategy.makeDecision(this, lastCards);
    }
}