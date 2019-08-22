package kaptainwutax.monkey.command;

import kaptainwutax.monkey.MonkeyBot;
import kaptainwutax.monkey.holder.HolderGuild;
import kaptainwutax.monkey.holder.UserInfo;
import kaptainwutax.monkey.init.Commands;
import kaptainwutax.monkey.init.Guilds;
import kaptainwutax.monkey.utility.Log;
import kaptainwutax.monkey.utility.StrUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CommandMod extends Command {

    public CommandMod(String[] prefix) {
        super(prefix);
    }

    @Override
    public void processCommand(MessageReceivedEvent message, String rawCommand) {
        rawCommand = this.removePrefix(rawCommand);

        if(!message.getMember().hasPermission(Permission.ADMINISTRATOR))return;

        if(rawCommand.startsWith("setChannel")) {
            this.setChannel(message, StrUtils.removeFirstTrim(rawCommand, "setChannel"));
        } else if(rawCommand.startsWith("autoban")) {
            this.setAutoban(message, StrUtils.removeFirstTrim(rawCommand, "autoban"));
        } else if(rawCommand.startsWith("banMessage")) {
            this.setBanMessage(message, StrUtils.removeFirstTrim(rawCommand, "banMessage"));
        } else if(rawCommand.startsWith("sendAlert")) {
            this.setSendAlert(message, StrUtils.removeFirstTrim(rawCommand, "sendAlert"));
        } else if(rawCommand.startsWith("funCommands")) {
            this.setFunCommands(message, StrUtils.removeFirstTrim(rawCommand, "funCommands"));
        } else if(rawCommand.startsWith("limit")) {
            this.setLimit(message, StrUtils.removeFirstTrim(rawCommand, "limit"));
        } else if (rawCommand.startsWith("yunDefense")) {
            this.setYunDefense(message, StrUtils.removeFirstTrim(rawCommand, "yunDefense"));
        } else if (rawCommand.startsWith("unautoban")) {
            this.unautoban(message, StrUtils.removeFirstTrim(rawCommand, "unautoban"));
        }
    }

    private void setChannel(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        String channelId = StrUtils.getChannelId(params);
        TextChannel channel = message.getGuild().getTextChannelById(StrUtils.getChannelId(channelId));

        if(channel == null) {
            Log.print(message.getTextChannel(), "There was an error accessing this channel.");
        } else {
            Log.print(channel, "This channel was successfully set for moderation.");
            server.controller.moderationChannel = channelId;
        }
    }

    private void setAutoban(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        params = params.toLowerCase();

        if(params.equals("false")) {
            server.controller.autoban = false;
            Log.print(moderationChannel, "Autoban has been updated to false.");
        } else if(params.equals("true")) {
            server.controller.autoban = true;
            Log.print(moderationChannel, "Autoban has been updated to true.");
        } else {
            Log.print(moderationChannel, "Unknown argument \"" + params + "\".");
        }
    }

    private void setBanMessage(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        params = params.toLowerCase();

        if(params.equals("false")) {
            server.controller.banMessage = false;
            Log.print(moderationChannel, "Public ban messages will not be displayed.");
        } else if(params.equals("true")) {
            server.controller.banMessage = true;
            Log.print(moderationChannel, "Public ban messages will be displayed.");
        } else {
            Log.print(moderationChannel, "Unknown argument \"" + params + "\".");
        }
    }

    private void setSendAlert(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        params = params.toLowerCase();

        if(params.equals("false")) {
            server.controller.sendAlert = false;
            Log.print(moderationChannel, "This server won't be sending nor getting spam alerts from other discords.");
        } else if(params.equals("true")) {
            server.controller.sendAlert = true;
            Log.print(moderationChannel, "This server will be sending and getting spam alerts from other discords.");
        } else {
            Log.print(moderationChannel, "Unknown argument \"" + params + "\".");
        }
    }

    private void setFunCommands(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        params = params.toLowerCase();

        if(params.equals("false")) {
            server.controller.funCommands = false;
            Log.print(moderationChannel, "Disabled fun commands.");
        } else if(params.equals("true")) {
            server.controller.funCommands = true;
            Log.print(moderationChannel, "Enabled fun commands.");
        } else {
            Log.print(moderationChannel, "Unknown argument \"" + params + "\".");
        }
    }

    private void setLimit(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));

        List<String> rawLimits = Arrays.asList(params.split(" "));

        String roleId = null;
        int[] limits;

        if(rawLimits.size() == 4) {
            try {
                limits = rawLimits.stream().mapToInt(Integer::parseInt).toArray();
            } catch(Exception e) {
                Log.print(message.getTextChannel(), "Ping limit must be a valid integer.");
                return;
            }

            server.controller.setRole(null, limits);
        } else if(rawLimits.size() == 5) {
            roleId = rawLimits.get(0);

            try {
                limits = rawLimits.subList(1, rawLimits.size()).stream().mapToInt(Integer::parseInt).toArray();
            } catch(Exception e) {
                Log.print(message.getTextChannel(), "Ping limit must be a valid integer.");
                return;
            }

            Role role = server.getGuild().getRoleById(roleId.replaceFirst("<@&", "").replaceFirst(">", ""));

            if(role == null) {
                Log.print(message.getTextChannel(), "The role you specified is invalid.");
                return;
            }

            server.controller.setRole(role, limits);
        } else {
            Log.print(message.getTextChannel(), "Invalid command arguments.");
            return;
        }

        Log.print(moderationChannel, "Updated role limits successfully.");
    }

    private void setYunDefense(MessageReceivedEvent message, String params) {
        HolderGuild server = Guilds.instance().getOrCreateServer(new HolderGuild(message.getGuild()));

        if(server.controller.moderationChannel == null || server.controller.moderationChannel.isEmpty()) {
            Log.print(message.getTextChannel(), "This command requires a moderation channel. Use [monkey mod setChannel <#channel>].");
        }

        TextChannel moderationChannel = message.getGuild().getTextChannelById(StrUtils.getChannelId(server.controller.moderationChannel));
        params = params.toLowerCase();

        if(params.equals("false")) {
            server.controller.yunDefense = false;
            Log.print(moderationChannel, "Disabled Yun defense.");
        } else if(params.equals("true")) {
            server.controller.yunDefense = true;
            Log.print(moderationChannel, "Enabled Yun defense.");
        } else {
            Log.print(moderationChannel, "Unknown argument \"" + params + "\".");
        }
    }

    private void unautoban(MessageReceivedEvent message, String params) {
        final Set<String> GUILD_WHITELIST = new HashSet<>();
        GUILD_WHITELIST.add("468095753701818389"); // Earth's Place
        GUILD_WHITELIST.add("505310901461581824"); // Monkeys
        GUILD_WHITELIST.add("292684566966304768"); // TMC
        GUILD_WHITELIST.add("211786369951989762"); // SciCraft
        GUILD_WHITELIST.add("169373832510046208"); // ProtoTech
        GUILD_WHITELIST.add("403047405877985281"); // TechRock
        GUILD_WHITELIST.add("511912185388204034"); // TIS Trinity Union
        GUILD_WHITELIST.add("429700535206281217"); // PulseFiction
        GUILD_WHITELIST.add("309499810166210588"); // EigenCraft
        GUILD_WHITELIST.add("279782390958784512"); // FrostCraft
        if (!GUILD_WHITELIST.contains(message.getGuild().getId())) {
            message.getChannel().sendMessage("This server is not on the whitelist to use this command. Contact a Monkey bot dev if you think this is an error.").queue();
            return;
        }

        String[] args = params.split(" ");
        long userId;
        try {
            userId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            message.getChannel().sendMessage("Invalid user ID").queue();
            return;
        }

        UserInfo user = MonkeyBot.instance().config.getOrCreateUser(userId);
        if (user.autobannedServers.isEmpty()) {
            message.getChannel().sendMessage("That user wasn't autobanned by Monkey").queue();
            return;
        }
        AtomicBoolean hasApologized = new AtomicBoolean(false);

        String reason = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        for (String serverId : user.autobannedServers) {
            Guild guild = MonkeyBot.instance().jda.getGuildById(serverId);
            if (guild == null) continue;

            guild.retrieveBanById(user.id).queue(ban -> {
                guild.getController().unban(ban.getUser()).queue();

                if (!hasApologized.getAndSet(true)) {
                    ban.getUser().openPrivateChannel().queue(dms -> {
                        dms.sendMessage("We realize our mistake, and have un-autobanned you from " + user.autobannedServers.size() + " servers. Sorry about that!").queue();
                    }, t -> {});
                }

                HolderGuild holder = Guilds.instance().getOrCreateServer(new HolderGuild(guild));
                if (holder.controller.moderationChannel != null && !holder.controller.moderationChannel.isEmpty()) {
                    TextChannel moderationChannel = guild.getTextChannelById(holder.controller.moderationChannel);
                    if (moderationChannel != null) {
                        moderationChannel.sendMessage(
                                String.format("User %s#%s was un-autobanned by %s in %s%s",
                                        ban.getUser().getName(), ban.getUser().getDiscriminator(),
                                        message.getAuthor().getName(),
                                        message.getGuild().getName(),
                                        reason == null ? "" : (". Reason: " + reason)))
                                .queue();
                    }
                }
            }, t -> {});
        }

        user.autobannedServers.clear();
    }

    @Override
    public String[] getCommandDesc() {
        return new String[] {
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "setChannel <#channel> ` : Sets the moderation channel. Will be used to print logs.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "autoban <flag> ` : Should the user be automatically banned if he spams? Note that you will always be notified in the moderation channel if a user spams.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "banMessage <flag> ` : If on, this will print a custom ban message in the target channel whenever someone gets banned from spamming.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "sendAlert <flag> ` : If on, spam alerts will be sent to and from other discords.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "funCommands <flag> ` : If on, general fun commands will be enabled. Note that some of them are trollish and may cause issues.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "limit <everyone> <here> <role> <user> ` : Sets the limit of everyone, here, role and user pings for users without a role.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "limit <@&role> <everyone> <here> <role> <user> ` : Sets the limit of everyone, here, role and user pings for the specified role.",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "yunDefense <flag> ` : If on, I'm jokes will be made against Yun. BRING HIM DOWN!",
                "`" + Commands.MONKEY.getPrefixDesc() + this.getPrefixDesc() + "unautoban <user-id> [reason] `: Reverse a Monkey autoban. Can only unban users banned via a Monkey autoban. Only works on a whitelist of discord guilds - ask a Monkey bot dev to add your guild.",
        };
    }

}
