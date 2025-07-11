package github.api.io.network;



import github.api.common.ReflectionUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class NetworkCreator {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Getter
    private final SimpleChannel channel;
    private int id = 0;
    private final Set<Class<?>> registeredPackets = new HashSet<>();

    private NetworkCreator(ResourceLocation channelName, String protocolVersion) {
        this.channel = NetworkRegistry.ChannelBuilder.named(channelName)
                .clientAcceptedVersions(protocolVersion::equals)
                .serverAcceptedVersions(protocolVersion::equals)
                .networkProtocolVersion(() -> protocolVersion)
                .simpleChannel();
        this.logger.info(String.format("Initialized channel: %s, version: %s",channelName, protocolVersion));
    }

    public static NetworkCreator create(String modId, int version) {
        return create(modId, "main_channel", String.valueOf(version));
    }

    public static NetworkCreator create(String modId, String channelName, String version) {
        return new NetworkCreator(new ResourceLocation(modId, channelName), version);
    }

    public static Builder builder(ResourceLocation channelName) {
        return new Builder(channelName);
    }

    private boolean getSide(NetworkEvent.Context context, @NotNull Side side) {
        return side.matches(context.getDirection().getReceptionSide());
    }

    private <T extends AbstractNetworkPacket<T>> T decodePacket(Class<T> clazz, FriendlyByteBuf buf) {
        T instance = ReflectionUtils.instantiate(clazz);
        instance.decode(buf);
        return instance;
    }

    private <T extends AbstractNetworkPacket<T>> void handlePacket(T msg, NetworkEvent.Context context, Side side) {
        if (this.getSide(context, side)) {
            msg.handle(() -> context);
        }
    }

    public <T extends AbstractNetworkPacket<T>> NetworkCreator regPacket(@NotNull Class<T> packetClazz) {
        if (!this.registeredPackets.add(packetClazz)) {
            throw new IllegalStateException("Packet: " + packetClazz.getName() + " already registered!");
        }
        PacketSide annotation = ReflectionUtils.getAnnotation(packetClazz, PacketSide.class);
        Side side = annotation.side();
        this.channel.registerMessage(this.id++, packetClazz, AbstractNetworkPacket::encode, buf -> {
            return this.decodePacket(packetClazz, buf);
        }, (msg, ctx) -> {
            this.handlePacket(msg, ctx.get(), side);
        });
        this.logger.info("Registered packet: " + packetClazz.getSimpleName() + " with ID " + (id - 1));
        return this;
    }

    public <P> NetworkCreator sendToServer(P packet) {
        this.channel.sendToServer(packet);
        return this;
    }

    public <P> NetworkCreator sendToClient(@Nullable Player player, P packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
        return this;
    }

    public <P> NetworkCreator sendToPlayersInLevel(@NotNull ServerLevel level, @NotNull P packet) {
        this.channel.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
        return this;
    }

    public <P> NetworkCreator sendToTracking(ServerPlayer trackingTarget, P packet) {
        this.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> trackingTarget), packet);
        return this;
    }

    public <P> NetworkCreator sendToTracking(BlockPos pos, ServerLevel level, P packet) {
        this.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
        return this;
    }

    public <P> NetworkCreator sendToAll(P packet) {
        this.channel.send(PacketDistributor.ALL.noArg(), packet);
        return this;
    }

    public static class Builder {
        private final ResourceLocation channelName;
        private String version = "1";

        public Builder(ResourceLocation channelName) {
            this.channelName = channelName;
        }

        public Builder version(int version) {
            this.version = String.valueOf(version);
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public NetworkCreator build() {
            return new NetworkCreator(this.channelName, this.version);
        }
    }
}