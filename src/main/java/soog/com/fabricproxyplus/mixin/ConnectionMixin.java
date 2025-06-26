package soog.com.fabricproxyplus.mixin;

import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(targets = "net.minecraft.server.ServerNetworkIo$1")
public class ConnectionMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionMixin.class);
    
    @Inject(method = "initChannel", at = @At("TAIL"))
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (ProxyConfig.getInstance().isEnableBungeeCord()) {
            LOGGER.info("=== CHANNEL INIT DEBUG ===");
            LOGGER.info("Server channel initialized for BungeeCord support");
        }
    }
}