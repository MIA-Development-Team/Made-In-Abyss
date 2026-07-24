package com.altnoir.mementoinabyss.content.artifact.enhancement;

import com.altnoir.mementoinabyss.content.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaMenus;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class ArtifactEnhancementMenu extends AbstractContainerMenu {
    public static final int STATUS_INSERT_ARTIFACT = 0;
    public static final int STATUS_INSERT_MATERIAL = 1;
    public static final int STATUS_INVALID_MATERIAL = 2;
    public static final int STATUS_READY = 3;
    public static final int STATUS_MAX_LEVEL = 4;

    private static final int ARTIFACT_SLOT = 0;
    private static final int MATERIAL_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot status = DataSlot.standalone();
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final SimpleContainer artifactContainer = notifyingContainer();
    private final SimpleContainer materialContainer = notifyingContainer();
    private final ResultContainer resultContainer = new ResultContainer();
    private final Slot artifactSlot;
    private final Slot materialSlot;
    private final Slot resultSlot;
    private RecipeHolder<ArtifactEnhancementRecipe> selectedRecipe;

    public ArtifactEnhancementMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public ArtifactEnhancementMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access
    ) {
        super(MiaMenus.ARTIFACT_ENHANCEMENT.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        this.status.set(STATUS_INSERT_ARTIFACT);
        this.selectedRecipeIndex.set(-1);

        this.artifactSlot = addSlot(new Slot(artifactContainer, 0, 20, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArtifactApi.isEnhanceable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.materialSlot = addSlot(new Slot(materialContainer, 0, 20, 66));
        this.resultSlot = addSlot(new Slot(resultContainer, 0, 143, 37) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return hasItem();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                RecipeHolder<ArtifactEnhancementRecipe> recipe = selectedRecipe;
                if (recipe == null) return;
                int materialCount = recipe.value().materialCount();
                materialSlot.remove(materialCount);
                artifactSlot.set(ItemStack.EMPTY);
                player.onEnchantmentPerformed(stack, 0);
                stack.onCraftedBy(player, stack.getCount());
                super.onTake(player, stack);
                updateResult();
            }
        });

        addStandardInventorySlots(inventory, 8, 102);
        addDataSlot(status);
        addDataSlot(selectedRecipeIndex);
    }

    private SimpleContainer notifyingContainer() {
        return new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                ArtifactEnhancementMenu.this.slotsChanged(this);
            }
        };
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == artifactContainer || container == materialContainer) {
            updateResult();
        }
    }

    private void updateResult() {
        selectedRecipe = null;
        selectedRecipeIndex.set(-1);
        resultContainer.setRecipeUsed(null);
        resultSlot.set(ItemStack.EMPTY);

        ItemStack artifact = artifactSlot.getItem();
        if (artifact.isEmpty()) {
            status.set(STATUS_INSERT_ARTIFACT);
        } else if (!ArtifactApi.canEnhance(artifact)) {
            status.set(STATUS_MAX_LEVEL);
        } else if (materialSlot.getItem().isEmpty()) {
            status.set(STATUS_INSERT_MATERIAL);
        } else {
            List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = recipes();
            for (int i = 0; i < recipes.size(); i++) {
                RecipeHolder<ArtifactEnhancementRecipe> candidate = recipes.get(i);
                if (candidate.value().matches(createInput(RandomSource.create(0L)), player.level())) {
                    selectedRecipe = candidate;
                    selectedRecipeIndex.set(i);
                    break;
                }
            }
            if (selectedRecipe == null) {
                status.set(STATUS_INVALID_MATERIAL);
            } else {
                ItemStack result = selectedRecipe.value().assemble(
                        createInput(lockedRandom(selectedRecipe))
                );
                if (!result.isEmpty()) {
                    resultContainer.setRecipeUsed(selectedRecipe);
                    resultSlot.set(result);
                    status.set(STATUS_READY);
                } else {
                    selectedRecipe = null;
                    selectedRecipeIndex.set(-1);
                    status.set(STATUS_INVALID_MATERIAL);
                }
            }
        }
        broadcastChanges();
    }

    private ArtifactEnhancementRecipeInput createInput(RandomSource random) {
        return new ArtifactEnhancementRecipeInput(
                artifactSlot.getItem(),
                materialSlot.getItem(),
                random
        );
    }

    private RandomSource lockedRandom(RecipeHolder<ArtifactEnhancementRecipe> recipe) {
        long playerSeed = Integer.toUnsignedLong(player.getEnchantmentSeed());
        long recipeSeed = Integer.toUnsignedLong(recipe.id().identifier().hashCode());
        return RandomSource.create(playerSeed ^ recipeSeed << 32 ^ recipeSeed);
    }

    private List<RecipeHolder<ArtifactEnhancementRecipe>> recipes() {
        var server = player.level().getServer();
        if (server == null) return java.util.List.of();
        return server.getRecipeManager().recipeMap().byType(MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get()).stream()
                .sorted(Comparator.comparing(holder -> holder.id().identifier()))
                .toList();
    }

    public int status() {
        return status.get();
    }

    public int selectedRecipeIndex() {
        return selectedRecipeIndex.get();
    }

    public boolean hasEnhanceableArtifact() {
        return ArtifactApi.canEnhance(artifactSlot.getItem());
    }

    public boolean hasMaterial(ArtifactEnhancementRecipe recipe) {
        int found = materialSlot.getItem().is(recipe.material())
                ? materialSlot.getItem().getCount() : 0;
        for (int i = 0; i < Inventory.INVENTORY_SIZE && found < recipe.materialCount(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(recipe.material())) {
                found += stack.getCount();
            }
        }
        return found >= recipe.materialCount();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (player.level().isClientSide()) {
            return buttonId >= 0;
        }
        List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = recipes();
        if (buttonId < 0 || buttonId >= recipes.size()) {
            return false;
        }
        ArtifactEnhancementRecipe recipe = recipes.get(buttonId).value();
        if (!hasMaterial(recipe)) {
            return false;
        }
        return putMaterialInSlot(recipe);
    }

    private boolean putMaterialInSlot(ArtifactEnhancementRecipe recipe) {
        ItemStack current = materialSlot.getItem();
        int alreadyPresent = current.is(recipe.material()) ? current.getCount() : 0;
        if (!current.isEmpty() && !current.is(recipe.material())) {
            if (!canReturnCurrentMaterial(recipe, current)) {
                return false;
            }
            ItemStack returned = current.copy();
            materialSlot.set(ItemStack.EMPTY);
            int remaining = takeMaterialFromInventory(recipe, recipe.materialCount());
            player.getInventory().placeItemBackInInventory(returned);
            materialSlot.set(new ItemStack(
                    recipe.material(),
                    recipe.materialCount() - remaining
            ));
            player.getInventory().setChanged();
            broadcastChanges();
            return remaining == 0;
        }

        int toTake = Math.max(0, recipe.materialCount() - alreadyPresent);
        toTake = takeMaterialFromInventory(recipe, toTake);
        if (alreadyPresent == 0) {
            materialSlot.set(new ItemStack(recipe.material(), recipe.materialCount() - toTake));
        } else {
            int added = recipe.materialCount() - alreadyPresent - toTake;
            if (added > 0) {
                current.grow(added);
            }
            materialSlot.setChanged();
        }
        player.getInventory().setChanged();
        broadcastChanges();
        return toTake == 0;
    }

    private int takeMaterialFromInventory(
            ArtifactEnhancementRecipe recipe,
            int toTake
    ) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE && toTake > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(recipe.material())) {
                continue;
            }
            int removed = Math.min(stack.getCount(), toTake);
            stack.shrink(removed);
            toTake -= removed;
        }
        return toTake;
    }

    private boolean canReturnCurrentMaterial(
            ArtifactEnhancementRecipe recipe,
            ItemStack current
    ) {
        int targetRemaining = recipe.materialCount();
        int returnCapacity = 0;
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            int projectedCount = stack.getCount();
            if (stack.is(recipe.material()) && targetRemaining > 0) {
                int removed = Math.min(projectedCount, targetRemaining);
                projectedCount -= removed;
                targetRemaining -= removed;
            }
            if (projectedCount == 0) {
                returnCapacity += current.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, current)) {
                returnCapacity += Math.max(0, stack.getMaxStackSize() - projectedCount);
            }
            if (returnCapacity >= current.getCount()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, MiaBlocks.ARTIFACT_SMITHING_TABLE.get());
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            Item item = stack.getItem();
            copied = stack.copy();
            if (index == RESULT_SLOT) {
                item.onCraftedBy(stack, player);
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copied);
            } else if (index == ARTIFACT_SLOT || index == MATERIAL_SLOT) {
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ArtifactApi.isEnhanceable(stack)) {
                if (!moveItemStackTo(stack, ARTIFACT_SLOT, ARTIFACT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isEnhancementMaterial(stack)) {
                if (!moveItemStackTo(stack, MATERIAL_SLOT, MATERIAL_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INVENTORY_START && index < INVENTORY_END) {
                if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_START && index < HOTBAR_END
                    && !moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            slot.setChanged();
            if (stack.getCount() == copied.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
            if (index == RESULT_SLOT) player.drop(stack, false);
        }
        return copied;
    }

    private boolean isEnhancementMaterial(ItemStack stack) {
        return recipes().stream().anyMatch(holder -> holder.value().isMaterial(stack));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        resultContainer.removeItemNoUpdate(0);
        access.execute((level, pos) -> {
            clearContainer(player, artifactContainer);
            clearContainer(player, materialContainer);
        });
    }
}
