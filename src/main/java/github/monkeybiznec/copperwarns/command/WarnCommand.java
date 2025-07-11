package github.monkeybiznec.copperwarns.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import github.api.command.ArgumentNode;
import github.api.command.PermissionLevel;
import github.api.command.builder.CommandNodeBuilder;
import github.monkeybiznec.annotations.AutoRegCommand;
import github.monkeybiznec.copperwarns.*;
import github.monkeybiznec.copperwarns.io.network.packet.PlaySoundPacket;
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
@AutoRegCommand(name = "warn")
public class WarnCommand {
    public void register(@NotNull CommandNodeBuilder builder) {
        ArgumentNode<String> reasonArg = new ArgumentNode<>("reason", StringArgumentType.greedyString()).executes(ctx -> {
            ServerPlayer moderator = ctx.source().getPlayerOrException();
            MinecraftServer server = ctx.source().getServer();
            String targetName = ctx.getString("player");
            String reason = ctx.getString("reason");
            Optional<PlayerInfo> playerInfoOpt = PlayerInfo.findPlayer(server, targetName);
            if (playerInfoOpt.isEmpty()) {
                moderator.sendSystemMessage(MessageUtils.playerNotFound(targetName));
                return 0;
            }
            PlayerInfo playerInfo = playerInfoOpt.get();
            if (playerInfo.mode() == PlayerMode.ONLINE) {
                ServerPlayer target = playerInfo.onlinePlayer();
                Warn.INSTANCE.addWarn(target);
                Warn.INSTANCE.notifyWarnGiven(moderator, target, Warn.INSTANCE.getWarns(target.getUUID()), reason);
            } else {
                UUID offlineUUID = playerInfo.offlineProfile().getId();
                Warn.INSTANCE.addWarn(offlineUUID);
                Utils.broadcastForEveryone(moderator, () -> MessageUtils.warnGivenOffline(moderator, playerInfo.offlineProfile().getName(), reason));
                Warn.INSTANCE.saveWarns(server);
            }
            return 1;
        });
        ArgumentNode<String> playerArg = new ArgumentNode<>("player", StringArgumentType.word()).suggests(Utils.ONLINE_PLAYER_NAMES).addChild(reasonArg);
        builder.requires(PermissionLevel.OPERATOR).addArgumentNode(playerArg);
    }
}