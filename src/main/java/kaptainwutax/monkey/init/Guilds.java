package kaptainwutax.monkey.init;

import com.google.gson.annotations.Expose;
import kaptainwutax.monkey.MonkeyBot;
import kaptainwutax.monkey.holder.HolderGuild;
import kaptainwutax.monkey.utility.Log;
import kaptainwutax.monkey.utility.MessageLimiter;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

public class Guilds {

    @Expose
    public List<HolderGuild> servers = new ArrayList<HolderGuild>();

    public static Guilds instance() {
        return MonkeyBot.instance().config.guilds;
    }

    public HolderGuild getOrCreateServer(HolderGuild server) {
        System.out.println("Old size " + servers.size());
        if(!this.isServerRegistered(server)) {
            this.servers.add(server);
            return server;
        }
        System.out.println("New size " + servers.size());
        return this.getServerFromId(server.getId());
    }

    public void unregisterServer(HolderGuild server) {
        if(this.isServerRegistered(server)) {
            this.servers.remove(server);
        }
    }

    public boolean isServerRegistered(HolderGuild server) {
        for(HolderGuild s : this.servers) {
            if(server.equals(s))return true;
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
