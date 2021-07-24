package ru.ruscalworld.pollbot.interactions;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.i18n.Translation;
import ru.ruscalworld.pollbot.core.interactions.DefaultInteractionHandler;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.util.Ensure;

public class LanguageInteraction extends DefaultInteractionHandler {
    public LanguageInteraction() {
        super("language");
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event, GuildSettings settings) throws Exception {
        if (event.getValues().size() != 1) return;
        assert event.getMember() != null;
        Ensure.ifMemberIsAdministrator(settings, event.getMember());

        String code = event.getValues().get(0);
        Translation translation = PollBot.getInstance().getTranslations().get(code);
        if (translation == null) throw new InteractionException("Unknown translation: \"" + code + "\"");

        settings.setLanguage(code);
        settings.save();

        event.getHook().sendMessage(settings.translate("responses.language.success", translation.getLocalizedName())).queue();
    }
}
