package dev.cognitivity.resingens.resindata.punishment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.cognitivity.resingens.resindata.DataUtils;
import dev.cognitivity.resingens.resindata.Messages;
import dev.cognitivity.resingens.resindata.ResinData;
import dev.cognitivity.resingens.resindata.data.PlayerData;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Punishment {
    private final UUID playerUUID;
    private final UUID staffUUID;
    
    private final String playerName;
    private final String staffName;
    
    private final @Nullable OfflinePlayer player;
    private final @Nullable OfflinePlayer staff;
    
    private final String reason;
    private final PunishmentType punishmentType;
    private final long start;
    private final long duration;
    private final long end;
    private final int pID;
    private boolean active;
    
    public Punishment(UUID playerUUID, UUID staffUUID, String reason, long start, long duration, int pID, boolean active) {
        this.playerUUID = playerUUID;
        this.staffUUID = staffUUID;
        
        player = ResinData.getInstance().getServer().getOfflinePlayer(playerUUID);
        staff = ResinData.getInstance().getServer().getOfflinePlayer(staffUUID);
        playerName = player.getName() == null ? "UNKNOWN" : player.getName();
        staffName = staff.getName() == null ? staffUUID.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                ? "CONSOLE" : "UNKNOWN" : staff.getName();
        this.reason = reason;
        this.start = start;
        this.duration = duration;
        this.end = start + duration;
        this.pID = pID;
        this.punishmentType = PunishmentType.fromPID(pID);
        this.active = active;
    }
    
    @SuppressWarnings("unused")
    public static Punishment generate(UUID playerUUID, UUID staffUUID, PunishmentType punishmentType, String reason, long start, long duration) {
        return new Punishment(playerUUID, staffUUID, reason, start, duration, Integer.parseInt(punishmentType.getId() +
                String.valueOf(ResinData.getInstance().getPunishmentManager().getJsonArray().size() + 1)), false);
    }
    
    public static Punishment of(JsonObject jsonObject) {
        String playerUUID = jsonObject.get("target").getAsString();
        String staffUUID = jsonObject.get("staff").getAsString();
        String reason = jsonObject.get("reason").getAsString();
        long start = jsonObject.get("start").getAsLong();
        long duration = jsonObject.get("duration").getAsLong();
        int pID = jsonObject.get("id").getAsInt();
        boolean active = jsonObject.get("active").getAsBoolean();
        return new Punishment(UUID.fromString(playerUUID), UUID.fromString(staffUUID), reason, start, duration, pID, active);
    }
    
    @Nullable public static Punishment of(int id) {
        Punishment punishment = ResinData.getInstance().getPunishmentManager().getPunishments().stream().filter(punishments -> punishments.getPID() == id).findFirst().orElse(null);
        if(punishment != null) {
            return punishment;
        }
        JsonObject jsonObject = ResinData.getInstance().getPunishmentManager().getJsonArray().asList()
                .stream().map(JsonElement::getAsJsonObject).filter(object -> object.getAsJsonObject().get("id").getAsInt() == id).findFirst().orElse(null);
        if(jsonObject != null) {
            return of(jsonObject);
        } return null;
    }
    
    public JsonObject getAsJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id", pID);
        object.addProperty("target", String.valueOf(playerUUID));
        object.addProperty("staff", String.valueOf(staffUUID));
        object.addProperty("reason", reason);
        object.addProperty("start", start);
        object.addProperty("duration", duration);
        object.addProperty("active", active);
        return object;
    }
    
    @SuppressWarnings("unused")
    public void add() {
        ResinData.getInstance().getPunishmentManager().addPunishment(this);
        PlayerData data = ResinData.getInstance().getData(player);
        data.getPunishments().add(this);
        data.saveData();
        setActive(true);
        if(player != null && player.isOnline() && player.getPlayer() != null) {
            if (punishmentType.isNotification()) {
                player.getPlayer().sendMessage(getPunishmentMessage());
            }
            if (punishmentType.isKick()) {
                player.getPlayer().kick(getPunishmentMessage());
            }
        }
        sendPunishmentWarning();
    }
    
    @SuppressWarnings("unused")
    public void remove(@Nullable String staff) {
        PlayerData data = ResinData.getInstance().getData(player);
        data.saveData();
        setActive(false);
        String length = DataUtils.timeLength(this.duration);
        String startDate = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
        String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
        String until = DataUtils.timeUntil(end);
        if (staff != null) {
            switch (punishmentType) {
                case PERMANENT_BAN -> alert(Messages.PUNISHMENT_REMOVED_STAFF.getMessage(
                        playerName, "unbanned", staff, "banned", staffName, reason, "Permanent", "Never"));
                case TEMPORARY_BAN -> alert(Messages.PUNISHMENT_REMOVED_STAFF.getMessage(
                        playerName, "unbanned", staff, "banned", staffName, reason, length + " (" + until + " remaining", ends));
                case PERMANENT_MUTE -> alert(Messages.PUNISHMENT_REMOVED_STAFF.getMessage(
                        playerName, "unmuted", staff, "muted", staffName, reason, "Permanent", "Never"));
                case TEMPORARY_MUTE -> alert(Messages.PUNISHMENT_REMOVED_STAFF.getMessage(
                        playerName, "unmuted", staff, "muted", staffName, reason, length + " (" + until + " remaining", ends));
            }
        }
        if(player != null && player.getPlayer() != null) {
            String type = punishmentType.isBan() ? "ban" : punishmentType.isMute() ? "mute" : punishmentType.isKick() ? "kick" : "warn";
            player.getPlayer().sendMessage(Messages.PUNISHMENT_REMOVED_PLAYER.getMessage(type, reason, startDate, length, pID));
        }
    }
    
    @SuppressWarnings("unused")
    public Component sendPunishmentWarning() {
        String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
        String until = DataUtils.timeUntil(end);
        switch(punishmentType) {
            case PERMANENT_BAN ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "permanently banned", staffName, reason, "<white>never expire"));
            case TEMPORARY_BAN ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "temporarily banned", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
            case PERMANENT_MUTE ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "permanently muted", staffName, reason, "<white>never <gray>expire"));
            case TEMPORARY_MUTE ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "temporarily muted", staffName, reason, "expire in <white>"+until+" ("+ends+")"));
            case KICK ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "kicked", staffName, reason, "<white>never <gray>expire"));
            case WARN ->
                alert(Messages.PUNISHMENT_ADMINISTERED.getMessage(playerName, "warned", staffName, reason, "<white>never <gray>expire"));
        }
        return getPunishmentMessage();
    }
    
    public Component getPunishmentMessage() {
        switch(punishmentType) {
            case PERMANENT_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_BAN_REASON.getMessage(reason, banned, pID);
            }
            case TEMPORARY_BAN -> {
                String banned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_BAN_REASON.getMessage(reason, banned, ends, until, pID);
            }
            case PERMANENT_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.PERMANENT_MUTE_REASON.getMessage(reason, muted, pID);
            }
            case TEMPORARY_MUTE -> {
                String muted = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                String ends = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(end));
                String until = DataUtils.timeUntil(end);
                return Messages.TEMPORARY_MUTE_REASON.getMessage(reason, muted, ends, until, pID);
            }
            case KICK -> {
                String kicked = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.KICK_REASON.getMessage(reason, kicked, pID);
            }
            case WARN -> {
                String warned = new SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss z").format(new Date(start));
                return Messages.WARN_REASON.getMessage(reason, warned, pID);
            }
            default -> {
                return Component.text().asComponent();
            }
        }
    }

    public void alert(Component message) {
        List<Player> onlineStaff = ResinData.getInstance().getServer().getOnlinePlayers().stream().map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull).filter(players -> players.hasPermission("valorant.staff")).toList();
        onlineStaff.forEach(staff ->
                staff.sendMessage(message));
        List<String> lines = List.of(MiniMessage.miniMessage().serialize(message).split("\n"));
        lines.forEach(line -> ResinData.getInstance().getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(line)));
    }

    @SuppressWarnings("unused")
    public boolean isActive() {
        if(!active) return false;
        if(punishmentType.isPermanent()) return true;
        long time = System.currentTimeMillis();
        return start <= time && time <= end;
    }

    private void setActive(boolean active) {
        this.active = active;
        File punishmentFile = ResinData.getInstance().getPunishmentManager().getPunishmentFile();
        JsonObject punishmentsObject = DataUtils.parseJson(punishmentFile);
        if(punishmentsObject != null) {
            JsonArray punishmentsArray = punishmentsObject.get("punishments").getAsJsonArray();
            for(JsonElement punishmentElement : punishmentsArray.asList()) {
                JsonObject punishment = punishmentElement.getAsJsonObject();
                if(punishment.get("id").getAsInt() == pID) {
                    int index = punishmentsArray.asList().indexOf(punishment);
                    punishment.addProperty("active", active);
                    punishmentsArray.set(index, punishment);
                    punishmentsObject.add("punishments", punishmentsArray);
                    DataUtils.writeJSONObject(punishmentFile, punishmentsObject);
                }
            }
        }
    }
    @Override
    public String toString() {
        return "Punishment[id="+pID+", player="+playerName+", staff="+staffName+", type="+punishmentType+", duration="+duration+", active="+active+"]";
    }
}
