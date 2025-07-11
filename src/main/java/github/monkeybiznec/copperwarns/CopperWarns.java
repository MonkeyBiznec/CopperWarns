package github.monkeybiznec.copperwarns;

import github.api.BaseMod;
import github.api.IModProxy;
import github.api.io.ModResources;
import github.api.io.network.NetworkCreator;
import github.generated.PacketAutoRegistry;
import github.monkeybiznec.copperwarns.client.CWClientProxy;
import github.monkeybiznec.copperwarns.server.CWServerProxy;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;

/**
 * каператив варны и баны мод
 */
@Slf4j
@Mod(CopperWarns.ID)
public class CopperWarns extends BaseMod {
    public static final String ID = "warns";
    public static final IModProxy PROXY = IModProxy.create(CWClientProxy::new, CWServerProxy::new);
    public static final ModResources RESOURCE = new ModResources(ID);
    public static final NetworkCreator NETWORK = NetworkCreator.create(ID, 1);

    @Override
    protected void onCommonSetup(@NotNull FMLCommonSetupEvent event) {
        PROXY.commonInit();
        PacketAutoRegistry.registerPackets(NETWORK);
    }

    @Override
    protected void onClientSetup(@NotNull FMLClientSetupEvent event) {
        event.enqueueWork(PROXY::clientInit);
    }

    /**
     * просто прямо засунуть в сетевой пакет не получится, так что эта залупа, которая проигрывает звук - перемещена в статик метод в основном классе
     * я ебал в рот это
     */
    @OnlyIn(Dist.CLIENT)
    public static void playSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
    }

    /**
     * пусигриффер
     */
    @SubscribeEvent
    public void onServerStarting(@NotNull ServerStartingEvent event) {
        log.info("pusidon");
    }
}