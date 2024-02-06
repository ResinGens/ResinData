package dev.cognitivity.resingens.resindata.data;

import com.google.gson.JsonObject;
import lombok.Getter;

public class DataUpdater {
    @Getter private static final int dataVersion = 1;

    public static JsonObject update(JsonObject data) {
        /*
        int version = data.get("version").getAsInt();
        if(version == dataVersion) return data;
        if(version == 1) {
            convert_2(data);
            version = 2;
        }
        */
        return data;
    }
    //private static void convert_2(JsonObject data) { ... }
}
