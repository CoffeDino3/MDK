package com.CoffeDino.lunacy.commands;

import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClassCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("class")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("class", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
                                        builder.suggest(playerClass.getId());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> setOwnClass(context, StringArgumentType.getString(context, "class")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> setOtherClass(context, StringArgumentType.getString(context, "class"), EntityArgument.getPlayer(context, "target")))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .requires(source -> source.hasPermission(0))
                        .executes(ClassCommand::getOwnClass)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> getOtherClass(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .executes(ClassCommand::clearOwnClass)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> clearOtherClass(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
                .then(Commands.literal("spellblade")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("element")
                                .requires(source -> source.hasPermission(0))
                                .then(Commands.literal("get")
                                        .requires(source -> source.hasPermission(0))
                                        .executes(ClassCommand::getOwnElement)
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> getOtherElement(context, EntityArgument.getPlayer(context, "target")))
                                        )
                                )
                                .then(Commands.literal("set")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("element", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    for (SpellbladeElement element : SpellbladeElement.values()) {
                                                        builder.suggest(element.getId());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> setOwnElement(context, StringArgumentType.getString(context, "element")))
                                                .then(Commands.argument("target", EntityArgument.player())
                                                        .executes(context -> setOtherElement(context, StringArgumentType.getString(context, "element"), EntityArgument.getPlayer(context, "target")))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setOwnClass(CommandContext<CommandSourceStack> context, String classId) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return setClassForPlayer(context, classId, player);
    }

    private static int setOtherClass(CommandContext<CommandSourceStack> context, String classId, ServerPlayer target) {
        return setClassForPlayer(context, classId, target);
    }

    private static int setClassForPlayer(CommandContext<CommandSourceStack> context, String classId, ServerPlayer player) {
        PlayerClasses.PlayerClass playerClass = findClassById(classId);
        if (playerClass == null) {
            context.getSource().sendFailure(Component.literal("Unknown class: " + classId + ". Available classes: " + getAvailableClassesList()));
            return 0;
        }

        PlayerClasses.setPlayerClass(player, playerClass);

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your class has been set to: " + playerClass.getDisplayName()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Set " + player.getDisplayName().getString() + "'s class to: " + playerClass.getDisplayName()), true);
            player.sendSystemMessage(Component.literal("Your class has been set to: " + playerClass.getDisplayName()));
        }

        return 1;
    }

    private static int getOwnClass(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return getClassForPlayer(context, player);
    }

    private static int getOtherClass(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return getClassForPlayer(context, target);
    }

    private static int getClassForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        PlayerClasses.PlayerClass playerClass = PlayerClasses.getPlayerClass(player);
        if (playerClass == null) {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + " has no class selected"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + "'s class: " + playerClass.getDisplayName()), false);
        }
        return 1;
    }

    private static int clearOwnClass(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return clearClassForPlayer(context, player);
    }

    private static int clearOtherClass(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return clearClassForPlayer(context, target);
    }

    private static int clearClassForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        PlayerClasses.clearPlayerClass(player);

        ClassDataManager dataManager = ClassDataManager.get(player);
        dataManager.setPlayerElement(player.getUUID(), null);

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your class has been cleared"), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Cleared " + player.getDisplayName().getString() + "'s class"), true);
            player.sendSystemMessage(Component.literal("Your class has been cleared"));
        }

        return 1;
    }

    private static PlayerClasses.PlayerClass findClassById(String classId) {
        for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
            if (playerClass.getId().equalsIgnoreCase(classId)) {
                return playerClass;
            }
        }
        return null;
    }

    private static String getAvailableClassesList() {
        StringBuilder sb = new StringBuilder();
        for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(playerClass.getId());
        }
        return sb.toString();
    }
    private static int getOwnElement(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return getElementForPlayer(context, player);
    }

    private static int getOtherElement(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        return getElementForPlayer(context, target);
    }

    private static int getElementForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ClassDataManager dataManager = ClassDataManager.get(player);
        String elementId = dataManager.getPlayerElement(player.getUUID());
        SpellbladeElement element = SpellbladeElement.fromId(elementId);

        if (element == null) {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + " has no element selected"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal(player.getDisplayName().getString() + "'s element: " + element.getDisplayName()), false);
        }
        return 1;
    }

    private static int setOwnElement(CommandContext<CommandSourceStack> context, String elementId) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player"));
            return 0;
        }
        return setElementForPlayer(context, elementId, player);
    }

    private static int setOtherElement(CommandContext<CommandSourceStack> context, String elementId, ServerPlayer target) {
        return setElementForPlayer(context, elementId, target);
    }

    private static int setElementForPlayer(CommandContext<CommandSourceStack> context, String elementId, ServerPlayer player) {
        SpellbladeElement element = SpellbladeElement.fromId(elementId);
        if (element == null) {
            context.getSource().sendFailure(Component.literal("Unknown element: " + elementId + ". Available elements: " + getAvailableElementsList()));
            return 0;
        }

        if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.SPELLBLADE) {
            context.getSource().sendFailure(Component.literal(player.getDisplayName().getString() + " is not a Spellblade"));
            return 0;
        }

        ClassDataManager dataManager = ClassDataManager.get(player);
        dataManager.setPlayerElement(player.getUUID(), element.getId());

        if (context.getSource().getEntity() == player) {
            context.getSource().sendSuccess(() -> Component.literal("Your element has been set to: " + element.getDisplayName()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Set " + player.getDisplayName().getString() + "'s element to: " + element.getDisplayName()), true);
            player.sendSystemMessage(Component.literal("Your element has been set to: " + element.getDisplayName()));
        }

        return 1;
    }

    private static String getAvailableElementsList() {
        StringBuilder sb = new StringBuilder();
        for (SpellbladeElement element : SpellbladeElement.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(element.getId());
        }
        return sb.toString();
    }
}