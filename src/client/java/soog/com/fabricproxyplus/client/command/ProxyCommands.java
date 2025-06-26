package soog.com.fabricproxyplus.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import soog.com.fabricproxyplus.network.ProxyNetworkHandler;

@Environment(EnvType.CLIENT)
public class ProxyCommands {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher, registryAccess);
        });
    }
    
    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("server")
            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(context -> switchServer(context)))
            .executes(context -> showCurrentServer(context))
        );
        
        dispatcher.register(ClientCommandManager.literal("servers")
            .executes(context -> listServers(context))
        );
        
        dispatcher.register(ClientCommandManager.literal("glist")
            .executes(context -> globalPlayerList(context))
            .then(ClientCommandManager.argument("server", StringArgumentType.word())
                .executes(context -> serverPlayerList(context)))
        );
        
        dispatcher.register(ClientCommandManager.literal("send")
            .then(ClientCommandManager.argument("player", StringArgumentType.word())
                .then(ClientCommandManager.argument("server", StringArgumentType.word())
                    .executes(context -> sendPlayer(context))))
        );
    }
    
    private static int switchServer(CommandContext<FabricClientCommandSource> context) {
        String serverName = StringArgumentType.getString(context, "name");
        ProxyNetworkHandler handler = ProxyNetworkHandler.getInstance();
        
        handler.connectToServer(serverName);
        context.getSource().sendFeedback(Text.literal("Attempting to connect to server: " + serverName));
        
        return 1;
    }
    
    private static int showCurrentServer(CommandContext<FabricClientCommandSource> context) {
        ProxyNetworkHandler handler = ProxyNetworkHandler.getInstance();
        handler.requestCurrentServer();
        
        String currentServer = handler.getCurrentServer();
        if (currentServer.isEmpty()) {
            context.getSource().sendFeedback(Text.literal("Requesting current server information..."));
        } else {
            context.getSource().sendFeedback(Text.literal("Current server: " + currentServer));
        }
        
        return 1;
    }
    
    private static int listServers(CommandContext<FabricClientCommandSource> context) {
        ProxyNetworkHandler handler = ProxyNetworkHandler.getInstance();
        handler.requestServerList();
        
        context.getSource().sendFeedback(Text.literal("Requesting server list..."));
        
        return 1;
    }
    
    private static int globalPlayerList(CommandContext<FabricClientCommandSource> context) {
        ProxyNetworkHandler handler = ProxyNetworkHandler.getInstance();
        
        for (String server : handler.getAvailableServers()) {
            handler.requestPlayerCount(server);
        }
        
        context.getSource().sendFeedback(Text.literal("Requesting global player list..."));
        
        return 1;
    }
    
    private static int serverPlayerList(CommandContext<FabricClientCommandSource> context) {
        String serverName = StringArgumentType.getString(context, "server");
        ProxyNetworkHandler handler = ProxyNetworkHandler.getInstance();
        
        handler.requestPlayerList(serverName);
        context.getSource().sendFeedback(Text.literal("Requesting player list for " + serverName + "..."));
        
        return 1;
    }
    
    private static int sendPlayer(CommandContext<FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        String serverName = StringArgumentType.getString(context, "server");
        
        context.getSource().sendFeedback(Text.literal("This command requires server-side permissions"));
        
        return 1;
    }
}