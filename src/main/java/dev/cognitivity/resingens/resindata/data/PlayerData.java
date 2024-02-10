package dev.cognitivity.resingens.resindata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.cognitivity.resingens.resindata.DataUtils;
import dev.cognitivity.resingens.resindata.Messages;
import dev.cognitivity.resingens.resindata.ResinData;
import dev.cognitivity.resingens.resindata.punishment.Punishment;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@Getter @Setter
public class PlayerData {
    @NotNull private OfflinePlayer player;
    private final File file;

    private ArrayList<PlayerQuest> quests = new ArrayList<>();
    private final ArrayList<Punishment> punishments = new ArrayList<>();
    private final ArrayList<String> hashedIps = new ArrayList<>();
    private long discordId;

    public PlayerData(@NotNull OfflinePlayer player) {
        long startTime = System.currentTimeMillis();
        this.player = player;
        ResinData.getInstance().log("<aqua>[DATA] <gray>Creating JSON data for player "+player.getName()+"...");
        file = new File(ResinData.getDataPath() + File.separator + player.getUniqueId() + ".json");
        if (DataUtils.createFile(file) || DataUtils.readFile(file).equals("-")) {
            DataUtils.writeJSONObject(file, createData());
        }
        updateData();
        loadData();
        ResinData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+(System.currentTimeMillis() - startTime)+" ms.");
    }
    public JsonObject createData() {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("version", DataUpdater.getDataVersion());
        JsonObject profileObject = new JsonObject();

        JsonArray ipArray = new JsonArray();
        hashedIps.forEach(ipArray::add);
        profileObject.add("addresses", ipArray);

        long firstPlayed = player.getFirstPlayed();
        if(firstPlayed == 0) firstPlayed = System.currentTimeMillis();
        profileObject.addProperty("firstLogin", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new Date(firstPlayed)));

        JsonArray nameHistoryArray = new JsonArray();
        nameHistoryArray.add(player.getName());
        profileObject.add("nameHistory", nameHistoryArray);

        dataObject.add("profile", profileObject);


        JsonObject statisticsObject = new JsonObject();

        JsonArray questsArray = new JsonArray();
        ArrayList<String> usedIds = new ArrayList<>();
        for(PlayerQuest quest : quests) {
            if(!usedIds.contains(quest.getId())) {
                questsArray.add(quest.toJson());
                usedIds.add(quest.getId());
            }
        }
        statisticsObject.add("quests", questsArray);

        dataObject.add("statistics", statisticsObject);


        JsonObject discordObject = new JsonObject();

        discordObject.addProperty("id", this.discordId);

        dataObject.add("discord", discordObject);


        dataObject.add("punishments", new JsonArray());

        return dataObject;
    }

    public void saveData() {
        ResinData.getInstance().log("<aqua>[DATA] <gray>Saving data for "+player.getName()+"...");
        long start = System.nanoTime();
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.SAVING_DATA.getMessage());
        JsonObject data = DataUtils.parseJson(file);
        assert data != null;

        JsonObject profileObject = data.get("profile").getAsJsonObject();

        JsonArray ipArray = new JsonArray();
        hashedIps.forEach(ipArray::add);
        profileObject.add("addresses", ipArray);

        JsonObject statistics = data.get("statistics").getAsJsonObject();

        JsonArray questsArray = new JsonArray();
        ArrayList<String> usedIds = new ArrayList<>();
        for(PlayerQuest quest : quests) {
            if(!usedIds.contains(quest.getId())) {
                questsArray.add(quest.toJson());
                usedIds.add(quest.getId());
            }
        }
        statistics.add("quests", questsArray);

        JsonObject discord = data.get("discord").getAsJsonObject();
        discord.addProperty("id", discordId);

        JsonArray punishmentsArray = data.get("punishments").getAsJsonArray();
        this.punishments.stream().filter(Objects::nonNull).map(Punishment::getPID).forEach(punishmentsArray::add);
        data.add("punishments", punishmentsArray);

        DataUtils.writeJSONObject(file, data);
        double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.SAVED_DATA.getMessage(ms));
        ResinData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+ms+" ms.");
    }

    public void loadData() {
        long start = System.nanoTime();
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.LOADING_DATA.getMessage());

        JsonObject data = DataUtils.parseJson(file);
        assert data != null;
        JsonObject profile = data.get("profile").getAsJsonObject();

        JsonArray ipArray = profile.get("addresses").getAsJsonArray();
        hashedIps.clear();
        ipArray.forEach(ip -> hashedIps.add(ip.getAsString()));

        JsonObject statistics = data.get("statistics").getAsJsonObject();
        JsonArray questsArray = statistics.get("quests").getAsJsonArray();
        quests.clear();
        ArrayList<String> usedIds = new ArrayList<>();
        for(JsonElement questObject : questsArray) {
            PlayerQuest quest = new PlayerQuest(questObject.getAsJsonObject());
            if(!usedIds.contains(quest.getId())) {
                usedIds.add(quest.getId());
                quests.add(quest);
            }
        }
        JsonArray nameHistory = profile.get("nameHistory").getAsJsonArray();
        if(player.getName() != null && !nameHistory.contains(new JsonPrimitive(player.getName()))) nameHistory.add(player.getName());

        JsonObject discord = data.get("discord").getAsJsonObject();
        discordId = discord.get("id").getAsLong();

        JsonArray punishmentsArray = data.get("punishments").getAsJsonArray();
        punishments.clear();
        punishments.addAll(punishmentsArray.asList().stream().map(punishment -> Punishment.of(punishment.getAsInt())).toList());

        DataUtils.writeJSONObject(file, data);
        double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendActionBar(Messages.LOADED_DATA.getMessage(ms));
    }
    public void updateData() {
        JsonObject data = DataUtils.parseJson(file);
        assert data != null;
        int version = data.get("version").getAsInt();
        int latestVersion = DataUpdater.getDataVersion();
        if(version < latestVersion) {
            data = DataUpdater.update(data);
        } else if(version > latestVersion) {
            if(player.getPlayer() != null) {
                player.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<red>Failed to load your player data! <white>(ID 1)\n" +
                                "<gray>ERROR INFO:\n" +
                                "\n" +
                                "<white>Invalid version!\n" +
                                version + " > " + latestVersion+"\n" +
                                "\n" +
                                "<red>Please report this on our Discord: <blue><u>discord.gg/example</u><red>."
                ));
            }
        }
        JsonObject profile = data.get("profile").getAsJsonObject();

        JsonArray nameHistory = profile.get("nameHistory").getAsJsonArray();
        if(player.getName() != null && !nameHistory.contains(new JsonPrimitive(player.getName()))) nameHistory.add(player.getName());

        DataUtils.writeJSONObject(file, data);
    }
}