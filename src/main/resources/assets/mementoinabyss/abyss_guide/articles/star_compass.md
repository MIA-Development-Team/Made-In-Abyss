# Star Compass

Use the **Star Compass** to locate the Abyss stronghold, then use it on the **Abyss Portal Core** in the portal room. The demonstration shows the core constructing a ring wall and center column below the room before forming the portal.

![Abyss map](texture:mementoinabyss:painting/abyss_map)

```recall-scene abyss_portal
```

## Activation sequence

> The portal core starts at stage 0. Using a Star Compass on it sets `compass=true`, then the core advances once every 20 ticks. The portal is created only at stage 12.

1. Find the Abyss stronghold with the Star Compass.
2. Use the Star Compass on the Abyss Portal Core.
3. Wait through the twelve activation stages.
   - Stages 1 through 11 clear the inside of each layer while placing a fossilized-wood block at its center and a ring of fossilized wood, tuff, or polished tuff around it. Each new layer remains in place, extending the column and wall downward.
   - Stage 12 creates the portal surface and frame 15 blocks below the core.
4. Step into the new portal surface to travel to the Abyss.
5. Review [Getting Started](guide:getting_started) before descending.

---

### Field notebook

Use `portal_center` for a reliable landmark. Replace ~~unverified shortcuts~~ with confirmed routes.

```text
portal_center: abyss_portal
  activation: star_compass
  core_stage: 0 -> 12
  vertical_offset: -15
```

The [Minecraft website](https://www.minecraft.net/) opens with the game's normal link confirmation.
