package github.api.command;

import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@Getter
public enum PermissionLevel {
    ALL(0),
    OPERATOR(2);

    private final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public boolean test(@NotNull CommandSourceStack source) {
        return source.hasPermission(this.level);
    }
}