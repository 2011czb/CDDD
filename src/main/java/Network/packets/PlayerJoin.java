package Network.packets;

/**
 * 玩家加入游戏的数据包
 */
public class PlayerJoin {
    private String clientId; // 客户端生成的临时ID
    private String playerName; // 玩家名称
    
    /**
     * 无参构造函数，用于Kryo序列化
     */
    public PlayerJoin() {
        this.clientId = "";
        this.playerName = "";
    }
    
    /**
     * 创建玩家加入数据包
     * @param clientId 客户端生成的临时ID
     * @param playerName 玩家名称
     */
    public PlayerJoin(String clientId, String playerName) {
        this.clientId = clientId;
        this.playerName = playerName;
    }
    
    /**
     * 获取客户端ID
     * @return 客户端ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * 设置客户端ID
     * @param clientId 客户端ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /**
     * 获取玩家名称
     * @return 玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * 设置玩家名称
     * @param playerName 玩家名称
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
} 