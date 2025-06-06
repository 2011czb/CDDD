package API.network;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;

import cards.Card;
import cards.Rank;
import cards.Suit;

/**
 * 网络注册类
 * 统一管理所有需要在网络传输中序列化的类
 */
public class NetworkRegistry {
    /**
     * 注册所有需要序列化的类
     * @param kryo Kryo实例
     */
    public static void register(Kryo kryo) {
        // 注册基础类
        kryo.register(ArrayList.class);
        kryo.register(Card.class);
        kryo.register(Card[].class);
        kryo.register(Rank.class);
        kryo.register(Suit.class);
        
        // 注册网络消息类
        kryo.register(NetworkPlayer.class);
        kryo.register(NetworkPacket.JoinGameRequest.class);
        kryo.register(NetworkPacket.GameStarted.class);
        kryo.register(NetworkPacket.PlayCardsRequest.class);
        kryo.register(NetworkPacket.PlayCardsResponse.class);
        kryo.register(NetworkPacket.PassRequest.class);
        kryo.register(NetworkPacket.GameStateUpdate.class);
        kryo.register(NetworkPacket.PlayerDisconnected.class);
    }
} 