package com.altnoir.mementoinabyss.impl.whistle;

import com.altnoir.mementoinabyss.content.item.whistle.WhistleItem;
import com.altnoir.mementoinabyss.impl.whistle.component.WhistleLoadout;
import com.altnoir.mementoinabyss.impl.whistle.component.PlacedWhistleFragment;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.WhistleFragmentItem;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.amplifier.WhistleAmplifierItem;
import com.altnoir.mementoinabyss.impl.whistle.grid.WhistleGrid;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridCell;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridRotation;
import com.altnoir.mementoinabyss.content.item.whistle.skill.WhistleSkillItem;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleSkillContext;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class WhistleApi {
    public static final String SLOT_ID = "whistle";

    public static boolean isWhistle(ItemStack stack) {
        return stack.getItem() instanceof WhistleItem;
    }

    public static boolean isFragment(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WhistleFragmentItem<?>;
    }

    public static Optional<WhistleGrid> grid(ItemStack stack) {
        return stack.getItem() instanceof WhistleItem whistle
                ? Optional.of(whistle.getGrid())
                : Optional.empty();
    }

    public static WhistleLoadout loadout(ItemStack stack) {
        return stack.getOrDefault(MiaDataComponents.WHISTLE_LOADOUT.get(), WhistleLoadout.EMPTY);
    }

    public static boolean canInstall(
            ItemStack whistle,
            ItemStack fragment,
            int x,
            int y,
            GridRotation rotation
    ) {
        if (!isWhistle(whistle) || !isFragment(fragment)) {
            return false;
        }
        return isValid(
                whistle,
                loadout(whistle).with(PlacedWhistleFragment.of(fragment, x, y, rotation))
        );
    }

    public static boolean install(
            ItemStack whistle,
            ItemStack fragment,
            int x,
            int y,
            GridRotation rotation
    ) {
        if (!canInstall(whistle, fragment, x, y, rotation)) {
            return false;
        }
        whistle.set(
                MiaDataComponents.WHISTLE_LOADOUT.get(),
                loadout(whistle).with(PlacedWhistleFragment.of(fragment, x, y, rotation))
        );
        return true;
    }

    public static Optional<ItemStack> removeAt(ItemStack whistle, GridCell cell) {
        WhistleLoadout current = loadout(whistle);
        for (int i = current.fragments().size() - 1; i >= 0; i--) {
            PlacedWhistleFragment placed = current.fragments().get(i);
            if (placed.occupiedCells().contains(cell)) {
                whistle.set(MiaDataComponents.WHISTLE_LOADOUT.get(), current.without(i));
                return Optional.of(placed.createStack());
            }
        }
        return Optional.empty();
    }

    public static Optional<PlacedWhistleFragment> fragmentAt(ItemStack whistle, GridCell cell) {
        return loadout(whistle).fragments().stream()
                .filter(fragment -> fragment.occupiedCells().contains(cell))
                .findFirst();
    }

    public static int usedCells(ItemStack whistle) {
        return occupiedCells(loadout(whistle)).size();
    }

    public static List<PlacedWhistleFragment> activeFragments(ItemStack whistle) {
        WhistleLoadout loadout = loadout(whistle);
        return isValid(whistle, loadout) ? loadout.fragments() : List.of();
    }

    public static List<SkillActivation> skills(ItemStack whistle) {
        WhistleLoadout loadout = loadout(whistle);
        if (!isValid(whistle, loadout)) {
            return List.of();
        }

        List<SkillActivation> result = new ArrayList<>();
        for (int i = 0; i < loadout.fragments().size(); i++) {
            PlacedWhistleFragment placed = loadout.fragments().get(i);
            if (!(placed.fragment().item().value() instanceof WhistleSkillItem skill)) {
                continue;
            }

            double power = 1.0;
            double cooldown = skill.getDefinition().cooldownTicks();
            for (int j = 0; j < loadout.fragments().size(); j++) {
                if (i == j) {
                    continue;
                }
                PlacedWhistleFragment neighbor = loadout.fragments().get(j);
                if (neighbor.fragment().item().value() instanceof WhistleAmplifierItem amplifier
                        && areAdjacent(placed, neighbor)) {
                    power *= amplifier.getDefinition().powerMultiplier();
                    cooldown *= amplifier.getDefinition().cooldownMultiplier();
                }
            }

            power = Math.max(0.1, Math.min(power, 8.0));
            int cooldownTicks = Math.max(1, Math.min((int) Math.round(cooldown), 72_000));
            result.add(new SkillActivation(
                    i,
                    placed,
                    skill,
                    placed.createStack(),
                    power,
                    cooldownTicks
            ));
        }
        return List.copyOf(result);
    }

    public static Optional<ItemStack> equippedWhistle(net.minecraft.world.entity.LivingEntity entity) {
        var curios = CuriosApi.getCuriosInventory(entity);
        if (curios.isEmpty()) {
            return Optional.empty();
        }
        var handler = curios.get().getStacksHandler(SLOT_ID);
        if (handler.isEmpty()) {
            return Optional.empty();
        }
        for (int i = 0; i < handler.get().getSlots(); i++) {
            ItemStack stack = handler.get().getStacks().getStackInSlot(i);
            if (isWhistle(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static boolean activate(ServerPlayer player, int fragmentIndex) {
        Optional<ItemStack> equipped = equippedWhistle(player);
        if (equipped.isEmpty()) {
            return false;
        }
        for (SkillActivation activation : skills(equipped.get())) {
            if (activation.fragmentIndex() != fragmentIndex
                    || player.getCooldowns().isOnCooldown(activation.stack())) {
                continue;
            }
            activation.skill().activate(new WhistleSkillContext(
                    player,
                    equipped.get(),
                    activation.fragment(),
                    activation.powerMultiplier(),
                    activation.cooldownTicks()
            ));
            player.getCooldowns().addCooldown(activation.stack(), activation.cooldownTicks());
            return true;
        }
        return false;
    }

    public static boolean isValid(ItemStack whistle, WhistleLoadout loadout) {
        Optional<WhistleGrid> grid = grid(whistle);
        if (grid.isEmpty() || loadout.fragments().size() > 64) {
            return false;
        }

        Set<GridCell> occupied = new HashSet<>();
        Set<Item> uniqueFragments = new HashSet<>();
        for (PlacedWhistleFragment placed : loadout.fragments()) {
            if (!(placed.fragment().item().value() instanceof WhistleFragmentItem<?> fragmentItem)) {
                return false;
            }
            Set<GridCell> cells = placed.occupiedCells();
            if (cells.isEmpty()) {
                return false;
            }
            if (fragmentItem.getDefinition().unique()
                    && !uniqueFragments.add(fragmentItem)) {
                return false;
            }
            for (GridCell cell : cells) {
                if (!grid.get().accepts(cell) || !occupied.add(cell)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Set<GridCell> occupiedCells(WhistleLoadout loadout) {
        Set<GridCell> cells = new HashSet<>();
        for (PlacedWhistleFragment fragment : loadout.fragments()) {
            cells.addAll(fragment.occupiedCells());
        }
        return cells;
    }

    private static boolean areAdjacent(
            PlacedWhistleFragment first,
            PlacedWhistleFragment second
    ) {
        Set<GridCell> secondCells = second.occupiedCells();
        for (GridCell cell : first.occupiedCells()) {
            if (secondCells.contains(cell.offset(1, 0))
                    || secondCells.contains(cell.offset(-1, 0))
                    || secondCells.contains(cell.offset(0, 1))
                    || secondCells.contains(cell.offset(0, -1))) {
                return true;
            }
        }
        return false;
    }

    public record SkillActivation(
            int fragmentIndex,
            PlacedWhistleFragment fragment,
            WhistleSkillItem skill,
            ItemStack stack,
            double powerMultiplier,
            int cooldownTicks
    ) {}

    private WhistleApi() {}
}
