package ru.ruscalworld.pollbot.core.polls;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.exceptions.CommandException;
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
    @Property(column = "allow_multiple_choice")
    private boolean allowMultipleChoice;
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
        if (oldPoll != null) throw new CommandException("Poll with this name already exists");

        Poll poll = new Poll(name, owner);
        poll.save();
        return poll;
    }

    public void preview(InteractionHook hook) throws Exception {
        Message message = hook.sendMessageEmbeds(this.getEmbed().build()).complete();
        if (this.getMessage() != null) {
            this.getMessage().editMessage("Sorry, but bot can handle only one poll message. " +
                    "Embed that you see below, will not be updated anymore. " +
                    "Newer message with this poll can be found [here](" + message.getJumpUrl() + ").").queue();
        }

        this.setMessage(message);
        this.save();
    }

    public void publish(TextChannel channel) throws Exception {
        if (this.getVariants().size() < 2) throw new CommandException("You must add at least 2 variants");
        if (this.isPublished()) throw new CommandException("This poll is already published");

        EmbedBuilder builder = this.getEmbed();
        Message message = channel.sendMessage(builder.build()).complete();
        for (Variant variant : this.getVariants()) message.addReaction(variant.getSign()).complete();

        this.setPublished(true);
        this.setMessage(message);
        this.save();
    }

    public void updateLatestMessage() throws Exception {
        if (this.getMessage() != null) this.getMessage().editMessage(this.getEmbed().build()).queue();
    }

    private String getEmbedFooter() {
        int memberCount = this.getMemberCount();
        return memberCount + " participants" +
                (this.isAnonymous() ? " • " + "Anonymous poll" : "") +
                (this.isMultipleChoiceAllowed() ? " • " + "Multiple choice allowed" : "") +
                (!this.isRevoteAllowed() ? " • " + "Revoting is disabled" : "");
    }

    public EmbedBuilder getEmbed() throws Exception {
        this.fetchVariants();
        int totalVotes = this.getTotalVotes();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getTitle());
        builder.setDescription(this.getDescription());

        for (Variant variant : this.getVariants()) {
            float percentage = totalVotes > 0 ? (float) Math.round((float) variant.getVotes().size() / (float) totalVotes * 1000) / 10 : 0;
            String name = variant.getSign() + " • " + variant.getDescription();
            String value = ProgressBar.makeDefault(Math.round(percentage), 15) + " • " +
                    variant.getVotes().size() + " (" + percentage + "%)";
            builder.addField(name, value, false);
        }

        builder.setFooter(this.getEmbedFooter());
        return builder;
    }

    public int getTotalVotes() throws Exception {
        int total = 0;
        for (Variant variant : this.getVariants()) total += variant.getVotes().size();
        return total;
    }

    public int getMemberCount() {
        if (this.getMessage() == null) return 0;
        List<String> members = new ArrayList<>();
        int total = 0;

        for (Variant variant : this.getVariants()) {
            MessageReaction reaction = variant.getReaction(this.getMessage());
            if (reaction == null) continue;

            List<User> currentMembers = reaction.retrieveUsers().complete();
            for (User currentMember : currentMembers) {
                if (members.contains(currentMember.getId())) continue;
                if (currentMember.isBot()) continue;
                members.add(currentMember.getId());
                total++;
            }
        }

        return total;
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

    public @Nullable Variant getVariant(long id) throws Exception {
        return Variant.get(id, this);
    }

    public @Nullable Variant getVariant(String name) throws Exception {
        return Variant.getByName(name, this);
    }

    public boolean isMultipleChoiceAllowed() {
        return this.allowMultipleChoice;
    }

    public void setMultipleChoiceAllowed(boolean multipleChoice) {
        this.allowMultipleChoice = multipleChoice;
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