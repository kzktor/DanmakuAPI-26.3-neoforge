package dev.xkmc.danmakuapi.content.entity;

import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * 26.3: {@code EntityRenderState} does not carry the entity, but the danmaku renderers need it —
 * the draw call lives in {@code ProjectileRenderer#render(entity, pTick, pose)}, which upstream
 * invoked from the old {@code EntityRenderer#render(...)}. Without stashing the entity here the
 * entity render path has nothing to draw, which makes every bullet invisible.
 */
public class ProjectileEntityRenderState extends EntityRenderState {

	public @Nullable SimplifiedProjectile projectile;
	public float partialTick;

}
