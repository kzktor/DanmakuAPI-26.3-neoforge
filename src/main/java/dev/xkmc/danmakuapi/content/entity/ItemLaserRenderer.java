package dev.xkmc.danmakuapi.content.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xkmc.danmakuapi.api.GrazeHelper;
import dev.xkmc.danmakuapi.api.IDanmakuEntity;
import dev.xkmc.danmakuapi.content.item.LaserItem;
import dev.xkmc.danmakuapi.init.data.DanmakuConfig;
import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class ItemLaserRenderer<T extends ItemLaserEntity> extends EntityRenderer<T, ProjectileEntityRenderState> implements ProjectileRenderer<T> {

	public ItemLaserRenderer(EntityRendererProvider.Context pContext) {
		super(pContext);
	}

	@Override
	public ProjectileEntityRenderState createRenderState() {
		return new ProjectileEntityRenderState();
	}

	@Override
	public void extractRenderState(T e, ProjectileEntityRenderState state, float partialTicks) {
		super.extractRenderState(e, state, partialTicks);
		state.projectile = e;
		state.partialTick = partialTicks;
	}

	/**
	 * 26.3: this replaces the old {@code EntityRenderer#render(...)} delegation. Without it the entity
	 * render path submits no geometry at all and the bullet is invisible (the batch path alone is
	 * never fed, because the port adds danmaku as real entities instead of sending them through
	 * {@code DanmakuManager#send}).
	 */
	@Override
	public void submit(ProjectileEntityRenderState state, PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
		@SuppressWarnings("unchecked")
		T e = (T) state.projectile;
		if (e != null) {
			render(e, state.partialTick, pose);
		}
	}

	@Override
	protected int getBlockLightLevel(T e, BlockPos pPos) {
		return e.fullBright() ? 15 : super.getBlockLightLevel(e, pPos);
	}

	@Override
	protected AABB getBoundingBoxForCulling(T e, float pTick) {
		var src = e.position().add(0, e.getBbHeight() / 2f, 0);
		return new AABB(src, src.add(e.getForward().scale(e.getLength()))).inflate(e.getBbWidth() / 2f);
	}

	@Override
	public boolean shouldRender(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ, float pTick) {
		return true;
	}

	@Override
	public double fading(SimplifiedProjectile e) {
		if (entityRenderDispatcher.camera.entity() == e.getOwner() ||
				e instanceof IDanmakuEntity dan && dan.isClientFriendly()) {
			return DanmakuConfig.CLIENT.selfDanmakuFading.get();
		}
		return GrazeHelper.globalInvulTime > 0 ? DanmakuConfig.CLIENT.selfDanmakuFading.get() : 1;
	}

	@Override
	public Quaternionf cameraOrientation() {
		return entityRenderDispatcher.camera.rotation();
	}

	@Override
	public Vec3 getRenderOffset(ProjectileEntityRenderState state) {
		return new Vec3(0, state.boundingBoxHeight / 2, 0);
	}

	@Override
	public void render(T e, float pTick, PoseStack pose) {
		if (!(e.getItem().getItem() instanceof LaserItem danmaku)) return;
		if (e.tickCount < 2) return;
		pose.pushPose();
		float scale = e.scale() * e.percentOpen(pTick);
		pose.rotate(Axis.YP.rotationDegrees(-e.getViewYRot(pTick)));
		pose.rotate(Axis.XP.rotationDegrees(e.getViewXRot(pTick) + 90));
		pose.scale(e.getBbWidth() * scale, e.effectiveLength(pTick), e.getBbWidth() * scale);
		danmaku.getTypeForRender().create(this, e, pose, pTick);
		pose.popPose();
	}

	public Identifier getTextureLocation(T pEntity) {
		return TextureAtlas.LOCATION_ITEMS;
	}

}
