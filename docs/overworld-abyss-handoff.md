# 主世界深渊实现交接

用户要求剩余工作交给 DeepSeek harness；本轮在此停止实现。不要把当前状态视为全部完成。

> **⚠️ 历史文档（交接时的快照，不再逐条维护）。** 本文记录的是「接手时的状态与待办」，其中大量条目**已经完成或已过时**：第 2 节的两个优先问题（半径外的预估高度污染、逐格扫描）已修复；「其他待完成工作」里的出生搜索、身份标记、守卫测试都已完成；测试数量已从 14 增加到 28。**当前状态请看功能说明 `docs/overworld-abyss.md`（数字最新）和本轮修改记录 `docs/overworld-abyss-gpt-review.md`**，本文只用来看需求来源与最初的问题清单。
>
> 下面第 9 行里的「洞底半径 160、山脊 320、影响半径 640」是**最初几何**，已经被改过两轮：陡化那一轮把外坡带从 `2R`–`4R` 收窄到 `2R`–`3R`（影响半径 480），本轮又把坡带宽度做成可配置的 `abyssSlopeBand`（默认 `1.25`），影响半径随之变成 `(2.0 + abyssSlopeBand) * abyssRadius` = **520**。出生点搜索的回退距离仍是 `4R + 64`（默认 704），与实际开洞边界无关，不要把它和坡带混为一谈。

## 已确认需求

- Minecraft 26.1.2 / NeoForge 26.1.2.109，JDK 25。
- 主世界 (0,0) 干燥无底洞，贯穿基岩；漏斗内坡、自然环形山脊、外坡融入原地形。
- 共用 `abyssRadius`，默认洞底半径160、山脊半径320、影响半径640，山脊高度224～256。
- 保留现有群系源及其他模组的噪声输入，不覆盖主世界维度或噪声设置JSON。
- 所有存档默认开启，仅新生成区块应用；旧区块不重建，允许新旧接缝。
- 默认出生危险时移到安全位置，不改床与重生锚；不新增跨维度传送。

## 当前改动（均未提交）

- `infrastructure/worldgen/density/OverworldAbyssShape.java`：不可变种子轮廓、R/2R/4R尺度。
- 同目录 `OverworldAbyssHole.java`：原密度局部包装、地表预估输出、Codec/visitor/值域。
- 同目录 `AbyssAquifer.java`：中心洞及内坡上方强制空气，防止天然流体和结构地基堵洞。
- `infrastructure/worldgen/OverworldAbyss.java`：按实际主世界身份接入每个世界自己的RandomState，提供compose函数。
- `OverworldAbyssSpawn.java`：新世界出生建议迁移与ServerStarted时有限安全搜索。
- `mixin/worldgen/`：ChunkMap初始化接入、RandomState router accessor、NoiseChunk含水层包装、SurfaceSystem恶地柱/冰山填块保护、MinecraftServer出生建议。
- `MiaDensityFunctionTypes` 注册 `overworld_abyss_hole`；配置新增 `overworldAbyssEnabled` 默认true；中英文开关名称。
- `build.gradle` 增加JUnit 5.11.4与ModDev unitTest配置，Maven Central仅用于测试依赖组。
- 两个测试类在 `src/test/java/com/altnoir/mementoinabyss/infrastructure/worldgen/`。
- 使用说明 `docs/overworld-abyss.md` 由6.1 Sol编写，里面测试数量仍写10，需要更新。
- 顺手修复了已有 `compat/iris/IrisRenderCompat.java` 三处同名类遮蔽：恢复外部IrisApi完整类名，否则项目无法编译。

## 已执行验证

- compileJava通过。
- 最新 `test` 通过，共14项（10项密度函数单测 + 4项生成链路测试）。报告见 `build/test-results/test/` 和 `build/reports/tests/test/`。
- 三种种子真实NoiseChunk列采样：中央从-64至319全部空气，山脊高度符合范围。
- 一个种子的4个范围外点位：原始/修改后整列方块相等。
- 完整ProtoChunk fillFromNoise + buildSurface测试：平原/恶地/冰海，中心/洞边/山脊，注入正结构地基密度后，洞形开口仍为空气。
- 已通过加载ChunkMap、MinecraftServer、SurfaceSystem等类验证Mixin能应用。
- 尚无游戏内人工观察、完整服务器出生流程测试、实际第三方地形模组联测或性能基准。

## 必须优先修复的问题（6.1 Sol只读审查发现）

1. **半径外严格保持原生成尚不完整。** 最终密度范围外直接原样返回，但替换后的preliminarySurfaceLevel会影响邻居：
   - Aquifer.NoiseBasedAquifer构造器调用NoiseChunk.maxPreliminarySurfaceLevel，computeFluid调用preliminarySurfaceLevel，邻居采样可跨边界几十格，导致边界外的流体/石头/空气发生变化。
   - SurfaceRules.Context.getMinSurfaceLevel对四个16格网格角的预估高度插值；边界外的列可能读取边界内角点，改变表层材质/冰山。
   - 当前4点测试不能覆盖这种情况。新增多个种子的完整边界环采样对照，包括地表阶段。
   - 建议保留原始预估高度图供含水层使用，仅洞形开口使用干燥包装；表面规则在实际列位于范围外时使用原始预估高度采样。避免全局ThreadLocal或跨世界可变状态。
   - 可在NoiseChunk设置预估高度表达式处捕获已mapAll的OverworldAbyssHole.surface()，建立每NoiseChunk原高度缓存与访问接口；在Aquifer的两种高度查询处通过WrapOperation使用原高度。注意构造器中调用发生在NoiseChunk RETURN注入之前，原高度必须提前设置。
2. `OverworldAbyssHole.surfaceHeight`每8格向下采样，可能漏掉其他模组不足8格厚的悬浮实体层。若要准确的最终地表高度，应改逐格扫描或采用不漏薄层的方案，并验证性能。

## 其他待完成工作

- 审核出生搜索：边界检查应在生成候选区块之前；安全地面当前只判断实心碰撞+空气，尚未排除伤害方块。搜索耗尽会保留原危险出生点并warn，需要决定/实现合适的安全处理，不偷偷创建平台。
- 考虑其他模组再次包装finalDensity后 `instanceof OverworldAbyssHole` 的脆弱性；可用世界RandomState显式标记轮廓，勿误伤其他维度。
- 补充开关关闭/其他维度/非噪声生成器/旧存档相关验证。
- 只格式化本次修改的文件，不要全仓Spotless改掉用户未提交内容。
- 最后检查差异、更新说明和测试结果，向用户准确报告未验证项。

## 命令与本地资料

PowerShell中执行：

```powershell
$env:JAVA_HOME='D:/Minecraft/Lib/temurin-25.0'
$env:GRADLE_USER_HOME='D:/Minecraft/ModDev/.gradle'
./gradlew.bat test --offline --console=plain
```

必须使用上述已有Gradle缓存；默认用户目录缺依赖，会慢速联网。需要传`-Dxxx`参数时在PowerShell用引号包住完整参数。

- 游戏源码压缩包：`build/moddev/artifacts/minecraft-patched-26.1.2.109-sources.jar`。
- 相关源码已解压至 `build/abyss-inspect/`，包括Aquifer、NoiseChunk、SurfaceRules、SurfaceSystem、MinecraftServer、ChunkMap等。
- 改动开始前工作区已有约40个用户修改文件，不能git reset/checkout覆盖。不要提交或丢弃用户改动。
- 本轮未创建分支或提交，未修改用户实际游戏存档。
