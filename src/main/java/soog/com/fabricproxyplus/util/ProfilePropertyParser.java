package soog.com.fabricproxyplus.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfilePropertyParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilePropertyParser.class);
    
    public static void parseAndApplyProperties(String propertiesJson, GameProfile profile) {
        try {
            JsonArray properties = JsonParser.parseString(propertiesJson).getAsJsonArray();
            
            for (JsonElement element : properties) {
                JsonObject property = element.getAsJsonObject();
                String name = property.get("name").getAsString();
                String value = property.get("value").getAsString();
                String signature = property.has("signature") ? property.get("signature").getAsString() : null;
                
                if (signature != null) {
                    profile.getProperties().put(name, new Property(name, value, signature));
                } else {
                    profile.getProperties().put(name, new Property(name, value));
                }
                
                LOGGER.info("Added property '{}' to profile {} (has signature: {})", name, profile.getName(), signature != null);
                if (name.equals("textures")) {
                    LOGGER.info("Texture value: {}", value.substring(0, Math.min(value.length(), 50)) + "...");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse profile properties", e);
        }
    }
}