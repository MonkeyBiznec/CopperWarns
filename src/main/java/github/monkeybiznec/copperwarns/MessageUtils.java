package github.monkeybiznec.copperwarns;

import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class MessageUtils {
    @Contract("_, _, _ -> new")
    public @NotNull Component warnGiven(@NotNull ServerPlayer moderator, @NotNull ServerPlayer target, @NotNull String reason) {
        return Component.literal("§cВарны §8» §eМодератор §6" + moderator.getName().getString() + " §eналожил варн на игрока §6" + target.getName().getString() + " §eпо причине §6" + reason + "§e!");
    }

    @Contract("_, _, _ -> new")
    public @NotNull Component warnGivenOffline(@NotNull ServerPlayer moderator, @NotNull String offlinePlayerName, @NotNull String reason) {
        return Component.literal("§cВарны §8» §eМодератор §6" + moderator.getName().getString()+ " §eналожил варн на оффлайн игрока §6" + offlinePlayerName + " §eпо причине §6" + reason + "§e!");
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Component warnReceived(int warns) {
        return Component.literal("§cВарны §8» §eВы получили варн! Ваше количество варнов теперь составляет §6" + warns + "§e");
    }

    @Contract("_, _ -> new")
    public @NotNull Component warnsRemoved(@NotNull ServerPlayer target, int amount) {
        return Component.literal("§cВарны §8» §eУбрано §6" + amount + " §eварнов с игрока §6" + target.getName().getString() + "§e");
    }

    @Contract("_, _ -> new")
    public @NotNull Component warnsRemovedOffline(@NotNull String playerName, int amount) {
        return Component.literal("§cВарны §8» §eУбрано §6" + amount + " §e" + declineWarnWord(amount) + " с оффлайн игрока §6" + playerName + "§e");
    }

    @Contract("_, _ -> new")
    public @NotNull Component warnCheck(@NotNull ServerPlayer target, int warns) {
        return Component.literal("§cВарны §8» §eИгрок §6" + target.getName().getString() + " §eимеет §6" + warns + " §e" + declineWarnWord(warns));
    }

    @Contract("_, _ -> new")
    public @NotNull Component warnCheckOffline(String playerName, int warns) {
        return Component.literal("§cВарны §8» §eОффлайн игрок §6" + playerName + " §eимеет §6" + warns + " §e" + declineWarnWord(warns));
    }

    @Contract("_ -> new")
    public @NotNull Component warnList(int warns) {
        return Component.literal("§cВарны §8» §eВы получили §6" + warns + " §e" + declineWarnWord(warns));
    }

    @Contract("_, _ -> new")
    public @NotNull Component banNotification(@NotNull String endTimeFormatted, @NotNull String reason) {
        return Component.literal("Вы забанены до " + endTimeFormatted + "\nПричина: " + reason);
    }

    @Contract("_ -> new")
    public @NotNull Component warnsOnLogin(int warns) {
        return Component.literal("§eУ вас есть §6" + warns + " §e" + declineWarnWord(warns));
    }

    @Contract("_, _, _, _, _ -> new")
    public @NotNull Component banBroadcast(@NotNull ServerPlayer moderator, String targetName, String durationStr, String reason, boolean offline) {
        String targetDesc = offline ? "оффлайн игрока §6" + targetName : "игрока §6" + targetName;
        return Component.literal("§cВарны §8» §eМодератор §6" + moderator.getName().getString() + " §eзабанил " + targetDesc + " §eна §6" + durationStr + " §eпо причине §6" + reason + "§e!");
    }

    public @NotNull Component invalidTimeFormat() {
        return Component.literal("§cНеверный формат времени! Используйте, например: 10m, 2h, 3d");
    }

    @Contract("_ -> new")
    public @NotNull Component playerNotFound(String playerName) {
        return Component.literal("§cИгрок с ником " + playerName + " не найден!");
    }

    public @NotNull String declineWarnWord(int warnCount) {
        int lastNumber = warnCount % 10;
        int lastTwoNumbers = warnCount % 100;
        if (lastNumber == 1 && lastTwoNumbers != 11) {
            return "варн";
        }
        if (lastNumber >= 2 && lastNumber <= 4 && !(lastTwoNumbers >= 12 && lastTwoNumbers <= 14)) {
            return "варна";
        }
        return "варнов";
    }
}