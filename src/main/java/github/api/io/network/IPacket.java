package github.api.io.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface IPacket {
    void encode(@NotNull FriendlyByteBuf buf);

    void decode(@NotNull FriendlyByteBuf buf);

    void handle(@NotNull Supplier<NetworkEvent.Context> context);
}