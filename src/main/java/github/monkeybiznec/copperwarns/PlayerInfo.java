package github.monkeybiznec.copperwarns;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PlayerInfo(PlayerMode mode, @NotNull ServerPlayer onlinePlayer, GameProfile offlineProfile) {
    public static @NotNull Optional<PlayerInfo> findPlayer(@NotNull MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return Optional.of(new PlayerInfo(PlayerMode.ONLINE, online, null));
        }
        GameProfileCache cache = server.getProfileCache();
        if (cache != null) {
            Optional<GameProfile> profile = cache.get(name);
            return profile.map(gameProfile -> new PlayerInfo(PlayerMode.OFFLINE, null, gameProfile));
        }
        return Optional.empty();
    }
}