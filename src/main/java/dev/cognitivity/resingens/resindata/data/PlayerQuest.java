package dev.cognitivity.resingens.resindata.data;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
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
}
/*

{
    quest: id,
    status: 0-3
    update: #

 */
