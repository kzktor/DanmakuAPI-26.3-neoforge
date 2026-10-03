package dev.xkmc.danmakuapi.content.item;

import dev.xkmc.danmakuapi.content.custom.data.ISpellFormData;
import dev.xkmc.danmakuapi.content.custom.data.SpellDataHolder;
import dev.xkmc.danmakuapi.content.custom.screen.ClientCustomSpellHandler;
import dev.xkmc.danmakuapi.content.spell.item.SpellContainer;
import dev.xkmc.danmakuapi.init.data.DanmakuLang;
import dev.xkmc.danmakuapi.init.registrate.DanmakuItems;
import dev.xkmc.l2core.content.raytrace.IGlowingTarget;
import dev.xkmc.l2core.content.raytrace.RayTraceUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class CustomSpellItem extends Item implements IGlowingTarget {

	private final SpellDataHolder def;
	private final boolean requireTarget;

	public CustomSpellItem(Properties properties, boolean requireTarget, ISpellFormData<?> def) {
		super(properties);
		this.requireTarget = requireTarget;
		this.def = new SpellDataHolder(def.cast());
	}

	private ISpellFormData<?> getData(ItemStack stack) {
		return DanmakuItems.SPELL_DATA.getOrDefault(stack, def).cast();
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;
		ISpellFormData<?> data = getData(stack);
		if (player.isShiftKeyDown()) {
			if (level.isClientSide()) {
				ClientCustomSpellHandler.open(stack.getHoverName(), data);
			}
		} else {
			LivingEntity target = RayTraceUtil.serverGetTarget(player);
			if (requireTarget && target == null)
				return InteractionResult.FAIL;
			if (!player.getAbilities().instabuild) {
				Item ammo = data.getAmmoCost();
				int toCost = data.cost();
				if (!consumeAmmo(ammo, toCost, player, false))
					return InteractionResult.FAIL;
				if (player instanceof ServerPlayer)
					consumeAmmo(ammo, toCost, player, true);
			}
			if (player instanceof ServerPlayer sp) {
				SpellContainer.castSpell(sp, data::createInstance, target);
				player.getCooldowns().addCooldown(stack, data.getDuration());
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
		ISpellFormData<?> data = getData(stack);
		list.accept(DanmakuLang.SPELL_COST.get(data.cost(), data.getAmmoCost().getDefaultInstance().getHoverName()));
		if (requireTarget) {
			list.accept(DanmakuLang.SPELL_TARGET.get());
		}
	}

	private static boolean consumeAmmo(Item ammo, int toCost, Player player, boolean execute) {
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (toCost <= 0) break;
			ItemStack item = inv.getItem(i);
			if (item.is(ammo)) {
				int consume = Math.min(toCost, item.getCount());
				if (execute) item.shrink(consume);
				toCost -= consume;
			}
		}
		return toCost == 0;
	}

	// 26.3: Item#inventoryTick now runs server-side only; the client raycast is driven by
	// IGlowingTarget/IClientTickItem#clientMainHandTick instead.

	@Override
	public int getDistance(ItemStack stack) {
		return 64;
	}

}
