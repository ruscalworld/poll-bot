package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.commands.Response;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.core.commands.Command;
import ru.ruscalworld.pollbot.util.Ensure;

import java.util.concurrent.CompletableFuture;

public class SlashCommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null) return;
        if (event.getMember() == null) return;

        String name = event.getName();
        Command command = PollBot.getInstance().getCommands().get(name);
        if (command == null) {
            logger.warn("Received slash-command interaction that corresponds with unknown command \"{}\"", name);
            return;
        }

        logger.debug(
                "Executing \"{}\" command for {} ({})",
                command.getCommandData().getName(),
                event.getUser().getAsTag(), event.getUser().getId()
        );

        InteractionHook hook = event.getHook();
        CompletableFuture.runAsync(() -> {
            try {
                GuildSettings settings = GuildSettings.getByGuild(event.getGuild());
                Ensure.ifMemberCanUseBot(settings, event.getMember());

                Response response = command.onExecute(event, settings);
                if (response != null) {
                    event.deferReply().queue();
                    hook.sendMessage(response.getMessage()).queue();
                }
            } catch (InteractionException exception) {
                event.deferReply(true).queue();
                hook.sendMessage(exception.getMessage()).queue();
            } catch (Exception exception) {
                command.onError(exception);
            }
        }).exceptionally(throwable -> {
            command.onError(throwable);
            return null;
        });
    }
}
