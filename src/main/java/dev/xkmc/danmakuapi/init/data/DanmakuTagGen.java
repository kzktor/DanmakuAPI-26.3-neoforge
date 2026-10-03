package dev.xkmc.danmakuapi.init.data;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import dev.xkmc.danmakuapi.init.DanmakuAPI;
import dev.xkmc.danmakuapi.init.registrate.DanmakuItems;
import mezz.jei.api.constants.Tags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class DanmakuTagGen {

	public static final TagKey<Item> CUSTOM_SPELL = item("custom_spell");
	public static final TagKey<Item> PRESET_SPELL = item("preset_spell");
	public static final TagKey<Item> DANMAKU = item("danmaku");
	public static final TagKey<Item> LASER = item("laser");
	public static final TagKey<Item> DANMAKU_SHOOTER = item("danmaku_shooter");

	public static TagKey<Item> item(String id) {
		return ItemTags.create(DanmakuAPI.loc(id));
	}

	public static void genItemTag(RegistrateItemTagsProvider pvd) {
		var danmaku = pvd.tag(DANMAKU);
		for (var e : DanmakuItems.Bullet.values()) {
			danmaku.addTag(e.tag);
		}
		var laser = pvd.tag(LASER);
		for (var e : DanmakuItems.Laser.values()) {
			laser.addTag(e.tag);
		}
		pvd.tag(PRESET_SPELL);
		pvd.tag(DANMAKU_SHOOTER).addTag(DANMAKU).addTag(LASER).addTag(CUSTOM_SPELL).addTag(PRESET_SPELL);
		pvd.tag(ItemTags.create(Tags.HIDDEN_FROM_RECIPE_VIEWERS))
				.addTag(DANMAKU).addTag(LASER);
	}

}
