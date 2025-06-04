package Network.server;

import java.io.IOException;
import java.util.List;

/**
 * 游戏服务端接口，定义服务端的所有方法
 */
public interface GameServerInterface {
    
    /**
     * 启动服务器
     * @throws IOException 启动失败时抛出异常
     */
    void start() throws IOException;
    
    /**
     * 停止服务器
     */
    void stop();
    
    /**
     * 设置主机名称
     * @param hostName 主机名称
     */
    void setHostName(String hostName);
    
    /**
     * 设置游戏规则
     * @param gameRule 游戏规则（"NORTH"或"SOUTH"）
     */
    void setGameRule(String gameRule);
    
    /**
     * 获取当前玩家数量
     * @return 当前玩家数量
     */
    int getPlayerCount();
    
    /**
     * 获取所有玩家名称
     * @return 所有玩家名称列表
     */
    List<String> getPlayerNames();
    
    /**
     * 检查游戏是否正在运行
     * @return 游戏是否正在运行
     */
    boolean isGameRunning();
    
    /**
     * 开始游戏
     */
    void startGame();
    
    /**
     * 广播游戏状态更新
     */
    void broadcastGameStateUpdate();
    
    /**
     * 服务器事件类型
     */
    enum ServerEvent {
        PLAYER_JOINED,       // 玩家加入
        PLAYER_LEFT,         // 玩家离开
        GAME_STARTED,        // 游戏开始
        GAME_ENDED,          // 游戏结束
        PLAYER_ACTION        // 玩家动作
    }
    
    /**
     * 服务器状态监听器接口
     */
    interface ServerStateListener {
        /**
         * 当服务器状态发生变化时调用
         * @param event 服务器事件类型
         * @param data 事件相关数据
         */
        void onServerStateChanged(ServerEvent event, Object data);
    }
    
    /**
     * 添加服务器状态监听器
     * @param listener 监听器对象
     */
    void addStateListener(ServerStateListener listener);
    
    /**
     * 移除服务器状态监听器
     * @param listener 要移除的监听器对象
     */
    void removeStateListener(ServerStateListener listener);
} 