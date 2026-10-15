"""Plot exported NoiseChunk blocks; run after gradlew test --tests '*OverworldAbyssProfileTest'."""

import argparse
import csv
import json
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
from matplotlib.colors import ListedColormap
import numpy as np


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=Path("build/reports/overworld-abyss"))
    parser.add_argument("--output", type=Path, default=Path("docs/overworld-abyss-validation"))
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    for font in [Path("C:/Windows/Fonts/msyh.ttc"), Path("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc")]:
        if font.exists():
            font_manager.fontManager.addfont(str(font))
            plt.rcParams["font.family"] = font_manager.FontProperties(fname=str(font)).get_name()
            break
    plt.rcParams.update({"axes.unicode_minus": False, "font.size": 11, "savefig.facecolor": "#fbfaf6"})
    with (args.input / "terrain-section.csv").open(encoding="utf-8") as stream:
        rows = list(csv.DictReader(stream))
    x = np.array([int(row["x"]) for row in rows])
    heights = {}
    grids = {}
    for name in ["original", "before", "after"]:
        grids[name] = np.array([[int(bit) for bit in row[name]] for row in rows]).T
        heights[name] = np.array([max(np.flatnonzero(column), default=-1) - 63 for column in grids[name].T])
    palette = ListedColormap(["#fbfaf6", "#6d786d"])

    def draw(ax, mode, label):
        ax.pcolormesh(np.r_[x - 4, x[-1] + 4], np.arange(-64, 321), grids[mode], cmap=palette, vmin=0, vmax=1, rasterized=True)
        ax.plot(x, heights["original"], color="#b38a62", lw=1.25, ls="--", label="同种子原始主世界地表")
        ax.axhline(256, color="#678aa4", lw=.8, ls="--", alpha=.7)
        ax.axhline(128, color="#678aa4", lw=.8, ls=":", alpha=.7)
        for radius, style in [(320, ":"), (640, "--"), (1040, ":")]:
            for sign in [-1, 1]:
                ax.axvline(sign * radius, color="#88918d", lw=.65, ls=style, alpha=.6)
        ax.set(xlim=(-1156, 1156), ylim=(-64, 320), ylabel="Y", title=label)
        ax.set_yticks([-64, 0, 128, 256, 320])
        ticks = [-1040, -640, -320, 0, 320, 640, 1040]
        ax.set_xticks(ticks, ["−3.25R\n−1040", "−2R\n−640", "−R\n−320", "0", "R\n320", "2R\n640", "3.25R\n1040"])
        ax.spines[["top", "right"]].set_visible(False)
        ax.set_facecolor("#fbfaf6")

    fig, ax = plt.subplots(figsize=(17, 6.5))
    fig.subplots_adjust(left=.055, right=.99, top=.91, bottom=.23)
    draw(ax, "after", "主世界深渊：实际 NoiseChunk 横切面（优化后）")
    ax.annotate("R 内贯通至世界底部", xy=(0, -56), xytext=(0, 25), ha="center", arrowprops={"arrowstyle": "->", "color": "#3a5148"})
    ax.legend(loc="upper right", frameon=False)
    ax.set_xlabel("X / 方块；Z=0，种子 12345；R=320，外坡指数=1，宽度=1.25R", labelpad=10)
    fig.text(.5, .025, "实心像素来自 NoiseChunk 列生成；横向每 8 格、纵向每 1 格采样。未包含后续 carvers、地表修饰、流体与装饰。", ha="center", fontsize=9)
    fig.savefig(args.output / "terrain-section.png", dpi=160, bbox_inches="tight")
    plt.close(fig)

    fig, axes = plt.subplots(2, 1, figsize=(17, 9), layout="constrained")
    draw(axes[0], "before", "修改前：旧密度公式（同样使用 R=320）")
    draw(axes[1], "after", "修改后：主世界基础噪声 + 插值前混合 + 连续山脊")
    axes[0].legend(loc="upper right", frameon=False)
    axes[1].set_xlabel("X / 方块；Z=0，种子 12345；相同半径与外坡配置")
    fig.savefig(args.output / "terrain-comparison.png", dpi=160, bbox_inches="tight")
    plt.close(fig)

    with (args.input / "terrain-profiles.csv").open(encoding="utf-8") as stream:
        profiles = list(csv.DictReader(stream))
    # Compare adjacent, equally spaced samples away from the intentionally vertical shaft.
    roughness = {mode: [] for mode in ["before", "after"]}
    by_section = {}
    for row in profiles:
        by_section.setdefault((row["seed"], row["direction"]), []).append(row)
    for section in by_section.values():
        section.sort(key=lambda row: int(row["distance"]))
        for i in range(1, len(section) - 1):
            a, b, c = section[i-1:i+2]
            if int(b["distance"]) - int(a["distance"]) != 8 or int(c["distance"]) - int(b["distance"]) != 8:
                continue
            if not 352 <= abs(int(b["distance"])) <= 1016:
                continue
            for mode in roughness:
                roughness[mode].append(abs(int(a[mode+"Height"]) - 2*int(b[mode+"Height"]) + int(c[mode+"Height"])))
    with (args.input / "terrain-profile-metrics.json").open(encoding="utf-8") as stream:
        metrics = json.load(stream)
    metrics["surfaceSecondDifference"] = {
        mode: {"samples": len(values), "meanAbsoluteBlocks": float(np.mean(values)), "p95Blocks": float(np.percentile(values, 95))}
        for mode, values in roughness.items()
    }
    transition_path = args.input / "transition-columns.csv"
    if transition_path.exists():
        with transition_path.open(encoding="utf-8") as stream:
            transitions = list(csv.DictReader(stream))
        metrics["outerTransition"] = {
            "columns": len(transitions),
            "seeds": sorted({int(row["seed"]) for row in transitions}),
            "slopeExponents": sorted({float(row["slope"]) for row in transitions}),
            "directionsDegrees": sorted({int(row["direction"]) for row in transitions}),
            "sampleSpacingBlocks": 4,
            "smallLiftNaturalEdgeRelocations": sum(int(row["excessStep"]) > 8 for row in transitions),
            "note": "Allow up to 8 blocks beyond the original local step. Only when nominal uplift is below 8, an original cave edge may move within an 8-block neighborhood; raw excessStep remains exported for review.",
        }
    support_path = args.input / "ridge-support.csv"
    if support_path.exists():
        with support_path.open(encoding="utf-8") as stream:
            support = list(csv.DictReader(stream))
        metrics["ridgeSupport"] = {
            "patches": len(support),
            "patchSizeBlocks": [17, 64, 17],
            "solidSamplesAbove256": sum(int(row["solidAbove256"]) for row in support),
            "unsupportedSamplesAbove256": sum(int(row["unsupportedAbove256"]) for row in support),
            "note": "Six-neighbor support within sampled patches; components touching a patch boundary are conservatively accepted.",
        }
    reported_path = args.input / "reported-seed-section.csv"
    if reported_path.exists():
        with reported_path.open(encoding="utf-8") as stream:
            reported = list(csv.DictReader(stream))
        distance = np.array([int(row["distance"]) for row in reported])
        reported_grids = {
            mode: np.array([[int(bit) for bit in row[mode]] for row in reported]).T
            for mode in ["original", "before", "after"]
        }
        reported_heights = {
            mode: np.array([max(np.flatnonzero(column), default=-1) - 63 for column in grid.T])
            for mode, grid in reported_grids.items()
        }
        fig, axes = plt.subplots(2, 1, figsize=(15, 8), layout="constrained")
        for ax, mode, label in zip(axes, ["before", "after"], [
            "修复前：JSON 图未识别，误走备用密度混合",
            "修复后：透明引用归一化，进入原版处理链",
        ]):
            ax.pcolormesh(np.r_[distance - 2, distance[-1] + 2], np.arange(-64, 321),
                          reported_grids[mode], cmap=palette, vmin=0, vmax=1, rasterized=True)
            ax.plot(distance, reported_heights["original"], color="#b38a62", ls="--", lw=1,
                    label="同种子原主世界")
            for radius in [640, 960, 1040]:
                ax.axvline(radius, color="#88918d", lw=.7, ls=":")
            ax.axhline(256, color="#678aa4", lw=.8, ls="--")
            ax.scatter([np.hypot(577, 660)], [217], color="#b84036", s=20, label="截图玩家位置")
            ax.set(title=label, ylim=(-64, 320), xlim=(624, 1072), ylabel="Y")
            ax.set_xticks([640, 800, 960, 1040], ["2R\n640", "800", "3R\n960", "外边界\n1040"])
            ax.spines[["top", "right"]].set_visible(False)
            ax.set_facecolor("#fbfaf6")
        axes[0].legend(loc="upper right", frameon=False)
        axes[1].set_xlabel("距中心 / 方块；沿 (577,660) 方向；种子 −3641258292459704473")
        fig.suptitle("用户种子复现：实际 NoiseChunk 方块剖面（每 4 格采样）")
        fig.savefig(args.output / "reported-seed-comparison.png", dpi=160, bbox_inches="tight")
        plt.close(fig)
        metrics["reportedSeed"] = {
            "seed": -3641258292459704473,
            "columnsPerMode": len(reported),
            "sampleSpacingBlocks": 4,
            "maxAdjacentHeightStep": {
                mode: int(np.abs(np.diff(values)).max())
                for mode, values in reported_heights.items()
            },
            "note": "Before deliberately selects the former fallback with an identity +0 wrapper; after loads the packaged overworld JSON and recognizes its terrain stages. These columns exclude the later surface/carver stages.",
        }
    runtime_path = args.input / "reported-runtime-surface.json"
    if runtime_path.exists():
        metrics["reportedSeedFullSurface"] = json.loads(runtime_path.read_text(encoding="utf-8"))
    runtime_regions = {
        region: json.loads(path.read_text(encoding="utf-8"))
        for region in ["player", "slope", "ridge", "wall"]
        if (path := args.input / f"reported-runtime-surface-{region}.json").exists()
    }
    if runtime_regions:
        metrics["reportedSeedFullSurfaceRegions"] = runtime_regions
    metrics["note"] = "Generation timings are warmed isolated column-query costs, not in-game FPS or full chunk throughput. Surface second difference uses 8-block samples and includes macro curvature."
    (args.output / "metrics.json").write_text(json.dumps(metrics, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps(metrics, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
