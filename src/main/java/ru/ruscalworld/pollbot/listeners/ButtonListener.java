package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.interactions.InteractionHandler;
import ru.ruscalworld.pollbot.exceptions.InteractionException;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ButtonListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ButtonListener.class);

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getButton() == null) return;
        String id = event.getButton().getId();
        if (id == null) return;
        String[] parts = id.split(" ");

        if (parts.length == 0) {
            logger.warn("Received button click with empty id ({})", id);
            return;
        }

        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        InteractionHandler handler = PollBot.getInstance().getInteractionHandlers().get(parts[0]);

        if (handler == null) {
            logger.warn("Received button click with unknown interaction name \"{}\"", parts[0]);
            return;
        }

        logger.debug("Handling button click \"{}\"", id);
        event.deferReply(true).queue();
        CompletableFuture.runAsync(() -> {
            try {
                handler.onButtonClick(event, args);
            } catch (InteractionException exception) {
                event.getHook().sendMessage(exception.getMessage()).queue();
            } catch (Exception exception) {
                handler.onError(exception);
            }
        }).exceptionally(throwable -> {
            handler.onError(throwable);
            return null;
        });
    }
}
