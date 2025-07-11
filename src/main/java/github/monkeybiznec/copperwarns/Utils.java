package github.monkeybiznec.copperwarns;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.experimental.UtilityClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@UtilityClass
public class Utils {
    public final @NotNull SuggestionProvider<CommandSourceStack> ONLINE_PLAYER_NAMES = (ctx, builder) -> {
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    public @NotNull String formatTimestamp(long timeMillis) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(timeMillis));
    }

    public long parseTime(String timeStr) {
        try {
            char last = timeStr.charAt(timeStr.length() - 1);
            long val = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            return switch (last) {
                case 'd' -> TimeUnit.DAYS.toMillis(val);
                case 'h' -> TimeUnit.HOURS.toMillis(val);
                case 'm' -> TimeUnit.MINUTES.toMillis(val);
                case 's' -> TimeUnit.SECONDS.toMillis(val);
                default -> -1L;
            };
        } catch (Exception e) {
            return -1L;
        }
    }

    public @NotNull String formatDurationRu(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" дн. ");
        }
        if (hours > 0) {
            builder.append(hours).append(" ч. ");
        }
        if (minutes > 0) {
            builder.append(minutes).append(" мин. ");
        }
        if (seconds > 0 || builder.isEmpty()) {
            builder.append(seconds).append(" сек.");
        }
        return builder.toString().trim();
    }

    public @NotNull Path getWorldFile(@NotNull MinecraftServer server, @NotNull String fileName) {
        return server.getWorldPath(LevelResource.ROOT).resolve(fileName);
    }

    public void broadcastForEveryone(@NotNull MinecraftServer server, @NotNull Supplier<Component> supplier) {
        server.getPlayerList().broadcastSystemMessage(supplier.get(), false);
    }

    public void broadcastForEveryone(@NotNull ServerPlayer player, @NotNull Supplier<Component> supplier) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            broadcastForEveryone(server, supplier);
        }
    }
}