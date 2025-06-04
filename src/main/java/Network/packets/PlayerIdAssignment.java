package Network.packets;

/**
 * 服务器分配玩家ID的数据包
 */
public class PlayerIdAssignment {
    private String clientId; // 客户端生成的临时ID
    private String serverId; // 服务器分配的正式ID
    
    /**
     * 无参构造函数，用于Kryo序列化
     */
    public PlayerIdAssignment() {
        this.clientId = "";
        this.serverId = "";
    }
    
    /**
     * 创建玩家ID分配数据包
     * @param clientId 客户端生成的临时ID
     * @param serverId 服务器分配的正式ID
     */
    public PlayerIdAssignment(String clientId, String serverId) {
        this.clientId = clientId;
        this.serverId = serverId;
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
     * 获取服务器分配的ID
     * @return 服务器分配的ID
     */
    public String getServerId() {
        return serverId;
    }
    
    /**
     * 设置服务器分配的ID
     * @param serverId 服务器分配的ID
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
} 