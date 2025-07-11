package github.api.io.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractNetworkPacket<T extends AbstractNetworkPacket<T>> implements IPacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        this.write(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.read(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (this.canExecute(ctx)) {
            ctx.enqueueWork(() -> this.execute(context));
        }
        ctx.setPacketHandled(true);
    }

    protected abstract void read(@NotNull FriendlyByteBuf buf);

    protected abstract void write(@NotNull FriendlyByteBuf buf);

    protected abstract void execute(@NotNull Supplier<NetworkEvent.Context> context);

    protected boolean canExecute(NetworkEvent.Context context) {
        return true;
    }
}