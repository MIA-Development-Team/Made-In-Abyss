package com.altnoir.mia.init.event;

import com.altnoir.mia.client.event.KeyBindingEvent;
import com.altnoir.mia.item.abs.IArtifactSkill;
import com.altnoir.mia.network.SkillCooldownPayload;
import com.altnoir.mia.network.SkillPlayPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
        boolean ctrlPressed = KeyBindingEvent.ACTIVATE_KEY.isDown();
        if (ctrlPressed || comboUI) {
            handleSkill();

        } else {
            // 如果没有激活组合键模式，清除累积的按键状态
            UP_KEY.consumeClick();
            DOWN_KEY.consumeClick();
            LEFT_KEY.consumeClick();
            RIGHT_KEY.consumeClick();
        }
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
                    DeleteCombo();
                    artifactSkills.clear();
                    return;
                }

                artifactSkills = skills; // 更新技能列表
                skillItemStacks = skillStacks; // 更新技能对应的ItemStack列表
                skillSlotIndices = slotIndices; // 更新技能对应的槽位索引列表

                // 检查每个技能的冷却时间（基于物品组件）
                for (int i = 0; i < artifactSkills.size(); i++) {
                    if (MC.level != null) {
                        boolean onCooldown = artifactSkills.get(i).isOnCooldown(skillStacks.get(i));
                        IN_COOLDOWNS.set(i, onCooldown);
                    }
                }

                boolean ctrlPressed = KeyBindingEvent.ACTIVATE_KEY.isDown();

                // 如果刚刚按下Ctrl键且至少有一个技能不在冷却中
                if (ctrlPressed && !comboUI && !artifactSkills.isEmpty()) {
                    activateCombo();
                } else if (!ctrlPressed && comboUI) {
                    // 如果释放了Ctrl键并且处于组合键模式
                    DeleteCombo();
                }

                // 只有在组合键模式激活且按住Ctrl键时才处理输入
                if (comboUI && ctrlPressed) {
                    keyInput();
                }
            }
        }
        // 如果无法获取Curios库存，清理状态
        else if (comboUI || !artifactSkills.isEmpty()) {
            DeleteCombo();
            artifactSkills.clear();
        }
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

    private static void DeleteCombo() {
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
        DeleteCombo();
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
            for (int skillIndex = 0; skillIndex < SKILL_SEQUENCES.size(); skillIndex++) {
                // 检查该技能是否在冷却中
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

                // 找到部分匹配的技能，设置为当前技能
                currentSkillIndex = skillIndex;
                updatePlayerSequence();
                return;
            }

            // 没有任何技能匹配
            playBadSound();
            PLAYER_SEQUENCE.clear();
            currentSkillIndex = -1;
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
                MC.player.displayClientMessage(Component.literal("未设置技能"), true);
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
    private static final int bgWidth = 60; // 背景宽度
    private static final int bgHeight = 12; // 背景高度

    @OnlyIn(Dist.CLIENT)
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
        int dynamicHeight = bgHeight + (!artifactSkills.isEmpty() ? artifactSkills.size() * 15 : 0);

        // 绘制半透明黑色背景
        guiGraphics.fill(x - 2, y - 2, x + bgWidth, y + dynamicHeight, 0x80000000);

        // 绘制标题
        guiGraphics.drawString(font, "输入组合键:", x, y, 0xFFFFFF);

        // 绘制每个技能的信息
        if (!artifactSkills.isEmpty()) {
            for (int i = 0; i < artifactSkills.size(); i++) {
                // 检查该技能是否在冷却中
                if (IN_COOLDOWNS.get(i)) {
                    // 显示冷却时间
                    IArtifactSkill skill = artifactSkills.get(i);
                    ItemStack skillStack = getSkillItemStack(i);
                    long remaining = 0;
                    if (skillStack != null && MC.level != null) {
                        remaining = skill.getRemainingCooldown(skillStack);
                    }

                    String cooldownText = getCoolDown(remaining);
                    guiGraphics.drawString(font, cooldownText, x, y + 15 + i * 15, 0xAAAAAA); // 冷却中用浅红色
                } else {
                    // 显示组合键序列
                    if (i < SKILL_ARROWS.size()) {
                        List<String> arrows = SKILL_ARROWS.get(i);

                        // 如果是当前正在输入的技能，使用不同颜色标识
                        int baseColor = (currentSkillIndex == i) ? 0xFFFF00 : 0xFFFFFF; // 当前技能用黄色

                        // 渲染物品图标
                        IArtifactSkill skill = artifactSkills.get(i);
                        ItemStack itemStack = skill.getItemStack();
                        guiGraphics.renderItem(itemStack, x, y + 18 + i * 15 - 8);

                        int offsetX = 0;
                        for (int j = 0; j < arrows.size(); j++) {
                            String arrow = arrows.get(j);
                            int color = (j < PLAYER_SEQUENCE.size()) ? 0xAAAAAA : baseColor; // 已输入部分用灰色

                            guiGraphics.drawString(font, arrow, x + 20 + offsetX, y + 15 + i * 15, color);
                            offsetX += font.width(arrow) + 2; // 字符宽度 + 2像素间隔
                        }
                    }
                }
            }
        }
    }

    private static @NotNull String getCoolDown(long remaining) {
        String cooldownText = "冷却中...";
        if (remaining > 0) {
            long seconds = remaining / 20;
            if (seconds >= 60) {
                // 超过60秒显示为分钟格式
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                cooldownText = "冷却: " + minutes + "m" + remainingSeconds + "s";
            } else if (seconds < 10) {
                // 最后10秒显示小数点
                long decimal = (remaining % 20) * 5; // 转换为0-99的值
                cooldownText = "冷却: " + seconds + "." + String.format("%02d", decimal) + "s";
            } else {
                // 10秒到60秒之间只显示整数秒
                cooldownText = "冷却: " + seconds + "s";
            }
        }
        return cooldownText;
    }
}
