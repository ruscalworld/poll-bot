package ru.ruscalworld.pollbot.core.sessions;

import net.dv8tion.jda.api.entities.Member;

public interface SessionManager {
    Session getMemberSession(Member member);
    void updateMemberSession(Session session);
}
