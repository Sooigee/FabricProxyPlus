package soog.com.fabricproxyplus.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ProxyNetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyNetworkHandler.class);
    private static ProxyNetworkHandler instance;
    
    private final Set<String> availableServers = new HashSet<>();
    private String currentServer = "";
    private boolean bungeeCordEnabled = false;
    
    private ProxyNetworkHandler() {}
    
    public static ProxyNetworkHandler getInstance() {
        if (instance == null) {
            instance = new ProxyNetworkHandler();
        }
        return instance;
    }
    
    public void initialize() {
        registerChannels();
        setupHandlers();
    }
    
    private void registerChannels() {
        ClientPlayNetworking.registerGlobalReceiver(
            BungeeCordPayload.ID,
            (payload, context) -> handleBungeeCordPayload(payload, context)
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
            LegacyBungeeCordPayload.ID,
            (payload, context) -> handleLegacyBungeeCordPayload(payload, context)
        );
    }
    
    private void setupHandlers() {
        LOGGER.info("FabricProxyPlus network handlers initialized");
    }
    
    private void handleBungeeCordPayload(BungeeCordPayload payload, ClientPlayNetworking.Context context) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(payload.data()));
        handleBungeeCordMessage(buf);
    }
    
    private void handleLegacyBungeeCordPayload(LegacyBungeeCordPayload payload, ClientPlayNetworking.Context context) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(payload.data()));
        handleBungeeCordMessage(buf);
    }
    
    private void handleBungeeCordMessage(PacketByteBuf buf) {
        try {
            String subchannel = buf.readString(32767);
            
            switch (subchannel) {
                case BungeeCordProtocol.GET_SERVERS_SUBCHANNEL:
                    handleGetServersResponse(buf);
                    break;
                case BungeeCordProtocol.GET_SERVER_SUBCHANNEL:
                    handleGetServerResponse(buf);
                    break;
                case BungeeCordProtocol.PLAYER_COUNT_SUBCHANNEL:
                    handlePlayerCountResponse(buf);
                    break;
                case BungeeCordProtocol.PLAYER_LIST_SUBCHANNEL:
                    handlePlayerListResponse(buf);
                    break;
                default:
                    LOGGER.debug("Received unknown BungeeCord subchannel: {}", subchannel);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling BungeeCord message", e);
        }
    }
    
    private void handleGetServersResponse(PacketByteBuf buf) {
        String[] servers = buf.readString(32767).split(", ");
        availableServers.clear();
        for (String server : servers) {
            availableServers.add(server);
        }
        LOGGER.info("Available servers: {}", availableServers);
    }
    
    private void handleGetServerResponse(PacketByteBuf buf) {
        currentServer = buf.readString(32767);
        LOGGER.info("Current server: {}", currentServer);
    }
    
    private void handlePlayerCountResponse(PacketByteBuf buf) {
        String server = buf.readString(32767);
        int count = buf.readInt();
        LOGGER.info("Player count on {}: {}", server, count);
    }
    
    private void handlePlayerListResponse(PacketByteBuf buf) {
        String server = buf.readString(32767);
        String[] players = buf.readString(32767).split(", ");
        LOGGER.info("Players on {}: {}", server, String.join(", ", players));
    }
    
    public void sendPluginMessage(String subchannel, Object... args) {
        if (!ClientPlayNetworking.canSend(BungeeCordPayload.ID)) {
            LOGGER.warn("Cannot send BungeeCord plugin message - channel not registered");
            return;
        }
        
        PacketByteBuf buf = BungeeCordProtocol.createPluginMessage(subchannel, args);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        ClientPlayNetworking.send(new BungeeCordPayload(data));
    }
    
    public void connectToServer(String serverName) {
        LOGGER.info("Attempting to connect to server: {}", serverName);
        sendPluginMessage(BungeeCordProtocol.CONNECT_SUBCHANNEL, serverName);
    }
    
    public void requestServerList() {
        sendPluginMessage(BungeeCordProtocol.GET_SERVERS_SUBCHANNEL);
    }
    
    public void requestCurrentServer() {
        sendPluginMessage(BungeeCordProtocol.GET_SERVER_SUBCHANNEL);
    }
    
    public void requestPlayerCount(String serverName) {
        sendPluginMessage(BungeeCordProtocol.PLAYER_COUNT_SUBCHANNEL, serverName);
    }
    
    public void requestPlayerList(String serverName) {
        sendPluginMessage(BungeeCordProtocol.PLAYER_LIST_SUBCHANNEL, serverName);
    }
    
    public Set<String> getAvailableServers() {
        return new HashSet<>(availableServers);
    }
    
    public String getCurrentServer() {
        return currentServer;
    }
    
    public boolean isBungeeCordEnabled() {
        return bungeeCordEnabled;
    }
    
    public void setBungeeCordEnabled(boolean enabled) {
        this.bungeeCordEnabled = enabled;
    }
}