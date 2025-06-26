package soog.com.fabricproxyplus.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soog.com.fabricproxyplus.config.ProxyConfig;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerProxyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProxyHandler.class);
    private static ServerProxyHandler instance;
    
    private MinecraftServer server;
    private final Map<UUID, String> playerServers = new HashMap<>();
    private boolean bungeeCordDetected = false;
    
    private ServerProxyHandler() {}
    
    public static ServerProxyHandler getInstance() {
        if (instance == null) {
            instance = new ServerProxyHandler();
        }
        return instance;
    }
    
    public void initialize(MinecraftServer server) {
        this.server = server;
        registerHandlers();
        
        // Don't auto-detect, wait for actual BungeeCord connections
        if (ProxyConfig.getInstance().isEnableBungeeCord()) {
            LOGGER.info("BungeeCord mode enabled - waiting for proxy connections");
            bungeeCordDetected = false; // Will be set to true when we receive BungeeCord packets
        }
    }
    
    private void registerHandlers() {
        // Register BungeeCord channel handler
        ServerPlayNetworking.registerGlobalReceiver(
            BungeeCordPayload.ID,
            this::handleBungeeCordPayload
        );
        
        ServerPlayNetworking.registerGlobalReceiver(
            LegacyBungeeCordPayload.ID,
            this::handleLegacyBungeeCordPayload
        );
    }
    
    private void handleBungeeCordPayload(BungeeCordPayload payload, ServerPlayNetworking.Context context) {
        handleBungeeCordMessage(payload, context);
    }
    
    private void handleLegacyBungeeCordPayload(LegacyBungeeCordPayload payload, ServerPlayNetworking.Context context) {
        handleLegacyBungeeCordMessage(payload, context);
    }
    
    private void handleBungeeCordMessage(BungeeCordPayload payload, ServerPlayNetworking.Context context) {
        // Mark BungeeCord as detected when we receive a message
        if (!bungeeCordDetected) {
            bungeeCordDetected = true;
            LOGGER.info("BungeeCord proxy detected - received plugin message");
        }
        
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(payload.data()));
        ServerPlayerEntity player = context.player();
        
        try {
            String subchannel = buf.readString(32767);
            
            switch (subchannel) {
                case BungeeCordProtocol.CONNECT_SUBCHANNEL:
                    handleConnectRequest(player, buf);
                    break;
                case BungeeCordProtocol.GET_SERVER_SUBCHANNEL:
                    handleGetServerRequest(player);
                    break;
                case BungeeCordProtocol.FORWARD_SUBCHANNEL:
                    handleForwardMessage(player, buf);
                    break;
                default:
                    LOGGER.debug("Received BungeeCord message with subchannel: {}", subchannel);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling BungeeCord message", e);
        }
    }
    
    private void handleLegacyBungeeCordMessage(LegacyBungeeCordPayload payload, ServerPlayNetworking.Context context) {
        // Handle the same way as modern payload
        handleBungeeCordMessage(new BungeeCordPayload(payload.data()), context);
    }
    
    private void handleConnectRequest(ServerPlayerEntity player, PacketByteBuf buf) {
        String targetServer = buf.readString(32767);
        LOGGER.info("Player {} requested to connect to server: {}", player.getName().getString(), targetServer);
        
        // Send the request back to BungeeCord
        sendPluginMessage(player, BungeeCordProtocol.CONNECT_SUBCHANNEL, targetServer);
    }
    
    private void handleGetServerRequest(ServerPlayerEntity player) {
        // Request current server from BungeeCord
        sendPluginMessage(player, BungeeCordProtocol.GET_SERVER_SUBCHANNEL);
    }
    
    private void handleForwardMessage(ServerPlayerEntity player, PacketByteBuf buf) {
        String target = buf.readString(32767);
        String channel = buf.readString(32767);
        short len = buf.readShort();
        byte[] data = new byte[len];
        buf.readBytes(data);
        
        LOGGER.debug("Received forward message for {} on channel {}", target, channel);
    }
    
    public void sendPluginMessage(ServerPlayerEntity player, String subchannel, Object... args) {
        if (!ServerPlayNetworking.canSend(player, BungeeCordPayload.ID)) {
            LOGGER.warn("Cannot send BungeeCord message to {} - channel not available", player.getName().getString());
            return;
        }
        
        PacketByteBuf buf = BungeeCordProtocol.createPluginMessage(subchannel, args);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        
        ServerPlayNetworking.send(player, new BungeeCordPayload(data));
    }
    
    public void connectPlayer(ServerPlayerEntity player, String serverName) {
        sendPluginMessage(player, BungeeCordProtocol.CONNECT_SUBCHANNEL, serverName);
    }
    
    public void kickPlayer(ServerPlayerEntity player, String reason) {
        sendPluginMessage(player, BungeeCordProtocol.KICK_PLAYER_SUBCHANNEL, player.getName().getString(), reason);
    }
    
    public void sendMessage(ServerPlayerEntity player, String targetPlayer, String message) {
        sendPluginMessage(player, BungeeCordProtocol.MESSAGE_SUBCHANNEL, targetPlayer, message);
    }
    
    public boolean isBungeeCordDetected() {
        return bungeeCordDetected;
    }
    
    public void setBungeeCordDetected(boolean detected) {
        this.bungeeCordDetected = detected;
    }
    
    public void shutdown() {
        playerServers.clear();
        bungeeCordDetected = false;
    }
}