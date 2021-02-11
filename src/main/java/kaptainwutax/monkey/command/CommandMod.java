package kaptainwutax.monkey.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import kaptainwutax.monkey.MonkeyBot;
import kaptainwutax.monkey.holder.HolderGuild;
import kaptainwutax.monkey.holder.UserInfo;
import kaptainwutax.monkey.init.Guilds;
import kaptainwutax.monkey.utility.Log;
import kaptainwutax.monkey.utility.StrUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static kaptainwutax.monkey.command.arguments.ChannelArgumentType.*;
import static kaptainwutax.monkey.command.arguments.GuildArgumentType.*;
import static kaptainwutax.monkey.command.arguments.RoleArgumentType.*;
import static kaptainwutax.monkey.command.arguments.UserArgumentType.*;
import static kaptainwutax.monkey.init.Commands.*;

public class CommandMod {

    private static final SimpleCommandExceptionType CANNOT_PUNISH_BOTS_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Cannot punish bots"));
    private static final SimpleCommandExceptionType CANNOT_BLACKLIST_SELF_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Cannot blacklist your own server"));

    public static void register(CommandDispatcher<MessageCommandSource> dispatcher) {
        dispatcher.register(literal("mod", "Moderation tools for Monkey Bot.")
                .requires(MessageCommandSource::isAdministrator)
                .then(literal("setChannel", "Sets the moderation channel. Will be used to print logs.")
                        .then(argument("channel", channel())
                                .executes(ctx -> setChannel(ctx.getSource(), getChannel(ctx, "channel")))))
                .then(literal("autoban", "Should the user be automatically banned if he spams? Note that you will always be notified in the moderation channel if a user spams.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setAutoban(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("banMessage", "If on, this will print a custom ban message in the target channel whenever someone gets banned from spamming.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setBanMessage(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("sendAlert", "If on, spam alerts will be sent to and from other discords.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setSendAlert(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("funCommands", "If on, general fun commands will be enabled. Note that some of them are trollish and may cause issues.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setFunCommands(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("disableSay", "By default the say command is on but if you don't like it, just disable it with that setting.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setSayCommand(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("limit", "Sets the limit of everyone, here, role and user pings for users with the specified role. If the role is unspecified, this applies to users without a role.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("everyoneLimit", integer(-1))
                                .then(argument("hereLimit", integer(-1))
                                        .then(argument("roleLimit", integer(-1))
                                                .then(argument("userLimit", integer(-1))
                                                        .executes(ctx -> setLimits(ctx.getSource(), null, getInteger(ctx, "everyoneLimit"), getInteger(ctx, "hereLimit"), getInteger(ctx, "roleLimit"), getInteger(ctx, "userLimit")))
                                                        .then(argument("role", role())
                                                                .executes(ctx -> setLimits(ctx.getSource(), getRole(ctx, "role"), getInteger(ctx, "everyoneLimit"), getInteger(ctx, "hereLimit"), getInteger(ctx, "roleLimit"), getInteger(ctx, "userLimit")))))))))
                .then(literal("yunDefense", "If on, I'm jokes will be made against Yun. BRING HIM DOWN!")
                        .requires(CommandMod::hasModerationChannel)
                        .then(argument("value", bool())
                                .executes(ctx -> setYunDefense(ctx.getSource(), getBool(ctx, "value")))))
                .then(literal("unautoban", "Reverse a Monkey autoban. Can only unban users banned via a Monkey autoban.")
                        .requires(source -> MonkeyBot.instance().config.botAdmins.contains(source.getUser().getId()))
                        .then(argument("userId", longArg())
                                .executes(ctx -> unautoban(ctx.getSource(), getLong(ctx, "userId"), null))
                                .then(argument("reason", greedyString())
                                        .executes(ctx -> unautoban(ctx.getSource(), getLong(ctx, "userId"), getString(ctx, "reason"))))))
                .then(literal("mute", "Commands to handle the mute role.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(literal("setRole", "Sets the mute role to an existing role on the server.")
                                .then(argument("value", role())
                                        .executes(ctx -> setMuteRole(ctx.getSource(), getRole(ctx, "value")))))
                        .then(literal("unsetRole", "Sets the server to have no mute role (or at least, none Monkey knows about).")
                                .executes(ctx -> unsetMuteRole(ctx.getSource())))
                        .then(literal("automanage", "Sets whether Monkey should auto-manage the mute role.")
                                .then(argument("value", bool())
                                        .executes(ctx -> setAutomanageMuteRole(ctx.getSource(), getBool(ctx, "value")))))
                        .then(literal("setupRole", "Creates a mute role and sets it up to do its job. The mute role will be auto-managed unless configured otherwise.")
                                .executes(ctx -> setupMuteRole(ctx.getSource()))))
                .then(literal("globalPunish", "Manually globally punish a user. The user will be banned on the discord you run the command, and given a mute role on all discords which support it, subject to the following conditions:\n1. There is an admin of that discord which is also in the discord you run the command.\n2. The discord in which you run the command is not on that discord's blacklist.\n3. That discord has a moderation channel.\nYour name and guild will be broadcast to all affected guilds.")
                        .then(argument("victim", user())
                                .executes(ctx -> globalPunish(ctx.getSource(), getUser(ctx, "victim"), null))
                                .then(argument("reason", greedyString())
                                        .executes(ctx -> globalPunish(ctx.getSource(), getUser(ctx, "victim"), getString(ctx, "reason"))))))
                .then(literal("serverBlacklist", "Commands to manage the server blacklist, which restricts global punishments from the discords on the blacklist from affecting your discord.")
                        .requires(CommandMod::hasModerationChannel)
                        .then(literal("add", "Adds a server to your server blacklist.")
                                .then(argument("guild", greedyGuild())
                                        .executes(ctx -> addServerToBlacklist(ctx.getSource(), getGuild(ctx, "guild")))))
                        .then(literal("remove", "Removes a server from your server blacklist.")
                                .then(argument("guild", greedyGuild())
                                        .executes(ctx -> removeServerFromBlacklist(ctx.getSource(), getGuild(ctx, "guild")))))));
    }

    private static boolean hasModerationChannel(MessageCommandSource source) {
        if (source.isDMs())
            return false;
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));
        return server.controller.moderationChannel != null && !server.controller.moderationChannel.isEmpty() && source.getGuild().getTextChannelById(server.controller.moderationChannel) != null;
    }

    private static void sendModerationFeedback(MessageCommandSource source, String message) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        TextChannel moderationChannel = server.controller.moderationChannel == null ? null : source.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));

        source.getChannel().sendMessage(message).queue();
        if (moderationChannel != null && moderationChannel.getIdLong() != source.getChannel().getIdLong())
            moderationChannel.sendMessage(message).queue();
    }

    private static int setChannel(MessageCommandSource source, MessageChannel channel) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.moderationChannel = channel.getId();
        if (source.getChannel().getIdLong() != channel.getIdLong())
            source.getChannel().sendMessage("That channel was successfully set for moderation.").queue();
        channel.sendMessage("This channel was successfully set for moderation.").queue();

        return 0;
    }

    private static int setAutoban(MessageCommandSource source, boolean value) {
        if (!source.checkBotPerms("Ban Members", Permission.BAN_MEMBERS))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.autoban = value;
        sendModerationFeedback(source, "Autoban has been updated to " + value);

        return 0;
    }

    private static int setBanMessage(MessageCommandSource source, boolean value) {
        if (!source.checkBotPerms("Ban Members", Permission.BAN_MEMBERS))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.banMessage = value;
        sendModerationFeedback(source, value ? "Public ban messages will be displayed." : "Public ban messages will not be displayed.");

        return 0;
    }

    private static int setSendAlert(MessageCommandSource source, boolean value) {
        if (!source.checkBotPerms("Ban Members", Permission.BAN_MEMBERS))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.sendAlert = value;
        if (value)
            sendModerationFeedback(source, "This server will be sending and receiving spam alerts from other discords.");
        else
            sendModerationFeedback(source, "This server won't be sending nor receiving spam alerts from other discords.");

        return 0;
    }

    private static int setFunCommands(MessageCommandSource source, boolean value) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.funCommands = value;
        sendModerationFeedback(source, value ? "Enabled fun commands." : "Disabled fun commands.");

        return 0;
    }

    private static int setSayCommand(MessageCommandSource source, boolean value) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.sayCommand = !value;
        sendModerationFeedback(source, value ? "Disabled say command." : "Enabled say command.");

        return 0;
    }

    private static int setLimits(MessageCommandSource source, @Nullable Role role, int everyoneLimit, int hereLimit, int roleLimit, int userLimit) {
        if (!source.checkBotPerms("Ban Members", Permission.BAN_MEMBERS))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.setRole(role, new int[] {everyoneLimit, hereLimit, roleLimit, userLimit});
        sendModerationFeedback(source, "Updated role limits successfully.");

        return 0;
    }

    private static int setYunDefense(MessageCommandSource source, boolean value) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.yunDefense = value;
        sendModerationFeedback(source, value ? "Enabled Yun defense." : "Disabled Yun defense.");

        return 0;
    }

    private static int unautoban(MessageCommandSource source, long userId, @Nullable String reason) {
        UserInfo user = MonkeyBot.instance().config.getOrCreateUser(userId);
        if (user.autobannedServers.isEmpty()) {
            source.getChannel().sendMessage("That user wasn't autobanned by Monkey").queue();
            return 0;
        }

        AtomicBoolean hasApologized = new AtomicBoolean(false);

        for (String serverId : user.autobannedServers) {
            Guild guild = MonkeyBot.instance().jda.getGuildById(serverId);
            if (guild == null) continue;

            guild.retrieveBanById(user.id).queue(ban -> {
                guild.unban(ban.getUser()).queue();

                if (!hasApologized.getAndSet(true)) {
                    ban.getUser().openPrivateChannel().queue(dms -> {
                        dms.sendMessage("We realize our mistake, and have un-autobanned you from " + user.autobannedServers.size() + " servers. Sorry about that!").queue();
                    }, t -> {});
                }

                HolderGuild holder = Guilds.instance().getOrCreateServer(new HolderGuild(guild));
                if (holder.controller.moderationChannel != null && !holder.controller.moderationChannel.isEmpty()) {
                    TextChannel moderationChannel = guild.getTextChannelById(holder.controller.moderationChannel);
                    if (moderationChannel != null) {
                        assert source.getGuild() != null;
                        moderationChannel.sendMessage(
                                String.format("User %s#%s was un-autobanned by %s in %s%s",
                                        ban.getUser().getName(), ban.getUser().getDiscriminator(),
                                        source.getUser().getName(),
                                        source.getGuild().getName(),
                                        reason == null ? "" : (". Reason: " + reason)))
                                .queue();
                    }
                }
            }, t -> {});
        }

        int count = user.autobannedServers.size();
        user.autobannedServers.clear();
        return count;
    }

    private static int setMuteRole(MessageCommandSource source, Role role) {
        if (!source.checkBotPerms("Manage Roles", Permission.MANAGE_ROLES))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.muteRoleId = role.getId();
        sendModerationFeedback(source, "Mute role has been updated to " + role.getName() + ".");

        return 0;
    }

    private static int unsetMuteRole(MessageCommandSource source) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.muteRoleId = null;
        sendModerationFeedback(source, "Unset the mute role.");

        return 0;
    }

    private static int setAutomanageMuteRole(MessageCommandSource source, boolean value) {
        if (!source.checkBotPerms("Manage Roles", Permission.MANAGE_ROLES))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.autoManageMuteRole = value;
        sendModerationFeedback(source, value ? "Mute role will be auto-managed." : "Mute role will not be auto-managed.");

        return 0;
    }

    private static int setupMuteRole(MessageCommandSource source) {
        if (!source.checkBotPerms("Manage Roles", Permission.MANAGE_ROLES))
            return 0;

        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        TextChannel moderationChannel = source.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        assert moderationChannel != null;

        sendModerationFeedback(source, "Setting up mute role...");

        source.getGuild().createRole().setName("Muted").setPermissions(Collections.emptyList()).queue(muteRole -> {
            server.controller.muteRoleId = muteRole.getId();
            for (GuildChannel channel : source.getGuild().getChannels()) {
                if (channel.getParent() == null || !channel.getPermissionOverrides().isEmpty())
                    addMuteRoleToChannel(channel, muteRole, error -> moderationChannel.sendMessage(error).queue());
            }

            server.controller.autoManageMuteRole = true;
            sendModerationFeedback(source, "Successfully set up the mute role.");
        });

        return 0;
    }

    public static void addMuteRoleToChannel(GuildChannel channel, Role muteRole, Consumer<String> errorFeedback) {
        try {
            if (channel.getType() == ChannelType.VOICE || channel.getType() == ChannelType.CATEGORY) {
                channel.upsertPermissionOverride(muteRole).deny(Permission.VOICE_CONNECT).queue();
            }
            if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.CATEGORY) {
                channel.upsertPermissionOverride(muteRole).deny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
            }
        } catch (InsufficientPermissionException e) {
            errorFeedback.accept("Insufficient permission " + e.getPermission() + " in channel " + channel.getName());
        }
    }

    private static int globalPunish(MessageCommandSource source, User victim, @Nullable String reason) throws CommandSyntaxException {
        assert source.getGuild() != null;

        if (victim.isBot())
            throw CANNOT_PUNISH_BOTS_EXCEPTION.create();

        // Ban the user in the discord the command was executed at (no checks needed)
        source.getGuild().ban(victim, 0, reason).queue();

        for (HolderGuild server : Guilds.instance().servers) {
            if (server.id.equals(source.getGuild().getId())) continue;

            // Check if monkey is still in the server
            Guild guild = MonkeyBot.instance().jda.getGuildById(server.id);
            if (guild == null) continue;

            // Check if that server has a mute role
            if (server.controller.muteRoleId == null) continue;
            Role muteRole = guild.getRoleById(server.controller.muteRoleId);
            if (muteRole == null) continue;

            // Check if an admin is in the source guild
            boolean isAdminInSourceGuild = false;
            for (Member member : guild.getMembers()) {
                if (member.hasPermission(Permission.ADMINISTRATOR) && !member.getUser().isBot()) {
                    if (source.getGuild().isMember(member.getUser())) {
                        isAdminInSourceGuild = true;
                        break;
                    }
                }
            }
            if (!isAdminInSourceGuild) continue;

            // Check that the source server isn't on this server's blacklist
            if (server.controller.serverBlacklist.contains(source.getGuild().getId())) continue;

            // Check if the server has a moderation channel
            if (server.controller.moderationChannel == null) continue;
            TextChannel moderationChannel = guild.getTextChannelById(server.controller.moderationChannel);
            if (moderationChannel == null) continue;

            // All the checks passed, apply the mute role
            try {
                if (guild.isMember(victim)) {
                    Member member = guild.getMember(victim);
                    assert member != null;
                    guild.addRoleToMember(member, muteRole).queue();
                } else {
                    if (server.controller.autoManageMuteRole)
                        server.controller.leftMutedMembers.add(victim.getId());
                }

                moderationChannel.sendMessage(String.format("User %s#%s (ID %d) was muted due to a global punishment by %s#%s (ID %d) on server %s (ID %d).%s",
                        victim.getName(), victim.getDiscriminator(), victim.getIdLong(),
                        source.getUser().getName(), source.getUser().getDiscriminator(), source.getUser().getIdLong(),
                        source.getGuild().getName(), source.getGuild().getIdLong(),
                        reason == null ? "" : (" Reason: " + reason))).queue();
            } catch (InsufficientPermissionException ignore) {
                // one guild having bad permissions should not prevent subsequent guilds from attempting to mute
            }
        }

        sendModerationFeedback(source, "Globally punished " + victim.getName() + "#" + victim.getDiscriminator());

        return 0;
    }

    private static int addServerToBlacklist(MessageCommandSource source, Guild guild) throws CommandSyntaxException {
        assert source.getGuild() != null;
        if (source.getGuild().equals(guild))
            throw CANNOT_BLACKLIST_SELF_EXCEPTION.create();
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.serverBlacklist.add(guild.getId());
        sendModerationFeedback(source, "Added server \"" + guild.getName() + "\" to the server blacklist.");

        return 0;
    }

    private static int removeServerFromBlacklist(MessageCommandSource source, Guild guild) {
        assert source.getGuild() != null;
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(source.getGuild()));

        server.controller.serverBlacklist.remove(guild.getId());
        sendModerationFeedback(source, "Removed server \"" + guild.getName() + "\" from the server blacklist.");

        return 0;
    }
}
