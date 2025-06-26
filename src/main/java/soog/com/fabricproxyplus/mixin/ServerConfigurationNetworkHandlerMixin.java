package soog.com.fabricproxyplus.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerConfigurationNetworkHandler.class)
public class ServerConfigurationNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigurationNetworkHandlerMixin.class);
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void logBungeeCordConnectionInit(CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableDebugLogging()) {
            LOGGER.info("ServerConfigurationNetworkHandler created - BungeeCord mode enabled");
        }
    }
    
    @Inject(method = "onReady", at = @At("HEAD"))
    private void logConfigurationReady(CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableDebugLogging()) {
            LOGGER.info("Configuration phase ready - proceeding with BungeeCord connection");
        }
    }
}