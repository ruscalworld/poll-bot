package ru.ruscalworld.pollbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import ru.ruscalworld.pollbot.core.polls.Poll;
import ru.ruscalworld.pollbot.core.sessions.Session;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;

public class Ensure {
    public static Poll ifPollIsSelected(GuildSettings settings, Session session) throws InteractionException {
        if (session.getSelectedPoll() == null) throw new InteractionException(settings, "responses.poll.generic.not-selected");
        return session.getSelectedPoll();
    }

    public static void ifPollIsEditable(GuildSettings settings, Poll poll) throws InteractionException {
        if (poll.isPublished()) throw new InteractionException(settings, "responses.poll.generic.not-editable");
    }

    public static void ifMemberHasPermission(GuildSettings settings, Member member, Permission... permissions) throws InteractionException {
        if (!member.hasPermission(permissions)) throw new InteractionException(settings, "responses.generic.no-permission");
    }

    public static void ifMemberIsAdministrator(GuildSettings settings, Member member) throws InteractionException {
        if (!member.hasPermission(Permission.ADMINISTRATOR) || !member.isOwner())
            throw new InteractionException(settings, "responses.generic.no-permission");
    }

    public static void ifMemberCanUseCommands(GuildSettings settings, Member member) throws InteractionException {
        if (settings.isAvailableToAllMembers()) return;
        Ensure.ifMemberIsAdministrator(settings, member);
    }
}
