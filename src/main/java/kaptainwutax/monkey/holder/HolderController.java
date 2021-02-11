package kaptainwutax.monkey.holder;

import com.google.gson.annotations.Expose;
import kaptainwutax.monkey.MonkeyBot;
import kaptainwutax.monkey.command.CommandMod;
import kaptainwutax.monkey.init.Guilds;
import kaptainwutax.monkey.utility.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.*;

public class HolderController {

    public static String[] BAN_MESSAGES = new String[] {
            "user went off with a bang!",
            "user tried to outrun a train, the train won.",
            "user disappeared from the universe.",
            "user died from a creeper explosion.",
            "user disrespected the tall kaktoos."
    };

    private static final String[] YUN_DEFENSE_TRIGGERS = {"im ", "i'm ", "i am ", "i’m ", "i‘m ", "i`m "};
    private static final String[] PUNCTUATION = {".", ",", "!", "?", ":", ";"};

    public HolderGuild server;
    @Expose private String serverId;
    @Expose @Nullable public String moderationChannel = null;
    @Expose public boolean autoban = false;
    @Expose public boolean banMessage = true;
    @Expose public boolean sendAlert = true;
    @Expose public boolean funCommands = false;
    @Expose public boolean sayCommand = true;
    @Expose public boolean yunDefense = true;

    @Expose private MessageLimiter noobLimit = new MessageLimiter(-1, -1, -1,-1);
    @Expose private List<RoleMap> roleLimits = new ArrayList<RoleMap>();

    @Expose @Nullable public String muteRoleId;
    @Expose public boolean autoManageMuteRole = false;
    // Users which had the mute role but left the discord
    @Expose public Set<String> leftMutedMembers = new HashSet<>(0);

    // Set of untrusted servers
    @Expose public Set<String> serverBlacklist = new HashSet<>(0);

    private transient PingInfo everyonePingInfo = new PingInfo();
    private transient PingInfo herePingInfo = new PingInfo();
    private transient PingInfo rolePingInfo = new PingInfo();
    private transient PingInfo userPingInfo = new PingInfo();

    // for gson
    private HolderController() {}

    public HolderController(HolderGuild server) {
        this.server = server;
        this.serverId = this.server.id;
    }

    public void onChannelCreate(Guild guild, GuildChannel channel) {
        if (!autoManageMuteRole || muteRoleId == null) return;
        Role muteRole = guild.getRoleById(muteRoleId);
        if (muteRole == null) return;

        if (this.moderationChannel == null) return;
        TextChannel moderationChannel = guild.getTextChannelById(this.moderationChannel);

        CommandMod.addMuteRoleToChannel(channel, muteRole, error -> {
            if (moderationChannel != null)
                moderationChannel.sendMessage(error).queue();
        });

        if (moderationChannel == null) return;
        moderationChannel.sendMessage("Added mute role overrides to new channel " + channel.getName()).queue();
    }

    public void onMemberJoin(GuildMemberJoinEvent event) {
        if (autoManageMuteRole && muteRoleId != null && leftMutedMembers.remove(event.getMember().getId())) {
            Role muteRole = event.getGuild().getRoleById(muteRoleId);
            if (muteRole != null) {
                event.getGuild().addRoleToMember(event.getMember(), muteRole).queue(n -> {
                    if (moderationChannel != null) {
                        TextChannel moderationChannel = event.getGuild().getTextChannelById(this.moderationChannel);
                        if (moderationChannel != null) {
                            moderationChannel.sendMessage("Given back " + muteRole.getName() + " role to user " + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + " as they had the role when they last left.").queue();
                        }
                    }
                });
            }
        }
    }

    public void onMemberLeave(GuildMemberLeaveEvent event) {
        if (autoManageMuteRole && muteRoleId != null) {
            Role muteRole = event.getGuild().getRoleById(muteRoleId);
            if (muteRole != null) {
                if (event.getMember().getRoles().contains(muteRole)) {
                    leftMutedMembers.add(event.getMember().getId());
                }
            }
        }
    }

    public void setRole(Role role, int[] limits) {
        if(role == null) {
            this.noobLimit = new MessageLimiter(limits);
            return;
        }

        for(RoleMap roleMap: this.roleLimits) {
            if(roleMap.getId() == role.getIdLong()) {
                roleMap.setValue(new MessageLimiter(limits));
                return;
            }
        }

        this.roleLimits.add(new RoleMap(role.getIdLong()).setValue(new MessageLimiter(limits)));
    }

    public void sanitize(MessageReceivedEvent event) {
        if (this.moderationChannel == null) return;
        assert event.getMember() != null;

        // Yun Defense
        if (this.yunDefense && event.getAuthor().getIdLong() == 389507745113440291L) { // Yun's ID
            String message = event.getMessage().getContentDisplay();
            String lowercaseMessage = message.toLowerCase(Locale.ENGLISH);

            int lastOccurrence = -1;
            String trigger = "";

            for(String yunDefenseTrigger: YUN_DEFENSE_TRIGGERS) {
                int occurrence = lowercaseMessage.lastIndexOf(yunDefenseTrigger);
                if (occurrence == -1 || occurrence >= message.length()) continue;
                if (occurrence > lastOccurrence) {
                    lastOccurrence = occurrence;
                    trigger = yunDefenseTrigger;
                }
            }

            if(lastOccurrence < 0)return;

            String rest = "";

            for(int i = lastOccurrence + trigger.length(); i < message.length(); i++) {
                String c = message.charAt(i) + "";

                if(Arrays.stream(PUNCTUATION).noneMatch(s -> s.equals(c))) {
                    rest += c;
                } else {
                    break;
                }
            }

            if(!rest.isEmpty()) {
                String finalRest = rest;
                event.getChannel().sendMessage("Hi Yun, I'm Monkey!")
                        .queue(m -> m.editMessage("Hi " + finalRest + ", I'm Monkey!").queue());
            }
        }

        if (!this.isConsideredSpam(event)) return;

        if (MonkeyBot.instance().config.simulateBans) {
            event.getChannel().sendMessage("If I were real monkey bot, I would have banned you O_o").queue();
            return;
        }

        this.attemptBan(event, false);
        if(!this.sendAlert)return;

        for(HolderGuild s: Guilds.instance().servers) {
            if(!s.equals(this.getServer()) && s.controller.moderationChannel != null && s.controller.sendAlert) {
                TextChannel moderationChannel = s.getGuild().getTextChannelById(StrUtils.getChannelId(s.controller.moderationChannel));
                if (moderationChannel != null) {
                    Log.print(moderationChannel, "Spam alert from **" + event.getGuild().getName() + "**. User <@" + event.getAuthor().getIdLong() + "> has been spamming pings.");

                    if (s.controller.autoban) {
                        Log.print(moderationChannel, "Banned <@" + event.getAuthor().getIdLong() + ">, please double check to make sure it wasn't a mistake.");
                        s.getGuild().ban(event.getMember(), 0, "Automatic ping ban from " + event.getGuild().getName() + ".").queue(ban -> {
                            MonkeyBot.instance().config.getOrCreateUser(event.getMember().getIdLong()).autobannedServers.add(s.getGuild().getId());
                        }, t -> {
                        });
                    }
                }
            }
        }
    }

    private void attemptBan(MessageReceivedEvent event, boolean force) {
        TextChannel moderationChannel = this.getServer().getGuild().getTextChannelById(StrUtils.getChannelId(this.moderationChannel));
        if (moderationChannel == null) return;
        assert event.getMember() != null;
        Log.print(moderationChannel, "Member <@" + event.getAuthor().getIdLong() + "> is spamming pings in " + StrUtils.getChannelIdAsMessage(event.getChannel().getId()) + ".");
        if(!force && !this.autoban)return;
        if(this.banMessage)Log.print(event.getTextChannel(), BAN_MESSAGES[new Random().nextInt(BAN_MESSAGES.length)].replaceFirst("user", "<@" + event.getMember().getId() + ">"));
        Log.print(moderationChannel, "Banned <@" + event.getAuthor().getIdLong() + ">, please double check to make sure it wasn't a mistake.");
        this.getServer().getGuild().ban(event.getMember(), 0, "Automatic ping ban.").queue(ban -> {
            MonkeyBot.instance().config.getOrCreateUser(event.getMember().getIdLong()).autobannedServers.add(getServer().getGuild().getId());
        });
    }

    public boolean isConsideredSpam(MessageReceivedEvent event) {
        assert event.getMember() != null;
        MessageLimiter messageMentions = new MessageLimiter(this.getMentions(event));
        return !messageMentions.respectsLimits(this.getLimit(event.getMember()));
    }

    public MessageLimiter getLimit(Member member) {
        List<Role> roles = member.getRoles();

        if(roles.isEmpty()) {
            return this.noobLimit;
        } else {
            Role role = roles.get(0);

            for(RoleMap roleMap: roleLimits) {
                if(roleMap.id == role.getIdLong())return roleMap.getValue();
            }

            RoleMap roleMap1 = new RoleMap(role.getIdLong());
            roleLimits.add(roleMap1);
            return roleMap1.getValue();
        }
    }

    private int[] getMentions(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentDisplay();
        messageContent = removeHiddenText(messageContent, "```");
        messageContent = removeHiddenText(messageContent, "`");

        UserPingInfo everyonePings = everyonePingInfo.get(event.getGuild().getId(), event.getAuthor().getId());
        if (findSimplePing(messageContent, "@everyone") > 0)
            everyonePings.addPing(event.getMessageId());
        UserPingInfo herePings = herePingInfo.get(event.getGuild().getId(), event.getAuthor().getId());
        if (findSimplePing(messageContent, "@here") > 0)
            herePings.addPing(event.getMessageId());
        UserPingInfo rolePings = rolePingInfo.get(event.getGuild().getId(), event.getAuthor().getId());
        for (Role role : event.getMessage().getMentionedRoles())
            rolePings.addPing(role.getId());
        UserPingInfo userPings = userPingInfo.get(event.getGuild().getId(), event.getAuthor().getId());
        for (Member member : event.getMessage().getMentionedMembers())
            userPings.addPing(member.getId());

        everyonePings.purge();
        int everyone = everyonePings.getPingCount();
        herePings.purge();
        int here = herePings.getPingCount();
        rolePings.purge();
        int role = rolePings.getPingCount();
        userPings.purge();
        int user = userPings.getPingCount();

        return new int[] {everyone, here, role, user};
    }

    private String removeHiddenText(String s, String brackets) {
        String finalText = s.trim();
        s = s.trim();

        int start = -1;

        for(int i = 0; i < s.length(); i++) {
            StringBuilder c = new StringBuilder();

            for(int j = i; j < s.length() && j < i + brackets.length(); j++) {
                c.append(s.charAt(j));
            }

            if(c.toString().equals(brackets)) {
                if(start == -1) {
                    start = i;
                } else {
                    finalText = finalText.replace(s.substring(start, i + brackets.length()), "");
                    start = -1;
                }
            }
        }

        return finalText;
    }

    private int findSimplePing(String rawText, String ping) {
        int count = 0;

        while(rawText.contains(ping)) {
            rawText = StrUtils.removeFirst(rawText, ping);
            count++;
        }

        return count;
    }

    private HolderGuild getServer() {
        if(this.server == null) {
            for(HolderGuild server: Guilds.instance().servers) {
                if(server.id.equals(this.serverId)) {
                    this.server = server;
                    break;
                }
            }
        }

        return this.server;
    }

    private static class RoleMap {

        @Expose long id;
        @Expose MessageLimiter limiter = new MessageLimiter(-1, -1, -1, -1);

        public RoleMap(long id) {
            this.id = id;
        }

        public long getId() {
            return this.id;
        }

        public RoleMap setValue(MessageLimiter limiter) {
            this.limiter = limiter;
            return this;
        }

        public MessageLimiter getValue() {
            return this.limiter;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj.getClass() != HolderController.class) return false;
            RoleMap target = ((RoleMap) obj);
            return target.getId() == this.id;
        }
    }

}
