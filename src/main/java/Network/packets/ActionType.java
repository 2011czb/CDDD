package Network.packets;

public enum ActionType {
    PLAY_CARD,    // 出牌
    PASS,         // 过牌
    END_TURN,     // 结束回合
    READY,        // 准备
    UNREADY,      // 取消准备
    JOIN,         // 加入游戏
    LEAVE_GAME    // 离开游戏
} 