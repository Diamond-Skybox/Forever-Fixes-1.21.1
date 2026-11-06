package com.diamondskybox.foreverfixes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lootr boss chests (bossChest.*): a boss that doesn't leave its own chest drops nothing on the ground; instead a Lootr chest appears where
 * it died. Only players who damaged the boss can open it, each gets their own roll of the boss's loot table (as if they had landed the
 * kill), and the chest vanishes after bossChest.minutes. The rolls are made at the moment of death and kept until the chest goes.
 */
public final class BossChests
{
	private static final String DAMAGERS = "foreverfixes_boss_damagers";

	private BossChests()
	{
	}

	// ---- who fought it

	public static void onDamage(LivingDamageEvent.Post event)
	{
		final LivingEntity boss = event.getEntity();
		if (boss.level().isClientSide() || !(event.getSource().getEntity() instanceof ServerPlayer player) || event.getNewDamage() <= 0.0F
				|| !isChestBoss(boss.getType()))
		{
			return;
		}
		final CompoundTag data = boss.getPersistentData();
		final ListTag list = data.getList(DAMAGERS, Tag.TAG_INT_ARRAY);
		final Tag uuid = NbtUtils.createUUID(player.getUUID());
		if (!list.contains(uuid))
		{
			list.add(uuid);
			data.put(DAMAGERS, list);
		}
	}

	// ---- the chest replaces the drops

	public static void onDrops(LivingDropsEvent event)
	{
		final LivingEntity boss = event.getEntity();
		if (!(boss.level() instanceof ServerLevel level) || !isChestBoss(boss.getType()))
		{
			return;
		}
		final List<ServerPlayer> fighters = new ArrayList<>();
		for (Tag t : boss.getPersistentData().getList(DAMAGERS, Tag.TAG_INT_ARRAY))
		{
			final ServerPlayer p = level.getServer().getPlayerList().getPlayer(NbtUtils.loadUUID(t));
			if (p != null)
			{
				fighters.add(p);
			}
		}
		if (fighters.isEmpty())
		{
			return;
		}
		final List<ItemStack> groundDrops = new ArrayList<>();
		for (ItemEntity e : event.getDrops())
		{
			groundDrops.add(e.getItem().copy());
		}
		final LootTable table = level.getServer().reloadableRegistries().getLootTable(boss.getLootTable());
		final Map<UUID, List<ItemStack>> rolls = new HashMap<>();
		boolean any = false;
		for (ServerPlayer p : fighters)
		{
			final List<ItemStack> roll = roll(level, boss, event.getSource(), p, table);
			if (roll.isEmpty())
			{
				// bosses whose real drops aren't in their loot table (the Wither's star): everyone gets what it actually dropped
				for (ItemStack s : groundDrops)
				{
					roll.add(s.copy());
				}
			}
			any |= !roll.isEmpty();
			rolls.put(p.getUUID(), roll);
		}
		if (!any)
		{
			return;
		}
		final BlockPos pos = findSpot(level, boss.blockPosition());
		final Block chest = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("lootr", "lootr_chest"));
		if (pos == null || chest == null || chest == net.minecraft.world.level.block.Blocks.AIR)
		{
			return;
		}
		BlockState state = chest.defaultBlockState();
		if (state.hasProperty(ChestBlock.FACING))
		{
			state = state.setValue(ChestBlock.FACING, facingToward(pos, fighters.get(0)));
		}
		if (!level.setBlock(pos, state, Block.UPDATE_ALL))
		{
			return;
		}
		final BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof RandomizableContainerBlockEntity container)
		{
			container.setLootTable(boss.getLootTable(), level.getRandom().nextLong());
			container.setChanged();
		}
		final Set<UUID> ids = new HashSet<>(rolls.keySet());
		final int minutes = ForeverFixesCommonConfig.BOSS_CHEST_MINUTES.get();
		data(level.getServer()).add(new Entry(level.dimension(), pos.immutable(), level.getServer().overworld().getGameTime() + minutes * 1200L, ids, rolls));
		event.setCanceled(true);
		final Component msg = Component.translatable("%s left a chest for those who fought it at %s, %s, %s. It vanishes in %s minutes.",
				boss.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), minutes);
		for (ServerPlayer p : fighters)
		{
			p.sendSystemMessage(msg);
		}
	}

	private static List<ItemStack> roll(ServerLevel level, LivingEntity boss, DamageSource source, ServerPlayer player, LootTable table)
	{
		final List<ItemStack> out = new ArrayList<>();
		if (table == LootTable.EMPTY)
		{
			return out;
		}
		final LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, boss)
				.withParameter(LootContextParams.ORIGIN, boss.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source)
				.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
				.withParameter(LootContextParams.ATTACKING_ENTITY, player)
				.withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
				.withLuck(player.getLuck())
				.create(LootContextParamSets.ENTITY);
		for (ItemStack s : table.getRandomItems(params))
		{
			if (!s.isEmpty())
			{
				out.add(s);
			}
		}
		return out;
	}

	private static BlockPos findSpot(ServerLevel level, BlockPos death)
	{
		BlockPos p = death;
		for (int up = 0; up < 4 && !free(level, p); up++)
		{
			p = p.above();
		}
		if (!free(level, p))
		{
			for (BlockPos q : BlockPos.betweenClosed(death.offset(-2, -1, -2), death.offset(2, 2, 2)))
			{
				if (free(level, q))
				{
					p = q.immutable();
					break;
				}
			}
		}
		if (!free(level, p))
		{
			return null;
		}
		// settle onto the ground (bosses die mid-air)
		for (int down = 0; down < 32 && p.getY() > level.getMinBuildHeight() && free(level, p.below()); down++)
		{
			p = p.below();
		}
		return p;
	}

	private static boolean free(ServerLevel level, BlockPos p)
	{
		return level.isInWorldBounds(p) && level.getBlockState(p).canBeReplaced() && level.getBlockEntity(p) == null;
	}

	private static Direction facingToward(BlockPos pos, Player player)
	{
		final double dx = player.getX() - (pos.getX() + 0.5D);
		final double dz = player.getZ() - (pos.getZ() + 0.5D);
		return Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
	}

	// ---- Lootr asks us for a player's loot (mixin into its default filler)

	public static boolean fill(Level level, BlockPos pos, Player player, Container inventory)
	{
		if (!(level instanceof ServerLevel server) || player == null)
		{
			return false;
		}
		final Entry entry = data(server.getServer()).find(server.dimension(), pos);
		if (entry == null)
		{
			return false;
		}
		final List<ItemStack> items = entry.rolls.getOrDefault(player.getUUID(), List.of());
		int slot = 0;
		for (ItemStack s : items)
		{
			if (slot < inventory.getContainerSize())
			{
				inventory.setItem(slot++, s.copy());
			}
			else if (!player.getInventory().add(s.copy()))
			{
				player.drop(s.copy(), false);
			}
		}
		return true;
	}

	// ---- only fighters open it, nobody breaks it

	public static void onRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player))
		{
			return;
		}
		final Entry entry = data(level.getServer()).find(level.dimension(), event.getPos());
		if (entry != null && !entry.fighters.contains(player.getUUID()) && !player.isCreative())
		{
			player.displayClientMessage(Component.literal("Only those who fought this boss can open its chest."), true);
			event.setCanceled(true);
		}
	}

	public static void onBreak(BlockEvent.BreakEvent event)
	{
		if (event.getLevel() instanceof ServerLevel level && !event.getPlayer().isCreative()
				&& data(level.getServer()).find(level.dimension(), event.getPos()) != null)
		{
			event.setCanceled(true);
		}
	}

	// ---- 20 minutes later

	public static void onServerTick(ServerTickEvent.Post event)
	{
		final MinecraftServer server = event.getServer();
		if (server.getTickCount() % 100 != 0)
		{
			return;
		}
		final Data data = data(server);
		final long now = server.overworld().getGameTime();
		for (Iterator<Entry> it = data.entries.iterator(); it.hasNext(); )
		{
			final Entry e = it.next();
			if (now < e.expiry)
			{
				continue;
			}
			final ServerLevel level = server.getLevel(e.dimension);
			if (level == null)
			{
				it.remove();
				data.setDirty();
				continue;
			}
			if (!level.isLoaded(e.pos))
			{
				continue;
			}
			final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(e.pos).getBlock());
			if ("lootr".equals(id.getNamespace()))
			{
				level.removeBlockEntity(e.pos);
				level.setBlock(e.pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
			it.remove();
			data.setDirty();
		}
	}

	// ---- which bosses

	private static List<? extends String> matchConfig, excludeConfig;
	private static final Map<EntityType<?>, Boolean> CACHE = new HashMap<>();

	public static boolean isChestBoss(EntityType<?> type)
	{
		if (!ForeverFixesCommonConfig.BOSS_CHEST_ENABLED.get())
		{
			return false;
		}
		final List<? extends String> match = ForeverFixesCommonConfig.BOSS_CHEST_BOSSES.get();
		final List<? extends String> exclude = ForeverFixesCommonConfig.BOSS_CHEST_EXCLUDED.get();
		if (match != matchConfig || exclude != excludeConfig)
		{
			CACHE.clear();
			matchConfig = match;
			excludeConfig = exclude;
		}
		return CACHE.computeIfAbsent(type, t -> matches(t, match) && !matches(t, exclude));
	}

	/** Called on tag reload so tag membership is re-read. */
	public static void clearCache()
	{
		CACHE.clear();
	}

	private static boolean matches(EntityType<?> type, List<? extends String> list)
	{
		for (String s : list)
		{
			if (s.startsWith("#"))
			{
				final ResourceLocation rl = ResourceLocation.tryParse(s.substring(1));
				if (rl != null && type.is(TagKey.create(Registries.ENTITY_TYPE, rl)))
				{
					return true;
				}
			}
			else if (s.equals(EntityType.getKey(type).toString()))
			{
				return true;
			}
		}
		return false;
	}

	// ---- saved state

	private record Entry(ResourceKey<Level> dimension, BlockPos pos, long expiry, Set<UUID> fighters, Map<UUID, List<ItemStack>> rolls)
	{
	}

	private static Data data(MinecraftServer server)
	{
		return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), "foreverfixes_boss_chests");
	}

	private static final class Data extends SavedData
	{
		private final List<Entry> entries = new ArrayList<>();

		void add(Entry e)
		{
			entries.removeIf(o -> o.dimension.equals(e.dimension) && o.pos.equals(e.pos));
			entries.add(e);
			setDirty();
		}

		Entry find(ResourceKey<Level> dimension, BlockPos pos)
		{
			for (Entry e : entries)
			{
				if (e.pos.equals(pos) && e.dimension.equals(dimension))
				{
					return e;
				}
			}
			return null;
		}

		static Data load(CompoundTag tag, HolderLookup.Provider registries)
		{
			final Data d = new Data();
			for (Tag t : tag.getList("chests", Tag.TAG_COMPOUND))
			{
				final CompoundTag c = (CompoundTag) t;
				final ResourceLocation dim = ResourceLocation.tryParse(c.getString("dim"));
				if (dim == null)
				{
					continue;
				}
				final Set<UUID> fighters = new HashSet<>();
				final Map<UUID, List<ItemStack>> rolls = new HashMap<>();
				for (Tag pt : c.getList("players", Tag.TAG_COMPOUND))
				{
					final CompoundTag pc = (CompoundTag) pt;
					final UUID id = pc.getUUID("id");
					fighters.add(id);
					final List<ItemStack> items = new ArrayList<>();
					for (Tag it : pc.getList("items", Tag.TAG_COMPOUND))
					{
						ItemStack.parse(registries, it).ifPresent(items::add);
					}
					rolls.put(id, items);
				}
				d.entries.add(new Entry(ResourceKey.create(Registries.DIMENSION, dim), BlockPos.of(c.getLong("pos")), c.getLong("expiry"), fighters, rolls));
			}
			return d;
		}

		@Override
		public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)
		{
			final ListTag list = new ListTag();
			for (Entry e : entries)
			{
				final CompoundTag c = new CompoundTag();
				c.putString("dim", e.dimension.location().toString());
				c.putLong("pos", e.pos.asLong());
				c.putLong("expiry", e.expiry);
				final ListTag players = new ListTag();
				for (UUID id : e.fighters)
				{
					final CompoundTag pc = new CompoundTag();
					pc.putUUID("id", id);
					final ListTag items = new ListTag();
					for (ItemStack s : e.rolls.getOrDefault(id, List.of()))
					{
						items.add(s.save(registries));
					}
					pc.put("items", items);
					players.add(pc);
				}
				c.put("players", players);
				list.add(c);
			}
			tag.put("chests", list);
			return tag;
		}
	}
}
