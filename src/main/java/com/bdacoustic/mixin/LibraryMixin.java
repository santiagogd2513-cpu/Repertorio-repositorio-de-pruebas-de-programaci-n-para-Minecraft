package com.bdacoustic.mixin;

import com.bdacoustic.audio.OpenAlAcoustics;
import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Library.class)
public abstract class LibraryMixin {
    @Inject(method = "cleanup", at = @At("HEAD"))
    private void bdacoustic$resetEffectsForDeviceReload(CallbackInfo callback) {
        OpenAlAcoustics.onContextClosing();
    }
}
