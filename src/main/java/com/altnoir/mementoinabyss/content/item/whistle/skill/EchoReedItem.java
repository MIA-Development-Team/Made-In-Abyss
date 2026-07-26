package com.altnoir.mementoinabyss.content.item.whistle.skill;

import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillContext;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillDefinition;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class EchoReedItem extends WhistleSkillItem {
    private static final double BASE_RANGE = 12.0;
    private static final int BASE_DURATION = 100;

    public EchoReedItem(Properties properties, WhistleSkillDefinition definition) {
        super(properties, definition);
    }

    @Override
    public void activate(WhistleSkillContext context) {
        double range = BASE_RANGE * context.powerMultiplier();
        int duration = Math.max(1, (int) Math.round(BASE_DURATION * context.powerMultiplier()));
        context.player().level().playSound(
                null,
                context.player().blockPosition(),
                SoundEvents.NOTE_BLOCK_CHIME.value(),
                SoundSource.PLAYERS,
                0.8F,
                1.25F
        );
        for (Monster monster : context.player().level().getEntitiesOfClass(
                Monster.class,
                context.player().getBoundingBox().inflate(range),
                Monster::isAlive
        )) {
            monster.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    duration,
                    0,
                    true,
                    false
            ));
        }
    }
}
