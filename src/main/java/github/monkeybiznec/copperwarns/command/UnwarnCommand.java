package github.monkeybiznec.copperwarns.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import github.api.command.ArgumentNode;
import github.api.command.PermissionLevel;
import github.api.command.builder.CommandNodeBuilder;
import github.monkeybiznec.annotations.AutoRegCommand;
import github.monkeybiznec.copperwarns.*;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
@AutoRegCommand(name = "unwarn")
public class UnwarnCommand {
    public void register(@NotNull CommandNodeBuilder builder) {
        ArgumentNode<Integer> amountArg = new ArgumentNode<>("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
            ServerPlayer moderator = ctx.source().getPlayerOrException();
            MinecraftServer server = ctx.source().getServer();
            String targetName = ctx.getString("player");
            int amount = ctx.getInt("amount");
            Optional<PlayerInfo> playerInfoOpt = PlayerInfo.findPlayer(server, targetName);
            if (playerInfoOpt.isEmpty()) {
                moderator.sendSystemMessage(MessageUtils.playerNotFound(targetName));
                return 0;
            }
            PlayerInfo playerInfo = playerInfoOpt.get();
            if (playerInfo.mode() == PlayerMode.ONLINE) {
                ServerPlayer target = playerInfo.onlinePlayer();
                Warn.INSTANCE.removeWarns(target, amount);
                moderator.sendSystemMessage(MessageUtils.warnsRemoved(target, amount));
            } else {
                Warn.INSTANCE.removeWarns(playerInfo.offlineProfile().getId(), amount);
                moderator.sendSystemMessage(MessageUtils.warnsRemovedOffline(playerInfo.offlineProfile().getName(), amount));
                Warn.INSTANCE.saveWarns(server);
            }
            return 1;
        });
        ArgumentNode<String> playerArg = new ArgumentNode<>("player", StringArgumentType.word()).suggests(Utils.ONLINE_PLAYER_NAMES).addChild(amountArg);
        builder.requires(PermissionLevel.OPERATOR).addArgumentNode(playerArg);
    }
}