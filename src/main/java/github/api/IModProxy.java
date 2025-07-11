package github.api;

import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface IModProxy {
    void commonInit();

    void clientInit();

    static <T extends IModProxy> T create(@NotNull Supplier<T> client, @NotNull Supplier<T> server) {
        return DistExecutor.safeRunForDist(() -> client::get, () -> server::get);
    }
}