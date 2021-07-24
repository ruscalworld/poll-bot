package ru.ruscalworld.pollbot.core.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;

public interface InteractionHandler {
    default void onSelectionMenu(SelectionMenuEvent event) throws Exception {};
    default void onButtonClick(ButtonClickEvent event, String[] args) throws Exception {};
    void onError(Throwable throwable);
    String getName();
}
