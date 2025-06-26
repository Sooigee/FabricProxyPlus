package soog.com.fabricproxyplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProxyConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ProxyConfig instance;
    
    private boolean enableBungeeCord = true;
    private boolean enableIpForwarding = true;
    private boolean autoDetectProxy = true;
    private String defaultServer = "";
    private boolean showServerInTab = true;
    private boolean enableDebugLogging = false;
    
    private ProxyConfig() {}
    
    public static ProxyConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    private static ProxyConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fabricproxyplus.json");
        
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                ProxyConfig config = GSON.fromJson(json, ProxyConfig.class);
                LOGGER.info("Loaded FabricProxyPlus configuration");
                return config;
            } catch (IOException e) {
                LOGGER.error("Failed to load configuration", e);
            }
        }
        
        ProxyConfig config = new ProxyConfig();
        config.save();
        return config;
    }
    
    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fabricproxyplus.json");
        
        try {
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("Saved FabricProxyPlus configuration");
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }
    
    public boolean isEnableBungeeCord() {
        return enableBungeeCord;
    }
    
    public void setEnableBungeeCord(boolean enableBungeeCord) {
        this.enableBungeeCord = enableBungeeCord;
    }
    
    public boolean isEnableIpForwarding() {
        return enableIpForwarding;
    }
    
    public void setEnableIpForwarding(boolean enableIpForwarding) {
        this.enableIpForwarding = enableIpForwarding;
    }
    
    public boolean isAutoDetectProxy() {
        return autoDetectProxy;
    }
    
    public void setAutoDetectProxy(boolean autoDetectProxy) {
        this.autoDetectProxy = autoDetectProxy;
    }
    
    public String getDefaultServer() {
        return defaultServer;
    }
    
    public void setDefaultServer(String defaultServer) {
        this.defaultServer = defaultServer;
    }
    
    public boolean isShowServerInTab() {
        return showServerInTab;
    }
    
    public void setShowServerInTab(boolean showServerInTab) {
        this.showServerInTab = showServerInTab;
    }
    
    public boolean isEnableDebugLogging() {
        return enableDebugLogging;
    }
    
    public void setEnableDebugLogging(boolean enableDebugLogging) {
        this.enableDebugLogging = enableDebugLogging;
    }
}