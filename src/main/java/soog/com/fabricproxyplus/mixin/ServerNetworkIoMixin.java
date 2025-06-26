package soog.com.fabricproxyplus.mixin;

import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerNetworkIo.class)
public class ServerNetworkIoMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerNetworkIoMixin.class);
    
    @Inject(method = "bind", at = @At("TAIL"))
    private void onBind(CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord()) {
            LOGGER.info("ServerNetworkIo bound - BungeeCord support is ACTIVE");
            LOGGER.info("Expecting BungeeCord connections with modified handshake packets");
        }
    }
}