package github.api.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ArgumentNode<T> {
    private final String name;
    private final ArgumentType<T> type;
    private final List<ArgumentNode<?>> children = new ArrayList<>();
    private CommandExecutor executor;
    private @Nullable SuggestionProvider<CommandSourceStack> suggestions;

    public ArgumentNode(String name, @NotNull ArgumentType<T> type) {
        this.name = name;
        this.type = type;
    }

    public @NotNull ArgumentNode<T> addChild(@NotNull ArgumentNode<?> child) {
        this.children.add(child);
        return this;
    }

    public @NotNull ArgumentNode<T> executes(@NotNull CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public @NotNull ArgumentNode<T> suggests(@NotNull SuggestionProvider<CommandSourceStack> provider) {
        this.suggestions = provider;
        return this;
    }
}