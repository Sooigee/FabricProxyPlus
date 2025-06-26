package soog.com.fabricproxyplus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import soog.com.fabricproxyplus.network.ServerProxyHandler;

public class ServerProxyCommands {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, registryAccess);
        });
    }
    
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        // Send player to another server
        dispatcher.register(CommandManager.literal("send")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", StringArgumentType.word())
                .then(CommandManager.argument("server", StringArgumentType.word())
                    .executes(context -> sendPlayer(context))))
        );
        
        // Send all players to another server
        dispatcher.register(CommandManager.literal("sendall")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("server", StringArgumentType.word())
                .executes(context -> sendAllPlayers(context)))
        );
        
        // Kick player from proxy
        dispatcher.register(CommandManager.literal("gkick")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", StringArgumentType.word())
                .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                    .executes(context -> kickPlayer(context))))
        );
        
        // Check if BungeeCord is detected
        dispatcher.register(CommandManager.literal("proxyinfo")
            .requires(source -> source.hasPermissionLevel(2))
            .executes(context -> showProxyInfo(context))
        );
    }
    
    private static int sendPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        String serverName = StringArgumentType.getString(context, "server");
        
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player != null) {
            ServerProxyHandler.getInstance().connectPlayer(player, serverName);
            context.getSource().sendFeedback(
                () -> Text.literal("Sent " + playerName + " to server " + serverName),
                true
            );
        } else {
            context.getSource().sendError(Text.literal("Player " + playerName + " not found"));
        }
        
        return 1;
    }
    
    private static int sendAllPlayers(CommandContext<ServerCommandSource> context) {
        String serverName = StringArgumentType.getString(context, "server");
        ServerProxyHandler handler = ServerProxyHandler.getInstance();
        
        int count = 0;
        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            handler.connectPlayer(player, serverName);
            count++;
        }
        
        final int finalCount = count;
        context.getSource().sendFeedback(
            () -> Text.literal("Sent " + finalCount + " players to server " + serverName),
            true
        );
        
        return count;
    }
    
    private static int kickPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        String reason = StringArgumentType.getString(context, "reason");
        
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        
        if (player != null) {
            ServerProxyHandler.getInstance().kickPlayer(player, reason);
            context.getSource().sendFeedback(
                () -> Text.literal("Kicked " + playerName + " from proxy: " + reason),
                true
            );
        } else {
            context.getSource().sendError(Text.literal("Player " + playerName + " not found"));
        }
        
        return 1;
    }
    
    private static int showProxyInfo(CommandContext<ServerCommandSource> context) {
        boolean detected = ServerProxyHandler.getInstance().isBungeeCordDetected();
        
        context.getSource().sendFeedback(
            () -> Text.literal("BungeeCord proxy: " + (detected ? "DETECTED" : "NOT DETECTED")),
            false
        );
        
        return 1;
    }
}