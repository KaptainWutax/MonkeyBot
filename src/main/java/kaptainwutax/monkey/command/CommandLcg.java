//
// Decompiled by Procyon v0.5.36
//

package kaptainwutax.monkey.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kaptainwutax.monkey.init.Commands;
import kaptainwutax.seedutils.lcg.LCG;

import static kaptainwutax.monkey.command.arguments.MultibaseLongArgumentType.getMultibaseLong;
import static kaptainwutax.monkey.command.arguments.MultibaseLongArgumentType.multibaseLong;

public class CommandLcg {
    public static void register(final CommandDispatcher<MessageCommandSource> dispatcher) {
        dispatcher.register(Commands.literal("lcg", "Prints the constants for the Java LCG")
                .executes(ctx -> printDefaultLCGConstants(ctx.getSource()))
                .then(Commands.literal("combine", "Prints the LCG equivalent to calling the Java LCG *n* times")
                        .then(Commands.argument("n", multibaseLong())
                                .executes(ctx -> combine(ctx.getSource(), getMultibaseLong(ctx, "n")))))
                .then(Commands.literal("next", "Prints the seed *n* seeds after *seed* in the Java LCG")
                        .then(Commands.argument("seed", multibaseLong())
                                .executes(ctx -> next(ctx.getSource(), getMultibaseLong(ctx, "seed"), 1L))
                                .then(Commands.argument("n", multibaseLong())
                                        .executes(ctx -> next(ctx.getSource(), getMultibaseLong(ctx, "seed"), getMultibaseLong(ctx, "n"))))))
                .then(Commands.literal("previous", "Prints the seed *n* seeds before *seed* in the Java LCG. Equivalent to `next <seed> -n`")
                        .then(Commands.argument("seed", multibaseLong()).executes(ctx -> next(ctx.getSource(), getMultibaseLong(ctx, "seed"), -1L))
                                .then(Commands.argument("n", multibaseLong())
                                        .executes(ctx -> next(ctx.getSource(), getMultibaseLong(ctx, "seed"), -getMultibaseLong(ctx, "n"))))))
                .then(Commands.literal("distance", "Prints the number of calls required to reach the number from zero, or the number of calls between the two inputs")
                        .then(Commands.argument("a", multibaseLong()).executes(ctx -> distance(ctx.getSource(), 0L, getMultibaseLong(ctx, "a")))
                                .then(Commands.argument("b", multibaseLong())
                                        .executes(ctx -> distance(ctx.getSource(), getMultibaseLong(ctx, "a"), getMultibaseLong(ctx, "b")))))));
    }

    private static int printDefaultLCGConstants(final MessageCommandSource source) {
        source.getChannel().sendMessage(LCG.JAVA.toPrettyString()).queue();
        return 0;
    }

    private static int combine(final MessageCommandSource source, final long n) {
        source.getChannel().sendMessage(LCG.JAVA.combine(n).toPrettyString()).queue();
        return 0;
    }

    private static int next(final MessageCommandSource source, final long seed, final long n) {
        final long nextSeed = LCG.JAVA.combine(n).nextSeed(seed);
        source.getChannel().sendMessage(String.format("0x%X (%d)", nextSeed, nextSeed)).queue();
        return 0;
    }

    private static int distance(final MessageCommandSource source, final long a, final long b) throws CommandSyntaxException {
        final long distance = LCG.JAVA.distance(a, b);
        source.getChannel().sendMessage(String.format("0x%X (%d)", distance & 0xFFFFFFFFFFFFL, distance)).queue();
        return 0;
    }
}
