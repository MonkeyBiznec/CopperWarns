package github.api.command.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@UtilityClass
public class CommandBuilderUtils {
    public <T> @NotNull ArgumentNodeBuilder<T> createArgument(String name, ArgumentType<T> type, @NotNull Consumer<ArgumentNodeBuilder<T>> child) {
        ArgumentNodeBuilder<T> arg = new ArgumentNodeBuilder<>(name, type);
        child.accept(arg);
        return arg;
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> createLiteral(String name, @NotNull Consumer<CommandNodeBuilder> child) {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(name);
        child.accept(new CommandNodeBuilder(literal));
        return literal;
    }
}