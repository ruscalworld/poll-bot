package ru.ruscalworld.pollbot.core.sessions;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.core.polls.Poll;

public class Session {
    private final Member member;
    private final SessionManager manager;
    @Nullable private Poll selectedPoll;

    public Session(Member member, SessionManager manager) {
        this.member = member;
        this.manager = manager;
    }

    public @Nullable Poll getSelectedPoll() {
        return selectedPoll;
    }

    public void setSelectedPoll(@Nullable Poll selectedPoll) {
        this.selectedPoll = selectedPoll;
        this.getManager().updateMemberSession(this);
    }

    public SessionManager getManager() {
        return manager;
    }

    public Member getMember() {
        return member;
    }
}
