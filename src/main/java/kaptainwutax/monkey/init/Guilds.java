package kaptainwutax.monkey.init;

import kaptainwutax.monkey.MonkeyBot;
import kaptainwutax.monkey.holder.HolderGuild;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Guilds {

    public List<HolderGuild> servers = new ArrayList<HolderGuild>();

    public static Guilds instance() {
        return MonkeyBot.instance().config.guilds;
    }

    public HolderGuild getOrCreateServer(HolderGuild server) {
        if(!this.isServerRegistered(server)) {
            this.servers.add(server);
            return server;
        }

        return this.getServerFromId(server.getId());
    }

    public void unregisterServer(HolderGuild server) {
        if(this.isServerRegistered(server)) {
            this.servers.remove(server);
        }
    }

    public boolean isServerRegistered(HolderGuild server) {
        for(HolderGuild s : this.servers) {
            if(server.equals(s)) return true;
        }

        return false;
    }

    @Nullable
    public HolderGuild getServerFromId(String guildId) {
        for(HolderGuild server : this.servers) {
            if(server.getId().equals(guildId)) {
                return server;
            }
        }

        return null;
    }

    @Nullable
    public HolderGuild getServerFromGuild(Guild guild) {
        return this.getServerFromId(guild.getId());
    }

}
