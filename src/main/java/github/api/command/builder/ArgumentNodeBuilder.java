package github.api.command.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.api.command.CommandExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class ArgumentNodeBuilder<T> {
    private final RequiredArgumentBuilder<CommandSourceStack, T> node;

    public ArgumentNodeBuilder(String name, @NotNull ArgumentType<T> type) {
        this.node = Commands.argument(name, type);
    }

    public <R> @NotNull ArgumentNodeBuilder<T> createArgument(String name, ArgumentType<R> type, @NotNull Consumer<ArgumentNodeBuilder<R>> child) {
        this.node.then(CommandBuilderUtils.createArgument(name, type, child).getNode());
        return this;
    }

    public @NotNull ArgumentNodeBuilder<T> literal(String name, @NotNull Consumer<CommandNodeBuilder> child) {
        this.node.then(CommandBuilderUtils.createLiteral(name, child));
        return this;
    }

    public @NotNull ArgumentNodeBuilder<T> suggests(@NotNull SuggestionProvider<CommandSourceStack> provider) {
        this.node.suggests(provider);
        return this;
    }

    public @NotNull ArgumentNodeBuilder<T> executes(@NotNull CommandExecutor executor) {
        this.node.executes(ctx -> {
            return CommandExecutor.execute(ctx, executor);
        });
        return this;
    }
}