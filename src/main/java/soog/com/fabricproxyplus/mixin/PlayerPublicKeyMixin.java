package soog.com.fabricproxyplus.mixin;

import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@Mixin(PlayerPublicKey.class)
public class PlayerPublicKeyMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPublicKeyMixin.class);
    
    @Inject(
        method = "verifyAndDecode",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void allowBungeeCordProfiles(SignatureVerifier servicesSignatureVerifier, UUID playerUuid, PlayerPublicKey.PublicKeyData publicKeyData, CallbackInfoReturnable<PlayerPublicKey> cir) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableIpForwarding()) {
            LOGGER.info("Bypassing public key verification for potential BungeeCord player UUID: {}", playerUuid);
            // Return null to indicate no public key (which is acceptable for offline-mode compatible servers)
            cir.setReturnValue(null);
        }
    }
}