package dev.xkmc.danmakuapi.init.registrate;

import com.tterrag.registrate.util.entry.EntityEntry;
import dev.xkmc.danmakuapi.content.entity.ItemBulletEntity;
import dev.xkmc.danmakuapi.content.entity.ItemLaserEntity;
import dev.xkmc.danmakuapi.init.DanmakuAPI;
import net.minecraft.world.entity.MobCategory;

public class DanmakuEntities {


	public static final EntityEntry<ItemBulletEntity> ITEM_DANMAKU;
	public static final EntityEntry<ItemLaserEntity> ITEM_LASER;

	static {

		/*
		 * 26.3: the entity renderers are registered from DanmakuClient, which is a Dist.CLIENT
		 * subscriber, rather than through Registrate's EntityBuilder#renderer. That method wants the
		 * renderer as a lambda whose body constructs a client-only EntityRenderer; verifying this
		 * class on the dedicated server therefore loaded net.minecraft.client.renderer.entity.EntityRenderer
		 * and aborted mod construction (NeoForgeDevDistCleaner: "Attempted to load class ... which is
		 * not present on the dedicated server"). Registrate's own Dist check only keeps the supplier
		 * from being *called* on a server, not from being loaded.
		 */
		ITEM_DANMAKU = DanmakuAPI.REGISTRATE
				.<ItemBulletEntity>entity("item_danmaku", ItemBulletEntity::new, MobCategory.MISC)
				.properties(e -> e.sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(1 << 16))
				.register();

		ITEM_LASER = DanmakuAPI.REGISTRATE
				.<ItemLaserEntity>entity("item_laser", ItemLaserEntity::new, MobCategory.MISC)
				.properties(e -> e.sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(1 << 16))
				.register();

	}

	public static void register() {
	}

}
