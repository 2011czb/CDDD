package Network.networktest;

import Network.network.GameServer;
import Network.network.GameClient;
import Network.network.model.NetworkPlayer;
import Network.network.packets.*;
import cards.Card;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkGameTest {
    private GameServer server;
    private List<GameClient> clients;
    private static final int CLIENT_COUNT = 4; // 修改为4个玩家
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 54555;
    private static final int TIMEOUT_SECONDS = 10; // 增加超时时间

    @Before
    public void setUp() throws Exception {
        // 启动服务器
        server = new GameServer();
        server.start();
        System.out.println("服务器已启动");

        // 创建并启动客户端
        clients = new ArrayList<>();
        for (int i = 0; i < CLIENT_COUNT; i++) {
            GameClient client = new GameClient();
            client.connect(SERVER_HOST, SERVER_PORT);
            clients.add(client);
            System.out.println("客户端 " + (i + 1) + " 已连接");
        }

        // 等待所有客户端连接
        Thread.sleep(2000); // 增加等待时间
    }

    @After
    public void tearDown() {
        // 关闭所有客户端
        for (GameClient client : clients) {
            if (client != null) {
                client.disconnect();
            }
        }
        // 关闭服务器
        if (server != null) {
            server.stop();
        }
        System.out.println("测试环境已清理");
    }

    @Test
    public void testGameFlow() throws Exception {
        // 1. 测试玩家加入
        CountDownLatch joinLatch = new CountDownLatch(CLIENT_COUNT);
        AtomicInteger connectedPlayers = new AtomicInteger(0);
        
        for (int i = 0; i < CLIENT_COUNT; i++) {
            final int index = i;
            clients.get(i).addStateListener(new GameStateListener() {
                @Override
                public void onGameStateUpdate(List<NetworkPlayer> players, String currentPlayerId) {
                    int count = connectedPlayers.incrementAndGet();
                    System.out.println("当前连接玩家数: " + count);
                    if (count == CLIENT_COUNT) {
                        joinLatch.countDown();
                    }
                }
            });
        }
        assertTrue("所有玩家应在" + TIMEOUT_SECONDS + "秒内加入", 
            joinLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 2. 测试准备状态
        CountDownLatch readyLatch = new CountDownLatch(CLIENT_COUNT);
        for (int i = 0; i < CLIENT_COUNT; i++) {
            final int index = i;
            clients.get(i).addStateListener(new GameStateListener() {
                @Override
                public void onGameStateUpdate(List<NetworkPlayer> players, String currentPlayerId) {
                    if (players.stream().allMatch(NetworkPlayer::isReady)) {
                        readyLatch.countDown();
                    }
                }
            });
            clients.get(i).sendReady();
            System.out.println("玩家 " + (i + 1) + " 已准备");
        }
        assertTrue("所有玩家应在" + TIMEOUT_SECONDS + "秒内准备", 
            readyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 3. 测试游戏开始和发牌
        CountDownLatch startLatch = new CountDownLatch(CLIENT_COUNT);
        CountDownLatch handLatch = new CountDownLatch(CLIENT_COUNT);
        
        for (int i = 0; i < CLIENT_COUNT; i++) {
            final int index = i;
            clients.get(i).addStateListener(new GameStateListener() {
                @Override
                public void onGameStart(List<NetworkPlayer> players, NetworkPlayer firstPlayer) {
                    System.out.println("玩家 " + (index + 1) + " 收到游戏开始消息");
                    startLatch.countDown();
                }

                @Override
                public void onGameStateUpdate(List<NetworkPlayer> players, String currentPlayerId) {
                    NetworkPlayer player = players.stream()
                        .filter(p -> p.getId().equals(clients.get(index).getPlayerId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (player != null && player.getHand().size() == 13) {
                        System.out.println("玩家 " + (index + 1) + " 收到13张牌");
                        handLatch.countDown();
                    }
                }
            });
        }
        
        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内开始", 
            startLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertTrue("所有玩家应在" + TIMEOUT_SECONDS + "秒内收到手牌", 
            handLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 4. 测试出牌
        CountDownLatch playLatch = new CountDownLatch(1);
        clients.get(0).addStateListener(new GameStateListener() {
            @Override
            public void onGameStateUpdate(List<NetworkPlayer> players, String currentPlayerId) {
                NetworkPlayer firstPlayer = players.stream()
                    .filter(p -> p.getId().equals(clients.get(0).getPlayerId()))
                    .findFirst()
                    .orElse(null);
                
                if (firstPlayer != null && firstPlayer.getHand().size() < 13) {
                    System.out.println("第一个玩家成功出牌，剩余手牌: " + firstPlayer.getHand().size());
                    playLatch.countDown();
                }
            }
        });

        // 第一个玩家出第一张牌
        List<Card> hand = clients.get(0).getPlayerHand();
        if (!hand.isEmpty()) {
            List<Card> cardsToPlay = new ArrayList<>();
            cardsToPlay.add(hand.get(0));
            System.out.println("第一个玩家尝试出牌: " + cardsToPlay.get(0));
            clients.get(0).sendPlayCard(cardsToPlay);
        }
        assertTrue("第一个玩家应在" + TIMEOUT_SECONDS + "秒内出牌", 
            playLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 5. 测试游戏结束
        CountDownLatch endLatch = new CountDownLatch(1);
        for (GameClient client : clients) {
            client.addStateListener(new GameStateListener() {
                @Override
                public void onGameEnd(NetworkPlayer winner, String reason) {
                    System.out.println("游戏结束，原因: " + reason);
                    endLatch.countDown();
                }
            });
        }

        // 模拟游戏结束：第一个玩家出完所有牌
        NetworkPlayer firstPlayer = server.getPlayerList().get(0);
        firstPlayer.clearHand();
        server.checkGameEnd();

        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内结束", 
            endLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testDisconnection() throws Exception {
        // 等待游戏开始
        CountDownLatch startLatch = new CountDownLatch(1);
        clients.get(0).addStateListener(new GameStateListener() {
            @Override
            public void onGameStart(List<NetworkPlayer> players, NetworkPlayer firstPlayer) {
                System.out.println("游戏开始，测试断线场景");
                startLatch.countDown();
            }
        });

        // 准备并开始游戏
        for (GameClient client : clients) {
            client.sendReady();
        }
        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内开始", 
            startLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 断开第一个客户端
        CountDownLatch disconnectLatch = new CountDownLatch(1);
        for (int i = 1; i < clients.size(); i++) {
            clients.get(i).addStateListener(new GameStateListener() {
                @Override
                public void onGameEnd(NetworkPlayer winner, String reason) {
                    if (reason.contains("断开连接")) {
                        System.out.println("检测到玩家断线，游戏结束");
                        disconnectLatch.countDown();
                    }
                }
            });
        }

        System.out.println("模拟第一个玩家断线");
        clients.get(0).disconnect();
        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内因断开连接而结束", 
            disconnectLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testMultipleDisconnections() throws Exception {
        // 等待游戏开始
        CountDownLatch startLatch = new CountDownLatch(1);
        clients.get(0).addStateListener(new GameStateListener() {
            @Override
            public void onGameStart(List<NetworkPlayer> players, NetworkPlayer firstPlayer) {
                System.out.println("游戏开始，测试多个玩家断线场景");
                startLatch.countDown();
            }
        });

        // 准备并开始游戏
        for (GameClient client : clients) {
            client.sendReady();
        }
        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内开始", 
            startLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 断开多个客户端
        CountDownLatch disconnectLatch = new CountDownLatch(1);
        clients.get(CLIENT_COUNT - 1).addStateListener(new GameStateListener() {
            @Override
            public void onGameEnd(NetworkPlayer winner, String reason) {
                if (reason.contains("断开连接")) {
                    System.out.println("检测到多个玩家断线，游戏结束");
                    disconnectLatch.countDown();
                }
            }
        });

        // 依次断开前三个客户端
        for (int i = 0; i < CLIENT_COUNT - 1; i++) {
            System.out.println("模拟玩家 " + (i + 1) + " 断线");
            clients.get(i).disconnect();
            Thread.sleep(1000); // 等待1秒，确保服务器有时间处理断线
        }

        assertTrue("游戏应在" + TIMEOUT_SECONDS + "秒内因多个玩家断线而结束", 
            disconnectLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
} 