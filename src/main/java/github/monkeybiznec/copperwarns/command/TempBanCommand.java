package github.monkeybiznec.copperwarns.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import github.api.command.ArgumentNode;
import github.api.command.PermissionLevel;
import github.api.command.builder.CommandNodeBuilder;
import github.monkeybiznec.annotations.AutoRegCommand;
import github.monkeybiznec.copperwarns.*;
import github.monkeybiznec.copperwarns.io.network.packet.PlaySoundPacket;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@UtilityClass
@AutoRegCommand(name = "tempban")
public class TempBanCommand {
    public void register(@NotNull CommandNodeBuilder builder) {
        ArgumentNode<String> reasonArg = new ArgumentNode<>("reason", StringArgumentType.greedyString()).executes(ctx -> {
            ServerPlayer moderator = ctx.source().getPlayerOrException();
            MinecraftServer server = ctx.source().getServer();
            String targetName = ctx.getString("player");
            String timeStr = ctx.getString("time");
            String reason = ctx.getString("reason");
            long durationMs = Utils.parseTime(timeStr);
            if (durationMs <= 0) {
                moderator.sendSystemMessage(MessageUtils.invalidTimeFormat());
                return 0;
            }
            Optional<PlayerInfo> playerInfo = PlayerInfo.findPlayer(server, targetName);
            if (playerInfo.isEmpty()) {
                moderator.sendSystemMessage(MessageUtils.playerNotFound(targetName));
                return 0;
            }
            PlayerInfo info = playerInfo.get();
            if (info.mode() == PlayerMode.ONLINE) {
                banOnlinePlayer(moderator, info.onlinePlayer(), reason, durationMs, Utils.formatDurationRu(durationMs));
            } else {
                banOfflinePlayer(server, moderator, info.offlineProfile(), reason, durationMs, Utils.formatDurationRu(durationMs));
            }
            return 1;
        });
        ArgumentNode<String> timeArg = new ArgumentNode<>("time", StringArgumentType.word()).addChild(reasonArg);
        ArgumentNode<String> playerArg = new ArgumentNode<>("player", StringArgumentType.word()).suggests(Utils.ONLINE_PLAYER_NAMES).addChild(timeArg);
        builder.requires(PermissionLevel.OPERATOR).addArgumentNode(playerArg);
    }

    private void banOnlinePlayer(@NotNull ServerPlayer moderator, @NotNull ServerPlayer targetPlayer, String reason, long durationMs, String timeStr) {
        Warn.INSTANCE.addTempBan(targetPlayer, reason, durationMs);
        CopperWarns.NETWORK.sendToClient(targetPlayer, PlaySoundPacket.INSTANCE);
        broadcastBanMessage(moderator, targetPlayer.getName().getString(), timeStr, reason, false);
    }

    private void banOfflinePlayer(@NotNull MinecraftServer server, @NotNull ServerPlayer moderator, @NotNull GameProfile profile, String reason, long durationMs, String timeStr) {
        Warn.INSTANCE.getBanMap().put(profile.getId(), new BanData(reason, System.currentTimeMillis() + durationMs));
        Warn.INSTANCE.saveBans(server);
        broadcastBanMessage(moderator, profile.getName(), timeStr, reason, true);
    }

    private void broadcastBanMessage(@NotNull ServerPlayer moderator, String targetName, String timeStr, String reason, boolean offline) {
        Utils.broadcastForEveryone(moderator, () -> {
            return MessageUtils.banBroadcast(moderator, targetName, timeStr, reason, offline);
        });
    }
}