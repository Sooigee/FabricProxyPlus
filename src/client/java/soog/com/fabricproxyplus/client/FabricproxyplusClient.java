package soog.com.fabricproxyplus.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soog.com.fabricproxyplus.client.command.ProxyCommands;
import soog.com.fabricproxyplus.network.ProxyNetworkHandler;
import soog.com.fabricproxyplus.network.BungeeCordPayload;
import soog.com.fabricproxyplus.network.LegacyBungeeCordPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FabricproxyplusClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricproxyplusClient.class);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing FabricProxyPlus client");
        
        // Register custom payloads
        PayloadTypeRegistry.playS2C().register(BungeeCordPayload.ID, BungeeCordPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LegacyBungeeCordPayload.ID, LegacyBungeeCordPayload.CODEC);
        
        ProxyNetworkHandler.getInstance().initialize();
        ProxyCommands.register();
        
        LOGGER.info("FabricProxyPlus client initialized");
    }
}
