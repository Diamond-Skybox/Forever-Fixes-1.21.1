package com.diamondskybox.foreverfixes;

import com.chromeware.client.cyberware.ArmFirstPerson;
import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Chromeware shows Mantis Blades / Launcher / Monowire / Gorilla Arms in first person by drawing the whole player model from the camera
 * (Player Animation Lib third-person-model mode) with the arms swung toward the eyes. Mo' Bends animated that body too and pulled the
 * arms away (too low on screen); it now defers to Chromeware for that pass only, through its own API.
 */
public final class ChromeFirstPersonDeferral
{
	private ChromeFirstPersonDeferral()
	{
	}

	public static void register()
	{
		MoBendsAnimationControl.registerAnimationDeferral("foreverfixes:chromeware_first_person",
				entity -> entity instanceof AbstractClientPlayer player && ArmFirstPerson.applies(player));
	}
}
