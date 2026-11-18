package com.bdacoustic.sound;

import com.bdacoustic.physics.AcousticResult;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.CompletableFuture;

/**
 * Transparent SoundInstance decorator. It preserves the original stream implementation,
 * location, position, looping and attenuation while applying modelled gain and Doppler pitch.
 */
public class AcousticSoundInstance implements SoundInstance {
    private final SoundInstance delegate;
    private volatile AcousticResult result;
    private final boolean applyGain;
    private final boolean applyDoppler;

    public AcousticSoundInstance(SoundInstance delegate, AcousticResult result,
                                 boolean applyGain, boolean applyDoppler) {
        this.delegate = delegate;
        this.result = result;
        this.applyGain = applyGain;
        this.applyDoppler = applyDoppler;
    }

    protected final void updateResult(AcousticResult next) {
        if (next != null) this.result = next;
    }

    protected final SoundInstance delegate() { return delegate; }
    @Override public ResourceLocation getLocation() { return delegate.getLocation(); }
    @Override public WeighedSoundEvents resolve(SoundManager manager) { return delegate.resolve(manager); }
    @Override public Sound getSound() { return delegate.getSound(); }
    @Override public SoundSource getSource() { return delegate.getSource(); }
    @Override public boolean isLooping() { return delegate.isLooping(); }
    @Override public boolean isRelative() { return delegate.isRelative(); }
    @Override public int getDelay() { return delegate.getDelay(); }
    @Override public float getVolume() {
        AcousticResult current = result;
        return delegate.getVolume() * (applyGain && current != null ? (float) current.totalGain() : 1.0f);
    }
    @Override public float getPitch() {
        AcousticResult current = result;
        double pitch = delegate.getPitch() * (applyDoppler && current != null ? current.dopplerRatio() : 1.0);
        return (float) Math.max(0.01, Math.min(2.0, pitch));
    }
    @Override public double getX() { return delegate.getX(); }
    @Override public double getY() { return delegate.getY(); }
    @Override public double getZ() { return delegate.getZ(); }
    @Override public Attenuation getAttenuation() { return delegate.getAttenuation(); }
    @Override public boolean canStartSilent() { return delegate.canStartSilent(); }
    @Override public boolean canPlaySound() { return delegate.canPlaySound(); }
    @Override public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
        return delegate.getStream(buffers, sound, looping);
    }
}
