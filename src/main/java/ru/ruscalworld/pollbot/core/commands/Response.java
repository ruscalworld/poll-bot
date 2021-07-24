package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private final String message;
    private final List<ActionRow> rows = new ArrayList<>();

    protected Response(String message) {
        this.message = message;
    }

    public static Response simple(String message) {
        return new Response(message);
    }

    public static Response selection(String message, SelectionMenu menu) {
        Response response = new Response(message);
        response.getRows().add(ActionRow.of(menu));
        return response;
    }

    public static Response translation(GuildSettings settings, String key, Object... args) {
        return simple(settings.translate(key, args));
    }

    public String getMessage() {
        return message;
    }

    public List<ActionRow> getRows() {
        return rows;
    }
}
