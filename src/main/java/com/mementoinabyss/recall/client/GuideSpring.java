package com.mementoinabyss.recall.client;

import lombok.Getter;
import lombok.Setter;

/** A critically damped spring that preserves velocity when its target changes. */
public final class GuideSpring {
    private final float response;
    @Getter private float value;
    @Getter @Setter private float target;
    private float velocity;

    public GuideSpring(float response) {
        this.response = response;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
        velocity = 0;
    }

    public void step(float deltaSeconds) {
        if (value == target && velocity == 0) return;
        float omega = (float) (Math.PI * 2 / response);
        float displacement = value - target;
        float impulse = velocity + omega * displacement;
        float decay = (float) Math.exp(-omega * deltaSeconds);
        value = target + (displacement + impulse * deltaSeconds) * decay;
        velocity = (velocity - omega * impulse * deltaSeconds) * decay;
        if (isSettled()) snap(target);
    }

    public boolean isSettled() {
        return Math.abs(target - value) < 0.001F && Math.abs(velocity) < 0.001F;
    }
}
