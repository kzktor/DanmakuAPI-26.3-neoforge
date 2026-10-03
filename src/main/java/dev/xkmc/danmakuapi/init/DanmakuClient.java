package dev.xkmc.danmakuapi.init;

import dev.xkmc.danmakuapi.content.entity.ItemBulletEntity;
import dev.xkmc.danmakuapi.content.entity.ItemBulletRenderer;
import dev.xkmc.danmakuapi.content.entity.ItemLaserEntity;
import dev.xkmc.danmakuapi.content.entity.ItemLaserRenderer;
import dev.xkmc.danmakuapi.content.particle.DanmakuPoofParticle;
import dev.xkmc.danmakuapi.content.item.DanmakuItemDeco;
import dev.xkmc.danmakuapi.content.item.SpellItem;
import dev.xkmc.danmakuapi.init.registrate.DanmakuEntities;
import dev.xkmc.danmakuapi.init.registrate.DanmakuItems;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderHelper;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = DanmakuAPI.MODID)
public class DanmakuClient {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			for (var e : DanmakuItems.Bullet.values())
				for (var d : DyeColor.values())
					e.get(d).get().getTypeForRender();
			for (var e : DanmakuItems.Laser.values())
				for (var d : DyeColor.values())
					e.get(d).get().getTypeForRender();
			ProjectileRenderHelper.setup();
		});
	}

	/**
	 * Entity renderers live here rather than in Registrate's EntityBuilder#renderer: that call takes
	 * a lambda whose body constructs a client-only renderer, and verifying the entity registration
	 * class on the dedicated server loaded net.minecraft.client.renderer.entity.EntityRenderer,
	 * aborting mod construction. This class is only loaded on the client, so it is safe.
	 */
	@SubscribeEvent
	public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(DanmakuEntities.ITEM_DANMAKU.get(),
				ctx -> new ItemBulletRenderer<ItemBulletEntity>(ctx));
		event.registerEntityRenderer(DanmakuEntities.ITEM_LASER.get(),
				ctx -> new ItemLaserRenderer<ItemLaserEntity>(ctx));
	}

	@SubscribeEvent
	public static void registerItemDeco(RegisterItemDecorationsEvent event) {
		var deco = new DanmakuItemDeco();
		for (var col : DyeColor.values()) {
			for (var e : DanmakuItems.Bullet.values()) {
				event.register(e.get(col), deco);
			}
			for (var e : DanmakuItems.Laser.values()) {
				event.register(e.get(col), deco);
			}
		}
		event.register(DanmakuItems.CUSTOM_SPELL_RING.get(), deco);
		event.register(DanmakuItems.CUSTOM_SPELL_HOMING.get(), deco);
		for (var e : SpellItem.LIST) {
			event.register(e, deco);
		}
	}

	@SubscribeEvent
	public static void registerParticle(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(DanmakuItems.POOF.get(), DanmakuPoofParticle.Provider::new);
	}

}
