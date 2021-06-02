package ru.ruscalworld.pollbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import ru.ruscalworld.pollbot.commands.PollCommand;
import ru.ruscalworld.pollbot.config.Config;
import ru.ruscalworld.pollbot.core.Command;
import ru.ruscalworld.pollbot.listeners.GuildJoinListener;
import ru.ruscalworld.pollbot.listeners.SlashCommandListener;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.impl.SQLiteStorage;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class PollBot {
    private static PollBot instance;

    private final HashMap<String, Command> commands = new HashMap<>();
    private final Config config;
    private Storage storage;
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

    public void registerCommand(Command command) {
        CommandData data = command.getCommandData();
        String name = data.getName();
        command.onRegister(data);
        this.getCommands().put(name, command);
    }

    public void onStart() {
        JDABuilder builder = JDABuilder.createDefault(this.getConfig().getBotToken());

        builder.addEventListeners(new GuildJoinListener());
        builder.addEventListeners(new SlashCommandListener());

        try {
            JDA jda = builder.build();
            jda.awaitReady();
            this.setJDA(jda);
            this.onClientReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClientReady() {
        this.registerCommand(new PollCommand());
        this.onCommandsReady();
    }

    public void onCommandsReady() {
        SQLiteStorage storage = new SQLiteStorage("jdbc:sqlite:" + this.getConfig().getStoragePath() + "/pollbot.db");
        storage.registerMigration("polls");
        storage.registerMigration("variants");
        storage.registerMigration("votes");

        try {
            storage.actualizeStorageSchema();
            this.setStorage(storage);
            this.onReady();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void onReady() {
        CommandUpdateAction commands = this.getJDA().updateCommands();
        for (Command command : this.getCommands().values()) {
            if (!command.isGlobal()) continue;
            commands = commands.addCommands(command.getCommandData());
        }
        commands.complete();
    }

    public CommandUpdateAction updateGuildCommands(Guild guild) {
        CommandUpdateAction commands = guild.updateCommands();
        for (Command command : this.getCommands().values()) {
            commands = commands.addCommands(command.getCommandData());
        }
        return commands;
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

    public HashMap<String, Command> getCommands() {
        return commands;
    }

    public Storage getStorage() {
        return storage;
    }

    protected void setStorage(Storage storage) {
        this.storage = storage;
    }
}
