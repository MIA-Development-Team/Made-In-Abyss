package com.altnoir.mia.event.client;

import com.altnoir.mia.client.handler.HookHandler;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaKeyBinding;
import com.altnoir.mia.item.abs.IArtifactSkill;
import com.altnoir.mia.network.server.SkillCooldownPayload;
import com.altnoir.mia.network.server.SkillPlayPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyArrowEvent {
    public static final String SKILL_UNSKILL = "skill.mia.unskill";
    public static final String SKILL_COMBO_SKILLS = "skill.mia.combo_skills";
    public static final String SKILL_COOLDOWN = "skill.mia.cooldown";
    public static final String SKILL_COOLDOWN_TIME = "skill.mia.cooldown_time";
    public static final String SKILL_MINUTE = "skill.mia.minute";
    public static final String SKILL_SECOND = "skill.mia.second";

    private static final Minecraft MC = Minecraft.getInstance();

    public static final KeyMapping UP_KEY = MC.options.keyUp;
    public static final KeyMapping DOWN_KEY = MC.options.keyDown;
    public static final KeyMapping LEFT_KEY = MC.options.keyLeft;
    public static final KeyMapping RIGHT_KEY = MC.options.keyRight;

    // Ctrl键用于激活组合键输入模式
    private static boolean comboUI = false;
    private static List<IArtifactSkill> artifactSkills = new ArrayList<>();
    private static List<ItemStack> skillItemStacks = new ArrayList<>();
    private static List<Integer> skillSlotIndices = new ArrayList<>();

    private static final List<Integer> PLAYER_SEQUENCE = new ArrayList<>();
    private static final List<Long> COOLDOWN_START_TIMES = new ArrayList<>();            // 每个技能独立的冷却时间开始时间
    private static final List<Boolean> IN_COOLDOWNS = new ArrayList<>();                // 每个技能独立的冷却状态
    private static final List<List<Integer>> SKILL_SEQUENCES = new ArrayList<>();       // 每个技能的序列
    private static int currentSkillIndex = -1;
    // 预渲染数据缓存
    private static final List<List<String>> SKILL_ARROWS = new ArrayList<>();      // 用于存储多个技能的箭头
    private static final List<List<Integer>> SKILL_COLORS = new ArrayList<>();     // 用于存储多个技能的颜色

    public static void onClientTick(ClientTickEvent.Post event) {
        if (MC.level == null || MC.player == null) return;
        boolean ctrlPressed = MiaKeyBinding.SKILL_DIAL.isDown();
        if (ctrlPressed || comboUI) {
            handleSkill();

        } else {
            // 如果没有激活组合键模式，清除累积的按键状态
            UP_KEY.consumeClick();
            DOWN_KEY.consumeClick();
            LEFT_KEY.consumeClick();
            RIGHT_KEY.consumeClick();
        }

        HookHandler.handler(MC.player, MC.level, MC.options.keyJump.consumeClick());
    }

    private static void handleSkill() {
        // 检查玩家是否装备了IArtifactSkill物品
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(MC.player);

        if (curiosInventory.isPresent()) {
            var stacksHandler = curiosInventory.get().getStacksHandler("artifact");

            if (stacksHandler.isPresent()) {
                List<IArtifactSkill> skills = new ArrayList<>(); // 临时存储所有找到的技能
                List<ItemStack> skillStacks = new ArrayList<>(); // 存储对应的ItemStack
                List<Integer> slotIndices = new ArrayList<>(); // 存储对应的槽位索引

                for (int i = 0; i < stacksHandler.get().getSlots(); i++) {
                    ItemStack stack = stacksHandler.get().getStacks().getStackInSlot(i);

                    if (stack.getItem() instanceof IArtifactSkill skill) {
                        skills.add(skill);
                        skillStacks.add(stack);
                        slotIndices.add(i);
                    }
                }
                // 如果技能列表大小发生变化，需要调整冷却时间列表大小
                if (artifactSkills.size() != skills.size()) {
                    // 调整冷却时间列表大小
                    while (COOLDOWN_START_TIMES.size() < skills.size()) {
                        COOLDOWN_START_TIMES.add(0L);
                    }
                    while (IN_COOLDOWNS.size() < skills.size()) {
                        IN_COOLDOWNS.add(false);
                    }
                    while (COOLDOWN_START_TIMES.size() > skills.size()) {
                        COOLDOWN_START_TIMES.remove(COOLDOWN_START_TIMES.size() - 1);
                    }
                    while (IN_COOLDOWNS.size() > skills.size()) {
                        IN_COOLDOWNS.remove(IN_COOLDOWNS.size() - 1);
                    }
                }
                // 如果之前有技能但现在没有了，需要清理状态
                if (!artifactSkills.isEmpty() && skills.isEmpty()) {
                    ComboClear();
                    artifactSkills.clear();
                    return;
                }

                artifactSkills = skills; // 更新技能列表
                skillItemStacks = skillStacks; // 更新技能对应的ItemStack列表
                skillSlotIndices = slotIndices; // 更新技能对应的槽位索引列表

                // 检查每个技能的冷却时间（基于物品组件）
                for (int i = 0; i < artifactSkills.size(); i++) {
                    if (MC.level != null) {
                        IN_COOLDOWNS.set(i, isOnCooldown(skillStacks.get(i)));
                    }
                }

                boolean ctrlPressed = MiaKeyBinding.SKILL_DIAL.isDown();

                // 如果刚刚按下Ctrl键且至少有一个技能不在冷却中
                if (ctrlPressed && !comboUI && !artifactSkills.isEmpty()) {
                    activateCombo();
                } else if (!ctrlPressed && comboUI) {
                    // 如果释放了Ctrl键并且处于组合键模式
                    ComboClear();
                }

                // 只有在组合键模式激活且按住Ctrl键时才处理输入
                if (comboUI && ctrlPressed) {
                    keyInput();
                }
            }
        }
        // 如果无法获取Curios库存，清理状态
        else if (comboUI || !artifactSkills.isEmpty()) {
            ComboClear();
            artifactSkills.clear();
        }
    }

    private static boolean isOnCooldown(ItemStack stack) {
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        return cooldownValue != null && cooldownValue > 0;
    }

    private static void activateCombo() {
        comboUI = true;
        PLAYER_SEQUENCE.clear();
        currentSkillIndex = -1;

        // 清空旧的技能序列
        SKILL_SEQUENCES.clear();
        SKILL_ARROWS.clear();
        SKILL_COLORS.clear();

        // 为所有技能生成序列和渲染数据
        for (IArtifactSkill skill : artifactSkills) {
            List<Integer> sequence = skill.getComboSequence();
            SKILL_SEQUENCES.add(new ArrayList<>(sequence));

            // 为每个技能生成箭头显示
            List<String> arrows = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            for (Integer direction : sequence) {
                String arrow = switch (direction) {
                    case 0 -> "↑";
                    case 1 -> "↓";
                    case 2 -> "←";
                    case 3 -> "→";
                    default -> "?";
                };
                arrows.add(arrow);
                colors.add(0xFFFFFF); // 默认白色
            }
            SKILL_ARROWS.add(arrows);
            SKILL_COLORS.add(colors);
        }
    }

    private static void ComboClear() {
        comboUI = false;
        PLAYER_SEQUENCE.clear();
        SKILL_SEQUENCES.clear(); // 需要清除技能序列列表
        // 重置渲染数据
        SKILL_ARROWS.clear();
        SKILL_COLORS.clear();
        currentSkillIndex = -1; // 重置当前技能索引
    }

    private static void startCooldown() {
        // 为当前使用的技能启动冷却（这里假设使用的是第一个非冷却中的技能）
        if (currentSkillIndex >= 0 && currentSkillIndex < artifactSkills.size()) {
            IN_COOLDOWNS.set(currentSkillIndex, true);
            if (MC.level != null) {
                // 获取当前技能对应的ItemStack
                ItemStack skillStack = getSkillItemStack(currentSkillIndex);
                if (skillStack != null) {
                    if (!MC.level.isClientSide()) {
                        // 在服务端直接设置冷却
                        artifactSkills.get(currentSkillIndex).setCooldown(skillStack);
                    } else {
                        // 在客户端发送网络包到服务端
                        int slotIndex = getSkillSlotIndex(currentSkillIndex);
                        if (slotIndex >= 0) {
                            PacketDistributor.sendToServer(new SkillCooldownPayload(slotIndex));
                        }
                    }
                }
            }
        }
        ComboClear();
    }

    // 辅助方法：获取技能对应的ItemStack
    private static ItemStack getSkillItemStack(int skillIndex) {
        if (skillIndex >= 0 && skillIndex < skillItemStacks.size()) {
            return skillItemStacks.get(skillIndex);
        }
        return null;
    }

    private static int getSkillSlotIndex(int skillIndex) {
        if (skillIndex >= 0 && skillIndex < skillSlotIndices.size()) {
            return skillSlotIndices.get(skillIndex);
        }
        return -1;
    }

    private static void keyInput() {
        // 检测方向键输入
        KeyMapping[] keys = {UP_KEY, DOWN_KEY, LEFT_KEY, RIGHT_KEY};
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].consumeClick()) {
                PLAYER_SEQUENCE.add(i);
                playGoodSound();
                checkSequence();
                break;
            }
        }
    }


    private static void checkSequence() {
        if (currentSkillIndex == -1) {
            // 尝试匹配任何一个非冷却中的技能
            int matchingSkillCount = 0;
            int lastMatchingSkill = -1;

            for (int skillIndex = 0; skillIndex < SKILL_SEQUENCES.size(); skillIndex++) {
                // 棜查该技能是否在冷却中
                if (IN_COOLDOWNS.get(skillIndex)) continue;

                List<Integer> skillSequence = SKILL_SEQUENCES.get(skillIndex);

                // 先检查长度，避免不必要的比较
                if (PLAYER_SEQUENCE.size() > skillSequence.size()) {
                    continue; // 继续检查下一个技能
                }

                // 检查当前输入是否匹配该技能序列的对应位置
                int lastIndex = PLAYER_SEQUENCE.size() - 1;
                if (lastIndex >= 0) {
                    if (!PLAYER_SEQUENCE.get(lastIndex).equals(skillSequence.get(lastIndex))) {
                        continue; // 继续检查下一个技能
                    }
                }

                // 如果序列完全匹配该技能
                if (PLAYER_SEQUENCE.size() == skillSequence.size()) {
                    currentSkillIndex = skillIndex;
                    // 触发成功事件
                    onComboSuccess();
                    startCooldown();
                    return;
                }

                // 找到部分匹配的技能
                matchingSkillCount++;
                lastMatchingSkill = skillIndex;
            }

            // 如果只有一个技能匹配，则锁定到该技能
            if (matchingSkillCount == 1) {
                currentSkillIndex = lastMatchingSkill;
            } else if (matchingSkillCount == 0) {
                // 没有任何技能匹配
                playBadSound();
                PLAYER_SEQUENCE.clear();
                currentSkillIndex = -1;
            }

            updatePlayerSequence();
        } else {
            // 已经确定了当前技能，继续检查该技能
            List<Integer> skillSequence = SKILL_SEQUENCES.get(currentSkillIndex);

            // 先检查长度，避免不必要的比较
            if (PLAYER_SEQUENCE.size() > skillSequence.size()) {
                playBadSound();
                PLAYER_SEQUENCE.clear();
                currentSkillIndex = -1;
                updatePlayerSequence();
                return;
            }

            // 检查当前输入是否匹配目标序列的对应位置
            int lastIndex = PLAYER_SEQUENCE.size() - 1;
            if (lastIndex >= 0) {
                if (!PLAYER_SEQUENCE.get(lastIndex).equals(skillSequence.get(lastIndex))) {
                    playBadSound();
                    PLAYER_SEQUENCE.clear();
                    currentSkillIndex = -1;
                    updatePlayerSequence();
                    return;
                }
            }

            // 如果序列完全匹配
            if (PLAYER_SEQUENCE.size() == skillSequence.size()) {
                // 触发成功事件
                onComboSuccess();
                startCooldown();
            }
            // 更新玩家输入渲染状态
            updatePlayerSequence();
        }
    }

    // 更新玩家输入渲染状态
    private static void updatePlayerSequence() {
        // 更新所有技能的渲染状态
        for (int s = 0; s < SKILL_COLORS.size(); s++) {
            List<Integer> colors = SKILL_COLORS.get(s);
            // 重置所有颜色为默认色
            for (int i = 0; i < colors.size(); i++) {
                // 如果是当前正在输入的技能，使用黄色作为基础色
                int baseColor = (currentSkillIndex == s) ? 0xFFFF00 : 0xFFFFFF;
                colors.set(i, baseColor);
            }

            // 将已输入的部分设置为灰色
            for (int i = 0; i < PLAYER_SEQUENCE.size() && i < colors.size(); i++) {
                colors.set(i, 0xAAAAAA);
            }
        }
    }

    private static void onComboSuccess() {
        if (MC.player != null) {
            if (currentSkillIndex >= 0 && currentSkillIndex < artifactSkills.size()) {
                // 通过网络包在服务端执行技能
                int slotIndex = getSkillSlotIndex(currentSkillIndex);
                if (slotIndex >= 0) {
                    PacketDistributor.sendToServer(new SkillPlayPayload(slotIndex));
                }
            } else {
                // 如果没有技能，显示默认消息
                MC.player.displayClientMessage(Component.translatable(SKILL_UNSKILL), true);
            }
        }
    }

    private static void playGoodSound() {
        if (MC.player != null) {
            MC.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.0F);
        }
    }

    private static void playBadSound() {
        if (MC.player != null) {
            MC.player.playSound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 0.5F, 1.0F);
        }
    }


    private static final int x = 10; // 整体宽度
    private static final int y = 10; // 整体高度
    private static final int yOffset = 16;
    private static final int bgWidth = 102; // 背景宽度
    private static final int bgHeight = 12; // 背景高度

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (MC.level == null || MC.player == null) return;

        // 只有在组合键模式激活时才显示界面
        if (comboUI) {
            renderComboUI(event.getGuiGraphics());
        }
    }

    private static void renderComboUI(GuiGraphics guiGraphics) {
        Font font = MC.font;

        // 计算背景高度
        int dynamicHeight = bgHeight + (!artifactSkills.isEmpty() ? artifactSkills.size() * yOffset : 0);

        // 绘制半透明黑色背景
        guiGraphics.fill(x - 2, y - 2, x + bgWidth, y + dynamicHeight, 0x80000000);

        // 绘制标题
        guiGraphics.drawString(font, Component.translatable(SKILL_COMBO_SKILLS), x, y, 0xFFFFFF);

        // 绘制每个技能的信息
        if (!artifactSkills.isEmpty()) {
            for (int i = 0; i < artifactSkills.size(); i++) {
                IArtifactSkill skill = artifactSkills.get(i);
                ItemStack itemStack = skill.getItemStack();

                // 检查该技能是否在冷却中
                if (IN_COOLDOWNS.get(i)) {
                    ItemStack skillStack = getSkillItemStack(i);
                    long remaining = 0;
                    if (skillStack != null && MC.level != null) {
                        remaining = getCooldownTicks(skillStack);
                    }
                    // 显示冷却时间
                    Component cooldownText = getCoolDown(remaining);
                    guiGraphics.renderItem(itemStack, x, y + 18 + i * yOffset - 8);
                    guiGraphics.drawString(font, cooldownText, x + 18, y + 15 + i * yOffset, 0xAAAAAA);
                } else {
                    // 显示组合键序列
                    if (i < SKILL_ARROWS.size()) {
                        List<String> arrows = SKILL_ARROWS.get(i);
                        boolean canMatch = isCanMatch(i);

                        // 根据是否可能匹配设置基础颜色
                        int baseColor = 0xFFFFFF; // 默认白色
                        if (currentSkillIndex == i) {
                            baseColor = 0xFFFF00; // 当前技能用黄色
                        } else if (!canMatch) {
                            baseColor = 0x30FFFFFF;
                            ; // 不可匹配
                        }

                        // 渲染物品图标
                        guiGraphics.renderItem(itemStack, x, y + 18 + i * yOffset - 8);

                        int offsetX = 0;
                        for (int j = 0; j < arrows.size(); j++) {
                            String arrow = arrows.get(j);
                            int color = baseColor;

                            // 已输入的部分用灰色，但如果技能已不可能匹配则用黑色
                            if (j < PLAYER_SEQUENCE.size()) {
                                if (canMatch) {
                                    color = 0xAAAAAA; // 已输入部分用灰色
                                } else {
                                    color = 0x30FFFFFF;// 不可匹配
                                }
                            }

                            guiGraphics.drawString(font, arrow, x + 18 + offsetX, y + 15 + i * yOffset, color);
                            offsetX += font.width(arrow) + 2; // 字符宽度 + 2像素间隔
                        }
                    }
                }
            }
        }
    }

    private static boolean isCanMatch(int i) {
        List<Integer> skillSequence = SKILL_SEQUENCES.get(i);

        // 判断该技能是否仍可能被匹配
        boolean canMatch = true;
        if (currentSkillIndex != -1 && currentSkillIndex != i) {
            // 如果已经确定了当前技能且不是这个技能，则不能匹配
            canMatch = false;
        } else {
            // 检查当前输入序列是否匹配该技能的前缀
            for (int j = 0; j < PLAYER_SEQUENCE.size() && j < skillSequence.size(); j++) {
                if (!PLAYER_SEQUENCE.get(j).equals(skillSequence.get(j))) {
                    canMatch = false;
                    break;
                }
            }
            // 如果输入序列长度超过技能序列长度，也不能匹配
            if (PLAYER_SEQUENCE.size() > skillSequence.size()) {
                canMatch = false;
            }
        }
        return canMatch;
    }

    private static @NotNull Component getCoolDown(long remaining) {
        Component cooldownText =Component.translatable(SKILL_COOLDOWN);
        Component cooldownRemainingText = Component.translatable(SKILL_COOLDOWN_TIME);
        Component m = Component.translatable(SKILL_MINUTE);
        Component s = Component.translatable(SKILL_SECOND);
        if (remaining > 0) {
            long seconds = remaining / 20;
            if (seconds >= 60) {
                // 超过60秒显示为分钟格式
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                cooldownText = Component.empty()
                        .append(cooldownRemainingText)
                        .append(String.valueOf(minutes)).append(m)
                        .append(String.valueOf(remainingSeconds)).append(s);
            } else if (seconds < 10) {
                // 最后10秒显示小数点
                long decimal = (remaining % 20) * 5; // 转换为0-99的值
                cooldownText = Component.empty()
                        .append(cooldownRemainingText)
                        .append(String.valueOf(seconds)).append(".")
                        .append(String.format("%02d", decimal)).append(s);
            } else {
                // 10秒到60秒之间只显示整数秒
                cooldownText = Component.empty()
                        .append(cooldownRemainingText)
                        .append(String.valueOf(seconds)).append(s);
            }
        }
        return cooldownText;
    }

    private static long getCooldownTicks(ItemStack stack) {
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        return cooldownValue != null ? cooldownValue : 0;
    }
}
