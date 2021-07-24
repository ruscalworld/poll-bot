package ru.ruscalworld.pollbot.core.polls;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.exceptions.NotFoundException;
import ru.ruscalworld.pollbot.util.ProgressBar;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;
import ru.ruscalworld.storagelib.builder.expressions.Comparison;
import ru.ruscalworld.storagelib.builder.expressions.Condition;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Model(table = "polls")
public class Poll extends DefaultModel {
    @Property(column = "name")
    private String name;
    @Property(column = "title")
    @Nullable private String title;
    @Property(column = "description")
    @Nullable private String description;
    @Property(column = "ends_at")
    @Nullable private Timestamp endsAt;
    @Property(column = "owner_id")
    private String ownerId;
    @Property(column = "message_id")
    private String messageId;
    @Property(column = "channel_id")
    private String channelId;
    @Property(column = "guild_id")
    private String guildId;
    @Nullable private Message message;
    private Member owner;
    @Property(column = "allow_revote")
    private boolean allowRevote;
    @Property(column = "votes_per_user")
    private int votesPerUser;
    @Property(column = "anonymous")
    private boolean anonymous;
    @Property(column = "published")
    private boolean published;

    private final @NotNull List<Variant> variants = new ArrayList<>();

    public Poll(String name, @NotNull Member member) {
        this.name = name;
        this.owner = member;
        this.guildId = member.getGuild().getId();
        this.ownerId = member.getId();
    }

    public Poll() {

    }

    public static boolean isPoll(Message message) {
        if (message.getReactions().size() == 0) return false;
        if (message.getEmbeds().size() != 1) return false;
        MessageEmbed embed = message.getEmbeds().get(0);
        if (embed.getTitle() == null) return false;
        if (embed.getColor() == null) return false;
        return embed.getFields().size() != 0;
    }

    public static @Nullable Poll get(long id) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        try {
            return storage.retrieve(Poll.class, id);
        } catch (NotFoundException exception) {
            return null;
        }
    }

    public static @Nullable Poll getFromMessage(Message message) throws Exception {
        if (!isPoll(message)) return null;
        Storage storage = PollBot.getInstance().getStorage();
        Poll poll = storage.find(Poll.class, "message_id", message.getId());
        poll.setMessage(message);
        poll.fetchVariants();
        return poll;
    }

    public static @Nullable Poll getByName(String name, Guild guild) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Poll> polls = storage.findAll(Poll.class, Condition.and(
                Comparison.equal("name", name),
                Comparison.equal("guild_id", guild.getId())
        ));
        if (polls.size() == 0) return null;
        return polls.get(0);
    }

    public static Poll create(String name, @NotNull Member owner) throws Exception {
        Poll oldPoll = getByName(name, owner.getGuild());
        if (oldPoll != null) throw new InteractionException("Poll with this name already exists");

        Poll poll = new Poll(name, owner);
        poll.save();
        return poll;
    }

    public void preview(InteractionHook hook, GuildSettings settings) throws Exception {
        if (this.isPublished()) throw new InteractionException(settings.translate("responses.poll.preview.fail"));

        Message message = hook.sendMessageEmbeds(this.getEmbed(settings).build()).complete();
        if (this.getMessage() != null) {
            this.getMessage().editMessage(settings.translate("responses.poll.message-limit", message.getJumpUrl())).queue();
        }

        this.setMessage(message);
        this.save();
    }

    public void publish(TextChannel channel, GuildSettings settings) throws Exception {
        this.fetchVariants();
        if (this.getVariants().size() < 2) throw new InteractionException(settings.translate("responses.poll.publish.variant-limit.min", 2));
        if (this.isPublished()) throw new InteractionException(settings.translate("responses.poll.publish.already-published"));

        List<Component> buttons = new ArrayList<>();
        for (Variant variant : this.getVariants()) buttons.add(variant.makeButton());

        EmbedBuilder builder = this.getEmbed(settings);
        Message message = channel.sendMessageEmbeds(builder.build()).setActionRows(ActionRow.of(buttons)).complete();

        this.setPublished(true);
        this.setMessage(message);
        this.save();
    }

    public void updateLatestMessage(GuildSettings settings) throws Exception {
        if (this.getMessage() != null) this.getMessage().editMessageEmbeds(this.getEmbed(settings).build()).queue();
    }

    private String getEmbedFooter(GuildSettings settings) throws Exception {
        int memberCount = this.getMemberCount();
        return memberCount + " " + settings.translate(memberCount, "words.participant") +
                (this.isAnonymous() ? " • " + settings.translate("phrases.poll.anonymous") : "") +
                (this.isMultipleChoiceAllowed() ? " • " + settings.translate("phrases.poll.multiple-choice", this.getVotesPerUser()) : "") +
                (!this.isRevoteAllowed() ? " • " + settings.translate("phrases.poll.no-revoting") : "");
    }

    public EmbedBuilder getEmbed(GuildSettings settings) throws Exception {
        this.fetchVariants();
        int totalVotes = this.getTotalVotes();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getTitle());
        builder.setDescription(this.getDescription());

        for (Variant variant : this.getVariants()) {
            variant.fetchVotes();
            float percentage = totalVotes > 0 ? (float) Math.round((float) variant.getVotes().size() / (float) totalVotes * 1000) / 10 : 0;
            String name = variant.getSign() + " • " + variant.getTitle();
            String value = (variant.getDescription() != null ? variant.getDescription() + "\n" : "") +
                    ProgressBar.makeDefault(Math.round(percentage), 15) + " • " +
                    variant.getVotes().size() + " (" + percentage + "%)";
            builder.addField(name, value, false);
        }

        builder.setFooter(this.getEmbedFooter(settings));
        return builder;
    }

    public int getTotalVotes() throws Exception {
        int total = 0;
        for (Variant variant : this.getVariants()) total += variant.getVotes().size();
        return total;
    }

    public int getMemberCount() throws Exception {
        if (this.getMessage() == null) return 0;
        HashSet<String> members = new HashSet<>();

        for (Variant variant : this.getVariants()) {
            variant.fetchVotes();
            for (Vote vote : variant.getVotes()) members.add(vote.getMemberId());
        }

        return members.size();
    }

    public @NotNull List<Variant> getVariants() {
        return variants;
    }

    public void fetchVariants() throws Exception {
        List<Variant> variants = this.getVariants();
        variants.clear();
        Storage storage = PollBot.getInstance().getStorage();

        List<Variant> storedVariants = storage.findAll(Variant.class, Comparison.equal("poll_id", this.getId()));
        storedVariants.forEach(variant -> {
            variant.setPoll(this);
            variants.add(variant);
        });
    }

    public void save() throws Exception {
        Message message = this.getMessage();
        if (message != null) {
            this.setMessageId(message.getId());
            this.setChannelId(message.getChannel().getId());
        }

        Storage storage = PollBot.getInstance().getStorage();
        long id = storage.save(this);
        this.setId(id);
    }

    public List<Vote> getVotes(User user) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Vote> votes = storage.findAll(Vote.class, Comparison.equal("member_id", user.getId()));

        for (Vote vote : votes) {
            Variant variant = this.getVariant(vote.getVariantId());
            if (variant == null) continue;
            vote.setVariant(variant);
            vote.setMember(user);
        }

        return votes;
    }

    public @Nullable Variant getVariant(long id) {
        return Variant.get(id, this);
    }

    public @Nullable Variant getVariant(String name) throws Exception {
        return Variant.getByName(name, this);
    }

    public boolean isMultipleChoiceAllowed() {
        return this.getVotesPerUser() > 0;
    }

    public int getVotesPerUser() {
        return this.votesPerUser;
    }

    public void setVotesPerUser(int votesPerUser) {
        this.votesPerUser = votesPerUser;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable Timestamp getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(@Nullable Timestamp endsAt) {
        this.endsAt = endsAt;
    }

    public @Nullable Message getMessage() {
        if (message == null && this.getMessageId() != null) {
            TextChannel channel = PollBot.getInstance().getJDA().getTextChannelById(this.getChannelId());
            if (channel == null) return null;
            MessageHistory history = channel.getHistoryAround(this.getMessageId(), 1).complete();
            this.message = history.getMessageById(this.getMessageId());
        }

        return message;
    }

    public void setMessage(@Nullable Message message) {
        this.message = message;
    }

    public boolean isRevoteAllowed() {
        return allowRevote;
    }

    public void setAllowRevote(boolean allowRevote) {
        this.allowRevote = allowRevote;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public @NotNull String getTitle() {
        return this.title != null ? this.title : this.name;
    }

    public String getName() {
        return name;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public @NotNull Member getOwner() {
        return owner;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}