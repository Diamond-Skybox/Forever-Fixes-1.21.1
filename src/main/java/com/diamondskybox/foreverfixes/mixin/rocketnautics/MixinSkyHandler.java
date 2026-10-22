package com.diamondskybox.foreverfixes.mixin.rocketnautics;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create: Cosmonautics silences the game while the camera is between Y 1000 and 2000 (plus the dimension's sky offset): every new
 * sound except its own high wind is dropped, and all playing sounds are stopped on entering the band. That band is the edge of
 * space in a normal world, but the mountain tops of the 2096-block-tall JJThunder overworld, where the game then went silent.
 * Both steps are skipped; the high wind sound stays.
 */
@Mixin(targets = "dev.devce.rocketnautics.client.SkyHandler", remap = false)
public abstract class MixinSkyHandler
{
	@WrapOperation(method = "onPlaySound", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/client/event/sound/PlaySoundEvent;setSound(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
	private static void foreverfixes$keepSounds(PlaySoundEvent event, SoundInstance sound, Operation<Void> original)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.NO_HIGH_ALTITUDE_SILENCE))
		{
			original.call(event, sound);
		}
	}

	@WrapOperation(method = "onClientTick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
	private static void foreverfixes$keepPlaying(SoundManager manager, Operation<Void> original)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.NO_HIGH_ALTITUDE_SILENCE))
		{
			original.call(manager);
		}
	}
}
