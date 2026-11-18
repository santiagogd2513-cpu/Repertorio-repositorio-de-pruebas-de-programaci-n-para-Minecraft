package com.bdacoustic.audio;

import com.mojang.blaze3d.audio.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import java.util.concurrent.atomic.AtomicBoolean;

/** Attaches OpenAL EFX to the real Minecraft source on the sound-engine thread. */
public final class OpenAlAcoustics {
    private static final Logger LOGGER = LogManager.getLogger("BDAcousticEngine/EFX");
    private static final AtomicBoolean WARNED = new AtomicBoolean();
    private static volatile int reverbEffect;
    private static volatile int reverbSlot;
    private static volatile boolean unavailable;

    private OpenAlAcoustics() {}

    public static void attachTo(Channel channel) {
        if (channel == null || unavailable) return;
        try {
            int source = ((com.bdacoustic.mixin.ChannelAccessor) (Object) channel).bdacoustic$getSource();
            attachToSource(source);
        } catch (Throwable error) {
            unavailable = true;
            if (WARNED.compareAndSet(false, true)) {
                LOGGER.warn("OpenAL EFX is unavailable; vanilla audio remains active without environmental reverb.", error);
            }
        }
    }

    private static synchronized void attachToSource(int source) {
        if (source <= 0) return;
        if (!ALC.getCapabilities().ALC_EXT_EFX) {
            unavailable = true;
            if (WARNED.compareAndSet(false, true)) LOGGER.warn(
                    "The active OpenAL device does not support ALC_EXT_EFX; acoustic source effects are disabled.");
            return;
        }
        if (reverbEffect == 0 || reverbSlot == 0) createReverb();
        if (reverbSlot != 0) {
            AL11.alSource3i(source, EXTEfx.AL_AUXILIARY_SEND_FILTER, reverbSlot, 0, EXTEfx.AL_FILTER_NULL);
        }
    }

    private static void createReverb() {
        int effect = 0;
        int slot = 0;
        try {
            effect = EXTEfx.alGenEffects();
            EXTEfx.alEffecti(effect, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DENSITY, 0.82f);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DIFFUSION, 0.88f);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAIN, 0.22f);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAINHF, 0.78f);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_TIME, 1.15f);
            EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.72f);
            slot = EXTEfx.alGenAuxiliaryEffectSlots();
            EXTEfx.alAuxiliaryEffectSloti(slot, EXTEfx.AL_EFFECTSLOT_EFFECT, effect);
            reverbEffect = effect;
            reverbSlot = slot;
            LOGGER.info("OpenAL EFX environmental reverb attached to Minecraft audio sources.");
        } catch (Throwable error) {
            if (slot != 0) {
                try { EXTEfx.alDeleteAuxiliaryEffectSlots(slot); } catch (Throwable ignored) {}
            }
            if (effect != 0) {
                try { EXTEfx.alDeleteEffects(effect); } catch (Throwable ignored) {}
            }
            unavailable = true;
            if (WARNED.compareAndSet(false, true)) LOGGER.warn(
                    "Could not initialize OpenAL EFX; Minecraft audio will continue without the optional effects.", error);
        }
    }

    /** The OpenAL context is about to be destroyed; old effect IDs must not be reused. */
    public static synchronized void onContextClosing() {
        reverbEffect = 0;
        reverbSlot = 0;
        unavailable = false;
    }
}
