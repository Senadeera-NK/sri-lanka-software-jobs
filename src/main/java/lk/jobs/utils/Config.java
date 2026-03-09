package lk.jobs.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("⚠️ Warning: config.properties not found!");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    // Add a helper for Integers like "max.days.old"
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }

    //to get the keywords list
    public static List<String> getList(String key){
        String value = properties.getProperty(key);
        if(value==null || value.isEmpty())return List.of();
        return Arrays.asList(value.split(","));
    }

    //----Optional----
    // Add these to Config.java for convenience
    public static List<String> getTechKeywords() {
        return getList("tech.keywords");
    }

    public static List<String> getJobRoleKeywords() {
        return getList("techjobs.keywords");
    }
}