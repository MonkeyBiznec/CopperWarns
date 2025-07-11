package github.api.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CommandCtx {
    private final CommandContext<CommandSourceStack> ctx;

    public <T> T get(String name, Class<T> clazz) {
        return this.ctx.getArgument(name, clazz);
    }

    public @NotNull ServerPlayer getPlayer(String name) throws CommandSyntaxException {
        return EntityArgument.getPlayer(this.ctx, name);
    }

    public int getInt(String name) {
        return this.ctx.getArgument(name, Integer.class);
    }

    public double getDouble(String name) {
        return this.ctx.getArgument(name, Double.class);
    }

    public String getString(String name) {
        return this.ctx.getArgument(name, String.class);
    }

    public @NotNull CommandSourceStack source() {
        return this.ctx.getSource();
    }
}