package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private boolean ephemeral;
    private final String message;
    private final List<ActionRow> rows = new ArrayList<>();

    protected Response(String message) {
        this.message = message;
    }

    protected Response(String message, boolean ephemeral) {
        this.message = message;
        this.ephemeral = ephemeral;
    }

    public static Response simple(String message) {
        return simple(message, false);
    }

    public static Response simple(String message, boolean ephemeral) {
        return new Response(message, ephemeral);
    }

    public static Response selection(String message, SelectionMenu menu) {
        return selection(message, menu, false);
    }

    public static Response selection(String message, SelectionMenu menu, boolean ephemeral) {
        Response response = new Response(message, ephemeral);
        response.getRows().add(ActionRow.of(menu));
        return response;
    }

    public static Response translation(GuildSettings settings, String key, Object... args) {
        return translation(settings, false, key, args);
    }

    public static Response translation(GuildSettings settings, boolean ephemeral, String key, Object... args) {
        return simple(settings.translate(key, args), ephemeral);
    }

    public String getMessage() {
        return message;
    }

    public List<ActionRow> getRows() {
        return rows;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
}
