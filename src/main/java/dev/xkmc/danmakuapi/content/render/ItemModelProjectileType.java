package dev.xkmc.danmakuapi.content.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Consumer;

/**
 * Directional danmaku rendering that draws the danmaku item's own baked model instead of a
 * hand-written quad, so the shape comes from the item model and can be swapped or made
 * three-dimensional without touching this class.
 * <p>
 * The model is laid flat in the glide plane and oriented by projectile yaw/pitch like
 * {@link FlatProjectileType}, so its texture top edge points along the flight direction and
 * elongated sprites always point where they fly, Touhou-style. An optional slow roll around
 * the flight axis banks the model out of the glide plane and makes paper-like danmaku flutter.
 * <p>
 * 26.3: raw baked models are no longer exposed (there is no {@code BakedModel#getQuads} and
 * {@code ItemQuads} is private to the render state), and manual vertex writing was replaced by
 * the item render pipeline. The model is therefore resolved into an {@link ItemStackRenderState}
 * and submitted through it.
 * // TODO(26.3): ItemStackRenderState#submit cannot carry the per-instance fading alpha nor the
 * // danmaku DisplayType blend mode; both were vertex-level in 1.21.1. Visual fading of this one
 * // render variant is lost until the platform exposes tint/alpha on submitItem.
 *
 * @param item the danmaku item whose model is drawn
 * @param spin ticks per full roll around the flight axis; 0 disables rolling
 */
public record ItemModelProjectileType(Item item, DisplayType display, double spin)
		implements RenderableDanmakuType<ItemModelProjectileType, ItemModelProjectileType.Ins> {

	/**
	 * Item models are authored in a [0,1] cube, so this centres the cube on the projectile and
	 * tips it over 90 degrees to drop the art into the glide plane.
	 */
	private static final Matrix4f FLAT = new Matrix4f().translate(-0.5F, -0.5F, -0.5F).rotateX((float) (Math.PI / 2));

	@Override
	public void start(SubmitNodeCollector collector, PoseStack pose, List<Ins> list) {
		for (var ins : list) {
			pose.pushPose();
			pose.mulPose(ins.m4());
			ins.state.submit(pose, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
	}

	@Override
	public void create(Consumer<Ins> holder, ProjectileRenderer<?> r, SimplifiedProjectile e, PoseStack pose, float pTick) {
		pose.rotate(Axis.YP.rotationDegrees(-Mth.lerp(pTick, e.yRotO, e.getYRot())));
		pose.rotate(Axis.XP.rotationDegrees(Mth.lerp(pTick, e.xRotO, e.getXRot())));
		if (spin > 0) {
			pose.rotate(Axis.ZP.rotationDegrees((e.tickCount + pTick) * 360f / (float) spin));
		}
		pose.mulPose(FLAT);
		ItemStackRenderState state = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForNonLiving(state,
				new ItemStack(item), ItemDisplayContext.NONE, e);
		holder.accept(new Ins(new Matrix4f(pose.last().pose()), state));
	}

	public record Ins(Matrix4f m4, ItemStackRenderState state) {}

}
