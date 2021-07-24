package ru.ruscalworld.pollbot.core.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public interface InteractionHandler {
    default void onSelectionMenu(SelectionMenuEvent event, GuildSettings settings) throws Exception {};
    default void onButtonClick(ButtonClickEvent event, String[] args, GuildSettings settings) throws Exception {};
    void onError(Throwable throwable);
    String getName();
}
