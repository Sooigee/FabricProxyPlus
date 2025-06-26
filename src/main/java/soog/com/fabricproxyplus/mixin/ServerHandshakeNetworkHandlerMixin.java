package soog.com.fabricproxyplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.network.BungeeCordInfo;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandshakeNetworkHandlerMixin.class);
    
    @Shadow @Final
    private ClientConnection connection;
    
    @Inject(method = "onHandshake", at = @At("HEAD"))
    private void onHandshake(HandshakeC2SPacket packet, CallbackInfo ci) {
        try {
            ProxyConfig config = ProxyConfig.getInstance();
            
            HandshakeC2SPacketAccessor accessor = (HandshakeC2SPacketAccessor) (Object) packet;
            String address = accessor.getAddress();
            
            LOGGER.info("=== HANDSHAKE DEBUG ===");
            LOGGER.info("Handshake received with address: '{}'", address);
            LOGGER.info("Address length: {}, contains null char: {}", address.length(), address.contains("\0"));
            LOGGER.info("BungeeCord mode enabled: {}", config.isEnableBungeeCord());
            
            // Log raw bytes for debugging
            byte[] addressBytes = address.getBytes();
            StringBuilder hexString = new StringBuilder();
            for (byte b : addressBytes) {
                hexString.append(String.format("%02X ", b));
            }
            LOGGER.info("Address bytes (hex): {}", hexString.toString());
            
            if (config.isEnableBungeeCord()) {
                // Check if this is a BungeeCord handshake
                if (address.contains("\0")) {
                    String[] parts = address.split("\0", -1);
                    String hostname = parts[0];
                    
                    ConnectionDataAccess connectionAccess = (ConnectionDataAccess) connection;
                    if (connectionAccess.getBungeeCordData() != null) {
                        connectionAccess.getBungeeCordData().setBungeeCordConnection(true);
                        
                        if (parts.length >= 4 && config.isEnableIpForwarding()) {
                            // Format: hostname\0ip\0uuid\0properties
                            String playerIp = parts[1];
                            String playerUuidString = parts[2];
                            
                            try {
                                UUID playerUuid = UUID.fromString(playerUuidString.replaceFirst(
                                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                    "$1-$2-$3-$4-$5"
                                ));
                                
                                connectionAccess.getBungeeCordData().setRealIp(playerIp);
                                connectionAccess.getBungeeCordData().setPlayerUuid(playerUuid);
                                
                                // Store for login handler
                                BungeeCordInfo.storePlayerInfo(playerUuidString, playerIp, hostname);
                                
                                // Create spoofed profile
                                GameProfile spoofedProfile = new GameProfile(playerUuid, "");
                                
                                // Parse and apply properties if available
                                if (parts.length >= 4 && !parts[3].isEmpty()) {
                                    String propertiesJson = parts[3];
                                    if (config.isEnableDebugLogging()) {
                                        LOGGER.info("BungeeCord properties JSON: {}", propertiesJson);
                                    }
                                    soog.com.fabricproxyplus.util.ProfilePropertyParser.parseAndApplyProperties(propertiesJson, spoofedProfile);
                                }
                                
                                connectionAccess.getBungeeCordData().setSpoofedProfile(spoofedProfile);
                                
                                if (config.isEnableDebugLogging()) {
                                    LOGGER.info("BungeeCord handshake with IP forwarding - Host: {}, Real IP: {}, UUID: {}", 
                                        hostname, playerIp, playerUuid);
                                    LOGGER.info("Profile has {} properties", spoofedProfile.getProperties().size());
                                }
                            } catch (Exception e) {
                                LOGGER.error("Failed to parse BungeeCord data", e);
                            }
                        } else {
                            if (config.isEnableDebugLogging()) {
                                LOGGER.info("BungeeCord handshake without IP forwarding - Host: {}", hostname);
                            }
                        }
                    }
                    
                    // Update the address to only contain the hostname
                    accessor.setAddress(hostname);
                } else {
                    if (config.isEnableDebugLogging()) {
                        LOGGER.warn("Non-BungeeCord connection from {} but BungeeCord mode is enabled", address);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in handshake handler", e);
        }
    }
}