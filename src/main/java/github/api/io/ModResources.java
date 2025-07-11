package github.api.io;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ModResources {
    public String modId;

    public ModResources(String modId) {
        this.modId = modId;
    }

    @Contract("_ -> new")
    public @NotNull ResourceLocation modLoc(String path){
        return new ResourceLocation(this.modId, path);
    }

    @Contract("_ -> new")
    public @NotNull ResourceLocation mc(String path){
        return new ResourceLocation(path);
    }

    @Contract("_ -> new")
    public @NotNull ResourceLocation png(String path){
        return this.modLoc("textures/" + path + ".png");
    }

    @Contract("_ -> new")
    public @NotNull ResourceLocation entity(String path) {
        return this.png("entity/" + path);
    }

    @Override
    public String toString() {
        return "ModResources{" + "modId='" + this.modId + '\'' + '}';
    }
}