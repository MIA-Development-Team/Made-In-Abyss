"""Plot real NoiseChunk base-column output; no surface/carver or LOD data implied."""
import csv
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
from matplotlib.colors import ListedColormap
import numpy as np


def main():
    source = Path("build/reports/overworld-abyss/cross-dimension-seam.csv")
    with source.open(encoding="utf-8") as stream:
        rows = list(csv.DictReader(stream))
    seeds = list(dict.fromkeys(row["seed"] for row in rows))
    angles = [0, 45, 135, 225]
    fig, axes = plt.subplots(len(angles), len(seeds), figsize=(17, 13), layout="constrained", sharex=True, sharey=True)
    for column, seed in enumerate(seeds):
        for index, angle in enumerate(angles):
            ax = axes[index, column]
            for dimension in ("abyss", "overworld"):
                samples = sorted((r for r in rows if r["seed"] == seed and int(r["angle"]) == angle and r["dimension"] == dimension), key=lambda r: int(r["r"]))
                radii = np.array([int(r["r"]) for r in samples])
                grid = np.array([[int(bit) for bit in r["blocks"]] for r in samples]).T
                minimum = int(samples[0]["min_display_y"])
                ax.pcolormesh(np.r_[radii - .5, radii[-1] + .5], np.arange(minimum, minimum + grid.shape[0] + 1), grid,
                              cmap=ListedColormap(["#faf8f2", "#62786e"]), vmin=0, vmax=1, rasterized=True)
            ax.axhline(-64, color="#db6944", linewidth=1.3, label="Dimension boundary")
            ax.axhspan(-80, -64, color="#e3a858", alpha=.12)
            ax.set(title=f"Seed {seed}\nBearing {angle}°", xlim=(304, 392), ylim=(-112, 33))
            if column == 0:
                ax.set_ylabel("OW display Y (abyss Y − 448)")
            if index == len(angles) - 1:
                ax.set_xlabel("Radial sample distance (blocks)")
            ax.grid(alpha=.12)
    fig.suptitle("Cross-dimension shared abyss wall • actual NoiseChunk base columns\nOverworld above Y −64; abyss below (Y 336–383). Green: solid; white: non-solid. Before surface/carvers; no LOD data.", fontsize=13)
    output = Path("docs/overworld-abyss-validation/cross-dimension-seam.png")
    output.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output, dpi=170)
    plt.close(fig)
    print(output)

    # These are actual production LOD section-tree snapshots, accepted by the CPU mesher.
    # Cell 1 is the generated block reference, not an independently invented approximation.
    with Path("build/reports/overworld-abyss/cross-dimension-lod.csv").open(encoding="utf-8") as stream:
        lod_rows = list(csv.DictReader(stream))
    fig, axes = plt.subplots(3, 4, figsize=(16, 9), layout="constrained", sharex=True, sharey=True)
    for index, seed in enumerate(seeds):
        for column, cell in enumerate((1, 4, 8, 16)):
            ax = axes[index, column]
            samples = [r for r in lod_rows if r["seed"] == seed and int(r["cell"]) == cell and int(r["x"]) >= 0]
            for row in samples:
                if row["solid"] == "1":
                    ax.add_patch(Rectangle((int(row["x"]), int(row["y"])), cell, cell, facecolor="#62786e", edgecolor="none"))
            ax.axhline(-64, color="#db6944", lw=1)
            ax.set(xlim=(336, 352), ylim=(-112, -16), title=f"{'Generated blocks' if cell == 1 else 'LOD'}: {cell} block cell")
            if column == 0:
                ax.set_ylabel(f"Seed {seed}\nOW display Y")
            if index == 2:
                ax.set_xlabel("X; centre slice of generated chunk (Z=8)")
            ax.grid(alpha=.1)
    fig.suptitle("Generated blocks and production LOD reduction after surface + adversarial carver pass\nSame 12-chunk fixture; production section-tree reduction and CPU mesher. Coarse cells intentionally lose detail.", fontsize=12)
    lod_output = output.with_name("cross-dimension-lod.png")
    fig.savefig(lod_output, dpi=160)
    plt.close(fig)
    print(lod_output)


if __name__ == "__main__":
    main()
