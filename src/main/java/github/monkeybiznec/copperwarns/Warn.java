package github.monkeybiznec.copperwarns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.monkeybiznec.copperwarns.io.network.packet.PlaySoundPacket;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * почти что вся дрисня мода находится прямо тут
 */
@Slf4j
public class Warn {
    public static final Warn INSTANCE = new Warn();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private final Map<UUID, Integer> warnList = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, BanData> banMap = new ConcurrentHashMap<>();

    private @NotNull Path getWarnsFile(@NotNull MinecraftServer server) {
        return Utils.getWorldFile(server, "warns.json");
    }

    private @NotNull Path getBansFile(@NotNull MinecraftServer server) {
        return Utils.getWorldFile(server, "bans.json");
    }

    @Contract("null -> fail")
    private @NotNull UUID requireUUID(@Nullable Player player) {
        if (player == null) {
            throw new NotFoundPlayerUUID("Player cannot be null");
        }
        return player.getUUID();
    }

    public int getWarns(@Nullable UUID playerUUID) {
        if (playerUUID == null) {
            throw new NotFoundPlayerUUID();
        }
        return this.warnList.getOrDefault(playerUUID, 0);
    }

    public @NotNull Warn addWarn(@Nullable Player player) {
        UUID uuid = this.requireUUID(player);
        int warns = this.getWarns(uuid);
        this.warnList.put(uuid, warns + 1);
        log.info("Player has been warned: {}, current number of warnings {}: ", player.getName(), warns);
        if (player.getServer() != null) {
            this.saveWarns(player.getServer());
        }
        return this;
    }

    public @NotNull Warn addWarn(@NotNull UUID playerUUID) {
        int warns = this.getWarns(playerUUID);
        this.warnList.put(playerUUID, warns + 1);
        log.info("Offline player warned: {}, current warns: {}", playerUUID, warns + 1);
        return this;
    }

    public @NotNull Warn removeWarns(@Nullable Player player, int amount) {
        UUID uuid = this.requireUUID(player);
        this.warnList.computeIfPresent(uuid, (key, prev) -> {
            int remained = prev - amount;
            if (remained <= 0) {
                return null;
            }
            return remained;
        });
        log.info("Removed {} warnings from player: {}", amount, player.getName().getString());
        if (player.getServer() != null) {
            this.saveWarns(player.getServer());
        }
        return this;
    }

    public @NotNull Warn removeWarns(@Nullable UUID playerUUID, int amount) {
        if (playerUUID == null) {
            throw new NotFoundPlayerUUID();
        }
        this.warnList.computeIfPresent(playerUUID, (key, prev) -> {
            int remained = prev - amount;
            return remained <= 0 ? null : remained;
        });
        return this;
    }

    @Contract("null->false")
    public boolean hasWarns(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return this.getWarns(player.getUUID()) > 0;
    }

    public void addTempBan(@Nullable Player player, @NotNull String reason, long durationMs) {
        UUID uuid = this.requireUUID(player);
        long endTime = System.currentTimeMillis() + durationMs;
        BanData banData = new BanData(reason, endTime);
        this.banMap.put(uuid, banData);
        log.info("Player: {} has been temporarily banned until {}, reason: {}", player.getName(), endTime, reason);
        if (player.getServer() != null) {
            this.saveBans(player.getServer());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.disconnect(Component.literal("Вы забанены до " + Utils.formatTimestamp(endTime) + "\nПричина: " + reason));
        }
    }

    public void removeTempBan(@Nullable Player player) {
        UUID uuid = this.requireUUID(player);
        if (this.banMap.remove(uuid) != null) {
            log.info("Remove temporary ban from player: {}", player.getName());
            if (player.getServer() != null) {
                this.saveBans(player.getServer());
            }
        }
    }

    public boolean isBanned(@Nullable UUID playerUUID) {
        BanData ban = this.banMap.get(playerUUID);
        if (ban == null) {
            return false;
        }
        if (System.currentTimeMillis() > ban.endTime()) {
            this.banMap.remove(playerUUID);
            return false;
        }
        return true;
    }

    public @Nullable BanData getBanData(@Nullable UUID playerUUID) {
        if (playerUUID == null) {
            throw new NotFoundPlayerUUID();
        }
        return this.banMap.get(playerUUID);
    }

    @SneakyThrows
    private void saveJson(@NotNull Path file, @NotNull JsonObject root, @NotNull String successLogMessage) {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            GSON.toJson(root, writer);
        }
        log.info(successLogMessage);
    }

    @SneakyThrows
    private JsonObject loadJson(@NotNull Path file) {
        if (!Files.exists(file)) {
            log.info("{} file not found, creating a new one", file.getFileName());
            return new JsonObject();
        }
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }

    public void saveWarns(@NotNull MinecraftServer server) {
        Path file = this.getWarnsFile(server);
        JsonObject root = new JsonObject();
        for (Map.Entry<UUID, Integer> entry : this.warnList.entrySet()) {
            root.addProperty(entry.getKey().toString(), entry.getValue());
        }
        this.saveJson(file, root, "File warns.json saved successfully");
    }

    public void saveBans(@NotNull MinecraftServer server) {
        Path file = this.getBansFile(server);
        JsonObject root = new JsonObject();
        for (Map.Entry<UUID, BanData> entry : this.banMap.entrySet()) {
            JsonObject banJson = new JsonObject();
            banJson.addProperty("reason", entry.getValue().reason());
            banJson.addProperty("endTime", entry.getValue().endTime());
            root.add(entry.getKey().toString(), banJson);
        }
        this.saveJson(file, root, "File bans.json saved successfully");
    }

    public void loadWarns(@NotNull MinecraftServer server) {
        Path file = this.getWarnsFile(server);
        this.warnList.clear();
        for (Map.Entry<String, JsonElement> entry : this.loadJson(file).entrySet()) {
            this.warnList.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
        }
        log.info("The warns.json file has been successfully loaded");
    }

    public void loadBans(@NotNull MinecraftServer server) {
        Path file = this.getBansFile(server);
        this.banMap.clear();
        for (Map.Entry<String, JsonElement> entry : this.loadJson(file).entrySet()) {
            JsonObject banJson = entry.getValue().getAsJsonObject();
            String reason = banJson.get("reason").getAsString();
            long endTime = banJson.get("endTime").getAsLong();
            this.banMap.put(UUID.fromString(entry.getKey()), new BanData(reason, endTime));
        }
        log.info("The bans.json file has been successfully loaded");
    }

    public void notifyWarnGiven(@NotNull ServerPlayer moderator, @NotNull ServerPlayer target, int warns, @NotNull String reason) {
        Utils.broadcastForEveryone(moderator, () -> MessageUtils.warnGiven(moderator, target, reason));
        CopperWarns.NETWORK.sendToClient(target, PlaySoundPacket.INSTANCE);
        target.sendSystemMessage(MessageUtils.warnReceived(warns));
    }

    @UtilityClass
    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = CopperWarns.ID)
    private class Events {
        @SubscribeEvent
        public void onServerStarting(@NotNull ServerStartingEvent event) {
            MinecraftServer server = event.getServer();
            Warn.INSTANCE.loadWarns(server);
            Warn.INSTANCE.loadBans(server);
        }

        @SubscribeEvent
        public void onServerStopping(@NotNull ServerStoppingEvent event) {
            MinecraftServer server = event.getServer();
            Warn.INSTANCE.saveWarns(server);
            Warn.INSTANCE.saveBans(server);
        }

        @SubscribeEvent
        public void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                UUID uuid = player.getUUID();
                if (Warn.INSTANCE.isBanned(uuid)) {
                    BanData ban = Warn.INSTANCE.getBanData(uuid);
                    if (ban != null) {
                        player.connection.disconnect(MessageUtils.banNotification(Utils.formatTimestamp(ban.endTime()), ban.reason()));
                    }
                }
                if (Warn.INSTANCE.hasWarns(player)) {
                    player.sendSystemMessage(MessageUtils.warnsOnLogin(Warn.INSTANCE.getWarns(uuid)));
                }
            }
        }
    }
}