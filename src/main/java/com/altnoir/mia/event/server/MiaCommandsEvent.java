package com.altnoir.mia.event.server;

import com.altnoir.mia.init.MiaCapabilities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MiaCommandsEvent {
    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mia")
                        .then(Commands.literal("curse")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("max")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> setMaxCurse(ctx, EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "value")))
                                                )
                                        )
                                        .then(Commands.literal("clear")
                                                .executes(ctx -> clearCurse(ctx, EntityArgument.getPlayer(ctx, "player")))
                                        )
                                        .then(Commands.literal("get")
                                                .executes(ctx -> getCurse(ctx, EntityArgument.getPlayer(ctx, "player")))
                                        )
                                )
                        )
        );
    }

    private static int setMaxCurse(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int value) {
        var curse = player.getCapability(MiaCapabilities.CURSE, null);
        if (curse == null) {
            ctx.getSource().sendFailure(Component.literal("Curse capability not found on player."));
            return 0;
        }
        curse.setMaxCurse(value);
        ctx.getSource().sendSuccess(() -> Component.literal("Set max curse of " + player.getName().getString() + " to " + value), true);
        return 1;
    }

    private static int clearCurse(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        var curse = player.getCapability(MiaCapabilities.CURSE, null);
        if (curse == null) {
            ctx.getSource().sendFailure(Component.literal("Curse capability not found on player."));
            return 0;
        }
        curse.setCurse(0);
        ctx.getSource().sendSuccess(() -> Component.literal("Cleared curse of " + player.getName().getString()), true);
        return 1;
    }

    private static int getCurse(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        var curse = player.getCapability(MiaCapabilities.CURSE, null);
        if (curse == null) {
            ctx.getSource().sendFailure(Component.literal("Curse capability not found on player."));
            return 0;
        }
        var curseValue = curse.getCurse();
        var maxCurseValue = curse.getMaxCurse();
        ctx.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + "'s max curse is " + maxCurseValue + ", current curse is " + curseValue), true);
        return 1;
    }
}
