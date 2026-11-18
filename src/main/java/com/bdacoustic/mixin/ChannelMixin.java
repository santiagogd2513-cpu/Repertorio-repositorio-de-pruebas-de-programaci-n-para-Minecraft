package com.bdacoustic.mixin;

import com.bdacoustic.audio.OpenAlAcoustics;
import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public abstract class ChannelMixin {
    @Inject(method = "play", at = @At("HEAD"))
    private void bdacoustic$attachEnvironmentalEffects(CallbackInfo callback) {
        OpenAlAcoustics.attachTo((Channel) (Object) this);
    }
}
