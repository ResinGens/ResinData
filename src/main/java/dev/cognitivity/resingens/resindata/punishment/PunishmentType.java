package dev.cognitivity.resingens.resindata.punishment;

import lombok.Getter;

import java.util.Arrays;

@Getter
@SuppressWarnings("unused")
public enum PunishmentType {
    
    PERMANENT_BAN(6, "pban", "permanentban", "permanent_ban", "perm_ban", "permban"),
    TEMPORARY_BAN(5, "tban", "temporaryban", "temporary_ban", "temp_ban", "tempban"),
    PERMANENT_MUTE(4, "pmute", "permanentmute", "permanent_mute", "perm_mute", "permmute"),
    TEMPORARY_MUTE(3, "tmute", "temporarymute", "temporary_mute", "temp_mute", "tempmute"),
    KICK(2, "kick"),
    WARN(1, "warn"),
    NONE(9);
    
    private final int id;
    private final String[] names;
    
    PunishmentType(int id, String... names) {
        this.id = id;
        this.names = names;
    }
    
    public boolean isPermanent() { return this == PERMANENT_BAN || this == PERMANENT_MUTE; }
    public boolean isTemporary() { return this == TEMPORARY_BAN || this == TEMPORARY_MUTE; }
    public boolean isInstant() { return this == WARN || this == KICK; }
    public boolean isMute() { return this == TEMPORARY_MUTE || this == PERMANENT_MUTE; }
    public boolean isBan() { return this == TEMPORARY_BAN || this == PERMANENT_BAN; }
    /**
     * @return whether the player is kicked when the punishment active.
     */
    public boolean isKick() { return this == KICK || this == TEMPORARY_BAN || this == PERMANENT_BAN; }
    /**
     * @return whether the player is notified when the punishment active.
     */
    public boolean isNotification() { return this == WARN || this == TEMPORARY_MUTE || this == PERMANENT_MUTE; }
    
    public static PunishmentType fromPID(int pID) {
        return Arrays.stream(values()).filter(type -> type.getId() == Integer.parseInt(Integer.toString(Math.abs(pID)).substring(0, 1))).findFirst().orElse(NONE);
    }
    public static PunishmentType fromString(String name) {
        return Arrays.stream(values()).filter(type -> Arrays.stream(type.getNames()).anyMatch(typeName -> typeName.equalsIgnoreCase(name))).findFirst().orElse(NONE);
    }
}