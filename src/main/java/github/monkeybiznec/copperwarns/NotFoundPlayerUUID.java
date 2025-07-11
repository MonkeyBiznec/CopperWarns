package github.monkeybiznec.copperwarns;

public class NotFoundPlayerUUID extends IllegalArgumentException {
    public NotFoundPlayerUUID() {
        super("Player UUID not found");
    }

    public NotFoundPlayerUUID(String message) {
        super(message);
    }
}