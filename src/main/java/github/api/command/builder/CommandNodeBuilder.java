package github.api.command.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.api.command.ArgumentNode;
import github.api.command.CommandCtx;
import github.api.command.CommandExecutor;
import github.api.command.PermissionLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class CommandNodeBuilder {
    public final LiteralArgumentBuilder<CommandSourceStack> root;

    public CommandNodeBuilder(String name) {
        this.root = Commands.literal(name);
    }

    public @NotNull CommandNodeBuilder requires(@NotNull PermissionLevel level) {
        this.root.requires(stack -> stack.hasPermission(level.getLevel()));
        return this;
    }

    public <T> @NotNull CommandNodeBuilder createArgument(String name, ArgumentType<T> type, @NotNull Consumer<ArgumentNodeBuilder<T>> child) {
        this.root.then(CommandBuilderUtils.createArgument(name, type, child).getNode());
        return this;
    }

    public @NotNull CommandNodeBuilder playerArgument(String name, Consumer<ArgumentNodeBuilder<EntitySelector>> child) {
        return this.createArgument(name, EntityArgument.player(), child);
    }

    public @NotNull CommandNodeBuilder stringArgument(String name, Consumer<ArgumentNodeBuilder<String>> child) {
        return this.createArgument(name, StringArgumentType.greedyString(), child);
    }

    public @NotNull CommandNodeBuilder intArgument(String name, Consumer<ArgumentNodeBuilder<Integer>> child) {
        return this.createArgument(name, IntegerArgumentType.integer(), child);
    }

    public @NotNull CommandNodeBuilder literal(String name, @NotNull Consumer<CommandNodeBuilder> child) {
        this.root.then(CommandBuilderUtils.createLiteral(name, child));
        return this;
    }

    public @NotNull CommandNodeBuilder executes(@NotNull CommandExecutor executor) {
        this.root.executes(ctx -> {
            return CommandExecutor.execute(ctx, executor);
        });
        return this;
    }

    public @NotNull <T> CommandNodeBuilder addArgumentNode(@NotNull ArgumentNode<T> node) {
        this.root.then(this.buildNode(node));
        return this;
    }


    public <T> @NotNull ArgumentBuilder<CommandSourceStack, ?> buildNode(@NotNull ArgumentNode<T> node) {
        RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(node.getName(), node.getType());
        if (node.getSuggestions() != null) {
            builder.suggests(node.getSuggestions());
        }
        for (ArgumentNode<?> child : node.getChildren()) {
            builder.then(this.buildNode(child));
        }
        if (node.getExecutor() != null) {
            builder.executes(ctx -> {
                try {
                    return node.getExecutor().run(new CommandCtx(ctx));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return builder;
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> build() {
        return this.root;
    }
}