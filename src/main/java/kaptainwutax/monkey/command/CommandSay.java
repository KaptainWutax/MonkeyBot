package kaptainwutax.monkey.command;

import com.mojang.brigadier.CommandDispatcher;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static kaptainwutax.monkey.init.Commands.*;

public class CommandSay {

    public static void register(CommandDispatcher<MessageCommandSource> dispatcher) {
        dispatcher.register(literal("say", "Make me say something nice.")
                .requires(MessageCommandSource::canUseFunCommands)
                .requires(MessageCommandSource::canUseSayCommand)
                .then(argument("message", greedyString())
                        .executes(ctx -> say(ctx.getSource(), getString(ctx, "message")))));
    }

    private static int say(MessageCommandSource source, String message) {
        source.getChannel().sendMessage("stop abusing me").queue(placeholder -> placeholder.editMessage(message).queue());
        return message.length();
    }

}
