package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.KeybindKeeper;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/** Key bindings that came back as their defaults after a restart (see {@link KeybindKeeper}). */
@Mixin(value = Options.class, remap = false)
public abstract class MixinOptionsKeybinds
{
	@Shadow(remap = false)
	@Final
	private File optionsFile;

	@Inject(method = "save", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$deferDuringModLoading(CallbackInfo ci)
	{
		if (KeybindKeeper.deferSave())
		{
			ci.cancel();
		}
	}

	@Inject(method = "save", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$keepLaterKeys(CallbackInfo ci)
	{
		KeybindKeeper.afterSave((Options) (Object) this, this.optionsFile);
	}

	@Inject(method = "load(Z)V", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$afterLoad(boolean limited, CallbackInfo ci)
	{
		KeybindKeeper.afterLoad((Options) (Object) this, this.optionsFile, limited);
	}
}
