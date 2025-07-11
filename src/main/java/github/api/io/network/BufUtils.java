package github.api.io.network;

import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@UtilityClass
public class BufUtils {
    public <T> void writeList(FriendlyByteBuf buf, List<T> list, BiConsumer<FriendlyByteBuf, T> writer) {
        buf.writeVarInt(list.size());
        for (T element : list) {
            writer.accept(buf, element);
        }
    }

    public <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
        int size = buf.readVarInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }

    public void writeVec3(@NotNull FriendlyByteBuf buf, @NotNull Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    @NotNull
    public Vec3 readVec3(@NotNull FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vec3(x, y, z);
    }

    public void writeBlockPos(@NotNull FriendlyByteBuf buf, @NotNull BlockPos pos) {
        buf.writeLong(pos.asLong());
    }

    @NotNull
    public BlockPos readBlockPos(@NotNull FriendlyByteBuf buf) {
        return BlockPos.of(buf.readLong());
    }
}