package com.bdacoustic.sound;

import com.bdacoustic.config.AcousticConfig;
import com.bdacoustic.physics.AcousticEngine;
import com.bdacoustic.physics.AcousticEnvironment;
import com.bdacoustic.physics.AcousticMotionTracker;
import com.bdacoustic.physics.AcousticResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/** Routes every Forge PlaySoundEvent through the engine; relative sounds keep neutral spatial propagation. */
@Mod.EventBusSubscriber(modid = "bdacoustic", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientSoundHook {
    private static final Logger LOGGER = LogManager.getLogger("BDAcousticEngine");
    private static final AcousticMotionTracker SOURCE_MOTION = new AcousticMotionTracker();
    private static final AcousticResult NEUTRAL = new AcousticResult(0.0, 0.0, 1.0, 1.0,
            20000.0, 1.0, 0, 0, false, 1.0, 1.0, 1.0, 0.0, 343.2);

    private ClientSoundHook() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!AcousticConfig.ENABLED.get()) return;
        SoundInstance sound = event.getSound();
        if (sound == null || sound instanceof AcousticSoundInstance) return;

        Minecraft mc = Minecraft.getInstance();
        // Attenuation.NONE disables vanilla distance rolloff, not acoustic wall transmission.
        boolean spatial = !sound.isRelative();
        boolean canEvaluate = spatial && mc.level != null && mc.player != null;
        UUID trackingId = canEvaluate && sound instanceof TickableSoundInstance ? UUID.randomUUID() : null;
        AcousticResult result = canEvaluate ? evaluate(mc, sound, trackingId) : NEUTRAL;
        boolean applyGain = canEvaluate && AcousticConfig.APPLY_GAIN.get();
        boolean applyDoppler = canEvaluate && AcousticConfig.APPLY_DOPPLER.get();

        if (sound instanceof TickableSoundInstance tickable) {
            event.setSound(new AcousticTickableSoundInstance(tickable, result, applyGain, applyDoppler,
                    () -> canEvaluate ? evaluate(mc, sound, trackingId) : NEUTRAL));
        } else {
            event.setSound(new AcousticSoundInstance(sound, result, applyGain, applyDoppler));
        }

        if (canEvaluate && LOGGER.isDebugEnabled()) LOGGER.debug(
                "Acoustic route {}: distance={} gain={} cutoff={}Hz doppler={} blockedRays={}",
                sound.getLocation(), format(result.distance()), format(result.totalGain()),
                Math.round(result.lowPassHz()), format(result.dopplerRatio()), result.blockedRays());
    }

    private static AcousticResult evaluate(Minecraft mc, SoundInstance sound, UUID trackingId) {
        if (mc.level == null || mc.player == null) return NEUTRAL;
        Vec3 source = new Vec3(sound.getX(), sound.getY(), sound.getZ());
        Vec3 listener = mc.player.position();
        Vec3 direction = listener.subtract(source);
        double distance = direction.length();
        if (distance > AcousticConfig.MAX_DISTANCE.get()) return NEUTRAL;

        Vec3 sourceVelocity = trackingId == null ? Vec3.ZERO
                : SOURCE_MOTION.update(trackingId, source, System.nanoTime());
        Vec3 listenerVelocity = mc.player.getDeltaMovement().scale(20.0);
        Vec3 directionUnit = distance < 1.0e-8 ? Vec3.ZERO : direction.scale(1.0 / distance);
        double sourceRadialVelocity = sourceVelocity.dot(directionUnit);
        double listenerRadialVelocity = -listenerVelocity.dot(directionUnit);

        AcousticEnvironment environment = AcousticEnvironment.standard();
        int rays = Math.max(4, (int) Math.round(AcousticConfig.MAX_RAYS.get() * AcousticConfig.QUALITY.get()));
        return AcousticEngine.evaluate(mc.level, source, listener, environment,
                sourceRadialVelocity, listenerRadialVelocity, rays);
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
