import Game.Game;
import Network.NetworkManager;
import Players.*;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("扑克牌游戏 - 选择游戏模式");
        
        // 选择游戏模式
        Scanner scanner = new Scanner(System.in);
        System.out.println("请选择游戏模式：");
        System.out.println("1. 单人模式（1位玩家对战3个AI）");
        System.out.println("2. 多人模式（4位玩家联机对战）");
        System.out.println("3. 测试模式（基本功能测试）");
        
        int choice = 1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("输入无效，默认选择单人模式");
        }
        
        switch (choice) {
            case 1:
                playSinglePlayerMode();
                break;
            case 2:
                playMultiplayerMode();
                break;
            case 3:
                testGameFunctions();
                break;
            default:
                System.out.println("无效选择，默认进入单人模式");
                playSinglePlayerMode();
        }
        scanner.close();
    }
    
    /**
     * 单人模式（1人对战3个AI）
     */
    private static void playSinglePlayerMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n===== 单人模式 =====");
        
        // 选择游戏规则
        System.out.println("请选择游戏规则：");
        System.out.println("1. 北方规则");
        System.out.println("2. 南方规则");
        
        int ruleChoice = 1;
        try {
            ruleChoice = Integer.parseInt(scanner.nextLine().trim());
            if (ruleChoice != 1 && ruleChoice != 2) {
                System.out.println("无效选择，默认使用北方规则");
                ruleChoice = 1;
            }
        } catch (Exception e) {
            System.out.println("输入无效，默认使用北方规则");
        }
        
        System.out.println("请输入你的名字：");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) {
            playerName = "玩家1";
        }
        
        // 创建单人模式游戏
        Game game = Game.createSinglePlayerGame(playerName, ruleChoice);
        
        // 开始游戏
        System.out.println("\n游戏开始！使用" + game.getRuleName());
        game.startGame();
    }
    
    /**
     * 多人模式（4人联机对战）
     */
    private static void playMultiplayerMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n===== 多人模式 =====");
        
        // 选择游戏规则
        System.out.println("请选择游戏规则：");
        System.out.println("1. 北方规则");
        System.out.println("2. 南方规则");
        
        int ruleChoice = 1;
        try {
            ruleChoice = Integer.parseInt(scanner.nextLine().trim());
            if (ruleChoice != 1 && ruleChoice != 2) {
                System.out.println("无效选择，默认使用北方规则");
                ruleChoice = 1;
            }
        } catch (Exception e) {
            System.out.println("输入无效，默认使用北方规则");
        }
        
        System.out.println("请选择联机方式：");
        System.out.println("1. 创建房间（作为主机）");
        System.out.println("2. 加入房间（作为客户端）");
        
        int choice = 1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("输入无效，默认创建房间");
        }
        
        NetworkManager networkManager = NetworkManager.getInstance();
        
        if (choice == 1) {
            // 创建房间
            System.out.println("请输入房间名称：");
            String roomName = scanner.nextLine().trim();
            if (roomName.isEmpty()) {
                roomName = "游戏房间";
            }
            
            System.out.println("请输入你的名字：");
            String hostName = scanner.nextLine().trim();
            if (hostName.isEmpty()) {
                hostName = "房主";
            }
            
            // 创建服务器
            if (!networkManager.createServer(roomName)) {
                System.out.println("创建房间失败，返回主菜单");
                return;
            }
            
            System.out.println("房间创建成功，房间名：" + roomName);
            System.out.println("等待其他玩家加入...");
            
            // 模拟其他玩家加入
            simulatePlayersJoining(networkManager);
            
            // 所有玩家就绪后，创建游戏
            if (networkManager.areAllPlayersReady()) {
                System.out.println("所有玩家已就绪，开始游戏");
                
                // 创建玩家列表
                List<String> playerNames = new ArrayList<>();
                playerNames.add(hostName);
                playerNames.add("玩家2");
                playerNames.add("玩家3");
                playerNames.add("玩家4");
                
                // 创建多人模式游戏
                Game game = Game.createMultiplayerGame(playerNames, ruleChoice);
                
                // 发送游戏开始信号
                networkManager.sendGameStartSignal(game);
                
                // 开始游戏
                System.out.println("\n游戏开始！使用" + game.getRuleName());
                game.startGame();
            } else {
                System.out.println("等待超时，未收集到足够的玩家");
            }
        } else {
            // 加入房间
            System.out.println("请输入房间地址：");
            String serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost";
            }
            
            System.out.println("请输入你的名字：");
            String playerName = scanner.nextLine().trim();
            if (playerName.isEmpty()) {
                playerName = "玩家";
            }
            
            // 连接到服务器
            if (!networkManager.connectToServer(serverAddress, playerName)) {
                System.out.println("连接到房间失败，返回主菜单");
                return;
            }
            
            System.out.println("成功连接到房间！");
            System.out.println("等待游戏开始...");
            
            // 等待游戏开始
            // 实际应用中，这里会有一个监听循环等待服务器的游戏开始信号
            System.out.println("游戏开始！");
            
            // 这里简化处理，客户端玩家不会启动自己的游戏实例
            // 而是接收服务器发送的游戏状态
            System.out.println("接收游戏数据中...");
            
            // 模拟游戏进行
            simulateNetworkGamePlay(networkManager, playerName);
        }
    }
    
    // 模拟其他玩家加入房间
    private static void simulatePlayersJoining(NetworkManager networkManager) {
        System.out.println("模拟其他玩家加入房间...");
        
        // 模拟3个玩家加入
        for (int i = 2; i <= 4; i++) {
            try {
                Thread.sleep(1000); // 等待1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println("玩家" + i + "加入了房间");
            networkManager.connectToServer("localhost", "玩家" + i);
        }
    }
    
    // 模拟网络游戏进行
    private static void simulateNetworkGamePlay(NetworkManager networkManager, String playerName) {
        System.out.println("模拟网络游戏进行...");
        
        // 模拟接收几轮游戏数据
        for (int round = 1; round <= 3; round++) {
            System.out.println("\n===== 回合 " + round + " =====");
            
            // 模拟其他玩家出牌
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(1000); // 等待1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("玩家" + i + "出牌：红桃" + (i+round));
            }
            
            // 模拟当前玩家出牌
            System.out.println("轮到你出牌，" + playerName);
            System.out.println("输入任意内容表示出牌");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            
            System.out.println("你出牌：方片A");
        }
        
        System.out.println("\n游戏结束！");
    }
    
    /**
     * 测试游戏基本功能
     */
    private static void testGameFunctions() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n===== 测试模式 =====");
        System.out.println("请选择测试内容：");
        System.out.println("1. 人机对战测试");
        System.out.println("2. Game类初始化测试");
        System.out.println("3. 手动发牌出牌测试");
        
        int choice = 1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("输入无效，默认选择测试1");
        }
        
        switch (choice) {
            case 1:
                testHumanVsAI();
                break;
            case 2:
                testGameInit();
                break;
            case 3:
                manualDealAndPlay();
                break;
            default:
                System.out.println("无效选择，退出程序");
        }
    }
    
    // 测试Game类初始化
    private static void testGameInit() {
        // 创建游戏并初始化玩家
        List<String> playerNames = Arrays.asList("玩家1", "玩家2", "玩家3", "玩家4");
        Game game = Game.createMultiplayerGame(playerNames, Game.RULE_NORTH); // 使用北方规则进行测试
        
        // 初始化游戏（发牌）
        game.initGame();
        
        System.out.println("游戏初始化测试完成，使用" + game.getRuleName());
        
        // 显示所有玩家的手牌
        for (Player player : game.getPlayers()) {
            displayPlayerHand(player);
        }
    }
    
    // 测试人机对战
    private static void testHumanVsAI() {
        System.out.println("\n===== 人机对战测试 =====");
        
        // 创建玩家和AI玩家
        Player human = new HumanPlayer("玩家");
        Player ai = new AIPlayer("电脑");
        
        // 创建牌堆并洗牌
        Deck deck = new Deck();
        System.out.println("创建牌堆并洗牌，共有" + deck.cardsRemaining() + "张牌");
        
        // 发牌（每人10张牌）
        for (int i = 0; i < 10; i++) {
            human.drawCard(deck);
            ai.drawCard(deck);
        }
        
        // 给玩家手牌排序
        human.sortHand();
        ai.sortHand();
        
        // 显示玩家手牌
        displayPlayerHand(human);
        System.out.println("AI玩家手牌数量：" + ai.getHand().size());
        
        // 模拟几个回合的对战
        List<Card> lastCards = null; // 上一手牌，初始为空
        
        for (int round = 1; round <= 5; round++) {
            if (human.getHand().isEmpty() || ai.getHand().isEmpty()) {
                System.out.println("游戏结束，有玩家已无牌可出");
                break;
            }
            
            System.out.println("\n===== 回合 " + round + " =====");
            
            // 玩家出牌
            System.out.println("轮到 " + human.getName() + " 出牌");
            List<Card> humanCards = human.play(lastCards);
            lastCards = !humanCards.isEmpty() ? humanCards : lastCards;
            
            // 检查玩家是否已经出完牌
            if (human.getHand().isEmpty()) {
                System.out.println("恭喜！" + human.getName() + " 获胜！");
                break;
            }
            
            // AI出牌
            System.out.println("轮到 " + ai.getName() + " 出牌");
            List<Card> aiCards = ai.play(lastCards);
            lastCards = !aiCards.isEmpty() ? aiCards : lastCards;
            
            // 显示玩家手牌
            displayPlayerHand(human);
            System.out.println("AI玩家手牌数量：" + ai.getHand().size());
            
            // 检查AI是否已经出完牌
            if (ai.getHand().isEmpty()) {
                System.out.println(ai.getName() + " 获胜！");
                break;
            }
        }
        
        System.out.println("游戏测试结束");
    }
    
    // 演示手动发牌和出牌
    private static void manualDealAndPlay() {
        System.out.println("\n===== 手动发牌和出牌演示 =====");
        
        // 创建玩家
        Player player1 = new HumanPlayer("玩家A");
        Player player2 = new HumanPlayer("玩家B");
        
        // 创建牌堆并洗牌
        Deck deck = new Deck();
        System.out.println("创建牌堆并洗牌，共有" + deck.cardsRemaining() + "张牌");
        
        // 发牌（每人5张牌）
        for (int i = 0; i < 5; i++) {
            player1.drawCard(deck);
            player2.drawCard(deck);
        }
        
        // 给玩家手牌排序
        player1.sortHand();
        player2.sortHand();
        
        // 显示玩家手牌
        displayPlayerHand(player1);
        displayPlayerHand(player2);
        
        // 说明新的出牌方式
        System.out.println("\n出牌说明：输入牌的索引（从1开始），多张牌用空格分隔，如'1 3 5'");
        
        // 玩家A出牌
        System.out.println("\n轮到玩家A出牌：");
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要出的牌的索引，多张牌用空格分隔：");
        String input = scanner.nextLine().trim();
        
        // 解析输入的索引
        List<Integer> cardIndices = new ArrayList<>();
        try {
            String[] indexStrings = input.split("\\s+");
            for (String indexStr : indexStrings) {
                // 转换为0基索引
                cardIndices.add(Integer.parseInt(indexStr) - 1);
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，使用默认索引[0, 2]");
            cardIndices = Arrays.asList(0, 2);
        }
        
        List<Card> playedCards = player1.playCards(cardIndices);
        
        // 显示出的牌
        System.out.print("出牌: ");
        for (Card card : playedCards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();
        
        // 显示出牌后的手牌
        displayPlayerHand(player1);
        
        // 玩家B响应
        System.out.println("\n轮到玩家B出牌：");
        System.out.println("请输入要出的牌的索引，多张牌用空格分隔：");
        input = scanner.nextLine().trim();
        
        // 解析输入的索引
        cardIndices = new ArrayList<>();
        try {
            String[] indexStrings = input.split("\\s+");
            for (String indexStr : indexStrings) {
                // 转换为0基索引
                cardIndices.add(Integer.parseInt(indexStr) - 1);
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，使用默认索引[1, 3]");
            cardIndices = Arrays.asList(1, 3);
        }
        
        playedCards = player2.playCards(cardIndices);
        
        // 显示出的牌
        System.out.print("出牌: ");
        for (Card card : playedCards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();
        
        // 显示出牌后的手牌
        displayPlayerHand(player2);
    }
    
    // 显示玩家手牌
    private static void displayPlayerHand(Player player) {
        System.out.println(player.getName() + "的手牌：");
        List<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            System.out.print((i + 1) + "." + hand.get(i).getDisplayName() + " ");
        }
        System.out.println();
    }
    
    /**
     * 显示玩家手牌（保护隐私版本）
     * @param player 要显示手牌的玩家
     * @param currentPlayer 当前操作的玩家
     */
    private static void displayPlayerHand(Player player, Player currentPlayer) {
        // 只有当前玩家可以看到自己的详细手牌
        if (player.equals(currentPlayer)) {
            System.out.println(player.getName() + "的手牌：");
            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                System.out.print((i + 1) + "." + hand.get(i).getDisplayName() + " ");
            }
            System.out.println();
        } else {
            // 其他玩家只显示牌的数量
            System.out.println(player.getName() + "的手牌数量：" + player.getHand().size());
        }
    }
}