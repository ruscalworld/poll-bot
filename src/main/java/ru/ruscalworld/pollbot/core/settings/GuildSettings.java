package ru.ruscalworld.pollbot.core.settings;

import net.dv8tion.jda.api.entities.Guild;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.i18n.Translator;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;
import ru.ruscalworld.storagelib.exceptions.NotFoundException;

@Model(table = "guilds")
public class GuildSettings extends DefaultModel {
    @Property(column = "guild_id")
    private final String guildId;

    @Property(column = "language")
    private String language;

    public GuildSettings(String guildId) {
        this.guildId = guildId;
    }

    public GuildSettings() {
        this.guildId = null;
    }

    public static GuildSettings getByGuild(Guild guild) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();

        try {
            return storage.find(GuildSettings.class, "guild_id", guild.getId());
        } catch (NotFoundException exception) {
            return new GuildSettings(guild.getId());
        }
    }

    public void save() throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        storage.save(this);
    }

    public String translate(String key, Object... args) {
        return Translator.translate(this.getLanguage(), key, args);
    }

    public String translate(int number, String key, Object... args) {
        return Translator.translate(this.getLanguage(), number, key, args);
    }

    public Guild getGuild() {
        return PollBot.getInstance().getJDA().getGuildById(this.getGuildId());
    }

    public String getGuildId() {
        return guildId;
    }

    public String getLanguage() {
        return language == null ? Translator.DEFAULT_LANGUAGE : language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isAvailableToAllMembers() {
        return false;
    }
}
