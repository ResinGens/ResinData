package dev.cognitivity.resingens.resindata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public enum Messages {
    SAVING_DATA("<gray>Saving data..."),
    SAVED_DATA("<gray>Data saved in %s ms."),

    ADMIN_SAVING_DATA("<aqua>Saving data for %s players..."),
    ADMIN_SAVED_DATA("<aqua>Saved data for %s players in %s ms. <gray>(avg %s)"),

    LOADING_DATA("<gray>Loading data..."),
    LOADED_DATA("<gray>Loaded data in %s ms."),
    ;


    private final String message;

    Messages(@NotNull String message) {
        this.message = message;
    }

    public @NotNull Component getMessage() {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public @NotNull Component getMessage(Object... args) {
        return MiniMessage.miniMessage().deserialize(String.format(message, args));
    }
}