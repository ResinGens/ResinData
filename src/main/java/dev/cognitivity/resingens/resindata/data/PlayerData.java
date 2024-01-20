package dev.cognitivity.resingens.resindata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.cognitivity.resingens.resindata.DataUtils;
import dev.cognitivity.resingens.resindata.Messages;
import dev.cognitivity.resingens.resindata.ResinData;
import lombok.Getter;
import lombok.Setter;
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
        ResinData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+(System.currentTimeMillis() - startTime)+" ms.");
    }
    public JsonObject createData() {

        JsonObject firstLogin = new JsonObject();
        long firstPlayed = player.getFirstPlayed();
        if(firstPlayed == 0) firstPlayed = System.currentTimeMillis();
        firstLogin.addProperty("firstLogin", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new Date(firstPlayed)));

        JsonArray nameHistoryArray = new JsonArray();
        nameHistoryArray.add(player.getName());

        JsonObject nameHistory = new JsonObject();
        nameHistory.add("nameHistory", nameHistoryArray);

        JsonArray profileArray = new JsonArray();
        profileArray.add(firstLogin);
        profileArray.add(nameHistory);

        JsonObject profileObject = new JsonObject();
        profileObject.add("profile", profileArray);

        JsonObject questsObject = new JsonObject();
        JsonArray questsArray = new JsonArray();
        ArrayList<String> usedIds = new ArrayList<>();
        for(PlayerQuest quest : quests) {
            if(!usedIds.contains(quest.getId())) {
                questsArray.add(quest.toJson());
                usedIds.add(quest.getId());
            }
        }
        questsObject.add("quests", questsArray);

        JsonArray statisticsArray = new JsonArray();
        statisticsArray.add(questsObject);

        JsonObject statisticsObject = new JsonObject();
        statisticsObject.add("statistics", statisticsArray);

        JsonObject discordId = new JsonObject();
        discordId.addProperty("id", this.discordId);

        JsonArray discordArray = new JsonArray();
        discordArray.add(discordId);

        JsonObject discordObject = new JsonObject();
        discordObject.add("discord", discordArray);

        JsonArray dataArray = new JsonArray();
        dataArray.add(profileObject);
        dataArray.add(statisticsObject);
        dataArray.add(discordObject);

        JsonObject dataObject = new JsonObject();
        dataObject.add("data", dataArray);

        return dataObject;
    }

    public void saveData() {
        ResinData.getInstance().log("<aqua>[DATA] <gray>Saving data for "+player.getName()+"...");
        long start = System.nanoTime();
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.SAVING_DATA.getMessage());
        JsonObject dataFile = DataUtils.parseJSON(file);
        assert dataFile != null;
        JsonArray data = dataFile.getAsJsonArray("data");
        JsonArray statistics = data.get(1).getAsJsonObject().getAsJsonArray("statistics");

        JsonObject questsObject = statistics.get(0).getAsJsonObject();
        JsonArray questsArray = new JsonArray();
        questsObject.add("quests", questsArray);
        ArrayList<String> usedIds = new ArrayList<>();
        for(PlayerQuest quest : quests) {
            if(!usedIds.contains(quest.getId())) {
                questsArray.add(quest.toJson());
                usedIds.add(quest.getId());
            }
        }

        JsonArray discord = dataFile.getAsJsonArray("data").get(2).getAsJsonObject().getAsJsonArray("discord");
        set(discord, 0, "id", discordId);
        
        DataUtils.writeJSONObject(file, dataFile);
        double ms = DataUtils.round((float) (System.nanoTime() - start)/1000000, 2);
        if(player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Messages.SAVED_DATA.getMessage(ms));
        ResinData.getInstance().log("<aqua>[DATA] <gray>Finished creating data for player "+player.getName()+". Took "+ms+" ms.");
    }

    public void updateData() {
        JsonObject dataFile = DataUtils.parseJSON(file);
        assert dataFile != null;
        JsonArray data = dataFile.getAsJsonArray("data");
        JsonArray profile = data.get(0).getAsJsonObject().getAsJsonArray("profile");
        JsonArray statistics = data.get(1).getAsJsonObject().getAsJsonArray("statistics");
        JsonArray questsArray = statistics.get(0).getAsJsonObject().getAsJsonArray("quests");
        quests.clear();
        ArrayList<String> usedIds = new ArrayList<>();
        for(JsonElement questObject : questsArray) {
            PlayerQuest quest = new PlayerQuest(questObject.getAsJsonObject());
            if(!usedIds.contains(quest.getId())) {
                usedIds.add(quest.getId());
                quests.add(quest);
            }
        }
        JsonArray discord = data.get(2).getAsJsonObject().getAsJsonArray("discord");
        discordId = discord.get(0).getAsJsonObject().get("id").getAsLong();
        JsonArray nameHistory = profile.get(1).getAsJsonObject().getAsJsonArray("nameHistory");
        if(player.getName() != null && !nameHistory.contains(new JsonPrimitive(player.getName()))) nameHistory.add(player.getName());
        DataUtils.writeJSONObject(file, dataFile);
    }

    private void set(JsonArray jsonArray, int index, String property, Number value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(property, value);
        jsonArray.set(index, jsonObject);
    }
}