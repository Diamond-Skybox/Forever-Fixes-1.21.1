package com.diamondskybox.foreverfixes.mixin.databank;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Databank 1.3.2 logs the internal formats of the main and backup depth textures ("Main: 36013, Backup: 36013") every time it
 * runs a post-processing shader, i.e. every frame one is active: a leftover debug line that filled the client log. The line is
 * dropped; the format queries and the depth copies around it are unchanged.
 */
@Mixin(targets = "com.cmdpro.databank.shaders.PostShaderInstance", remap = false)
public abstract class MixinPostShaderInstance
{
	@WrapOperation(method = "processPostChain", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private void foreverfixes$noDepthFormatLog(Logger logger, String message, Object main, Object backup, Operation<Void> original)
	{
	}
}
