package dev.cognitivity.resingens.resindata.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.cognitivity.resingens.resindata.DataUtils;
import dev.cognitivity.resingens.resindata.ResinData;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

@Getter @Setter
public class PlayerQuest {
    private String id;
    private QuestStatus status;
    private int update;

    public PlayerQuest(String id, QuestStatus status, int update) {
        this.id = id;
        this.status = status;
        this.update = update;
    }
    public PlayerQuest(JsonObject jsonObject) {
        this.id = jsonObject.get("id").getAsString();
        this.status = QuestStatus.values()[jsonObject.get("status").getAsInt()];
        this.update = jsonObject.get("update").getAsInt();
    }

    public enum QuestStatus {
        LOCKED, INCOMPLETE, ACTIVE, COMPLETE;
    }
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", status.ordinal());
        jsonObject.addProperty("update", update);
        return jsonObject;
    }
    public void setStatus(QuestStatus status) {
        this.status = status;
        if(status == QuestStatus.COMPLETE) {
            JsonElement quests = DataUtils.parseJsonElement(DataUtils.readInputStream(ResinData.getInstance().getResource("quests.json")));
            if(quests == null) return;
            JsonArray questList = quests.getAsJsonArray();
            JsonObject quest = questList.asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(element -> element.get("id").getAsString().equals(id)).findFirst().orElse(null);
            if(quest == null || !quest.has("onComplete")) return;
            JsonObject completeEvent = quest.get("onComplete").getAsJsonObject();
            if(completeEvent.has("message")) {
                JsonArray messages = completeEvent.get("message").getAsJsonArray();
                messages.forEach(element -> Bukkit.broadcast(MiniMessage.miniMessage().deserialize(element.getAsString().replaceAll("\\{PLAYER}", "cognitivity"))));
            }
            if(completeEvent.has("command")) {
                JsonArray commands = completeEvent.get("command").getAsJsonArray();
                commands.forEach(element -> ResinData.getInstance().getServer().dispatchCommand(ResinData.getInstance().getServer().getConsoleSender(),
                        element.getAsString().replaceAll("\\{PLAYER}", "cognitivity")));
            }
        }
    }
}
/*

{
    quest: id,
    status: 0-3
    update: #

 */
