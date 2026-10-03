package dev.xkmc.danmakuapi.init.data;

import com.tterrag.registrate.providers.generators.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import dev.xkmc.danmakuapi.init.registrate.DanmakuItems;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;

public class DanmakuRecipeGen {

	public static void genRecipes(RegistrateRecipeProvider pvd) {

		// danmaku
		{
			for (var e : DyeColor.values()) {
				Item dye = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(e.getName() + "_dye"));
				for (var t : DanmakuItems.Bullet.values()) {
					var danmaku = t.get(e).get();
					unlock(pvd, ShapedRecipeBuilder.shaped(pvd.itemLookup(), RecipeCategory.COMBAT, danmaku, 8)::unlockedBy, danmaku)
							.pattern("AAA").pattern("ABA").pattern("AAA")
							.define('A', t.tag)
							.define('B', dye)
							.save(pvd);

				}
				for (var t : DanmakuItems.Laser.values()) {
					var danmaku = t.get(e).get();
					unlock(pvd, ShapedRecipeBuilder.shaped(pvd.itemLookup(), RecipeCategory.COMBAT, danmaku, 8)::unlockedBy, danmaku)
							.pattern("AAA").pattern("ABA").pattern("AAA")
							.define('A', t.tag)
							.define('B', dye)
							.save(pvd);
				}
				unlock(pvd, ShapelessRecipeBuilder.shapeless(pvd.itemLookup(), RecipeCategory.COMBAT, DanmakuItems.Laser.PENCIL.get(e), 4)::unlockedBy,
						DanmakuItems.Laser.LASER.get(e).get())
						.requires(DanmakuItems.Bullet.BALL.get(e))
						.requires(DanmakuItems.Laser.LASER.get(e))
						.save(pvd, DanmakuItems.Laser.PENCIL.get(e).getId().withSuffix("_upgrade").toString());
			}
		}

	}

	public static <T> T unlock(RegistrateRecipeProvider pvd, BiFunction<String, Criterion<InventoryChangeTrigger.TriggerInstance>, T> func, Item item) {
		return func.apply("has_" + pvd.safeName(item), DataIngredient.items(item).getCriterion(pvd));
	}

}
