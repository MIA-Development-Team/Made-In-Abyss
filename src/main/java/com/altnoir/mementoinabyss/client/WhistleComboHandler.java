package com.altnoir.mementoinabyss.client;

import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleNote;
import com.altnoir.mementoinabyss.network.ActivateWhistleSkillPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class WhistleComboHandler {
    private static final List<WhistleNote> INPUT = new ArrayList<>();
    private static final boolean[] PREVIOUS_DIRECTIONS = new boolean[WhistleNote.values().length];

    private static boolean active;
    private static boolean waitForRelease;

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            reset();
            return;
        }

        boolean dialDown = WhistleKeyMappings.SKILL_DIAL.isDown();
        if (!dialDown) {
            active = false;
            waitForRelease = false;
            INPUT.clear();
            rememberDirectionState(minecraft);
            return;
        }
        if (waitForRelease) {
            rememberDirectionState(minecraft);
            return;
        }

        List<WhistleApi.SkillActivation> skills = equippedSkills();
        if (skills.isEmpty()) {
            active = false;
            INPUT.clear();
            rememberDirectionState(minecraft);
            return;
        }
        if (!active) {
            active = true;
            INPUT.clear();
            rememberDirectionState(minecraft);
            return;
        }

        KeyMapping[] directions = directionKeys(minecraft);
        for (int i = 0; i < directions.length; i++) {
            boolean down = directions[i].isDown();
            if (down && !PREVIOUS_DIRECTIONS[i]) {
                accept(WhistleNote.values()[i], skills);
                rememberDirectionState(minecraft);
                return;
            }
        }
        rememberDirectionState(minecraft);
    }

    private static void accept(
            WhistleNote note,
            List<WhistleApi.SkillActivation> skills
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        INPUT.add(note);

        List<WhistleApi.SkillActivation> available = skills.stream()
                .filter(skill -> !minecraft.player.getCooldowns().isOnCooldown(skill.stack()))
                .toList();
        for (WhistleApi.SkillActivation activation : available) {
            if (activation.skill().getDefinition().sequence().equals(INPUT)) {
                ClientPacketDistributor.sendToServer(
                        new ActivateWhistleSkillPayload(activation.fragmentIndex())
                );
                minecraft.player.playSound(
                        SoundEvents.NOTE_BLOCK_CHIME.value(),
                        0.8F,
                        1.2F
                );
                INPUT.clear();
                active = false;
                waitForRelease = true;
                return;
            }
        }

        boolean hasPrefix = available.stream().anyMatch(activation ->
                startsWith(activation.skill().getDefinition().sequence(), INPUT));
        if (!hasPrefix) {
            minecraft.player.playSound(
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                    0.5F,
                    0.8F
            );
            INPUT.clear();
        } else {
            minecraft.player.playSound(
                    SoundEvents.NOTE_BLOCK_HAT.value(),
                    0.5F,
                    1.0F + INPUT.size() * 0.08F
            );
        }
    }

    public static List<WhistleApi.SkillActivation> equippedSkills() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return List.of();
        }
        return WhistleApi.equippedWhistle(minecraft.player)
                .map(WhistleApi::skills)
                .orElse(List.of());
    }

    public static List<WhistleNote> input() {
        return List.copyOf(INPUT);
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean canStillMatch(WhistleApi.SkillActivation activation) {
        return startsWith(activation.skill().getDefinition().sequence(), INPUT);
    }

    public static void reset() {
        active = false;
        waitForRelease = false;
        INPUT.clear();
        for (int i = 0; i < PREVIOUS_DIRECTIONS.length; i++) {
            PREVIOUS_DIRECTIONS[i] = false;
        }
    }

    private static boolean startsWith(
            List<WhistleNote> sequence,
            List<WhistleNote> prefix
    ) {
        if (prefix.size() > sequence.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (sequence.get(i) != prefix.get(i)) {
                return false;
            }
        }
        return true;
    }

    private static void rememberDirectionState(Minecraft minecraft) {
        KeyMapping[] directions = directionKeys(minecraft);
        for (int i = 0; i < directions.length; i++) {
            PREVIOUS_DIRECTIONS[i] = directions[i].isDown();
        }
    }

    private static KeyMapping[] directionKeys(Minecraft minecraft) {
        return new KeyMapping[]{
                minecraft.options.keyUp,
                minecraft.options.keyDown,
                minecraft.options.keyLeft,
                minecraft.options.keyRight
        };
    }

    private WhistleComboHandler() {}
}
