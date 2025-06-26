package soog.com.fabricproxyplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.network.BungeeCordInfo;
import soog.com.fabricproxyplus.network.ServerProxyHandler;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLoginNetworkHandlerMixin.class);
    
    @Shadow
    private GameProfile profile;
    
    @Shadow @Final
    private ClientConnection connection;
    
    @Shadow
    public abstract void disconnect(Text reason);
    
    @Inject(method = "onHello", at = @At("HEAD"))
    private void checkBungeeCordHandshake(CallbackInfo ci) {
        LOGGER.info("=== LOGIN HELLO DEBUG ===");
        LOGGER.info("Login hello received from: {}", connection.getAddress());
        
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord()) {
            ConnectionDataAccess connectionAccess = (ConnectionDataAccess) connection;
            
            if (connectionAccess.getBungeeCordData() != null) {
                LOGGER.info("BungeeCord data exists: {}", connectionAccess.getBungeeCordData().isBungeeCordConnection());
                
                if (connectionAccess.getBungeeCordData().isBungeeCordConnection()) {
                    LOGGER.info("This IS a BungeeCord connection");
                } else {
                    LOGGER.warn("This is NOT a BungeeCord connection but BungeeCord mode is enabled");
                }
            } else {
                LOGGER.error("No BungeeCord data attached to connection!");
            }
        }
    }
    
    @Inject(method = "onHello", at = @At("TAIL"))
    private void applyBungeeCordProfile(CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        ConnectionDataAccess connectionAccess = (ConnectionDataAccess) connection;
        
        if (config.isEnableBungeeCord() && connectionAccess.getBungeeCordData() != null) {
            if (connectionAccess.getBungeeCordData().isBungeeCordConnection()) {
                GameProfile spoofedProfile = connectionAccess.getBungeeCordData().getSpoofedProfile();
                if (spoofedProfile != null && profile != null) {
                    // Update the profile with BungeeCord data
                    profile = new GameProfile(spoofedProfile.getId(), profile.getName());
                    profile.getProperties().putAll(spoofedProfile.getProperties());
                    
                    LOGGER.info("Updated login profile for {} with {} BungeeCord properties", 
                        profile.getName(), spoofedProfile.getProperties().size());
                }
            }
        }
    }
    
    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    private void processBungeeCordData(GameProfile gameProfile, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        ConnectionDataAccess connectionAccess = (ConnectionDataAccess) connection;
        
        if (config.isEnableBungeeCord() && connectionAccess.getBungeeCordData() != null) {
            if (connectionAccess.getBungeeCordData().isBungeeCordConnection()) {
                // This is a BungeeCord connection
                GameProfile spoofedProfile = connectionAccess.getBungeeCordData().getSpoofedProfile();
                if (spoofedProfile != null) {
                    // Copy properties from spoofed profile to the actual profile
                    gameProfile.getProperties().clear();
                    gameProfile.getProperties().putAll(spoofedProfile.getProperties());
                    
                    if (config.isEnableDebugLogging()) {
                        LOGGER.info("Applied {} properties from BungeeCord to profile {}", 
                            spoofedProfile.getProperties().size(), gameProfile.getName());
                    }
                }
                
                if (config.isEnableDebugLogging()) {
                    LOGGER.info("Processing BungeeCord login for {}", gameProfile.getName());
                    if (connectionAccess.getBungeeCordData().getRealIp() != null) {
                        LOGGER.info("Real IP: {}", connectionAccess.getBungeeCordData().getRealIp());
                    }
                }
                
                // Mark the server as having received a BungeeCord connection
                ServerProxyHandler.getInstance().setBungeeCordDetected(true);
            }
        }
    }
}