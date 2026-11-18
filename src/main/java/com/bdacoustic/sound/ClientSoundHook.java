package com.bdacoustic.sound;

import com.bdacoustic.config.AcousticConfig;
import com.bdacoustic.physics.AcousticEngine;
import com.bdacoustic.physics.AcousticResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "bdacoustic", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientSoundHook {
    private static final Logger LOGGER = LogManager.getLogger("BDAcousticEngine");
    private ClientSoundHook() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!AcousticConfig.ENABLED.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        SoundInstance sound = event.getSound();
        if (sound == null || sound.isRelative()) return;
        double dx = sound.getX() - mc.player.getX();
        double dy = sound.getY() - mc.player.getY();
        double dz = sound.getZ() - mc.player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > AcousticConfig.MAX_DISTANCE.get()) return;
        AcousticResult result = AcousticEngine.evaluate(mc.level,
                new net.minecraft.world.phys.Vec3(sound.getX(), sound.getY(), sound.getZ()),
                mc.player.position(), 20.0, 50.0, 0.0, 0.0, AcousticConfig.MAX_RAYS.get());
        // The vanilla SoundEngine still owns final mixing and playback. This hook exposes
        // evaluated physics without replacing or double-processing the original sound.
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Sound {}: d={}m delay={}s gain={} cutoff={}Hz",
                sound.getLocation(), String.format(java.util.Locale.ROOT, "%.2f", result.distance()),
                String.format(java.util.Locale.ROOT, "%.3f", result.delaySeconds()),
                String.format(java.util.Locale.ROOT, "%.3f", result.totalGain()),
                Math.round(result.lowPassHz()));
    }
}