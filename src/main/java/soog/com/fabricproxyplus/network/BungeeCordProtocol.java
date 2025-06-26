package soog.com.fabricproxyplus.network;

import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class BungeeCordProtocol {
    public static final String BUNGEECORD_CHANNEL = "bungeecord:main";
    public static final String LEGACY_CHANNEL = "BungeeCord";
    
    public static final String CONNECT_SUBCHANNEL = "Connect";
    public static final String CONNECT_OTHER_SUBCHANNEL = "ConnectOther";
    public static final String IP_SUBCHANNEL = "IP";
    public static final String IP_OTHER_SUBCHANNEL = "IPOther";
    public static final String PLAYER_COUNT_SUBCHANNEL = "PlayerCount";
    public static final String PLAYER_LIST_SUBCHANNEL = "PlayerList";
    public static final String GET_SERVERS_SUBCHANNEL = "GetServers";
    public static final String MESSAGE_SUBCHANNEL = "Message";
    public static final String GET_SERVER_SUBCHANNEL = "GetServer";
    public static final String FORWARD_SUBCHANNEL = "Forward";
    public static final String FORWARD_TO_PLAYER_SUBCHANNEL = "ForwardToPlayer";
    public static final String UUID_SUBCHANNEL = "UUID";
    public static final String UUID_OTHER_SUBCHANNEL = "UUIDOther";
    public static final String SERVER_IP_SUBCHANNEL = "ServerIP";
    public static final String KICK_PLAYER_SUBCHANNEL = "KickPlayer";
    
    public static PacketByteBuf createPluginMessage(String subchannel, Object... args) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(subchannel);
        
        for (Object arg : args) {
            if (arg instanceof String) {
                buf.writeString((String) arg);
            } else if (arg instanceof Integer) {
                buf.writeInt((Integer) arg);
            } else if (arg instanceof Short) {
                buf.writeShort((Short) arg);
            } else if (arg instanceof byte[]) {
                buf.writeBytes((byte[]) arg);
            }
        }
        
        return buf;
    }
    
    public static void writeHandshakePacket(PacketByteBuf buf, String address, int port, int protocolVersion) {
        buf.writeVarInt(0x00);
        buf.writeVarInt(protocolVersion);
        buf.writeString(address + "\0FML\0");
        buf.writeShort(port);
        buf.writeVarInt(2);
    }
    
    public static void writeIpForwardingData(PacketByteBuf buf, String playerUuid, String playerName, String playerIp) {
        String data = String.format("\0%s\0%s\0%s", playerUuid, playerName, playerIp);
        buf.writeString(data);
    }
}