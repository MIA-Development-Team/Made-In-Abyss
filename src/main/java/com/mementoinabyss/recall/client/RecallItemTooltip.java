/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Tooltip insertion, hold message and progress line adapted from PonderTooltipHandler.
 * See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client;

import com.mementoinabyss.recall.data.GuideRepository;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

/** Native item-tooltip entry point: the hint is queried independently from the actual hover target. */
final class RecallItemTooltip {
    private final KeyMapping key;
    private final Identifier preferredGuide;
    @Nullable private Screen hoveredScreen;
    private ItemStack hoveredItem = ItemStack.EMPTY;
    private double mouseX, mouseY;
    private long hoverTime;
    private final RecallHoldProgress progress = new RecallHoldProgress();
    @Nullable private Identifier holdingItem;

    RecallItemTooltip(KeyMapping key, Identifier preferredGuide) {
        this.key = key;
        this.preferredGuide = preferredGuide;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::appendHint);
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Pre event) -> clearHover());
        NeoForge.EVENT_BUS.addListener(
                (ScreenEvent.Opening event) -> {
                    clearHover();
                    resetHold();
                });
        NeoForge.EVENT_BUS.addListener(this::captureHover);
        NeoForge.EVENT_BUS.addListener(this::beforeTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, this::keyPressed);
    }

    private void appendHint(ItemTooltipEvent event) {
        if (!hasGuide(event.getItemStack())) return;
        Identifier item = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        Component hint = holdMessage();
        if (RenderSystem.isOnRenderThread()) {
            var client = Minecraft.getInstance();
            if (client.screen instanceof GuideScreen guide && guide.isSubject(item)) {
                hint =
                        Component.translatable("recall.tooltip.subject")
                                .withStyle(ChatFormatting.GREEN);
            } else if (item.equals(holdingItem)) {
                float fill =
                        progress.visual(
                                client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
                if (fill > 0) hint = progressLine(fill);
            }
        }
        event.getToolTip().add(Math.min(1, event.getToolTip().size()), hint);
    }

    private Component holdMessage() {
        return Component.translatable(
                        "recall.tooltip.open",
                        key.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private Component progressLine(float fill) {
        var font = Minecraft.getInstance().font;
        int total = (int) ((float) font.width(holdMessage()) / Math.max(1, font.width("|")));
        int complete = (int) (fill * total);
        return Component.empty()
                .append(Component.literal("|".repeat(complete)).withStyle(ChatFormatting.GRAY))
                .append(
                        Component.literal("|".repeat(total - complete))
                                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private void resetHold() {
        holdingItem = null;
        progress.reset();
    }

    private void beforeTick(ClientTickEvent.Pre event) {
        var client = Minecraft.getInstance();
        var screen = client.screen;
        if (client.player == null
                || client.level == null
                || screen == null
                || editingText(screen)) {
            resetHold();
            return;
        }
        ItemStack stack = hoveredStack(client, screen);
        if (!hasGuide(stack)) {
            resetHold();
            return;
        }
        Identifier item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (screen instanceof GuideScreen guide && guide.isSubject(item)) {
            resetHold();
            return;
        }
        if (!item.equals(holdingItem)) {
            progress.reset();
            holdingItem = item;
        }
        boolean down =
                !key.isUnbound()
                        && key.isConflictContextAndModifierActive()
                        && InputConstants.isKeyDown(client.getWindow(), key.getKey().getValue());
        if (progress.tick(down)) {
            RecallClient.openItem(item);
            resetHold();
        }
    }

    private boolean hasGuide(ItemStack stack) {
        return !stack.isEmpty()
                && GuideRepository.findItem(
                                BuiltInRegistries.ITEM.getKey(stack.getItem()), preferredGuide)
                        .isPresent();
    }

    private void captureHover(RenderTooltipEvent.Pre event) {
        if (event.isCanceled() || event.getItemStack().isEmpty()) return;
        var client = Minecraft.getInstance();
        if (client.screen == null) return;
        hoveredScreen = client.screen;
        hoveredItem = event.getItemStack();
        mouseX = client.mouseHandler.getScaledXPos(client.getWindow());
        mouseY = client.mouseHandler.getScaledYPos(client.getWindow());
        hoverTime = Util.getMillis();
    }

    private void clearHover() {
        hoveredScreen = null;
        hoveredItem = ItemStack.EMPTY;
    }

    private void keyPressed(ScreenEvent.KeyPressed.Pre event) {
        var client = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (client.player == null
                || client.level == null
                || screen != client.screen
                || editingText(screen)
                || !key.isActiveAndMatches(InputConstants.getKey(event.getKeyEvent()))) return;
        ItemStack stack = hoveredStack(client, screen);
        if (!hasGuide(stack)) return;
        // Reserve this key only for an eligible hovered item; opening happens when the
        // original Ponder hold ramp finishes, not on the first press or an OS repeat.
        event.setCanceled(true);
    }

    private ItemStack hoveredStack(Minecraft client, Screen screen) {
        // Actual rendered tooltips support recipe viewers and other custom item UIs too.
        double x = client.mouseHandler.getScaledXPos(client.getWindow());
        double y = client.mouseHandler.getScaledYPos(client.getWindow());
        if (hoveredScreen == screen
                && Util.getMillis() - hoverTime < 250
                && Math.abs(x - mouseX) < 2
                && Math.abs(y - mouseY) < 2) return hoveredItem;
        if (screen instanceof AbstractContainerScreen<?> container) {
            var slot = container.getHoveredSlot();
            if (slot != null && slot.hasItem()) {
                int left = container.getLeftPos() + slot.x;
                int top = container.getTopPos() + slot.y;
                if (x >= left - 1 && x < left + 17 && y >= top - 1 && y < top + 17)
                    return slot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean editingText(Screen screen) {
        GuiEventListener focused = screen.getFocused();
        for (int depth = 0; focused != null && depth < 16; depth++) {
            if (focused instanceof EditBox box && box.canConsumeInput()) return true;
            focused = focused instanceof ContainerEventHandler parent ? parent.getFocused() : null;
        }
        return false;
    }
}
