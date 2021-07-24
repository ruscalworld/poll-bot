package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.commands.DefaultCommand;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.util.Ensure;

import java.util.Collections;

public class LanguageCommand extends DefaultCommand {
    public LanguageCommand() {
        super("language", "Changes language of bot responses for your guild");
    }

    @Override
    public void onExecute(SlashCommandEvent event) throws Exception {
        if (event.getGuild() == null) return;
        if (event.getMember() == null) return;

        GuildSettings settings = GuildSettings.getByGuild(event.getGuild());
        Ensure.ifMemberIsAdministrator(settings, event.getMember());

        SelectionMenu.Builder menu = SelectionMenu.create("language");
        menu.setMaxValues(1);
        menu.setMinValues(1);
        PollBot.getInstance().getTranslations().forEach((code, translation) -> {
            String name = translation.getLocalizedName();
            SelectOption option = SelectOption.of(name == null ? "Unknown" : name, code)
                    .withEmoji(Emoji.fromUnicode(translation.getEmoji()))
                    .withDefault(settings.getLanguage().equals(code));
            menu.addOptions(Collections.singletonList(option));
        });

        event.getHook().sendMessage("Please select your language").addActionRow(menu.build()).queue();
    }
}
