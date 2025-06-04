package Network;

import Network.model.NetworkPlayer;
import Network.packets.*;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class NetworkManager {
    private static final int TCP_PORT = 54555;
    private static final int UDP_PORT = 54777;
    
    private Server server;
    private Client client;
    private Map<Connection, NetworkPlayer> connectedPlayers;
    private List<NetworkPlayer> playerList;
    private NetworkPlayer localPlayer;
    private boolean isServer;
    
    public NetworkManager(boolean isServer) {
        this.isServer = isServer;
        this.connectedPlayers = new ConcurrentHashMap<>();
        this.playerList = new ArrayList<>();
        
        if (isServer) {
            initializeServer();
        } else {
            initializeClient();
        }
    }
    
    private void initializeServer() {
        server = new Server();
        registerPackets(server.getKryo());
        setupServerListener();
    }
    
    private void initializeClient() {
        client = new Client();
        registerPackets(client.getKryo());
        setupClientListener();
    }
    
    private void registerPackets(com.esotericsoftware.kryo.Kryo kryo) {
        kryo.register(PlayerAction.class);
        kryo.register(ActionType.class);
        kryo.register(GameStart.class);
        kryo.register(GameEnd.class);
        kryo.register(GameStateUpdate.class);
        kryo.register(NetworkPlayer.class);
        kryo.register(ArrayList.class);
    }
    
    private void setupServerListener() {
        server.addListener(new com.esotericsoftware.kryonet.Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PlayerAction) {
                    handlePlayerAction(connection, (PlayerAction) object);
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
                handleDisconnection(connection);
            }
        });
    }
    
    private void setupClientListener() {
        client.addListener(new com.esotericsoftware.kryonet.Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof GameStateUpdate) {
                    handleGameStateUpdate((GameStateUpdate) object);
                } else if (object instanceof GameStart) {
                    handleGameStart((GameStart) object);
                } else if (object instanceof GameEnd) {
                    handleGameEnd((GameEnd) object);
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
                handleClientDisconnection();
            }
        });
    }
    
    public void start() throws IOException {
        if (isServer) {
            server.start();
            server.bind(TCP_PORT, UDP_PORT);
            System.out.println("服务器已启动，监听端口: TCP=" + TCP_PORT + ", UDP=" + UDP_PORT);
        } else {
            client.start();
        }
    }
    
    public void stop() {
        if (isServer) {
            server.stop();
        } else {
            client.stop();
        }
    }
    
    public void connect(String host) throws IOException {
        if (!isServer) {
            client.connect(5000, host, TCP_PORT, UDP_PORT);
        }
    }
    
    public void setLocalPlayer(NetworkPlayer player) {
        this.localPlayer = player;
    }
    
    private void handlePlayerAction(Connection connection, PlayerAction action) {
        // 处理玩家动作
    }
    
    private void handleDisconnection(Connection connection) {
        // 处理断开连接
    }
    
    private void handleGameStateUpdate(GameStateUpdate update) {
        // 处理游戏状态更新
    }
    
    private void handleGameStart(GameStart gameStart) {
        // 处理游戏开始
    }
    
    private void handleGameEnd(GameEnd gameEnd) {
        // 处理游戏结束
    }
    
    private void handleClientDisconnection() {
        // 处理客户端断开连接
    }
    
    public void sendAction(PlayerAction action) {
        if (isServer) {
            server.sendToAllTCP(action);
        } else {
            client.sendTCP(action);
        }
    }
} 