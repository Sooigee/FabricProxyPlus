package soog.com.fabricproxyplus.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.network.BungeeCordConnectionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ConnectionDataAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionMixin.class);
    
    @Shadow
    private Channel channel;
    
    @Shadow @Final
    private NetworkSide side;
    
    private BungeeCordConnectionData bungeeCordData;
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(NetworkSide side, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) {
            this.bungeeCordData = new BungeeCordConnectionData();
        }
    }
    
    public BungeeCordConnectionData getBungeeCordData() {
        return bungeeCordData;
    }
    
    @Inject(method = "channelActive", at = @At("HEAD"))
    private void onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) {
            ProxyConfig config = ProxyConfig.getInstance();
            if (config.isEnableDebugLogging()) {
                LOGGER.info("=== CONNECTION DEBUG ===");
                LOGGER.info("New connection established from: {}", context.channel().remoteAddress());
                LOGGER.info("NetworkSide: {}", side);
                LOGGER.info("BungeeCord mode: {}", config.isEnableBungeeCord());
            }
            
            if (config.isEnableBungeeCord()) {
                // Store the real address before BungeeCord modifies it
                SocketAddress address = context.channel().remoteAddress();
                if (bungeeCordData != null) {
                    bungeeCordData.setOriginalAddress(address.toString());
                    if (config.isEnableDebugLogging()) {
                        LOGGER.info("Stored original address: {}", address);
                    }
                } else {
                    LOGGER.error("BungeeCord data is null!");
                }
            }
        }
    }
    
    @Inject(method = "channelInactive", at = @At("HEAD"))
    private void onChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND && ProxyConfig.getInstance().isEnableBungeeCord()) {
            if (ProxyConfig.getInstance().isEnableDebugLogging()) {
                LOGGER.info("=== CONNECTION CLOSED ===");
                LOGGER.info("Connection closed from: {}", context.channel().remoteAddress());
            }
        }
    }
    
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void onException(ChannelHandlerContext context, Throwable cause, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND && ProxyConfig.getInstance().isEnableBungeeCord()) {
            if (ProxyConfig.getInstance().isEnableDebugLogging()) {
                LOGGER.error("=== CONNECTION EXCEPTION ===", cause);
            }
        }
    }
}