package dev.cognitivity.resingens.resindata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;

public class DataUtils {
    public static boolean createFile(File file) {
        try {
            boolean mkdir = file.getParentFile().mkdirs();
            boolean created = file.createNewFile();
            return mkdir || created;
        } catch (IOException exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to create file at "+file.getPath(), exception);
            return false;
        }
    }

    @Nullable
    public static JsonObject parseJSON(File file) {
        try {
            return (JsonObject) JsonParser.parseReader(new FileReader(file.getCanonicalPath()));
        } catch(Exception exception){
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to parse JSON from "+file.getPath(), exception);
            return null;
        }
    }
    @Nullable public static JsonObject parseJSON(String json) {
        if(json == null) return null;
        try {
            return (JsonObject) JsonParser.parseString(json);
        } catch (Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to parse JSON.", exception);
            return null;
        }
    }
    public static void writeJSONObject(File file, JsonObject jsonObject) {
        try {
            PrintWriter printWriter = new PrintWriter(file.getCanonicalPath());
            printWriter.write(jsonObject.toString());
            printWriter.flush();
            printWriter.close();
        } catch(Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to write JSON to "+file.getPath(), exception);
        }
    }
    public static String readFile(File file) {
        try {
            createFile(file);
            StringBuilder text = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                text.append(scanner.nextLine());
            }
            if (text.toString().isBlank()) {
                scanner.close();
                return "-";
            }
            scanner.close();
            return text.toString();
        } catch(Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to read "+file.getPath(), exception);
            return "-";
        }
    }
    public static double round(float n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static double round(double n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }

}
