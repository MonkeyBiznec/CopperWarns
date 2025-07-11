package github.monkeybiznec.copperwarns.io.network.packet;

import com.google.common.reflect.Reflection;
import github.api.io.network.AbstractNetworkPacket;
import github.api.io.network.PacketSide;
import github.api.io.network.Side;
import github.monkeybiznec.annotations.AutoRegPacket;
import github.monkeybiznec.copperwarns.CopperWarns;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;

import java.util.function.Supplier;

@NoArgsConstructor
@AutoRegPacket
@PacketSide(side = Side.CLIENT)
public class PlaySoundPacket extends AbstractNetworkPacket<PlaySoundPacket> {
    public static final PlaySoundPacket INSTANCE = new PlaySoundPacket();

    @Override
    protected void read(@NotNull FriendlyByteBuf buf) {

    }

    @Override
    protected void write(@NotNull FriendlyByteBuf buf) {

    }

    @Override
    protected void execute(@NotNull Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CopperWarns::playSound);
    }
}