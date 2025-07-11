package github.monkeybiznec.copperwarns.command;

import github.api.command.PermissionLevel;
import github.api.command.builder.CommandNodeBuilder;
import github.monkeybiznec.annotations.AutoRegCommand;
import github.monkeybiznec.copperwarns.MessageUtils;
import github.monkeybiznec.copperwarns.Warn;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@UtilityClass
@AutoRegCommand(name = "warnlist")
public class WarnListCommand {
    public void register(@NotNull CommandNodeBuilder builder) {
        builder.requires(PermissionLevel.ALL).executes(ctx -> {
            ServerPlayer player = ctx.source().getPlayerOrException();
            player.sendSystemMessage(MessageUtils.warnList(Warn.INSTANCE.getWarns(player.getUUID())));
            return 1;
        });
    }
}