package soog.com.fabricproxyplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(MinecraftServer.class)
public class GameProfileMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameProfileMixin.class);
    
    @Inject(method = "shouldEnforceSecureProfile", at = @At("HEAD"), cancellable = true)
    private void disableSecureProfileForBungeeCord(CallbackInfoReturnable<Boolean> cir) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            if (config.isEnableDebugLogging()) {
                LOGGER.info("Disabling secure profile enforcement for BungeeCord connections");
            }
            cir.setReturnValue(false);
        }
    }
    
    // Override isOnlineMode temporarily when sending player list packets to ensure properties are sent
    @Inject(method = "isOnlineMode", at = @At("HEAD"), cancellable = true)
    private void forceOnlineModeForPlayerList(CallbackInfoReturnable<Boolean> cir) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            // Check if this is being called in the context of player list packets
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                String methodName = element.getMethodName();
                
                // If we're in the context of creating player list entries, return true
                if (className.contains("PlayerListS2CPacket") || 
                    (className.contains("ServerPlayerEntity") && methodName.contains("createSpawnPacket")) ||
                    (className.contains("PlayerManager") && methodName.contains("sendToAll"))) {
                    if (config.isEnableDebugLogging()) {
                        LOGGER.info("Temporarily returning online mode true for player list context: {}.{}", className, methodName);
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}