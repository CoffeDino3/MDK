package net.CoffeDino.testmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.CoffeDino.testmod.races.races;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;



public class RaceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("race")
                .requires(source -> source.hasPermission(2)) // Requires creative/op permission
                .then(Commands.literal("set")
                        .then(Commands.argument("race", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    // Tab completion for race names
                                    for (races.Race race : races.Race.values()) {
                                        builder.suggest(race.getId());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> setOwnRace(context, StringArgumentType.getString(context, "race")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> setOtherRace(context, StringArgumentType.getString(context, "race"), EntityArgument.getPlayer(context, "target")))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .executes(context -> getOwnRace(context))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> getOtherRace(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
                .then(Commands.literal("clear")
                        .executes(context -> clearOwnRace(context))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> clearOtherRace(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
        );
    }

    private static int setOwnRace(CommandContext<CommandSourceStack> context, String raceId) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return setRaceForPlayer(context, raceId, player);
    }

    private static int setOtherRace(CommandContext<CommandSourceStack> context, String raceId, ServerPlayer target) {
        return setRaceForPlayer(context, raceId, target);
    }

    private static int setRaceForPlayer(CommandContext<CommandSourceStack> context, String raceId, ServerPlayer player) {
        races.Race race = findRaceById(raceId);
        if (race == null) {
            context.getSource().sendFailure(Component.literal("Unknown race: " + raceId + ". Available races: " + getAvailableRacesList()));
            return 0;
        }

        races.setPlayerRace(player, race);

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your race has been set to: " + race.getDisplayName()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Set " + player.getDisplayName().getString() + "'s race to: " + race.getDisplayName()), true);
            player.sendSystemMessage(Component.literal("Your race has been set to: " + race.getDisplayName()));
        }

        return 1;
    }

    private static int getOwnRace(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return getRaceForPlayer(context, player);
    }

    private static int getOtherRace(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return getRaceForPlayer(context, target);
    }

    private static int getRaceForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        races.Race race = races.getPlayerRace(player);
        if (race == null) {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + " has no race selected"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + "'s race: " + race.getDisplayName()), false);
        }
        return 1;
    }

    private static int clearOwnRace(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return clearRaceForPlayer(context, player);
    }

    private static int clearOtherRace(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return clearRaceForPlayer(context, target);
    }

    private static int clearRaceForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        races.clearPlayerRace(player); // Use the class method

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your race has been cleared"), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Cleared " + player.getDisplayName().getString() + "'s race"), true);
            player.sendSystemMessage(Component.literal("Your race has been cleared"));
        }

        return 1;
    }

    private static races.Race findRaceById(String raceId) {
        for (races.Race race : races.Race.values()) {
            if (race.getId().equalsIgnoreCase(raceId)) {
                return race;
            }
        }
        return null;
    }

    private static String getAvailableRacesList() {
        StringBuilder sb = new StringBuilder();
        for (races.Race race : races.Race.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(race.getId());
        }
        return sb.toString();
    }

}