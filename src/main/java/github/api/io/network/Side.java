package github.api.io.network;

import net.minecraftforge.fml.LogicalSide;

public enum Side {
    CLIENT {
        @Override
        public boolean matches(LogicalSide side) {
            return side.isClient();
        }
    },
    SERVER {
        @Override
        public boolean matches(LogicalSide side) {
            return side.isServer();
        }
    };

    public abstract boolean matches(LogicalSide side);
}