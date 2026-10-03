package dev.xkmc.danmakuapi.content.spell.item;

import dev.xkmc.danmakuapi.init.DanmakuAPI;
import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.l2core.capability.conditionals.ConditionalToken;
import dev.xkmc.l2core.capability.conditionals.TokenKey;
import dev.xkmc.l2core.init.L2LibReg;
import dev.xkmc.l2serial.serialization.marker.SerialClass;
import dev.xkmc.l2serial.serialization.marker.SerialField;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@SerialClass
public class SpellContainer extends ConditionalToken {

	private static final TokenKey<SpellContainer> SPELL = TokenKey.of(DanmakuAPI.loc("spellcards"));

	public static void castSpell(ServerPlayer sp, Supplier<? extends ItemSpell> sup, @Nullable LivingEntity target) {
		ItemSpell spell = sup.get();
		spell.start(sp, target);
		L2LibReg.PLAYER_CONDITIONAL.type().getOrCreate(sp).getOrCreateData(SPELL, SpellContainer::new).spells.add(spell);
	}
	public static void clear(ServerPlayer sp) {
		var data = L2LibReg.PLAYER_CONDITIONAL.type().getOrCreate(sp).getOrCreateData(SPELL, SpellContainer::new);
		for (var spell : data.spells) {
			for (var e : spell.cache) {
				e.markErased(true);
			}
		}
		for (var e : data.cache) {
			e.markErased(true);
		}
		data.cache.clear();
		data.spells.clear();
	}

	public static void track(ServerPlayer sp, SimplifiedProjectile e) {
		var data = L2LibReg.PLAYER_CONDITIONAL.type().getOrCreate(sp).getOrCreateData(SPELL, SpellContainer::new);
		data.cache.add(e);
	}

	@SerialField
	private final List<ItemSpell> spells = new LinkedList<>();

	private final List<SimplifiedProjectile> cache = new LinkedList<>();

	@Override
	public boolean tick(LivingEntity player) {
		var itr = spells.iterator();
		while (itr.hasNext()) {
			var spell = itr.next();
			boolean remove = spell.tick(player);
			if (remove) {
				itr.remove();
				cache.addAll(spell.cache);
			}
		}
		cache.removeIf(e -> !e.isValid());
		return spells.isEmpty() && cache.isEmpty();
	}

}
