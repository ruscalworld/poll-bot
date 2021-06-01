package ru.ruscalworld.pollbot.config;

public class Config {
    private String botToken;

    public void load() {
        this.setBotToken(System.getenv("BOT_TOKEN"));
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
}
