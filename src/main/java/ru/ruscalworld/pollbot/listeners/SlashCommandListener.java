package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.commands.Command;

import java.util.concurrent.CompletableFuture;

public class SlashCommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
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

        event.deferReply().queue();

        CompletableFuture.runAsync(() -> {
            try {
                command.onExecute(event);
            } catch (Exception exception) {
                command.handleError(exception);
            }
        }).exceptionally(throwable -> {
            command.handleError(throwable);
            return null;
        });
    }
}
