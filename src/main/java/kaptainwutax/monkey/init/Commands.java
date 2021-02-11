package kaptainwutax.monkey.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import kaptainwutax.monkey.command.*;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public class Commands {

    private static final Map<LiteralCommandNode<MessageCommandSource>, String> COMMAND_HELP = new IdentityHashMap<>();

    public static void registerCommands(CommandDispatcher<MessageCommandSource> dispatcher) {
        COMMAND_HELP.clear();
        CommandHelp.register(dispatcher);
        CommandPing.register(dispatcher);
        CommandSay.register(dispatcher);
        CommandCactus.register(dispatcher);
        CommandStronghold.register(dispatcher);
        CommandLcg.register(dispatcher);
        CommandSummary.register(dispatcher);
        CommandMod.register(dispatcher);
        CommandShutdown.register(dispatcher);
    }

    /**
     * Convenience method to help with generics
     */
    public static LiteralArgumentBuilder<MessageCommandSource> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public static LiteralArgumentBuilder<MessageCommandSource> literal(String literal, String help) {
        return new LiteralArgumentBuilder<MessageCommandSource>(literal) {
            @Override
            public LiteralCommandNode<MessageCommandSource> build() {
                LiteralCommandNode<MessageCommandSource> ret = super.build();
                COMMAND_HELP.put(ret, help);
                return ret;
            }
        };
    }

    /**
     * Convenience method to help with generics
     */
    public static <T> RequiredArgumentBuilder<MessageCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    @Nullable
    public static String getHelp(LiteralCommandNode<MessageCommandSource> commandNode) {
        return COMMAND_HELP.get(commandNode);
    }

}
