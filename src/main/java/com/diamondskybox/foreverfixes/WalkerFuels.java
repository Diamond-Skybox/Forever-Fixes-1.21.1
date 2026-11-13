package com.diamondskybox.foreverfixes;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.walker.WalkerFuel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Let it Walk!: walkers burn other fuels than lava, each going further per bucket (letitwalk.fuels). Let it Walk counts its jet fuel in
 * millibuckets of lava (hover, speed, charged jumps and dashes cost fixed amounts); here a millibucket of a fuel with efficiency 4
 * pays for 4 of them, so a tank lasts four times as long. Thrust and jump height do not depend on the fuel. The Walker Core's own
 * tank takes any listed fuel (one kind at a time, as a tank does), and the tank behind the core is drained of any listed fuel.
 */
public final class WalkerFuels
{
	private record Entry(TagKey<Fluid> tag, Fluid fluid, double efficiency)
	{
	}

	private static List<? extends String> parsedFrom;
	private static List<Entry> entries = List.of();

	private WalkerFuels()
	{
	}

	private static List<Entry> entries()
	{
		final List<? extends String> raw = ForeverFixesCommonConfig.LETITWALK_FUELS.get();
		if (raw != parsedFrom)
		{
			final List<Entry> list = new ArrayList<>();
			for (String line : raw)
			{
				final int eq = line.lastIndexOf('=');
				if (eq <= 0)
				{
					continue;
				}
				try
				{
					final double efficiency = Double.parseDouble(line.substring(eq + 1).trim());
					final String id = line.substring(0, eq).trim();
					if (efficiency <= 0)
					{
						continue;
					}
					if (id.startsWith("#"))
					{
						list.add(new Entry(TagKey.create(Registries.FLUID, ResourceLocation.parse(id.substring(1))), null, efficiency));
					}
					else
					{
						list.add(new Entry(null, BuiltInRegistries.FLUID.get(ResourceLocation.parse(id)), efficiency));
					}
				}
				catch (RuntimeException e)
				{
					ForeverFixes.LOGGER.warn("letitwalk.fuels: ignoring \"{}\" ({})", line, e.getMessage());
				}
			}
			entries = list;
			parsedFrom = raw;
		}
		return entries;
	}

	/** lava-millibuckets one millibucket of this fluid pays for; 0 if it is no fuel */
	public static double efficiency(FluidStack stack)
	{
		if (stack == null || stack.isEmpty())
		{
			return 0;
		}
		if (stack.is(Fluids.LAVA))
		{
			return 1;
		}
		for (Entry entry : entries())
		{
			if (entry.tag() != null ? stack.is(entry.tag()) : stack.is(entry.fluid()))
			{
				return entry.efficiency();
			}
		}
		return 0;
	}

	/** the fuel a walker has, in lava-millibuckets (its own tank and the tank behind its core) */
	public static int available(WalkerCoreBlockEntity be)
	{
		double total = be.lavaTank.getFluidAmount() * efficiency(be.lavaTank.getFluid());
		final IFluidHandler tank = WalkerFuel.tank(be);
		if (tank != null)
		{
			for (int i = 0; i < tank.getTanks(); i++)
			{
				final FluidStack stack = tank.getFluidInTank(i);
				total += stack.getAmount() * efficiency(stack);
			}
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.floor(total));
	}

	/** Let it Walk's burn in lava-millibuckets: whole millibuckets of fuel are drained as the cost reaches them */
	public static boolean burn(WalkerCoreBlockEntity be, double amount)
	{
		be.fuelOwed += amount;
		if (be.fuelOwed < 1)
		{
			return available(be) > 0;
		}
		double owed = be.fuelOwed;
		double burnt = 0;
		boolean fuelLeft = false;
		final FluidStack own = be.lavaTank.getFluid();
		final double ownEfficiency = efficiency(own);
		if (ownEfficiency > 0)
		{
			final int millibuckets = (int) Math.floor(owed / ownEfficiency);
			if (millibuckets > 0)
			{
				final int drained = be.lavaTank.drain(millibuckets, FluidAction.EXECUTE).getAmount();
				owed -= drained * ownEfficiency;
				burnt += drained * ownEfficiency;
			}
			fuelLeft = be.lavaTank.getFluidAmount() > 0;
		}
		if (owed >= 1)
		{
			final IFluidHandler tank = WalkerFuel.tank(be);
			if (tank != null)
			{
				for (int i = 0; i < tank.getTanks() && owed >= 1; i++)
				{
					final FluidStack stack = tank.getFluidInTank(i);
					final double efficiency = efficiency(stack);
					if (efficiency <= 0)
					{
						continue;
					}
					final int millibuckets = (int) Math.floor(owed / efficiency);
					if (millibuckets > 0)
					{
						final int drained = tank.drain(stack.copyWithAmount(millibuckets), FluidAction.EXECUTE).getAmount();
						owed -= drained * efficiency;
						burnt += drained * efficiency;
					}
					fuelLeft |= tank.getFluidInTank(i).getAmount() > 0;
				}
			}
		}
		be.lavaBurnt += (long) burnt;
		// a cost below one millibucket of the fuel stays owed while there is fuel; out of fuel, only the fraction is kept (as Let it Walk does)
		final boolean paid = owed < 1 || fuelLeft;
		be.fuelOwed = paid ? owed : owed - Math.floor(owed);
		return paid;
	}
}
