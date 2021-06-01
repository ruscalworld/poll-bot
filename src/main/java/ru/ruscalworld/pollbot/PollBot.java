package ru.ruscalworld.pollbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import ru.ruscalworld.pollbot.config.Config;

import javax.security.auth.login.LoginException;

public class PollBot {
    private static PollBot instance;

    private final Config config;
    private JDA jda;

    public PollBot(Config config) {
        this.config = config;
    }

    public static void main(String... args) {
        Config config = new Config();
        config.load();

        PollBot pollBot = new PollBot(config);
        instance = pollBot;
        pollBot.onStart();
    }

    public void onStart() {
        JDABuilder builder = JDABuilder.createDefault(this.getConfig().getBotToken());

        try {
            JDA jda = builder.build();
            jda.awaitReady();
            this.setJDA(jda);
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static PollBot getInstance() {
        return instance;
    }

    public JDA getJDA() {
        return jda;
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    public Config getConfig() {
        return config;
    }
}
