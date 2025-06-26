package soog.com.fabricproxyplus.network;

import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class BungeeCordConnectionData {
    private String originalAddress;
    private String realIp;
    private UUID playerUuid;
    private GameProfile spoofedProfile;
    private boolean bungeeCordConnection = false;
    
    public String getOriginalAddress() {
        return originalAddress;
    }
    
    public void setOriginalAddress(String originalAddress) {
        this.originalAddress = originalAddress;
    }
    
    public String getRealIp() {
        return realIp;
    }
    
    public void setRealIp(String realIp) {
        this.realIp = realIp;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }
    
    public GameProfile getSpoofedProfile() {
        return spoofedProfile;
    }
    
    public void setSpoofedProfile(GameProfile spoofedProfile) {
        this.spoofedProfile = spoofedProfile;
    }
    
    public boolean isBungeeCordConnection() {
        return bungeeCordConnection;
    }
    
    public void setBungeeCordConnection(boolean bungeeCordConnection) {
        this.bungeeCordConnection = bungeeCordConnection;
    }
}