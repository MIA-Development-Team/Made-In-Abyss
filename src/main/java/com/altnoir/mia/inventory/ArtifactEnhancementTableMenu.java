package com.altnoir.mia.inventory;

import java.util.ArrayList;
import java.util.List;
import com.altnoir.mia.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaMenus;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.item.EnhanceableArtifact;
import com.altnoir.mia.recipe.ArtifactEnhancementRecipe;
import com.altnoir.mia.recipe.ArtifactEnhancementRecipeInput;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class ArtifactEnhancementTableMenu extends AbstractContainerMenu {

    private static final int ARTIFACT_SLOT_INDEX = 0;
    private static final int MATERIAL_SLOT_INDEX = 1;
    private static final int RESULT_SLOT_INDEX = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final Slot materialSlot;
    private final Slot artifactSlot;
    private final Slot resultSlot;
    public final ResultContainer resultContainer;
    public final Container artifactContainer;
    public final Container materialContainer;

    private final Level level;
    private final Player player;
    protected final ContainerLevelAccess access;

    private final DataSlot selectedRecipeIndex;
    private List<RecipeHolder<ArtifactEnhancementRecipe>> availableRecipes;
    private List<RecipeHolder<ArtifactEnhancementRecipe>> unavailableRecipes;

    private final List<RecipeHolder<ArtifactEnhancementRecipe>> allRecipes;

    Runnable slotUpdateListener;

    public ArtifactEnhancementTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ArtifactEnhancementTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(MiaMenus.ARTIFACT_ENHANCEMENT_TABLE.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;
        this.level = playerInventory.player.level();
        this.availableRecipes = new ArrayList<>();
        this.unavailableRecipes = new ArrayList<>();
        this.selectedRecipeIndex = DataSlot.standalone();
        this.selectedRecipeIndex.set(-1);
        this.allRecipes = this.level.getRecipeManager().getAllRecipesFor(MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get());
        this.slotUpdateListener = () -> {
        };
        this.artifactContainer = new TransientCraftingContainer(this, 1, 1);
        this.materialContainer = new TransientCraftingContainer(this, 1, 1);
        this.resultContainer = new ResultContainer();
        this.artifactSlot = this.addSlot(new Slot(this.artifactContainer, 0, 20, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return allRecipes.stream().anyMatch(recipe -> recipe.value().isArtifactIngredient(stack));
            }
        });
        this.materialSlot = this.addSlot(new Slot(this.materialContainer, 0, 20, 48) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                selectedRecipeIndex.set(-1);
                setupResultSlot();
            }
        });
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                materialSlot.set(ItemStack.EMPTY);
                artifactSlot.set(ItemStack.EMPTY);
            }

            @Override
            public boolean mayPickup(Player player) {
                return selectedRecipeIndex.get() >= 0 && selectedRecipeIndex.get() < availableRecipes.size();
            }
        });

        int k;
        for (k = 0; k < 3; ++k) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
            }
        }

        for (k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

    }

    // select the recipe
    @Override
    public boolean clickMenuButton(Player player, int recipeIndex) {
        if (recipeIndex >= 0 && recipeIndex < this.availableRecipes.size()
                && recipeIndex != this.selectedRecipeIndex.get()) {
            if (playerHasMaterial(this.availableRecipes.get(recipeIndex).value())) {
                this.selectedRecipeIndex.set(recipeIndex);
                this.setupResultSlot();
                this.clearMaterialSlot();
                this.takeMaterialFromPlayer(this.availableRecipes.get(recipeIndex).value());
                return true;
            }
        }
        return super.clickMenuButton(player, recipeIndex);
    }

    @Override
    public void slotsChanged(Container inventory) {
        // if input slot changes, reset the recipe list and the result slot
        if (inventory == this.artifactContainer) {
            // clear avaliable recipes and selected recipes
            this.availableRecipes.clear();
            this.unavailableRecipes.clear();
            this.selectedRecipeIndex.set(-1);
            // clear result slot
            this.resultSlot.set(ItemStack.EMPTY);
            // clear material slot
            this.clearMaterialSlot();
            // add all possible enhancement recipe
            ItemStack artifact = this.artifactSlot.getItem();
            if (inputHasEnhancementRecipe()) {
                for (RecipeHolder<ArtifactEnhancementRecipe> recipe : allRecipes) {
                    if (recipe.value().isArtifactIngredient(artifact)) {
                        if (playerHasMaterial(recipe.value())) {
                            this.availableRecipes.add(recipe);
                        } else {
                            this.unavailableRecipes.add(recipe);
                        }
                    }
                }
            }
        }
        this.slotUpdateListener.run();
    }

    private void setupResultSlot() {
        if (!this.availableRecipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            RecipeHolder<ArtifactEnhancementRecipe> recipeholder = this.availableRecipes
                    .get(this.selectedRecipeIndex.get());
            ItemStack itemstack = recipeholder.value().assemble(createRecipeInput(), this.level.registryAccess());
            if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
                this.resultContainer.setRecipeUsed(recipeholder);
                this.resultSlot.set(itemstack);
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack selectedStack = slot.getItem();
            Item item = selectedStack.getItem();
            returnStack = selectedStack.copy();
            //
            if (index == RESULT_SLOT_INDEX) {
                item.onCraftedBy(selectedStack, player.level(), player);
                if (!this.moveItemStackTo(selectedStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(selectedStack, returnStack);
            } else if (index == ARTIFACT_SLOT_INDEX) {
                if (!this.moveItemStackTo(selectedStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == MATERIAL_SLOT_INDEX) {
                if (!this.moveItemStackTo(selectedStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (canMoveIntoArtifactSlots(selectedStack)) {
                if (!this.moveItemStackTo(selectedStack, ARTIFACT_SLOT_INDEX, ARTIFACT_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                if (!this.moveItemStackTo(selectedStack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END
                    && !this.moveItemStackTo(selectedStack, INV_SLOT_START, INV_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (selectedStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (selectedStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, selectedStack);
            this.broadcastChanges();
        }

        return returnStack;
    }

    public boolean canMoveIntoArtifactSlots(ItemStack stack) {
        return this.allRecipes.stream()
                .map(RecipeHolder::value).anyMatch(recipe -> recipe.isArtifactIngredient(stack));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    private ArtifactEnhancementRecipeInput createRecipeInput() {
        return new ArtifactEnhancementRecipeInput(this.artifactSlot.getItem(), this.materialSlot.getItem());
    }

    public void registerUpdateListener(Runnable listener) {
        this.slotUpdateListener = listener;
    }

    public List<RecipeHolder<ArtifactEnhancementRecipe>> getAvailableRecipes() {
        return this.availableRecipes;
    }

    public List<RecipeHolder<ArtifactEnhancementRecipe>> getUnavailableRecipes() {
        return this.unavailableRecipes;
    }

    public RecipeHolder<ArtifactEnhancementRecipe> getSelectedRecipe() {
        if (this.getSelectedRecipeIndex() == -1 || this.getSelectedRecipeIndex() >= this.availableRecipes.size()) {
            return null;
        }
        return this.availableRecipes.get(this.selectedRecipeIndex.get());
    }

    public int getNumRecipes() {
        return this.availableRecipes.size() + this.unavailableRecipes.size();
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex.get();
    }

    public boolean inputHasEnhancementRecipe() {
        ItemStack artifactSlotItem = this.artifactSlot.getItem();
        if (artifactSlotItem.isEmpty()) {
            return false;
        }
        if (artifactSlotItem.getItem() instanceof EnhanceableArtifact artifactType) {
            if (!allRecipes.stream().anyMatch(recipe -> recipe.value().isArtifactIngredient(artifactSlotItem))) {
                return false;
            }
            if (artifactSlotItem
                    .get(MiaComponents.ARTIFACT_ENHANCEMENT.get()) instanceof ArtifactEnhancementComponent stats
                    && stats.getLevel() < artifactType.getMaxLevel()) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidRecipeIndex(int recipeIndex) {
        return recipeIndex >= 0 && recipeIndex < this.availableRecipes.size();
    }

    @Override
    public boolean stillValid(Player player) {
        return (Boolean) this.access.evaluate((level, pos) -> {
            return !level.getBlockState(pos).is(MiaBlocks.ARTIFACT_ENHANCEMENT_TABLE) ? false
                    : player.canInteractWithBlock(pos, 4.0);
        }, true);
    }

    private boolean playerHasMaterial(ArtifactEnhancementRecipe recipe) {
        ItemStack required = recipe.getMaterial();
        int needed = required.getCount();
        int found = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && recipe.isMaterialIngredient(stack)) {
                found += stack.getCount();
                if (found >= needed) {
                    return true;
                }
            }
        }
        return false;
    }

    // try to take as much item possible from the player to fulfill the recipe
    // requirement
    private void takeMaterialFromPlayer(ArtifactEnhancementRecipe recipe) {

        ItemStack required = recipe.getMaterial();
        int toTake = required.getCount();

        for (int i = 0; i < player.getInventory().getContainerSize() && toTake > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && recipe.isMaterialIngredient(stack)) {
                int removed = Math.min(stack.getCount(), toTake);
                stack.shrink(removed);
                toTake -= removed;
            }
        }
        this.materialSlot.set(required.copyWithCount(required.getCount() - toTake));
        this.materialSlot.setChanged();
    }

    private void clearMaterialSlot() {
        // clear material slot
        ItemStack material = materialSlot.getItem();
        if (!material.isEmpty()) {
            player.getInventory().placeItemBackInInventory(material);
            materialSlot.set(ItemStack.EMPTY);
            materialSlot.setChanged();
        }
    }

    @Override
    public MenuType<?> getType() {
        return MiaMenus.ARTIFACT_ENHANCEMENT_TABLE.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.artifactContainer);
            this.clearContainer(player, this.materialContainer);
        });
    }

}
