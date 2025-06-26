package soog.com.fabricproxyplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soog.com.fabricproxyplus.config.ProxyConfig;
import soog.com.fabricproxyplus.util.ConnectionDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerManagerMixin.class);
    
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void processBungeeCordProfile(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord()) {
            ConnectionDataAccess connectionAccess = (ConnectionDataAccess) connection;
            if (connectionAccess.getBungeeCordData() != null && connectionAccess.getBungeeCordData().isBungeeCordConnection()) {
                GameProfile spoofedProfile = connectionAccess.getBungeeCordData().getSpoofedProfile();
                
                if (spoofedProfile != null && spoofedProfile.getProperties().size() > 0) {
                    // Apply BungeeCord properties to the player's profile
                    player.getGameProfile().getProperties().clear();
                    player.getGameProfile().getProperties().putAll(spoofedProfile.getProperties());
                    
                    LOGGER.info("Applied {} BungeeCord properties to player {} on connect", 
                        spoofedProfile.getProperties().size(), player.getName().getString());
                    
                    // Log details about the textures property
                    if (player.getGameProfile().getProperties().containsKey("textures")) {
                        var textureProps = player.getGameProfile().getProperties().get("textures");
                        if (!textureProps.isEmpty()) {
                            var prop = textureProps.iterator().next();
                            LOGGER.info("Player {} has texture property with signature: {}", 
                                player.getName().getString(), prop.hasSignature());
                        }
                    } else {
                        LOGGER.warn("No texture property found for player {} after applying BungeeCord properties!", 
                            player.getName().getString());
                    }
                }
                
                if (config.isEnableDebugLogging()) {
                    LOGGER.info("BungeeCord player {} connecting", player.getName().getString());
                }
            }
        }
    }
    
    @Inject(
        method = "remove",
        at = @At("HEAD")
    )
    private void logPlayerRemoval(ServerPlayerEntity player, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        
        if (config.isEnableBungeeCord() && config.isEnableDebugLogging()) {
            LOGGER.info("Player {} being removed from server", player.getName().getString());
            
            // Log stack trace to understand why player is being removed
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                LOGGER.info("  at {}", stackTrace[i]);
            }
        }
    }
}