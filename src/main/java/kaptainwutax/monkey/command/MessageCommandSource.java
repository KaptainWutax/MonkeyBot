package kaptainwutax.monkey.command;

import kaptainwutax.monkey.holder.HolderGuild;
import kaptainwutax.monkey.init.Guilds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageCommandSource {

    private final MessageReceivedEvent event;

    public MessageCommandSource(@Nonnull final MessageReceivedEvent event) {
        this.event = event;
    }

    /**
     * Returns true when the command was sent in a DM or group chat. Returns false when it's sent in a guild.
     */
    public boolean isDMs() {
        return !event.isFromGuild();
    }

    /**
     * Returns the {@link User} who sent the command. For information like the user's roles or permissions,
     * use {@link #getMember()} instead, but make sure you aren't in a DM channel first.
     */
    @Nonnull
    public User getUser() {
        return event.getAuthor();
    }

    /**
     * Returns the {@link Member} who sent the command. You must be sure this isn't a DMs channel first,
     * or this will return null. For basic information about the user irrespective of whether this is
     * a DM channel or not, use {@link #getUser()} instead.
     */
    @Nullable
    public Member getMember() {
        return event.getMember();
    }

    /**
     * Returns the {@link Guild} the command was sent in.
     */
    @Nullable
    public Guild getGuild() {
        return event.getGuild();
    }

    /**
     * Returns the {@link MessageChannel} the command was sent in.
     */
    public MessageChannel getChannel() {
        return event.getChannel();
    }

    /**
     * Returns the {@link MessageReceivedEvent} that generated this command response. You should usually
     * not use this directly, prefer to create more accessor methods in this class.
     */
    @Nonnull
    public MessageReceivedEvent getEvent() {
        return event;
    }

    // ===== HELPER METHODS ===== //

    /**
     * Convenience method to check whether the sender has administrator permissions. Returns false in DMs.
     */
    public boolean isAdministrator() {
        if (isDMs())
            return false;
        Member member = getMember();
        assert member != null;
        boolean b=member.hasPermission(Permission.ADMINISTRATOR);
        return b;
    }

    /**
     * Returns whether the user can use fun commands in this context.
     */
    public boolean canUseFunCommands() {
        if (isDMs() || isAdministrator())
            return true;
        Guild guild = getGuild();
        assert guild != null;
        HolderGuild holder = Guilds.instance().getOrCreateServer(new HolderGuild(guild));
        return holder.controller.funCommands;
    }

    public boolean canUseSayCommand(){
        if (isDMs() || isAdministrator())
            return true;
        Guild guild = getGuild();
        assert guild != null;
        HolderGuild holder = Guilds.instance().getOrCreateServer(new HolderGuild(guild));
        return holder.controller.sayCommand;
    }

    /**
     * Checks if monkey bot has the given permissions. If {@code permName} is not {@code null},
     * also sends an error message saying that monkey needs {@code permName}. Returns whether
     * monkey has the required perms.
     *
     * Caution: in DMs, always returns true.
     */
    public boolean checkBotPerms(@Nullable String permName, Permission... requiredPermissions) {
        if (isDMs()) return true;
        if (event.getGuild().getSelfMember().hasPermission(requiredPermissions) || event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            if (permName != null) {
                event.getChannel().sendMessage("Monkey Bot needs the \"" + permName + "\" permission to perform this action.");
            }
            return false;
        }
    }

}
