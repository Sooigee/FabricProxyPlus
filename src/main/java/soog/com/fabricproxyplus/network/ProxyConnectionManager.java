package soog.com.fabricproxyplus.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConnectionManager.class);
    private static ProxyConnectionManager instance;
    
    private boolean bungeeCordMode = false;
    private String targetServerAddress = "";
    private int targetServerPort = 25565;
    
    private ProxyConnectionManager() {}
    
    public static ProxyConnectionManager getInstance() {
        if (instance == null) {
            instance = new ProxyConnectionManager();
        }
        return instance;
    }
    
    public void handleHandshake(ClientConnection connection, HandshakeC2SPacket packet, CallbackInfo ci) {
        if (!bungeeCordMode) {
            return;
        }
        
        try {
            String address = packet.address();
            int port = packet.port();
            int protocolVersion = packet.protocolVersion();
            
            if (!address.contains("\0")) {
                String modifiedAddress = address + "\0FML\0";
                LOGGER.info("Modifying handshake for BungeeCord: {} -> {}", address, modifiedAddress);
                
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                BungeeCordProtocol.writeHandshakePacket(buf, address, port, protocolVersion);
                
            }
        } catch (Exception e) {
            LOGGER.error("Error handling handshake modification", e);
        }
    }
    
    public void enableBungeeCordMode(String serverAddress, int serverPort) {
        this.bungeeCordMode = true;
        this.targetServerAddress = serverAddress;
        this.targetServerPort = serverPort;
        LOGGER.info("BungeeCord mode enabled for {}:{}", serverAddress, serverPort);
    }
    
    public void disableBungeeCordMode() {
        this.bungeeCordMode = false;
        LOGGER.info("BungeeCord mode disabled");
    }
    
    public boolean isBungeeCordMode() {
        return bungeeCordMode;
    }
}