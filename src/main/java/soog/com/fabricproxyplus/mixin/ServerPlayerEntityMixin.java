package soog.com.fabricproxyplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPlayerEntityMixin.class);
    
    // Store whether this player was connected through BungeeCord
    private boolean isBungeeCordPlayer = false;
    
    // Since getGameProfile() is defined in PlayerEntity (parent class), we need to use a different approach
    // We'll inject into a method that's actually defined in ServerPlayerEntity
    @Inject(method = "updateLastActionTime", at = @At("HEAD"))
    private void checkBungeeCordPlayer(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ProxyConfig config = ProxyConfig.getInstance();
        
        // Only check once per player
        if (!isBungeeCordPlayer && config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            GameProfile profile = player.getGameProfile();
            if (profile.getProperties().containsKey("textures")) {
                isBungeeCordPlayer = true;
                if (config.isEnableDebugLogging()) {
                    LOGGER.info("Player {} is a BungeeCord player with texture properties", profile.getName());
                }
            }
        }
    }
}