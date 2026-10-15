"""Plot actual NoiseChunk shaft columns and their measured first-rock radius."""

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
    source = Path("build/reports/overworld-abyss")
    output = Path("docs/overworld-abyss-validation")
    output.mkdir(parents=True, exist_ok=True)
    font = Path("C:/Windows/Fonts/msyh.ttc")
    if font.exists():
        font_manager.fontManager.addfont(str(font))
        plt.rcParams["font.family"] = font_manager.FontProperties(fname=str(font)).get_name()
    plt.rcParams.update({"axes.unicode_minus": False, "font.size": 10,
                         "savefig.facecolor": "#fbfaf6"})
    with (source / "wall-columns.csv").open(encoding="utf-8") as stream:
        rows = list(csv.DictReader(stream))
    with (source / "wall-envelope.csv").open(encoding="utf-8") as stream:
        envelopes = list(csv.DictReader(stream))
    with (source / "wall-metrics.csv").open(encoding="utf-8") as stream:
        results = list(csv.DictReader(stream))
    seeds = list(dict.fromkeys(row["seed"] for row in rows))
    fig, axes = plt.subplots(2, 3, figsize=(17, 9), layout="constrained")
    metrics = {"source": "Packaged overworld JSON, actual NoiseChunk.getBaseColumn; R=320, plains biome; excludes later carvers/surface/decorations",
               "sampleSpacing": {"radial": 4, "angleDegrees": 15, "height": 1}, "seeds": []}
    for index, seed in enumerate(seeds):
        samples = [row for row in rows if row["seed"] == seed]
        section = [row for row in samples if int(row["angle"]) == 0]
        distances = np.array([int(row["r"]) for row in section])
        grid = np.array([[int(bit) for bit in row["blocks"]] for row in section]).T
        ax = axes[0, index]
        ax.pcolormesh(np.r_[distances - 2, distances[-1] + 2], np.arange(-64, 321),
                      grid, cmap=ListedColormap(["#fbfaf6", "#6d786d"]), vmin=0, vmax=1)
        envelope = [row for row in envelopes if row["seed"] == seed and int(row["angle"]) == 0]
        ax.plot([float(row["radius"]) for row in envelope], [int(row["y"]) for row in envelope],
                color="#467fa0", lw=1.2, label="跨维度共享井壁轮廓")
        ax.axvline(322, color="#b08560", ls="--", lw=1, label="旧固定圆柱参考 R+2")
        ax.axvline(320, color="#737b81", ls=":", lw=.8)
        ax.axhspan(112, 144, color="#81a799", alpha=.12)
        ax.axhline(128, color="#81a799", lw=.8, ls=":")
        ax.set(title=f"种子 {seed}\nX 轴方向：实际岩体剖面", xlim=(312, 394),
               ylim=(-64, 160), xlabel="距中心 / 方块", ylabel="Y")
        if index == 0:
            ax.legend(loc="lower left", frameon=False, fontsize=8)

        angles = sorted({int(row["angle"]) for row in samples})
        first_rock = np.full((176, len(angles)), np.nan)
        for angle_index, angle in enumerate(angles):
            ray = sorted([row for row in samples if int(row["angle"]) == angle], key=lambda row: int(row["r"]))
            for y_index in range(176):
                candidates = [int(row["r"]) - 320 for row in ray if row["blocks"][y_index] == "1"]
                if candidates:
                    first_rock[y_index, angle_index] = min(candidates)
        ax = axes[1, index]
        mesh = ax.pcolormesh(np.r_[np.array(angles) - 7.5, angles[-1] + 7.5], np.arange(-64, 113),
                            first_rock, cmap="viridis", vmin=0, vmax=72)
        ax.set(title="各方向最先遇到的实心方块半径 − R", xlabel="绕中心角度 / 度", ylabel="Y")
        ax.set_xticks([0, 90, 180, 270])
        report = next(row for row in results if row["seed"] == seed)
        metrics["seeds"].append({"seed": int(seed), "columns": int(report["columns"]),
                                  "guaranteedCoreAirSamples": int(report["coreAirSamples"]),
                                  "maxPreInterpolationVerticalRelief": float(report["maxVerticalRelief"])})
    for ax in axes.flat:
        ax.spines[["top", "right"]].set_visible(False)
        ax.set_facecolor("#fbfaf6")
    fig.colorbar(mesh, ax=list(axes[1]), label="偏移 / 方块；空白为采样带内没有岩体", shrink=.85)
    fig.suptitle("主世界深渊：Y=128 以下独立三维井壁；浅绿带 Y=112～144 接回肩部", fontsize=15)
    fig.supxlabel("上排实心像素来自真实 NoiseChunk 列；下排含原版洞穴开口，因此读数可超过井壁轮廓。", fontsize=10)
    fig.savefig(output / "wall-sections.png", dpi=160, bbox_inches="tight")
    plt.close(fig)
    support = source / "reported-runtime-surface-wall.json"
    if support.exists():
        metrics["fullSurfaceSupport"] = json.loads(support.read_text(encoding="utf-8"))
    (output / "wall-metrics.json").write_text(json.dumps(metrics, indent=2), encoding="utf-8")
    print(json.dumps(metrics, indent=2))


if __name__ == "__main__":
    main()
