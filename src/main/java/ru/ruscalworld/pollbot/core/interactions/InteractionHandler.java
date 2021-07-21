package ru.ruscalworld.pollbot.core.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public interface InteractionHandler {
    void onButtonClick(ButtonClickEvent event, String[] args) throws Exception;
    void onError(Throwable throwable);
    String getName();
}
