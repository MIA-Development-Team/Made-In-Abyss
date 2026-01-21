package com.altnoir.mia.compat.emi;

import com.altnoir.mia.common.recipe.LampTubeRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LampTubeEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    public LampTubeEmiRecipe(RecipeHolder<LampTubeRecipe> recipe) {
        this.id = recipe.id();
        this.input = List.of(EmiIngredient.of(
                recipe.value().getIngredients().getFirst(),
                recipe.value().ingredient().count()
        ));
        this.output = List.of(EmiStack.of(recipe.value().getResultItem(null)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return MiaEmiPlugin.LAMP_TUBE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 76;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // 添加箭头纹理来指示处理过程
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);

        // 在左侧增加一个输入槽
        widgets.addSlot(input.getFirst(), 0, 0);

        // 在右侧增加一个输出槽位
        // 请注意，输出槽需要调用‘ recipeContext ’来通知EMI它们的配方上下文
        // 这包括能够解析配方树、带有配方上下文的最喜欢的堆栈等等
        widgets.addSlot(output.getFirst(), 58, 0).recipeContext(this);
    }
}
