package dev.cognitivity.resingens.resindata.punishment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.cognitivity.resingens.resindata.DataUtils;
import dev.cognitivity.resingens.resindata.ResinData;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

@Getter
public class PunishmentManager {
    private final File punishmentFile = new File(ResinData.getInstance().getDataFolder() + File.separator + "punishments.json");
    private final ArrayList<Punishment> punishments = new ArrayList<>();
    
    public PunishmentManager() {
        try {
            if(DataUtils.createFile(punishmentFile)) {
                JsonObject punishmentsObject = new JsonObject();
                JsonArray punishmentsArray = new JsonArray();
                punishmentsObject.add("punishments", punishmentsArray);
                DataUtils.writeJSONObject(punishmentFile, punishmentsObject);
            }
            JsonObject punishmentsObject = DataUtils.parseJson(punishmentFile);
            assert punishmentsObject != null;
            JsonArray punishmentsArray = punishmentsObject.get("punishments").getAsJsonArray();
            for(JsonElement element : punishmentsArray) {
                Punishment punishment = Punishment.of(element.getAsJsonObject());
                punishments.add(punishment);
            }
        } catch(Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Couldn't instantiate PunishmentManager", exception);
        }
    }
    public void addPunishment(Punishment punishment) {
        punishments.add(punishment);
        JsonObject punishmentsObject = DataUtils.parseJson(punishmentFile);
        assert punishmentsObject != null;
        JsonArray punishmentsArray = punishmentsObject.get("punishments").getAsJsonArray();
        punishmentsArray.add(punishment.getAsJson());
        punishmentsObject.add("punishments", punishmentsArray);
        DataUtils.writeJSONObject(punishmentFile, punishmentsObject);
    }
    @SuppressWarnings("unused")
    public void removePunishment(Punishment punishment) {
        punishments.remove(punishment);
    }
    public JsonObject getJsonObject() {
        return DataUtils.parseJson(punishmentFile);
    }
    public JsonArray getJsonArray() {
        return getJsonObject().get("punishments").getAsJsonArray();
    }
}
