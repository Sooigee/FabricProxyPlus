package soog.com.fabricproxyplus.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(HandshakeC2SPacket.class)
public class HandshakeC2SPacketMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandshakeC2SPacketMixin.class);
    
    @Shadow @Final @Mutable
    private String address;
    
    @ModifyArg(
        method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readString(I)Ljava/lang/String;"),
        index = 0
    )
    private static int increaseStringLimitForBungeeCord(int maxLength) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && maxLength < 32767) {
            LOGGER.info("Increasing handshake string limit from {} to 32767 for BungeeCord", maxLength);
            return 32767;
        }
        
        return maxLength;
    }
    
    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void processHandshake(PacketByteBuf buf, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableDebugLogging()) {
            LOGGER.info("Handshake packet constructed with address: '{}' (length: {})", address, address.length());
        }
    }
}