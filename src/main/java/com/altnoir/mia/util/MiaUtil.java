package com.altnoir.mia.util;

import com.altnoir.mia.MIA;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.emi.emi.api.stack.Comparison;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MiaUtil {
    private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();

    public static MutableComponent literal(String s) {
        return Component.literal(s);
    }

    public static MutableComponent literal(String s, ChatFormatting formatting) {
        return Component.literal(s).withStyle(formatting);
    }

    public static MutableComponent literal(String s, ChatFormatting... formatting) {
        return Component.literal(s).withStyle(formatting);
    }

    public static MutableComponent literal(String s, Style style) {
        return Component.literal(s).setStyle(style);
    }

    public static MutableComponent translatable(String s) {
        return Component.translatable(s);
    }

    public static MutableComponent translatable(String s, ChatFormatting formatting) {
        return Component.translatable(s).withStyle(formatting);
    }

    public static MutableComponent translatable(String s, Object... objects) {
        return Component.translatable(s, objects);
    }

    public static MutableComponent append(MutableComponent text, Component appended) {
        return text.append(appended);
    }

    public static FormattedCharSequence ordered(Component text) {
        return text.getVisualOrderText();
    }

    public static Collection<ResourceLocation> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
        return manager.listResources(prefix, i -> pred.test(i.toString())).keySet();
    }

    public static InputStream getInputStream(Resource resource) {
        try {
            return resource.open();
        } catch (Exception e) {
            return null;
        }
    }

    public static BannerPatternLayers addRandomBanner(BannerPatternLayers patterns, Random random) {
        var bannerRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BANNER_PATTERN);
        return new BannerPatternLayers.Builder().addAll(patterns).add(bannerRegistry.getHolder(random.nextInt(bannerRegistry.size())).get(),
                DyeColor.values()[random.nextInt(DyeColor.values().length)]).build();
    }

    public static boolean canTallFlowerDuplicate(TallFlowerBlock tallFlowerBlock) {
        try {
            return tallFlowerBlock.isValidBonemealTarget(null, null, null) && tallFlowerBlock.isBonemealSuccess(null, null, null, null);
        } catch (Exception e) {
            return false;
        }
    }

    public static void setShader(VertexBuffer buf, Matrix4f mat) {
        buf.bind();
        buf.drawWithShader(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    public static List<BakedQuad> getQuads(BakedModel model) {
        return model.getQuads(null, null, RANDOM);
    }

    public static void draw(BufferBuilder bufferBuilder) {
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public static int getGuiScale(Minecraft client) {
        return (int) client.getWindow().getGuiScale();
    }

    public static void setPositionTexShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void setPositionColorTexShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }

    public static Registry<Item> getItemRegistry() {
        return BuiltInRegistries.ITEM;
    }

    public static Registry<Block> getBlockRegistry() {
        return BuiltInRegistries.BLOCK;
    }

    public static Registry<Fluid> getFluidRegistry() {
        return BuiltInRegistries.FLUID;
    }

    public static Registry<Potion> getPotionRegistry() {
        return BuiltInRegistries.POTION;
    }

    public static Registry<Enchantment> getEnchantmentRegistry() {
        Minecraft client = Minecraft.getInstance();
        return client.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
    }

    public static ItemStack getOutput(Recipe<?> recipe) {
        Minecraft client = Minecraft.getInstance();
        return recipe.getResultItem(client.level.registryAccess());
    }

    public static void focus(EditBox widget, boolean focused) {
        // Also ensure a current focus-element in the screen is cleared if it changes
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.screen != null) {
            var currentFocus = client.screen.getFocused();
            if (!focused && currentFocus == widget || focused && currentFocus != widget) {
                client.screen.setFocused(null);
            }
        }
        widget.setFocused(focused);
    }

    public static Stream<Item> getDisabledItems() {
        Minecraft client = Minecraft.getInstance();
        FeatureFlagSet fs = client.level.enabledFeatures();
        return getItemRegistry().stream().filter(i -> !i.isEnabled(fs));
    }

    public static String getBlockPath(Block block) {
        return Objects.requireNonNull(getBlockRegistry().getKey(block)).getPath();
    }

    public static String getBlockNamespace(Block block) {
        return Objects.requireNonNull(getBlockRegistry().getKey(block)).getNamespace();
    }

    public static ResourceLocation getBlockLoc(String name, Block block) {
        return id(getBlockNamespace(block), name + getBlockPath(block));
    }

    public static @Nullable RecipeHolder<?> getRecipe(ResourceLocation id) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && id != null) {
            RecipeManager manager = client.level.getRecipeManager();
            if (manager != null) {
                return manager.byKey(id).orElse(null);
            }
        }
        return null;
    }

    public static Comparison compareStrict() {
        return Comparison.compareComponents();
    }


    public static ResourceLocation id(String id) {
        return ResourceLocation.parse(id);
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation miaId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, path);
    }

    public static float format2(double value) {
        long l = (long) (value * 100);
        double d = l / 100D;
        return (float) d;
    }

    public static boolean isCreativeOrSpectator(Player player) {
        return player.isSpectator() || player.isCreative();
    }
}
