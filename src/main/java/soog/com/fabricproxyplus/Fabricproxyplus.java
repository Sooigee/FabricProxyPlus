package soog.com.fabricproxyplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.command.ServerProxyCommands;
import soog.com.fabricproxyplus.network.BungeeCordPayload;
import soog.com.fabricproxyplus.network.LegacyBungeeCordPayload;
import soog.com.fabricproxyplus.network.ServerProxyHandler;

public class Fabricproxyplus implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fabricproxyplus.class);
    public static final String MOD_ID = "fabricproxyplus";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing FabricProxyPlus server-side");
        
        // Load configuration
        ProxyConfig.getInstance();
        
        // Register custom payloads for server-side
        PayloadTypeRegistry.playC2S().register(BungeeCordPayload.ID, BungeeCordPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LegacyBungeeCordPayload.ID, LegacyBungeeCordPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BungeeCordPayload.ID, BungeeCordPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LegacyBungeeCordPayload.ID, LegacyBungeeCordPayload.CODEC);
        
        // Register server commands
        ServerProxyCommands.register();
        
        // Initialize server proxy handler
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerProxyHandler.getInstance().initialize(server);
            LOGGER.info("FabricProxyPlus: BungeeCord support enabled on server");
        });
        
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ServerProxyHandler.getInstance().shutdown();
        });
    }
}
