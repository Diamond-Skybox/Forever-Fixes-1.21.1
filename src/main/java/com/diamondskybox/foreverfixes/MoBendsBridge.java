package com.diamondskybox.foreverfixes;

import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Questions to Mo' Bends, asked by reflection so nothing here needs Mo' Bends on the classpath. */
public final class MoBendsBridge
{
	private static final MethodHandle IS_ENTITY_ANIMATED = find();

	private MoBendsBridge()
	{
	}

	private static MethodHandle find()
	{
		try
		{
			final Class<?> helper = Class.forName("goblinbob.mobends.core.util.BenderHelper");
			return MethodHandles.publicLookup().findStatic(helper, "isEntityAnimated",
					MethodType.methodType(boolean.class, LivingEntity.class));
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/** true = Mo' Bends animates this entity right now (its bender exists and is enabled). */
	public static boolean animates(LivingEntity entity)
	{
		if (IS_ENTITY_ANIMATED == null || entity == null)
		{
			return false;
		}
		try
		{
			return (boolean) IS_ENTITY_ANIMATED.invoke(entity);
		}
		catch (Throwable t)
		{
			return false;
		}
	}
}
