package soog.com.fabricproxyplus.network;

import java.util.HashMap;
import java.util.Map;

public class BungeeCordInfo {
    private static final Map<String, PlayerInfo> playerInfoMap = new HashMap<>();
    
    public static class PlayerInfo {
        public final String realIp;
        public final String uuid;
        public final String hostname;
        
        public PlayerInfo(String realIp, String uuid, String hostname) {
            this.realIp = realIp;
            this.uuid = uuid;
            this.hostname = hostname;
        }
    }
    
    public static void storePlayerInfo(String uuid, String realIp, String hostname) {
        playerInfoMap.put(uuid, new PlayerInfo(realIp, uuid, hostname));
    }
    
    public static PlayerInfo getPlayerInfo(String uuid) {
        return playerInfoMap.get(uuid);
    }
    
    public static void removePlayerInfo(String uuid) {
        playerInfoMap.remove(uuid);
    }
    
    public static void clear() {
        playerInfoMap.clear();
    }
}