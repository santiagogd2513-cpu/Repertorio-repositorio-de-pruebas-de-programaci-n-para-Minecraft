package com.bdacoustic.sound;

import com.bdacoustic.physics.AcousticResult;
import net.minecraft.client.resources.sounds.TickableSoundInstance;

import java.util.function.Supplier;

/** Tick-aware decorator that keeps moving/looping instances alive and refreshes propagation every two ticks. */
public final class AcousticTickableSoundInstance extends AcousticSoundInstance implements TickableSoundInstance {
    private final TickableSoundInstance tickable;
    private final Supplier<AcousticResult> resultSupplier;
    private int ticksSinceRefresh;

    public AcousticTickableSoundInstance(TickableSoundInstance delegate, AcousticResult initialResult,
            boolean applyGain, boolean applyDoppler, Supplier<AcousticResult> resultSupplier) {
        super(delegate, initialResult, applyGain, applyDoppler);
        this.tickable = delegate;
        this.resultSupplier = resultSupplier;
    }

    @Override public void tick() {
        tickable.tick();
        if (++ticksSinceRefresh >= 2) {
            ticksSinceRefresh = 0;
            updateResult(resultSupplier.get());
        }
    }

    @Override public boolean isStopped() { return tickable.isStopped(); }
}
