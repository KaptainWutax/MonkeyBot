package kaptainwutax.monkey.init;

import kaptainwutax.monkey.holder.HolderChannel;
import kaptainwutax.monkey.holder.HolderGuild;

public class Channels {

    public static boolean isChannelInSummary(HolderGuild server, String channelId) {
        if(server == null) return false;

        for(HolderChannel channel : server.channels) {
            if(channel.getIdAsMessage().equals(channelId)) return true;
        }

        return false;
    }

}
