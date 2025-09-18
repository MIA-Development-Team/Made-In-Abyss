package com.altnoir.mia.network;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.abs.IArtifactSkill;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

public record SkillCooldownPayload(int slotIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkillCooldownPayload> TYPE = new CustomPacketPayload.Type<>(MiaUtil.id(MIA.MOD_ID, "skill_cooldown"));
    public static final StreamCodec<ByteBuf, SkillCooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SkillCooldownPayload::slotIndex,
            SkillCooldownPayload::new
    );

    public static void handle(SkillCooldownPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            var curiosInventory = CuriosApi.getCuriosInventory(player);
            if (curiosInventory.isPresent()) {
                var stacksHandler = curiosInventory.get().getStacksHandler("artifact");
                if (stacksHandler.isPresent() && packet.slotIndex() < stacksHandler.get().getSlots()) {
                    ItemStack stack = stacksHandler.get().getStacks().getStackInSlot(packet.slotIndex());
                    // 检查物品是否是技能物品
                    if (stack.getItem() instanceof IArtifactSkill skill) {
                        // 设置冷却时间
                        stack.set(MiaComponents.SKILL_COOLDOWN.get(), skill.getCooldownTicks());
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}