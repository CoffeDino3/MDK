package com.CoffeDino.lunacy.commands;

import com.CoffeDino.lunacy.leveling.LevelDataManager;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LevelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("level")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("get")
                        .requires(source -> source.hasPermission(0))
                        .executes(LevelCommand::getOwnLevel)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> getOtherLevel(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(context -> setOwnLevel(context, IntegerArgumentType.getInteger(context, "level")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> setOtherLevel(context, IntegerArgumentType.getInteger(context, "level"), EntityArgument.getPlayer(context, "target")))
                                )
                        )
                )
                .then(Commands.literal("addxp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> addOwnXp(context, IntegerArgumentType.getInteger(context, "amount")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> addOtherXp(context, IntegerArgumentType.getInteger(context, "amount"), EntityArgument.getPlayer(context, "target")))
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .executes(LevelCommand::clearOwnLevel)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> clearOtherLevel(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
        );
    }

    private static int getOwnLevel(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return getLevelForPlayer(context, player);
    }

    private static int getOtherLevel(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return getLevelForPlayer(context, target);
    }

    private static int getLevelForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        int level = PlayerLevels.getLevel(player);
        int xp = PlayerLevels.getXp(player);
        int xpNeeded = PlayerLevels.getXpToNextLevel(level);

        context.getSource().sendSuccess(() -> Component.literal(
                player.getDisplayName().getString() + " - Level: " + level +
                        " | XP: " + xp + "/" + xpNeeded
        ), false);
        return 1;
    }

    private static int setOwnLevel(CommandContext<CommandSourceStack> context, int level) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return setLevelForPlayer(context, level, player);
    }

    private static int setOtherLevel(CommandContext<CommandSourceStack> context, int level, ServerPlayer target) {
        return setLevelForPlayer(context, level, target);
    }

    private static int setLevelForPlayer(CommandContext<CommandSourceStack> context, int level, ServerPlayer player) {
        PlayerLevels.setLevel(player, level, true);

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your level has been set to: " + level), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Set " + player.getDisplayName().getString() + "'s level to: " + level), true);
            player.sendSystemMessage(Component.literal("Your level has been set to: " + level));
        }
        return 1;
    }

    private static int addOwnXp(CommandContext<CommandSourceStack> context, int amount) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return addXpForPlayer(context, amount, player);
    }

    private static int addOtherXp(CommandContext<CommandSourceStack> context, int amount, ServerPlayer target) {
        return addXpForPlayer(context, amount, target);
    }

    private static int addXpForPlayer(CommandContext<CommandSourceStack> context, int amount, ServerPlayer player) {
        PlayerLevels.addXp(player, amount);
        context.getSource().sendSuccess(() -> Component.literal("Gave " + amount + " xp to " + player.getDisplayName().getString()), true);
        return 1;
    }

    private static int clearOwnLevel(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return clearLevelForPlayer(context, player);
    }

    private static int clearOtherLevel(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return clearLevelForPlayer(context, target);
    }

    private static int clearLevelForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        PlayerLevels.clear(player);

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your level has been cleared"), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Cleared " + player.getDisplayName().getString() + "'s level"), true);
            player.sendSystemMessage(Component.literal("Your level has been cleared"));
        }
        return 1;
    }
}