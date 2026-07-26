package com.altnoir.mementoinabyss.impl.artifact;

import com.altnoir.mementoinabyss.init.MiaDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ArtifactApi {
    public static final String SLOT_ID = "artifact";

    public static boolean isArtifact(ItemStack stack) {
        return stack.has(MiaDataComponents.ARTIFACT_PROFILE.get());
    }

    public static Optional<ArtifactProfile> profile(ItemStack stack) {
        return Optional.ofNullable(stack.get(MiaDataComponents.ARTIFACT_PROFILE.get()));
    }

    public static ArtifactEnhancement enhancement(ItemStack stack) {
        return stack.getOrDefault(MiaDataComponents.ARTIFACT_ENHANCEMENT.get(), ArtifactEnhancement.EMPTY);
    }

    public static boolean isEnhanceable(ItemStack stack) {
        return isArtifact(stack) && stack.has(MiaDataComponents.ARTIFACT_ENHANCEMENT.get());
    }

    public static boolean canEnhance(ItemStack stack) {
        ArtifactProfile profile = stack.get(MiaDataComponents.ARTIFACT_PROFILE.get());
        return profile != null
                && isEnhanceable(stack)
                && enhancement(stack).level() < profile.maxEnhancementLevel();
    }

    public static boolean setEnhancement(ItemStack stack, ArtifactEnhancement enhancement) {
        ArtifactProfile profile = stack.get(MiaDataComponents.ARTIFACT_PROFILE.get());
        if (profile == null
                || !stack.has(MiaDataComponents.ARTIFACT_ENHANCEMENT.get())
                || enhancement.level() > profile.maxEnhancementLevel()) {
            return false;
        }
        stack.set(MiaDataComponents.ARTIFACT_ENHANCEMENT.get(), enhancement);
        return true;
    }

    public static boolean addEnhancement(
            ItemStack stack,
            Holder<Attribute> attribute,
            Identifier modifierId,
            double amount,
            AttributeModifier.Operation operation
    ) {
        ArtifactEnhancement current = enhancement(stack);
        return setEnhancement(
                stack,
                current.addAttribute(attribute, modifierId, amount, operation)
                        .withLevel(current.level() + 1)
        );
    }

    private ArtifactApi() {}
}
