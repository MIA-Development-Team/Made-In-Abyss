# 主世界深渊：本轮修改交接（供 GPT 审查）

本文是给 GPT 审查用的交接说明，记录在主世界深渊上「改了什么、为什么改、怎么验证、还有什么没验证」。原有交接文档是 `docs/overworld-abyss-handoff.md`，功能说明是 `docs/overworld-abyss.md`。

第 0–5 节是**骨架那一轮**的交接（骨架、边界守恒、出生点、守卫测试），其中的数字已按陡化那一轮与本轮的实测更新。**本轮的改动是坡面这条线：外坡带宽度做成可配置的 `abyssSlopeBand`、默认 `1.25`，记录在第 6 节**；6.2、6.4、6.5 里保留陡化那一轮（把影响半径收窄到 `3R`）的缺陷、文件与回退记录作为背景，6.1、6.3、6.6 已按本轮更新。请连同第 6 节一起审查。

## 0. 结论摘要

骨架那一轮（GPT）完成了深渊的骨架并留下两条明确的遗留问题；陡化那一轮把它们全部落地，另外补了出生点搜索的安全性与成本问题，以及一组不依赖服务器的守卫测试：

1. **边界守恒（最重要）**：开洞只对洞口负责，`preliminary_surface_level`（含水层、地表规则、结构高度）在影响半径外必须读原始值。那一轮新增原版高度缓存 + 两处调用点重写，并补了逐点比对测试。
2. **地表高度报告与密度自洽**：`surfaceHeight` 改成「扫描组合后密度的最高实心方块 Y」，`lerp` 改成端点精确、`outerWeight` 改成严格归零，消除了外坡边界处的一格错位与跨界非零权重。（上一版这里写的是 2R/4R，那是更早一轮的影响半径；陡化那一轮先收窄到 `3R`，本轮把坡带宽度做成可配置、默认落在 `3.25R`。）
3. **深渊识别不再依赖 `instanceof`**：改为按 `RandomState` 身份查找（`INSTALLED` 映射 + `holeOf`），其他模组二次包装 `finalDensity()` 不会让含水层重新灌水。
4. **出生点搜索**：先做一次廉价的高度判定再进区块内 64 点遍历；全环搜索失败时退到 4R+64 的四个正轴向点；安全判定排除岩浆/火/细雪/仙人掌/浆果丛等伤害方块；床与重生锚始终不碰。
5. **测试**：28 个测试全绿（16 密度单元 + 4 真实区块生成链 + 8 新增守卫与出生半径）。

**状态**：`compileJava` / `compileTestJava` / `test` 全部通过；**没有任何游戏内实测**，也没有第三方地形模组联调、没有性能基准。

## 1. 关键文件与修改内容

### 1.1 `src/main/java/com/altnoir/mementoinabyss/infrastructure/worldgen/density/OverworldAbyssShape.java`

- `outerWeight(distance)` 的位置是 `clamp((distance - 2R) / (slopeBand * R), 0, 1)`，再 `1.0 - smooth(Math.pow(position, outerSteepness))`：位置被夹紧到 0–1 之后，`distance >= outerLimit()` 处权重**精确**为 `0.0`，调用方不可能把 smoothstep 端点的舍入残渣误读成「深渊还伸到这里」而去采样整套雕刻图。`outerLimit()` 陡化那一轮已从 `4R` 收窄为 `3R`，本轮改成 `(2.0 + slopeBand) * radius`（默认 `3.25R`，见第 6 节），而且 `affects()` 用的就是 `outerLimit()`，影响半径只有这一个来源。
- `lerp(t, a, b)` 端点精确：`t <= 0` 直接返回 `a`，`t >= 1` 直接返回 `b`，否则 `a + t * (b - a)`。**这是陡化那一轮定位到的一个真实缺陷**：`a + 1.0 * (b - a)` 在二进制浮点下可能比 `b` 少 1 ULP，导致地表报告与密度在边界列上差一格。
- 其余（`innerHeight`、`ridgeHeight`、`isOpening`、`affects`、值噪声哈希）未改。

### 1.2 `.../density/OverworldAbyssHole.java`

- `terrain(context)`（`OverworldAbyssHole.java:54-78`）：`distance <= R` 返回 `-1.0`（空气，`:58`）；否则**只有一条混合路径**——`weight = shape.outerWeight(distance)`（`:65`）、`top`（`:66`，方法体 `:91-96`）= `lerp(weight, max(minY - 1, estimate), innerHeight)`（世界底夹紧作用在混合**内部**的预估值上）、`target = clamp((top - y)/32, -1, 1)`（`:72`）、`lerp(weight, input.compute(context), target)`（`:77`）。**这里没有 `<= 2R` 分支**：`R..2R` 看起来像「纯漏斗壁」只是因为该区间 `outerWeight == 1.0`，`lerp` 恒返回 `target`。**修复点**：原实现在 `2R` 内把目标高度直接插值到 `ridgeHeight`，等于把漏斗当成实心锥，`R..2R` 整段被填成岩石（底部半径处高约 310 格）；现在锥壁只跟随 `innerHeight`，只有外坡才升到环缘。
- `surfaceHeight(context)`（`OverworldAbyssHole.java:116-138`）：三分支——`distance <= R` 返回 `minY - 1`（`:121`）；`weight <= 0` 返回 `floor(estimate(x, z, context))`（`:126`）；**其余在同一方法内联逐格下扫**（`:134-136`，`for (int y = maxY - 1; y >= minY; y--)` 命中第一个 `terrain(...) > 0`），兜底返回 `minY - 1`。**本仓库没有任何名为 `surfaceHeightScan` 的方法**，上一版文档这么写是错的，行为本身没变。**修复点**：原实现用 `ceil(...) - 1` 并插值到环缘，导致报告值比密度高一格，含水层据此看到悬崖。**陡化那一轮又修了两处**（见第 6.2 节）：`originalSurface` 必须在 `y = 0` 处按**高度**读取（`:104-108`，非 0 的 y 会用 `SinglePointContext(x, 0, z)`）、且必须在权重归零时短路，否则报告值不再是密度的最高实心方块。
- 逐格扫描（内联在 `surfaceHeight` 里）从 `maxY - 1` 下扫、步长 1，返回第一个 `terrain(...) > 0` 的 Y。原来是步长 8 再向上修正，会跳过其他模组注入的薄实心层；扫描只在初步地表高度的 4 格网格上被调用。**陡化那一轮确认这是唯一正确的做法**：混合两个密度得到的是零交叉点的混合，不是高度的混合，实测在 x=450 处「混合高度」报 234 而密度实测停到 227、x=500 处报 192 而实测 162，所以这里只能扫描组合后的密度。本轮补记了一条更明确的说法与一组新实测，见 6.2 第 3 条。
- `compute(context)`：影响半径外用 `input`（地形）或 `floor(originalSurface)`（地表）原样委托；开洞内走 `terrain` / `surfaceHeight`。
- record 新增 `originalSurface` 分量：`OverworldAbyssHole(input, surface, originalSurface, shape, surfaceOutput)`，`surface` 分量仅用于 codec/`mapAll` 保留子图，不再参与计算。

### 1.3 `.../density/AbyssOriginalSurface.java`（新）+ 两处 mixin

- 新接口 `AbyssOriginalSurface`，唯一方法 `@Nullable DensityFunction mia$originalPreliminarySurface()`，由 `NoiseChunkMixin` 实现。
- `src/main/java/com/altnoir/mementoinabyss/mixin/worldgen/NoiseChunkMixin.java`：在 `Aquifer.create(...)` 调用点注入 `mia$captureOriginalSurface`，把 `hole.originalSurface().mapAll(chunk::mia$wrap)`（即原版估算，已换成该区块自己的 `Cache2D`）存进 `@Unique` 字段；`mia$wrap` 是 `@Invoker("wrap")` 桥，因为 `NoiseChunk.wrap` 是 `protected`，外部包无法直接调用。同时在 `<init>` RETURN 注入 `mia$keepOpeningDry` 安装 `AbyssAquifer`。
- `src/main/java/com/altnoir/mementoinabyss/mixin/worldgen/AquiferMixin.java`（新）：`@WrapOperation` 重写 `NoiseBasedAquifer` 的两处原始高度查询——构造函数里的 `maxPreliminarySurfaceLevel(IIII)` 与 `computeFluid` 里的 `preliminarySurfaceLevel(II)`。**为什么必须改**：含水层用邻居列（最多 ±3 区块）的初步地表高度决定流体面，读到被挖空的高度就会在影响半径外挖出/灌水。
- `src/main/java/com/altnoir/mementoinabyss/mixin/worldgen/SurfaceRulesContextMixin.java`（新）：`@WrapOperation` 重写 `SurfaceRules$Context.getMinSurfaceLevel` 里的 `preliminarySurfaceLevel(II)`（`javap` 确认恰好 4 处，对应 4 个网格角）。**为什么必须改**：`getMinSurfaceLevel` 用 4 个角插值，半径外的列会读到半径内的角，改变地表材质/冰山。原来用 `@ModifyReturnValue` 会报 `InvalidInjectionException`。

### 1.4 `.../worldgen/OverworldAbyss.java`

- 新增身份映射 `private static final Map<RandomState, OverworldAbyssHole> INSTALLED = new ConcurrentHashMap<>()` 与 `holeOf(@Nullable RandomState)` / `shapeOf(@Nullable RandomState)`。
- **为什么改**：以前所有消费方（含水层、地表、出生点）都靠 `router().finalDensity() instanceof OverworldAbyssHole` 判断，其他模组在其上再包一层后该判断静默为假，洞口会被含水层重新灌水。
- 抽出 `public static Decision decide(ResourceKey<Level> dimension, ChunkGenerator generator, boolean enabled, boolean alreadyInstalled)`，枚举 `INSTALL / NOT_OVERWORLD / DISABLED / ALREADY_INSTALLED / NOT_NOISE_BASED`，判定顺序：维度 → 开关 → 已安装 → 噪声生成器。抽出来的原因是守卫可以在没有服务器、没有 mixin 转换过的 `RandomState` 的 JUnit 环境里直接测。
- `compose(state, original, shape[, originalSurface])`：构造 `surface`（进 `finalDensity` 槽）与 `terrain`（进初步地表槽）两个包装，并把 `surface` 登记进 `INSTALLED`。**不变式**：谁组合路由，谁就要登记，否则含水层找不到深渊。

### 1.5 `.../worldgen/OverworldAbyssSpawn.java`

- `suggested(shape)`：**本轮已改**——原来写死 `radius * 2.5`，在影响半径变成 `(2.0 + abyssSlopeBand) * radius` 之后这个候选落进了雕刻范围（默认 `2.5R = 400 < 520`）。现在从形状推导：`firstRing(shape) = shape.outerLimit() + 0.5 * radius`（默认 `600`），`suggest` 也只在原候选落在 `outerLimit() + 32` 内时才替换（原为 `2R + 32 = 352`，同样落在坡带里）。**不要写回固定倍数**，否则改 `abyssSlopeBand` 就复现漂移；新增测试 `OverworldAbyssGuardTest.suggestedSpawnIsOutsideTheCarvedSlope` 对 `slopeBand = 1.0 / 1.25 / 2.0` 三个取值断言候选点在雕刻范围之外且至少留出 `0.25R` 余量。
- `search(...)`：6 环 × 24 角，**先用区块中心的单一高度列做一次廉价安全判定，再进区块内 8×8（步长 2）遍历**。原实现每环先把 64 个候选全算完，代价是区块加载；现在海洋候选只花一次高度图。环半径同样改为从 `firstRing(shape)` 起步。
- `fallback(...)`（新）：所有环都失败时，取 `ceil(4R) + 64` 的四个正轴向点（默认半径下 704 格外，已是原生地形），返回第一个安全点；四个都失败才保留原出生点并告警。**原实现是直接保留不安全的出生点**。
- `passable(BlockState)`（新）：排除岩浆、火、灵魂火、岩浆块、营火、灵魂营火、细雪、仙人掌、浆果丛、凋灵玫瑰，并要求流体为空。床与重生锚记录不改写。

### 1.6 配置与资源文件

- `MiaDensityFunctionTypes` 注册 `overworld_abyss_hole`；`MiaConfigs` 增加 `overworldAbyssEnabled`（默认 `true`）、`abyssRadius`（默认 160，64–10240）、`abyssOuterSlope`（默认 1.0，范围 0.5–3.0，陡化那一轮新增）与 `abyssSlopeBand`（默认 1.25，范围 1.0–2.0，本轮新增；构造参数顺序见 6.3 节）。
- `src/main/resources/mementoinabyss.mixins.json` 的 `mixins[]` 增加 `worldgen.AquiferMixin` 与 `worldgen.SurfaceRulesContextMixin`。

## 2. 测试与验证

测试命令（Windows，必须用仓库已有的 Gradle 缓存）：

```powershell
$env:JAVA_HOME='D:\Minecraft\Lib\temurin-21.0'
$env:GRADLE_USER_HOME='D:\Minecraft\ModDev\.gradle'
.\gradlew.bat test --offline --no-configuration-cache -I build\local-repo-init.gradle --console=plain
```

`-I build\local-repo-init.gradle` 只是把 `build/local-m2`（预置的 Maven 目录）放在最前面，用于在无网络环境下解析依赖；它不改变编译或测试语义。

结果（`build/test-results/test/*.xml`）：

| 测试类 | 数量 | 结果 |
| --- | --- | --- |
| `OverworldAbyssHoleTest` | 16 | 全通过 |
| `OverworldAbyssGenerationTest` | 4 | 全通过 |
| `OverworldAbyssGuardTest`（新增） | 7 | 全通过 |

陡化那一轮改动了 `OverworldAbyssHoleTest` 的多个断言与测试替身，并把 `seamsAtRimAndOuterLimitAreContinuous` 的采样点从 `639 / 640` 改到当轮的边界 `479 / 480`、把 `realColumnsOutsideBoundaryAreUnchanged` 的边界点从 `640` 改到 `481`；本轮把跟影响半径有关的点位再挪到 `519 / 520`（`realColumnsOutsideBoundaryAreUnchanged` 用 `521`）；数量不变，全套仍是 **28 个测试全绿**：

`OverworldAbyssGenerationTest` 会在静态初始化时加载 `ChunkMap`、`MinecraftServer`、`SurfaceSystem`，因此这 4 个测试真正走的是被 mixin 转换过的代码路径（否则会抛 `MixinTransformerError`）。

`OverworldAbyssGuardTest` 覆盖：非主世界维度、开关关闭、同一 `RandomState` 二次安装、非噪声生成器、判定优先级、未安装世界无形状（`shapeOf(null)` / `holeOf(null)`）。

## 3. `surfaceHeight` 性能审计

- 调用点：`NoiseChunk.preliminarySurfaceLevel` 自身在 4 格网格上有 `Long2IntMap` 缓存，所以扫描**最多每区块 25 列**（16×16 区块上按 4 格一条：0、4、…、16 共 5 行 × 5 列；若按「每 4 格一条、不含右边界」记则是 16 列，取更保守的 25）。
- 单次扫描最多 384 次 `terrain()` 求值（`maxY - 1` 到 `minY` 逐格，默认 319 → -64）。**注意 `terrain()` 并不是「2R 内常数时间」**：`OverworldAbyssHole.java:77` 的 `lerp(weight, input.compute(context), target)` 会把输入密度**求值后再丢弃**（权重为 1 时 `lerp` 直接返回 `target`），所以 `distance > R` 的每一列都要付一次输入密度；同理 `top()` 在 `:92` 会先求 `estimate()`。真正短路的是 `surfaceHeight()`：`distance <= R` 与 `weight <= 0` 两个分支都在扫列之前返回（`:121`、`:126`）。
- 按默认几何，25 条列里落在雕刻范围内（`R`–`outerLimit`，默认 `160`–`520`）的约 13 条需要整列扫描（`13 × 384 ≈ 5 千`次 `terrain()`），其余 12 条被短路。这个 13 是按影响圆盘面积从陡化那一轮的约 11 条（影响半径 `480`）折算的：`(520² − 160²) / (480² − 160²) ≈ 1.2` 倍；也就是说坡带从 `2R`–`3R` 放宽到 `2R`–`3.25R` 之后，这部分开销随之上升，量级结论不变。
- **未做基准实测，也没有把「占原版多少百分比」算出来**：没有计时，也没有统计原版密集求值的真实次数（原版噪声区块还有 `cellWidth`/`cellHeight` 插值，逐格次数不能简单按 `16×16×384` 计）。想给出百分比必须先实测，这里不做估计。

## 4. 已知限制与未验证项（请审查时按此判断可信度）

1. **没有游戏内实测**：没有跑 `runClient` / `runServer`，没有生成过真实世界并观察洞口、环缘、接缝、出生点。
2. **专用服务器出生点流程未实测**：`MinecraftServerMixin` 与 `ServerStartedEvent` 的路径只有单元层面的逻辑覆盖。另外，外坡边界本轮扩到 `3.25R`（`520`）之后，`OverworldAbyssSpawn` 的环搜索前两环（`2.5R = 400`、`3.0R = 480`，以及 `suggested()` 用的 `2.5R` 候选）已经落在雕刻范围内：安全判定照常执行、不会把出生点放进洞里，但那份代码里「整段在影响半径之外、每步都是干地」的注释已与几何不符，需要一并核对（本轮没有改动这份代码）。
3. **第三方地形模组未联调**：本实现是噪声组合，其他模组若替换生成器、二次包装路由或混入同一调用点，需要实际联调。
4. **性能未实测**：见第 3 节，只有量级估算。
5. **`AbyssAquifer.drySample` 是实例可变状态**：`NoiseChunk` 是区块内单线程的，所以安全；但如果有人跨线程共享同一个 aquifer 实例调用 `computeSubstance`，`shouldScheduleFluidUpdate()` 会读到别的采样结果。当前没有共享路径，保留原样但记录在此。
6. **`INSTALLED` 映射按 `RandomState` 强引用，随服务器生命周期存续**：每次世界加载/服务器重启新建 `RandomState`，旧条目由 classloader 生命周期回收；已在字段注释中说明。
7. **旧存档与接缝**：只影响新生成区块，旧区块不回填，交界处允许接缝；这是原交接文档确认的既定行为，未改变。
8. **格式化范围**：骨架那一轮只对改动文件执行了 `google-java-format 1.36.1 --aosp`（Spotless 配置里的插件任务 `spotlessJava.target` 无法在离线 + 初始化脚本下安全收窄，因此直接用同版本、同风格的 CLI 处理指定文件）。**陡化那一轮改成了跑仓库自带的 `gradlew spotlessApply`（全量）**，因此工作区里其他此前已漂移的 Java 文件也被一起整理了（`git status` 里 tracked 修改文件从 47 个增加到 48 个，改动全是 import 收敛与换行：例如 `WhistleApi` 把 `net.minecraft.world.entity.LivingEntity` 收成 import、`RopeItem` 把 `net.minecraft.world.phys.Vec3` 收成 import）。随后 `spotlessCheck` 通过、28 个测试全绿，说明整理后的代码可以编译运行。**审查时请把这类 import/换行改动当作噪音**；如果不想让它们混进这次提交，可以只提交 `overworldAbyss` 相关文件。

## 5. 建议的审查重点

1. **`terrain` 的漏斗语义**：确认「`R..2R` 锥壁只到 `innerHeight`，外坡才升到 `ridgeHeight`」符合设计意图；如果设计想要的是「从中心到 2R 逐渐升到环缘」的实心锥，那么现在这版是错的（但那样会把洞口下半段全填成岩石）。
2. **外坡带宽度与影响半径的取舍**：这条线的轨迹是 `4R` → `3R`（收窄，坡度变陡）→ 本轮把宽度做成可配置的 `abyssSlopeBand`、默认 `1.25`（影响半径 `520`，主降段反而比 `3R` 时缓）。请确认默认宽度可接受、影响半径 `520` 是否偏大，以及下限 `1.0`（坡带必须至少覆盖混合落地所需的地面）是否就是想要的下限；上限 `2.0` 只是更缓、更占原生地形。
3. **`holeOf` 身份映射的清理策略**：是否需要监听服务器停止事件清空 `INSTALLED`。
4. **出生点回退半径 `4R + 64`**：是否合理，是否需要改成可配置。
5. **本轮实测出来的坡度是否过头**：默认坡带 `1.25` 下，主降段（`x=352`→`x=480`，128 格）实测平均约 `1.52` 格/格，换成 `x=352`→`x=464` 的 112 格则是约 `1.71`（区间取法见 6.6 的采样表），最陡单格下降 `6` 格；整条坡带（`x=320`–`520`，200 格）的平均是 `199 / 200 ≈ 1.0`，主降段比它陡，因为坡带两端各有一段平缓区。也就是说**主降段的平均坡度比需求里「平均约 1.2 格/格就够了」更陡**（只有把两端平缓区算进去的整段平均才低于 1.2），判断「够不够陡」时请说明用的是哪个口径。`6` 格/格等于一格横移掉 `6` 格高度，是否已经超出「可以行走/可以看」的范围请按实际预期判断；若仍嫌陡，第一顺位旋钮是把 `abyssSlopeBand` 调大（它决定平均坡度，越宽越缓），而不是调 `abyssOuterSlope`（指数只重塑坡带内部，不改平均坡度）。
6. **边界是否还有一格台阶**：默认几何下 `x=519`（最后一个被雕刻的列）的权重是 `1.24E-6`，`x=520` 起 `outerWeight` 精确为 `0.0`、该列密度与未改动的平面逐位相同。陡化那一轮记录的 `x=479` 到 `480` 的一格台阶（组合地面 70 → 原地形 69）在现在的采样里没有出现：探针在 `x=480`、`496`、`512` 都读到平面高度 `30`，`Plane(70)` 的单元测试在 `x=519` 读到的最高实心方块也与平面本身相同。请确认这个残差量级（`1.0E-6` 量级，测试里用 `2·w + 1.0E-5` 夹住）可以接受，还是要求把权重在边界前一格就压到 `0`（等于把坡带再收窄一格）。

## 6. 本轮：外坡带宽度做成可配置、默认 `1.25`（需求原文：「深渊大洞和主世界的边界连接处极其割裂，在深渊环山连接到主世界地面的坡度可以更加陡一些」）

（本节是坡面这条线的记录：6.2 的两个缺陷、6.4 的多数条目与 6.5 的坑都来自陡化那一轮（`4R` → `3R`、`slopeBand = 1.0`）；本轮新增 `abyssSlopeBand`、把默认宽度改成 `1.25`（影响半径 `520`），并重测了 6.6 的剖面。凡与旧数字冲突，以 6.1/6.3/6.6 为准。）

### 6.1 需求怎么被翻译成几何

陡度只有一个真正的杠杆：坡带的水平宽度。陡化那一轮把 `outerLimit()` 从 `4R` 收窄到 `3R`（坡带 `2R`–`3R`，`slopeBand = 1.0`）；本轮把宽度本身做成可配置的 `abyssSlopeBand`，默认 `1.25`（坡带 `2R`–`3.25R`）：

- 环缘高度由几何与世界种子决定（约 224–256，默认种子实测约 229），落差随周围地面高度变化；6.6 的探针在平坦测试平面（未雕刻处最高实心方块 `Y=30`）上读到约 199 格落差。
- 坡带宽度就是水平可用距离，`outerLimit() = (2.0 + slopeBand) * radius`：默认 `(2.0 + 1.25) × 160 = 520`。整条坡带的平均坡度就是「落差 ÷ 坡带宽度」，所以**宽度是唯一能整体改变平均坡度的旋钮**；`abyssOuterSlope` 只在坡带内部挪动落差的分布。
- 宽度下限 `1.0` 是硬约束：**权重归零的位置必须就是混合完全交还给原地形的位置**，坡带比混合要覆盖的地面还窄，权重就会在混合落地前归零，边界上留下断崖；上限 `2.0` 只是更缓、更占原生地形。
- 实测（6.6）：默认 `1.25` 时主降段 `x=352`（`225`）→ `x=480`（`30`）共 128 格、平均约 `1.52` 格/格、最陡单格 `6` 格；陡化那一轮的 `slopeBand = 1.0`（影响半径 `480`）在同一平面上是 `x=336`（`228`）→ `x=448`（`30`）共 112 格、平均约 `1.77`、最陡单格 `8` 格。放宽坡带让主降段变缓，这正是本轮拿到的结果。
- 历史读数是给对比用的：更早的 `2R`–`4R` 几何（陡化之前）把同样的落差摊在约 230 格（`x≈380`–`x≈610`）上，平均坡度只有约 `0.7`，之后是一段坡度≈0 的死平台，最后在 `639/640` 之间掉完剩余高度。现在坡带宽度与权重归零点已经对齐（权重正好在 `outerLimit()` 归零），不再有这种末端断崖。

### 6.2 陡化那一轮真正修掉的两个缺陷（都不是 tuning，是 bug）

1. **`preliminarySurfaceLevel` 是高度，不是密度。** `javap -p -c` 反编译 `DensityFunctions$FindTopSurface` 可以看到：它把 `upperBound` 按 `cellHeight` 取整后逐格下扫，返回**最高实心方块的 Y 坐标**（`iload_3; i2r; dreturn`），下界兜底到 `lowerBound`。此前包括测试替身在内，所有人都把**一个密度**喂进高度混合，于是 `top` 变成一个不动点并逐次塌缩，最终收敛到平原，产出 159 格的断崖。修法：`originalSurface` 只在 `y = 0` 处、按高度读取（`OverworldAbyssHole.estimate`）。
2. **混合参数顺序写反了。** `lerp(t, a, b) = a + t*(b-a)`，所以「权重为 1 时应当取到」的那个值必须放在**第二个**参数：`lerp(weight, estimate, innerHeight)` 才是「权重 1 → 漏斗壁 / 权重 0 → 原地形」。写成 `lerp(weight, innerHeight, estimate)` 会把整条坡面反向。
3. 另外确认（也是本轮补记的一条模型更正，它曾经花掉一整个调试时段）：**高度报告只能扫描组合后的密度，报告值既不是 `top`，也不是 `floor(top)`**。`terrain()` 返回 `lerp(weight, input.compute(context), clamp((top - y)/32, -1, 1))`，也就是把「斜坡」和**周围那条密度场**混在一起，所以组合后的岩石/空气界面落在两者抵消的位置，可以明显高于或低于 `top`：在 `y = top` 处目标项为 `0`，组合值只剩 `(1 - weight) * input`，平面上方 `input` 为负，界面就落到 `top` 下面。实测（早期探针，平坦输入、默认坡带）：`x=400` 处混合路径给出 `190.1`，同列的组合密度实际停在 `y=138`，原因就是该处平面上方的输入为负；6.6 的采样表是另一组探针数据（同一 `x=400` 读到 `98`），两组数字不同但结论一致。唯一可以依赖的两条不变式是：**预估地表报告等于组合密度为正的最高方块**，以及**雕刻边界在 `outerLimit()` 收敛到未改动地形**（`x=520` 起与未改动平面逐位相同，见 6.6）。

### 6.3 新增配置 `abyssOuterSlope` 与 `abyssSlopeBand`

```java
public ValidatedDouble abyssOuterSlope =
        new ValidatedDouble(1.0, 3.0, 0.5, ValidatedDouble.WidgetType.TEXTBOX);
```

- 语义：`outerWeight` 里的形状指数，`1.0 - smooth(pow(position, k))`。`k = 1.0` 是原来的平滑下降；`k < 1` 让环缘高度维持得离洞口更远、然后下降更快（读起来更陡）；`k > 1` 把坡带中段压平、把高度堆到最后一段。
- **注意一件容易被误读的事**：`Math.pow(position, k)` 作用在**已被 clamp 到 0–1 的位置**上，所以在坡带内部的整段上 `position` 并不恒为 1，指数确实有效；但一旦走到坡带末端，`1^k == 1` 使曲线在端点处对 `k` 不敏感。也就是说**指数只重塑坡带内部的高度分布，不改变坡带宽度，也不能让整个坡面一致变陡**——真正决定平均坡度的是坡带宽度（即 `abyssSlopeBand`）。

本轮新增的是这个宽度旋钮：

```java
public ValidatedDouble abyssSlopeBand =
        new ValidatedDouble(1.25, 2.0, 1.0, ValidatedDouble.WidgetType.TEXTBOX);
```

- 语义：外坡下降的宽度，以 `abyssRadius` 为单位、从环缘 `2R` 向外量，`outerLimit() = (2.0 + abyssSlopeBand) * radius`。**它是唯一能改变平均坡度的旋钮**：整个环缘到地面的落差都花在这条坡带里，平均坡度就是「落差 ÷ (`abyssSlopeBand` × `abyssRadius`)」，越宽越缓。默认 `1.25` → 坡带 `2R`–`3.25R`、影响半径 `520`。
- 下限 `1.0`（几何侧 `MIN_SLOPE_BAND`）：坡带必须至少和混合要覆盖的地面一样宽，否则权重在混合落地之前就归零、边界上留下断崖；上限 `2.0`（`MAX_SLOPE_BAND`）只是更缓、更占原生地形。几何侧 `DEFAULT_SLOPE_BAND = 1.25` 与配置默认一致，那里的 `Math.clamp` 同样只对反序列化进来的旧值兜底。
- 与 `abyssOuterSlope` 的分工：`abyssSlopeBand` 定平均坡度（宽度），`abyssOuterSlope` 只决定落差在坡带内部集中在哪里（形状），**不能**改变平均坡度。
- **构造参数顺序是 `(defaultValue, maxValue, minValue, widgetType)`，不是 `(default, max, step)`**，而且**没有 step 参数**；下限/上限靠校验而不是步长。`javap -s` 列出的全部 Java 可见重载（fzzy_config `me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble`）：

  | 签名 | 语义 |
  | --- | --- |
  | `(DDDDL…WidgetType;)V` 四参 | `(defaultValue, maxValue, minValue, widgetType)` |
  | `(DDD)V` 三参 | `(defaultValue, maxValue, minValue)` |
  | `(DDL…WidgetType;)V` | `(minValue, maxValue, widgetType)`，默认值取 `minValue`；**第三个参数是 `WidgetType`，不是 double** |
  | `(DD)V` | `(minValue, maxValue)` |
  | `(D)V` / `()V` | 单参默认值 / 空构造 |

  所以 `new ValidatedDouble(1.0, 3.0, 0.5, WidgetType.TEXTBOX)` 匹配四参重载，语义是「默认 1.0、上限 3.0、下限 0.5」，与几何侧 `MIN_OUTER_STEEPNESS = 0.5` / `MAX_OUTER_STEEPNESS = 3.0` / `DEFAULT_OUTER_STEEPNESS = 1.0` 一致，没有第二套范围；同理 `new ValidatedDouble(1.25, 2.0, 1.0, WidgetType.TEXTBOX)` 是「默认 1.25、上限 2.0、下限 1.0」，与 `MIN_SLOPE_BAND = 1.0` / `MAX_SLOPE_BAND = 2.0` / `DEFAULT_SLOPE_BAND = 1.25` 一致；几何侧的 `Math.clamp` 是对反序列化进来的旧配置值兜底。
- **更正上一版文档里的一个错误结论**：上一版说「三参 `new ValidatedDouble(2, 20, 0.1)` 走的是 `(min, max, widgetType)` 重载、语义是『最小 2、最大 20、默认 2』」——这是错的。第三个实参是 double `0.1`，不可能匹配 `WidgetType` 形参，它只能匹配三参 `(defaultValue, maxValue, minValue)`，所以 `MiaConfigs.java:62` 的 `blazeReapExplosionRadius` 实际语义是「默认 2、上限 20、**下限 0.1**」。审查时请以此表为准，不要沿用上一版的规则去读其他三参配置。

### 6.4 坡面这条线改动过的文件

- `src/main/java/com/altnoir/mementoinabyss/infrastructure/worldgen/density/OverworldAbyssShape.java`：record 现在是 6 个分量 `(radius, minY, maxY, seed, outerSteepness, slopeBand)`。陡化那一轮新增 `outerSteepness` 分量、`MIN_OUTER_STEEPNESS = 0.5` / `MAX_OUTER_STEEPNESS = 3.0` / `DEFAULT_OUTER_STEEPNESS = 1.0`、`outerLimit()`（当时写死返回 `3.0 * radius`）与 `outerWeight` 的 clamp 归零，`CODEC` 增加 `outer_steepness`（`Codec.DOUBLE`）；本轮新增 `slopeBand` 分量、`MIN_SLOPE_BAND = 1.0` / `MAX_SLOPE_BAND = 2.0` / `DEFAULT_SLOPE_BAND = 1.25`（构造器里 `Math.clamp`），`outerLimit()` 从写死的 `3.0 * radius` 改成 `(2.0 + slopeBand) * radius`，`outerWeight` 的位置是 `clamp((distance - 2R) / (slopeBand * R), 0, 1)` 且 `distance >= outerLimit()` 精确返回 `0.0`，`CODEC` 再增加 `slope_band`（`Codec.DOUBLE`）。`affects()` 用的始终是 `outerLimit()`。
- `src/main/java/com/altnoir/mementoinabyss/infrastructure/worldgen/density/OverworldAbyssHole.java`：新增 `estimate(x, z, context)`（高度语义 + `y = 0` 读取）、`top(x, z, weight, context)`（`lerp(weight, max(minY - 1, estimate), innerHeight)`，世界底夹紧作用在混合内部的预估值上）、`terrain` 的 `target = clamp((top - y)/32, -1, 1)`（零点精确落在 `top`）、`surfaceHeight` 的三分支（内部返回 `minY - 1` / 权重归零返回 `floor(estimate)` / 其余扫描）。
- `src/main/java/com/altnoir/mementoinabyss/infrastructure/worldgen/OverworldAbyss.java`：`OverworldAbyssShape` 构造按 `(abyssRadius, minY, maxY, seed, abyssOuterSlope, abyssSlopeBand)` 的顺序传 6 个参数；启动日志里的 `outer radius` 报的是 `shape.outerLimit()`（陡化那一轮从写死的 `radius * 4` 改过来，本轮公式变了但日志不用再改）。
- `src/main/java/com/altnoir/mementoinabyss/infrastructure/config/MiaConfigs.java`：陡化那一轮新增 `abyssOuterSlope`，本轮新增 `abyssSlopeBand`。
- `src/test/.../density/OverworldAbyssHoleTest.java`：新增 `PlainHeight` 替身（返回**高度**、`minValue -64` / `maxValue 320`、带可序列化的 `CODEC`），`plain` / `hole` 两个 helper 都改成喂高度；断言统一成一条不变式 `assertEquals((double) densityHeight(density, x, z), surface.compute(at(x, 0, z)), 1.0E-9)`，其中 `densityHeight` 从 `y = 319` 逐格下扫到 `-64`。本轮把所有跟影响半径有关的点位挪到 `519` / `520`（外坡扫描循环也到 `x < 520`）。
- `src/test/.../worldgen/OverworldAbyssGenerationTest.java`：构造点现在按 6 个分量传参（含 `1.25`）；`realColumnsOutsideBoundaryAreUnchanged` 的采样点是 `{521,0}` / `{0,-521}`（正好在新影响半径 `520` 外一格），更远的旧点保留。

### 6.5 陡化那一轮踩过并明确回退的坑（复审时可以直接跳过这些方向）

- **`target = clamp((top - y)/32, -1, 1)` 直接用在锥内**：坡带在 `top` 以下整段为正，等于把洞口整列填成岩石（实测环缘 230 时 y=100 仍是实心）。锥内必须走「漏斗壁」分支。
- **把 `top + 1.0` 当零点**：整体上移一格，产生 `expected: <230.0> but was: <229.0>`。零点必须精确落在 `top`。
- **把 y 相关的值喂进高度混合**：见 6.2 第 1 条。
- **`pow(position, k)` 想当成「整体更陡」的旋钮**：见 6.3。

### 6.6 本轮的验证与未验证项

- 验证方式：除常规单测外，用临时探针 `SlopeProbeTest`（用后已删除）在平坦测试平面上用真实组合密度**逐格扫描**外坡，逐点打印 `x → 最高实心方块 Y`，把「坡面剖面」变成可读数字再定公式（这是定位 6.2 两个 bug 的关键手段，本轮用它重测了下面的剖面）。
- **本轮重新实测的剖面**（默认几何 `R=160`、`slopeBand = 1.25`、`outerLimit = (2.0 + 1.25) × 160 = 520`、`outerSteepness = 1.0`、平坦测试平面（未雕刻处最高实心方块 `Y=30`）；`x` 沿 +X、步长 1 扫描，`top` 是该列组合密度的最高实心方块）：

  | x | 320 | 336 | 352 | 368 | 384 | 400 | 416 | 432 | 448 | 464 | 480 | 496 | 512 |
  | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
  | top | 229 | 229 | 225 | 204 | 165 | 98 | 67 | 50 | 40 | 33 | 30 | 30 | 30 |

- 对比陡化那一轮的一半径坡带（`slopeBand = 1.0`、`outerLimit = 480`，同一平面、同样逐格扫描）：

  | x | 336 | 352 | 368 | 384 | 400 | 416 | 432 | 448 |
  | --- | --- | --- | --- | --- | --- | --- | --- | --- |
  | top | 228 | 214 | 178 | 98 | 61 | 44 | 35 | 30 |

- 读法（默认 `1.25`）：**环缘 229 → 平面地面 30，落差 199 格**。坡带两端各有一段平缓区（`x=320`、`336` 都还在 `229`，`x=480` 起已经是 `30`），整条坡带（`x=320`–`520`，200 格）的平均是 `199 / 200 ≈ 1.0` 格/格——这正是「落差 ÷ 坡带宽度」；真正在下降的 `x=352`（`225`）→ `x=480`（`30`）共 128 格，平均约 `1.52` 格/格，把区间取成 `x=352` → `x=464` 的 112 格则是约 `1.71`（**说「平均坡度是多少」之前必须先说明区间**）。**最陡的单格下降是 6 格**，出现在 `x=384` 附近；16 格步长的散点表会把这个峰值抹平，峰值只认细网格逐格扫描。权重按 `1 − smooth(pow((x − 2R) / (slopeBand · R), outerSteepness))`（默认 `outerSteepness = 1.0`）从 1 单调降到 0，落地之后仍不为零（`x=480` 时约 `0.06`，`x=519` 时 `1.24E-6`），这正是混合要一路收敛回原地形的原因。
- 对比读数（`slopeBand = 1.0`，同一平面）：`x=336`（`228`）→ `x=448`（`30`）共 112 格、平均约 `1.77` 格/格，最陡单格下降 `8` 格。也就是说，本轮把坡带从 `1.0`（160 格宽）放宽到 `1.25`（200 格宽）之后，主降段的平均坡度从约 `1.77` 降到约 `1.52`、峰值从 `8` 降到 `6`；落差不变时，宽度是唯一能这样整体改变坡度的旋钮。
- 坡带末端与边界：`x=519`（最后一个被雕刻的列）的权重是 `1.24E-6`，`x=520` 起 `outerWeight` 精确为 `0.0`，该列密度与未改动的平面**逐位相同**。探针在 `x=480`、`496`、`512` 读到 `top=30`，与平面本身相同，所以陡化那一轮记录的「影响边界上一格台阶（组合地面 70 → 原地形 69）」在默认几何下没有出现（`Plane(70)` 的单元测试在 `x=519` 读到的最高实心方块也与平面本身相同）。
- 边界处不再「相等」而是「收敛」：相邻两列（`519` 与 `520`）的密度差由 `x=519` 的权重给出上界（测试断言 `|雕刻 − 未雕刻| ≤ 2·w + 1.0E-5`，实际量级在 `1.0E-6` 上下），`x=520` 起逐位相等；测试容差取 `1.0E-3`，比这个残差宽两个数量级以上、又比要抓的缺陷（数十）窄两个数量级。
- 更早的 `2R`–`4R` 几何本轮**没有重测**，只有陡化那一轮留下的 16 格步长读数（`x=560 top=71`、`x=608 top=70`、`x=640 top=69`：约 230 格水平距离、平均约 0.7、末端一段死平台）；要比较它的峰值请按同一张细网格重跑。
- 仍然**没有游戏内实测**：没有跑 `runClient` / `runServer`，没有截图或录像确认视觉效果；第 4 节的 8 条未验证项依然全部有效，本轮没有减少任何一条。

## 7. 给 GPT 的复盘：这一轮为什么花了很久

这一节不是功能说明，是本轮**调试过程中真实浪费掉的时间**和它的根因。审查时如果只看结论，很容易低估下面这些坑的复现概率——它们都不是「代码写错了」，而是「我判断错了」。

### 7.1 最主要的一条：我一直在断言一个错误的契约

现象：`originalEstimateIsReadInsideOuterLimitAndIgnoredOutside` 失败，`expected: <190.13851715128948> but was: <138.0>`。我据此认为「实现的边界与它命名的高度差了 52 格」，是个 bug，于是**两次改了生产代码**：

1. 把 `top()` 的世界地板从「混合之后取 max」改成「在混合之前夹住 estimate」；
2. 把 `terrain()` 里 `lerp(weight, input.compute(context), target)` 的两个参数调换。

两次都被随后的实测否定，**现在都已还原**（`git diff` 里 `OverworldAbyssHole.java` 的 `terrain()`/`top()` 与陡化那一轮一致）。真正的错误在测试侧：我断言的是「地表报告 == 高度混合值」，而实际契约是 6.2 第 3 条写的那个——`terrain()` 返回的是**密度**的混合，零点在斜坡与周围密度场抵消处，本来就不等于被混合命名的高度。在 `x=400`（`Plane(70)` 输入）实测：混合命名 `top=190.14`，而组合密度实际停在 `y=138`，因为那里平面输入是 `-3.72`、把零点拉了下来。两个数都对。

**教训**：当「实测值 != 手算值」时，先证明**哪一侧是契约**，再决定改代码还是改断言。我这里是把「我推导出来的公式」当成了契约，而它只是我对混合语义的直觉。

### 7.2 一个更贵的次要坑：Gradle 的 UP-TO-DATE 骗过我一次

有一次 28 个测试全绿，但那次是在**源码改动与 Gradle 的 up-to-date 判定落在同一秒**时跑的：`compileTestJava` 被报成 `UP-TO-DATE`，实际测的是旧字节码（`src` mtime `19:33:13.443` vs `class` mtime `19:29:58.587`）。后来真正的失败是在 `--rerun-tasks` 下才暴露的。

**教训**：凡是**结论依赖的那次运行**，要么加 `--rerun-tasks`，要么比对 `build/classes/**/*.class` 与 `src/**/*.java` 的 mtime；不要用一次增量的绿去证明结论。

### 7.3 配套的两条小规则

- **诊断打印必须放在会中断的断言之前**：`System.out.printf` 放在失败断言之后时永远不会执行，我为此白跑了一轮。
- **这个工程的 JUnit XML 里 `system-out` 是空的**（Gradle 也会吞掉测试 stdout，`-i` 也没用）。要看打印，用一次性的 init 脚本挂 `testLogging { showStandardStreams = true }` 作为**第二个** `-I` 传入。本轮用过的那份已删除。
- **不要把两次不同探针跑的数字混着算**：本轮的坡度数字一开始报成「平均 1.66」，就是把 `Plane(70)` 与 `Plane(30)` 两次跑的采样混在一起的产物；按同一张表算，`x=352→480` 是 `1.52`、`x=352→464` 是 `1.71`、整带 `x=320→520` 是 `1.0`。**任何平均坡度都要连同区间一起说**（6.6 已按此改写）。

### 7.4 顺带发现、已修的代码漂移（不在原始需求里）

- `OverworldAbyssSpawn`：`FIRST_RING = 2.5` 这个常量在影响半径变成 `520` 之后已经把出生建议放进雕刻范围内（`2.5R = 400`），而它的注释说「在内坡之外」；`suggest()` 的守卫阈值 `2R + 32 = 352` 同样落在坡带里。已改为从 `firstRing(shape) = outerLimit() + 0.5R` 推导（默认 `600`），`suggest()` 用 `outerLimit() + 32` 判定——**不能写死 3.25**，否则改 `abyssSlopeBand` 就会复现同样的漂移。`4R + 64` 的兜底距离保持不变。
- `DEFAULT_SLOPE_BAND` 的 javadoc 曾写「默认平均约 1.2」，与该常量自己的公式（落差 ÷ 宽度，标准主世界约 0.8）和实测（整带约 1.0）都不符，已改为按区间陈述。

### 7.5 本轮验证过的（以及没验证的）

- 已实测：28 个单测在 `--rerun-tasks` 下全绿（`16 + 4 + 8`）；`spotlessApply` 通过；`x=519` 权重 `1.24E-6`、`x=520` 起权重精确 `0.0` 且该列与未改动平面**逐位相同**；组合密度扫描出的最高实心方块 == 地表报告（多个 `x` 断言相等）。
- **没测**：游戏内视觉、专用服务器出生流程、第三方地形模组联调、性能基准。第 4 节的 8 条未验证项一条都没减少。
