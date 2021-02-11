package kaptainwutax.monkey.command;

import com.mojang.brigadier.CommandDispatcher;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import static kaptainwutax.monkey.init.Commands.*;

public class CommandPing {

    public static void register(CommandDispatcher<MessageCommandSource> dispatcher) {
        for (String command : new String[] {"ping", "\uD83C\uDFD3"})
            dispatcher.register(literal(command, "Ping pong...")
                .requires(MessageCommandSource::canUseFunCommands)
                .executes(ctx -> ping(ctx.getSource())));
    }

    private static int ping(MessageCommandSource source) {
        source.getChannel().sendMessage(String.format("Pong! (%sms,%sms)", source.getEvent().getJDA().getRestPing().complete(),source.getEvent().getJDA().getGatewayPing())).queue();
        return 0;
    }

}
