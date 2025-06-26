package soog.com.fabricproxyplus.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerPlayNetworkHandler.class)
public class MessageValidatorMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageValidatorMixin.class);
    
    @Shadow @Final
    private ServerPlayerEntity player;
    
    @Inject(
        method = "isSecureChatEnforced",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void disableSecureChatForBungeeCord(CallbackInfoReturnable<Boolean> cir) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            LOGGER.info("Disabling secure chat enforcement for BungeeCord player: {}", player.getName().getString());
            cir.setReturnValue(false);
        }
    }
}