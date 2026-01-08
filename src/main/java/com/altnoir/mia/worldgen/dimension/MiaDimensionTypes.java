package com.altnoir.mia.worldgen.dimension;

import com.altnoir.mia.client.event.ClientDimEffects;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;

public class MiaDimensionTypes {
    public static final ResourceKey<DimensionType> THE_ABYSS_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, MiaUtil.miaId("the_abyss_type"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(THE_ABYSS_TYPE, new DimensionType(
                        OptionalLong.of(6000L), // 固定时间 (注：固定时间之后玩家可随时睡觉)
                        true, // 天空光 （影响天空光照，阳光探测器，幻翼生成，天气）
                        false, // 天花板 （影响玩家重生高度和动物生成）
                        false, // 干旱 （影响放水，滴水石锥转化）
                        true, // 自然 （是否可以睡觉，下界传送门是否生成僵尸猪人）
                        1.0, // 坐标缩放值
                        true, // 床是否可用
                        false, // 重生锚是否可用
                        -256, // 最小Y
                        768, // 高度 (必须为16的倍数)
                        768, // 逻辑高度 (紫颂果的传送和游戏创建的传送门不会超过此高度)
                        BlockTags.INFINIBURN_OVERWORLD, // 火能在哪些方块上永久燃烧
                        ClientDimEffects.ABYSS_EDGE_EFFECTS, // 天空效果
                        0.12F, // 环境光
                        new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)
                )
        );
    }
}
