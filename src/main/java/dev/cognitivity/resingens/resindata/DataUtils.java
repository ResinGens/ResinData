package dev.cognitivity.resingens.resindata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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
    public static JsonObject parseJson(File file) {
        try {
            return (JsonObject) JsonParser.parseReader(new FileReader(file.getCanonicalPath()));
        } catch(Exception exception){
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to parse JSON from "+file.getPath(), exception);
            return null;
        }
    }
    @Nullable public static JsonObject parseJson(String json) {
        if(json == null) return null;
        try {
            return (JsonObject) JsonParser.parseString(json);
        } catch (Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to parse JsonObject.", exception);
            return null;
        }
    }
    @Nullable public static JsonElement parseJsonElement(String json) {
        if(json == null) return null;
        try {
            return JsonParser.parseString(json);
        } catch (Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to parse JsonElement.", exception);
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
    public static String readInputStream(InputStream inputStream) {
        if (inputStream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        try {
            String nextline;
            while ((nextline = reader.readLine()) != null) {
                builder.append(nextline);
            }
            return builder.toString();
        } catch(Exception exception) {
            ResinData.getInstance().getLogger().log(Level.SEVERE, "Failed to read InputStream.", exception);
            return null;
        }
    }
    public static double round(float n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static double round(double n, int r) {
        return Math.round(n * Math.pow(10, r)) / Math.pow(10, r);
    }
    public static String timeUntil(long end) {
        return timeUntil(System.currentTimeMillis(), end);
    }
    public static String timeUntil(long start, long end) {
        return timeLength(end - start);
    }
    public static String timeLength(long time) {
        if(time <= 0) {
            return "0 ms";
        }
        long years = (long) Math.floor((double) time / 31536000000L);
        time %= 31536000000L;
        long days = (long) Math.floor((double) time / 86400000);
        time %= 86400000;
        long hours = (long) Math.floor((double) time / 3600000);
        time %= 3600000;
        long minutes = (long) Math.floor((double) time / 60000);
        time %= 60000;
        long seconds = (long) Math.floor((double) time / 1000);
        long milliseconds = time % 1000;
        StringBuilder stringBuilder = new StringBuilder();
        if(years != 0) {
            stringBuilder.append(years).append("y ");
        }
        if(days != 0) {
            stringBuilder.append(days).append("d ");
        }
        if(hours != 0) {
            stringBuilder.append(hours).append("h ");
        }
        if(minutes != 0) {
            stringBuilder.append(minutes).append("m ");
        }
        if(seconds != 0) {
            stringBuilder.append(seconds).append("s ");
        }
        if(years == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            stringBuilder.append(milliseconds).append("ms");
        }
        return stringBuilder.toString().trim();
    }
}
