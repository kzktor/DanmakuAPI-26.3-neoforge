package dev.xkmc.danmakuapi.content.render;

import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderer;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/**
 * 26.3: the old {@code RenderStateShard}/{@code CompositeState}/{@code ShaderStateShard} render type
 * construction is gone. Danmaku render types are {@code POSITION_TEX_COLOR} textured quads and are now
 * built from the shared {@link ProjectileRenderTypes} pipelines (culling + blend mode only).
 */
public abstract class DanmakuRenderStates {

	private static ProjectileRenderTypes.Blend blend(DisplayType type) {
		return switch (type) {
			case SOLID -> ProjectileRenderTypes.Blend.SOLID;
			case TRANSPARENT -> ProjectileRenderTypes.Blend.TRANSLUCENT;
			case ADDITIVE -> ProjectileRenderTypes.Blend.ADDITIVE;
		};
	}

	public static RenderType danmaku(Identifier rl, DisplayType type) {
		if (type == DisplayType.SOLID) type = DisplayType.TRANSPARENT;
		return ProjectileRenderTypes.create("danmaku", rl, false, blend(type));
	}

	public static RenderType laser(Identifier rl, DisplayType type) {
		return ProjectileRenderTypes.create("laser", rl, true, blend(type));
	}

	/**
	 * Render state for danmaku drawn as a baked item model. Such a model samples the item
	 * atlas, so that is the bound texture.
	 * <p>
	 * It keeps {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#POSITION_TEX_COLOR} rather
	 * than the {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#ENTITY} vanilla item
	 * rendering uses: danmaku are always full bright and are shaded per quad into the vertex
	 * color, so the lightmap, overlay (no glint) and normal attributes can all be dropped.
	 */
	public static RenderType itemModel(DisplayType type) {
		if (type == DisplayType.SOLID) type = DisplayType.TRANSPARENT;
		return ProjectileRenderTypes.create("item_model", TextureAtlas.LOCATION_ITEMS, false, blend(type));
	}

	public static int fading(DisplayType display, int col, ProjectileRenderer<?> r, SimplifiedProjectile e) {
		double perc = r.fading(e);
		if (perc == 0) return col;
		int alpha = (int) ((col >>> 24) * perc);
		if (display == DisplayType.ADDITIVE) {
			return 0xff000000 | alpha << 16 | alpha << 8 | alpha;
		}
		return (alpha << 24) | col & 0xffffff;
	}

}
