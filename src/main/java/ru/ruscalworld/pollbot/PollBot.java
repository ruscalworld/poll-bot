package ru.ruscalworld.pollbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ruscalworld.pollbot.commands.PollCommand;
import ru.ruscalworld.pollbot.commands.VariantCommand;
import ru.ruscalworld.pollbot.config.Config;
import ru.ruscalworld.pollbot.core.commands.Command;
import ru.ruscalworld.pollbot.core.interactions.InteractionHandler;
import ru.ruscalworld.pollbot.core.sessions.MemorySessionManager;
import ru.ruscalworld.pollbot.core.sessions.SessionManager;
import ru.ruscalworld.pollbot.interactions.VoteInteraction;
import ru.ruscalworld.pollbot.listeners.ButtonListener;
import ru.ruscalworld.pollbot.listeners.GuildListener;
import ru.ruscalworld.pollbot.listeners.SlashCommandListener;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.impl.SQLiteStorage;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class PollBot {
    private static final Logger logger = LoggerFactory.getLogger(PollBot.class);
    private static PollBot instance;

    private final HashMap<String, InteractionHandler> interactionHandlers = new HashMap<>();
    private final HashMap<String, Command> commands = new HashMap<>();
    private SessionManager sessionManager;
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
        command.onPreRegister(data);
        this.getCommands().put(name, command);
    }

    public void registerInteractionHandler(InteractionHandler handler) {
        this.getInteractionHandlers().put(handler.getName(), handler);
    }

    public void onStart() {
        JDABuilder builder = JDABuilder.createDefault(this.getConfig().getBotToken());

        builder.addEventListeners(new GuildListener());
        builder.addEventListeners(new ButtonListener());
        builder.addEventListeners(new SlashCommandListener());
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);

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
        this.setSessionManager(new MemorySessionManager());
        this.registerInteractionHandler(new VoteInteraction());
        this.registerCommand(new PollCommand());
        this.registerCommand(new VariantCommand());
        this.onCommandsReady();
    }

    public void onCommandsReady() {
        String path = this.getConfig().getStoragePath() + "/pollbot.db";
        logger.info("Initializing storage ({})", path);
        SQLiteStorage storage = new SQLiteStorage("jdbc:sqlite:" + path);

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
            logger.debug("Registering global command \"{}\"", command.getCommandData().getName());
            commands = commands.addCommands(command.getCommandData());
        }
        commands.complete();
    }

    public CommandUpdateAction updateGuildCommands(Guild guild) {
        CommandUpdateAction commands = guild.updateCommands();
        for (Command command : this.getCommands().values()) {
            if (command.isGlobal()) continue;
            logger.debug("Updating \"{}\" command for guild \"{}\"", command.getCommandData().getName(), guild.getName());
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

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public HashMap<String, InteractionHandler> getInteractionHandlers() {
        return interactionHandlers;
    }
}
