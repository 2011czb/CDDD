package scoring;

import Players.Player;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameSettlementManager {
    private Map<Player, Integer> playerScores;  // 存储玩家得分
    
    public GameSettlementManager() {
        this.playerScores = new HashMap<>();
    }
    
    /**
     * 进行游戏结算
     * @param players 所有玩家列表
     * @param winner 获胜的玩家
     */
    public void settleGame(List<Player> players, Player winner) {
        // 计算每个玩家的牌分
        Map<Player, Integer> cardScores = new HashMap<>();
        
        // 250531 测试输出：显示每个玩家剩余的牌数
        System.out.println("\n250531 测试输出 - 结算时各玩家剩余牌数：");
        System.out.println("----------------------------------------");
        for (Player player : players) {
            int remainingCards = player.getHand().size();
            System.out.printf("%s 剩余 %d 张牌\n", player.getName(), remainingCards);
        }
        System.out.println("----------------------------------------");
        
        for (Player player : players) {
            int remainingCards = player.getHand().size();
            boolean hasSpade2 = ScoreCalculator.hasSpade2(player.getHand());
            int cardScore = ScoreCalculator.calculateCardScore(remainingCards, hasSpade2);
            cardScores.put(player, cardScore);
        }
        
        // 计算每个玩家的最终得分
        for (Player player : players) {
            int[] otherPlayersCardScores = players.stream()
                .filter(p -> p != player)
                .mapToInt(cardScores::get)
                .toArray();
                
            int finalScore = ScoreCalculator.calculateFinalScore(
                cardScores.get(player),
                otherPlayersCardScores
            );
            
            // 更新玩家得分
            playerScores.put(player, finalScore);
        }
    }
    
    /**
     * 获取玩家的得分
     * @param player 玩家
     * @return 玩家得分
     */
    public int getPlayerScore(Player player) {
        return playerScores.getOrDefault(player, 0);
    }
    
    /**
     * 获取所有玩家的得分
     * @return 玩家得分映射
     */
    public Map<Player, Integer> getAllPlayerScores() {
        return new HashMap<>(playerScores);
    }
    
    /**
     * 清空所有得分
     */
    public void clearScores() {
        playerScores.clear();
    }
    
    /**
     * 打印结算结果
     */
    public void printSettlementResults() {
        System.out.println("\n游戏结算结果：");
        System.out.println("----------------");
        for (Map.Entry<Player, Integer> entry : playerScores.entrySet()) {
            Player player = entry.getKey();
            int score = entry.getValue();
            System.out.printf("%s 得分：%d\n", player.getName(), score);
        }
        System.out.println("----------------");
    }
} 