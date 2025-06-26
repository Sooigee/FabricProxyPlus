package soog.com.fabricproxyplus.mixin;

import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPlayNetworkHandlerMixin.class);
    
    @Shadow @Final
    private ServerPlayerEntity player;
    
    @Inject(
        method = "onPlayerSession",
        at = @At("HEAD"),
        cancellable = true
    )
    private void skipSessionForBungeeCord(PlayerSessionC2SPacket packet, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            LOGGER.info("Skipping player session packet for player: {} in BungeeCord mode", player.getName().getString());
            // Just cancel the packet processing to avoid the NPE
            ci.cancel();
        }
    }
}