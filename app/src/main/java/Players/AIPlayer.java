package Players;

import cards.Card;
import java.util.List;
import AI.*;

/**
 * AI玩家
 * */
public class AIPlayer extends Player {
    private AIStrategy aiStrategy;

    public AIPlayer(String name, AIStrategy strategy) {
        super(name);
        this.aiStrategy = strategy;
    }

    public AIPlayer(String name) {
        this(name, MinAIStrategy.INSTANCE);//默认简单规则
    }

    public AIStrategy getStrategy() {
        return aiStrategy;
    }

    /**
     * 设置AI策略
     * @param strategy 新的AI策略
     */
    public void setStrategy(AIStrategy strategy) {
        this.aiStrategy = strategy;
    }

    @Override
    public List<Card> play(List<Card> lastCards) {
        return aiStrategy.makeDecision(this, lastCards);
    }
}