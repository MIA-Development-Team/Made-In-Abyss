package com.altnoir.mia.network;

import com.altnoir.mia.MIA;
import com.altnoir.mia.item.abs.IArtifactSkill;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

public record SkillPlayPayload(int slotIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkillPlayPayload> TYPE = new CustomPacketPayload.Type<>(MiaUtil.id(MIA.MOD_ID, "execute_skill"));
    public static final StreamCodec<ByteBuf, SkillPlayPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SkillPlayPayload::slotIndex,
            SkillPlayPayload::new
    );

    public static void handle(SkillPlayPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var level = player.level();

            var curiosInventory = CuriosApi.getCuriosInventory(player);
            if (curiosInventory.isPresent()) {
                var stacksHandler = curiosInventory.get().getStacksHandler("artifact");
                if (stacksHandler.isPresent() && packet.slotIndex() < stacksHandler.get().getSlots()) {
                    ItemStack stack = stacksHandler.get().getStacks().getStackInSlot(packet.slotIndex());
                    // 检查物品是否是技能物品
                    if (stack.getItem() instanceof IArtifactSkill skill) {
                        // 执行技能
                        skill.serverSkillPlay(level, player);
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