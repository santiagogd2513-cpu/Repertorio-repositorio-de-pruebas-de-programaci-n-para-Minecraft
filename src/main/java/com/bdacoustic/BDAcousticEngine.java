package com.bdacoustic;

import com.bdacoustic.config.AcousticConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BDAcousticEngine.MOD_ID)
public final class BDAcousticEngine {
    public static final String MOD_ID = "bdacoustic";
    public BDAcousticEngine() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AcousticConfig.SPEC);
    }
}