package github.api;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMod {
    protected IEventBus bus;

    public BaseMod() {
        this.bus = FMLJavaModLoadingContext.get().getModEventBus();
        this.bus.addListener(this::onCommonSetup);
        this.bus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected abstract void onCommonSetup(@NotNull final FMLCommonSetupEvent event);

    protected abstract void onClientSetup(@NotNull final FMLClientSetupEvent event);
}