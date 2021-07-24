package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.interactions.InteractionHandler;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.util.Ensure;

import java.util.concurrent.CompletableFuture;

public class SelectionMenuListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SelectionMenuListener.class);

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        if (event.getGuild() == null) return;
        if (event.getMember() == null) return;
        if (event.getSelectionMenu() == null) return;

        String id = event.getSelectionMenu().getId();
        if (id == null) return;
        InteractionHandler handler = PollBot.getInstance().getInteractionHandlers().get(id);

        if (handler == null) {
            logger.warn("Received selection menu click with unknown interaction name \"{}\"", id);
            return;
        }

        logger.debug("Handling selection menu click \"{}\"", id);
        event.deferReply(true).queue();
        CompletableFuture.runAsync(() -> {
            try {
                GuildSettings settings = GuildSettings.getByGuild(event.getGuild());
                handler.onSelectionMenu(event, settings);
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
