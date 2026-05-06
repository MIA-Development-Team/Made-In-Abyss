package com.altnoir.mementoinabyss.impl.curse;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.Nullable;

public class CurseAttachment implements ValueIOSerializable {
    @Getter
    @Nullable
    private final LivingEntity owner;

    @Getter
    @Setter
    private int minY = 0;

    @Getter
    @Setter
    private int level = 0;

    @Getter
    @Setter
    private int maxLevel = 10;

    public static final StreamCodec<ByteBuf, CurseAttachment> STREAM_CODEC =
            StreamCodec.ofMember(CurseAttachment::encode, CurseAttachment::new);

    public CurseAttachment(ByteBuf buffer) {
        this.owner = null;
        this.minY = buffer.readInt();
        this.level = buffer.readInt();
        this.maxLevel = buffer.readInt();
    }

    public CurseAttachment(IAttachmentHolder attachmentHolder) {
        this.owner = attachmentHolder instanceof LivingEntity ? (LivingEntity) attachmentHolder : null;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("minY", this.minY);
        output.putInt("level", this.level);
        output.putInt("maxLevel", this.maxLevel);
    }

    @Override
    public void deserialize(ValueInput input) {
        minY = input.getIntOr("minY", this.minY);
        level = input.getIntOr("level", this.level);
        maxLevel = input.getIntOr("maxLevel", this.maxLevel);
    }

    public void copyFrom(CurseAttachment other) {
        setMaxLevel(other.getMaxLevel());
    }

    public void encode(ByteBuf buffer) {
        buffer.writeInt(this.minY);
        buffer.writeInt(this.level);
        buffer.writeInt(this.maxLevel);
    }
}
