package ru.ruscalworld.pollbot.config;

public class Config {
    private String botToken;
    private String storagePath;

    public void load() {
        this.setBotToken(System.getenv("BOT_TOKEN"));
        this.setStoragePath(getEnvOrDefault("STORAGE_PATH", System.getProperty("user.home")));
    }

    public static String getEnvOrDefault(String name, String def) {
        String env = System.getenv(name);
        if (env == null) return def;
        return env;
    }

    public String getBotToken() {
        return botToken;
    }

    protected void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getStoragePath() {
        return storagePath;
    }

    protected void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
