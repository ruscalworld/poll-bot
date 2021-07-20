package ru.ruscalworld.pollbot.core.sessions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;

public class MemorySessionManager implements SessionManager {
    private final HashMap<String, HashMap<String, Session>> storage = new HashMap<>();

    @Override
    public Session getMemberSession(Member member) {
        HashMap<String, Session> guildSessions = this.getGuildSessions(member.getGuild());
        if (guildSessions.containsKey(member.getId())) return guildSessions.get(member.getId());

        Session session = new Session(member, this);
        guildSessions.put(member.getId(), session);
        return session;
    }

    @Override
    public void updateMemberSession(Session session) {
        // Do nothing because we don't need to update sessions explicitly
        // We don't override sessions in maps, so only one Session instance is always used for each member
    }

    public HashMap<String, Session> getGuildSessions(Guild guild) {
        if (this.getStorage().containsKey(guild.getId())) return this.getStorage().get(guild.getId());

        HashMap<String, Session> guildSessions = new HashMap<>();
        this.getStorage().put(guild.getId(), guildSessions);
        return guildSessions;
    }

    public HashMap<String, HashMap<String, Session>> getStorage() {
        return storage;
    }
}
