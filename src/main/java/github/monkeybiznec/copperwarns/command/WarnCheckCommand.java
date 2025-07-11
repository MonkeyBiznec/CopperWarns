package github.monkeybiznec.copperwarns.command;

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
@AutoRegCommand(name = "warncheck")
public class WarnCheckCommand {
    public void register(@NotNull CommandNodeBuilder builder) {
        ArgumentNode<String> playerArg = new ArgumentNode<>("player", StringArgumentType.word()).suggests(Utils.ONLINE_PLAYER_NAMES).executes(ctx -> {
            ServerPlayer moderator = ctx.source().getPlayerOrException();
            MinecraftServer server = ctx.source().getServer();
            String targetName = ctx.getString("player");
            Optional<PlayerInfo> playerInfoOpt = PlayerInfo.findPlayer(server, targetName);
            if (playerInfoOpt.isEmpty()) {
                moderator.sendSystemMessage(MessageUtils.playerNotFound(targetName));
                return 0;
            }
            PlayerInfo playerInfo = playerInfoOpt.get();
            int warns;
            if (playerInfo.mode() == PlayerMode.ONLINE) {
                ServerPlayer target = playerInfo.onlinePlayer();
                warns = Warn.INSTANCE.getWarns(target.getUUID());
                moderator.sendSystemMessage(MessageUtils.warnCheck(target, warns));
            } else {
                warns = Warn.INSTANCE.getWarns(playerInfo.offlineProfile().getId());
                moderator.sendSystemMessage(MessageUtils.warnCheckOffline(playerInfo.offlineProfile().getName(), warns));
            }
            return 1;
        });
        builder.requires(PermissionLevel.OPERATOR).addArgumentNode(playerArg);
    }
}