package Network;

import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import Network.model.NetworkCard;
import Network.model.NetworkPlayer;
import Network.packets.*;
import cards.Card;
import cards.Rank;
import cards.Suit;
import PokerPatterns.*;

import java.util.*;

public class PacketRegister {
    /**
     * Registers all necessary packet classes with Kryo for network serialization.
     * This includes game state updates, player actions, card data, and other network-related classes.
     * 
     * @param endpoint The network endpoint to register the packet classes with
     */
    public static void registerPacketsFor(EndPoint endpoint) {
        Kryo kryo = endpoint.getKryo();
        
        // 注册 UUID 的自定义序列化器
        kryo.register(UUID.class, new Serializer<UUID>() {
            @Override
            public void write(Kryo kryo, Output output, UUID uuid) {
                output.writeString(uuid.toString());
            }

            @Override
            public UUID read(Kryo kryo, Input input, Class<UUID> type) {
                return UUID.fromString(input.readString());
            }
        });

        // 注册常用集合类
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(HashMap.class);
        kryo.register(HashSet.class);
        kryo.register(Collections.EMPTY_LIST.getClass());
        kryo.register(Collections.EMPTY_MAP.getClass());
        kryo.register(Collections.EMPTY_SET.getClass());

        Class<?>[] classes = {
            GameStateUpdate.class,
            PlayerAction.class,
            GameStart.class,
            GameEnd.class,
            NetworkPlayer.class,
            NetworkCard.class,
            Card.class,
            Rank.class,
            Suit.class,
            ActionType.class,
            NetworkPlayer[].class,
            NetworkCard[].class,
            NetworkCard[][].class,
            java.util.Map.class,
            Heartbeat.class,
            EndTurn.class,
            ServerInfo.class
        };

        for (Class<?> c : classes) {
            kryo.register(c);
        }

        // 牌型相关
        kryo.register(PokerPatterns.basis.PokerPattern.class);
        kryo.register(PokerPatterns.basis.One.class);
        kryo.register(PokerPatterns.basis.Pair.class);
        kryo.register(PokerPatterns.basis.Three.class);
        kryo.register(PokerPatterns.basis.Straight.class);
        kryo.register(PokerPatterns.basis.Flush.class);
        kryo.register(PokerPatterns.basis.FullHouse.class);
        kryo.register(PokerPatterns.basis.FourofaKind.class);
        kryo.register(PokerPatterns.basis.StraightFlush.class);
    }
} 