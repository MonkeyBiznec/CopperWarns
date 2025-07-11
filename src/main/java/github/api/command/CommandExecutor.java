package github.api.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandExecutor {
    int run(@NotNull CommandCtx ctx) throws Exception;

    @SuppressWarnings("CallToPrintStackTrace")
    @Contract(pure = true)
    static int execute(CommandContext<CommandSourceStack> ctx, @NotNull CommandExecutor executor) {
        try {
            return executor.run(new CommandCtx(ctx));
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error executing command: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}