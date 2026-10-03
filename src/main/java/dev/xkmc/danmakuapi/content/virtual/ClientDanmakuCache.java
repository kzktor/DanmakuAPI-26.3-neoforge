package dev.xkmc.danmakuapi.content.virtual;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xkmc.danmakuapi.api.IDanmakuEntity;
import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.fastprojectileapi.render.ClientObjectCache;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderer;
import dev.xkmc.l2serial.util.Wrappers;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.LinkedList;

public class ClientDanmakuCache implements ClientObjectCache {

	private static ClientDanmakuCache CACHE = null;

	@SuppressWarnings("rawtypes")
	private static EntityRenderer[] RENDERERS;

	@SuppressWarnings("rawtypes")
	private static EntityRenderer getRenderer(EntityRenderDispatcher disp, SimplifiedProjectile e) {
		int id = e.getTypeId();
		if (RENDERERS == null || RENDERERS.length <= id) {
			RENDERERS = new EntityRenderer[id + 1];
		}
		if (RENDERERS[id] == null) {
			RENDERERS[id] = disp.getRenderer(e);
		}
		return RENDERERS[id];
	}

	public static ClientDanmakuCache get(Level level) {
		if (CACHE == null || CACHE.level != level) {
			CACHE = new ClientDanmakuCache(level);
		}
		return CACHE;
	}

	private final Level level;
	private final LinkedList<SimplifiedProjectile> all = new LinkedList<>();
	private final Int2ObjectOpenHashMap<SimplifiedProjectile> map = new Int2ObjectOpenHashMap<>(2048);


	public ClientDanmakuCache(Level level) {
		this.level = level;
	}

	public void add(SimplifiedProjectile sp) {
		all.add(sp);
		map.put(sp.getId(), sp);
	}

	public void erase(int id, boolean kill) {
		var e = map.get(id);
		e.markErased(kill);
	}

	public void tick() {
		var itr = all.iterator();
		while (itr.hasNext()) {
			var e = itr.next();
			e.setOldPosAndRot();
			++e.tickCount;
			e.tick();
			if (!e.isValid()) {
				itr.remove();
				map.remove(e.getId());
			}
		}
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void renderAll(SubmitNodeCollector collector, PoseStack pose, CameraRenderState camera, float pTick, boolean hitboxes) {
		Vec3 vec3 = camera.pos;
		double d0 = vec3.x();
		double d1 = vec3.y();
		double d2 = vec3.z();
		EntityRenderDispatcher disp = Minecraft.getInstance().getEntityRenderDispatcher();
		Frustum frustum = camera.cullFrustum;
		for (var e : all) {
			this.maybeRenderEntity(disp, frustum, e, d0, d1, d2, pTick, pose, collector, hitboxes);
		}
		if (hitboxes && Minecraft.getInstance().getCameraEntity() instanceof Player pl && !all.isEmpty() &&
				!camera.isFirstPerson) {
			renderPlayerHitbox(pose, collector, pl, d0, d1, d2, pTick);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <E extends SimplifiedProjectile> void maybeRenderEntity(
			EntityRenderDispatcher disp, Frustum frustum, E e,
			double camx, double camy, double camz, float pTick,
			PoseStack pose, SubmitNodeCollector collector, boolean hitboxes
	) {
		EntityRenderer er = getRenderer(disp, e);
		if (!er.shouldRender(e, frustum, camx, camy, camz, pTick)) return;
		double dx = Mth.lerp(pTick, e.xOld, e.getX());
		double dy = Mth.lerp(pTick, e.yOld, e.getY());
		double dz = Mth.lerp(pTick, e.zOld, e.getZ());
		this.renderEntity(e, er, dx - camx, dy - camy, dz - camz, pTick, pose, collector, hitboxes);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <E extends SimplifiedProjectile> void renderEntity(
			E e, EntityRenderer er,
			double x, double y, double z, float pTick,
			PoseStack pose, SubmitNodeCollector collector, boolean hitboxes
	) {
		if (!(er instanceof ProjectileRenderer<?> pr)) return;
		ProjectileRenderer<E> r = Wrappers.cast(pr);
		Vec3 vec3 = er.getRenderOffset(er.createRenderState(e, pTick));
		double dx = x + vec3.x();
		double dy = y + vec3.y();
		double dz = z + vec3.z();
		pose.pushPose();
		pose.translate(dx, dy, dz);
		r.render(e, pTick, pose);
		if (hitboxes) {
			pose.translate(-vec3.x(), -vec3.y(), -vec3.z());
			renderHitbox(pose, collector, e, pTick);
		}
		pose.popPose();
	}

	/**
	 * 26.3: {@code LevelRenderer#renderLineBox} is gone. Debug boxes are submitted through the
	 * node collector as a VoxelShape outline.
	 */
	public static void renderLineBox(PoseStack pose, SubmitNodeCollector collector, AABB box, int color) {
		collector.submitShapeOutline(pose, Shapes.create(box), RenderTypes.lines(), color, 1.0F, false);
	}

	public static void renderHitbox(PoseStack pose, SubmitNodeCollector collector, Entity e, float pTick) {
		AABB aabb = e.getBoundingBox().move(-e.getX(), -e.getY(), -e.getZ());
		renderLineBox(pose, collector, aabb, 0xFFFFFFFF);
		Vec3 vec3 = e.getViewVector(pTick);
		collector.submitCustomGeometry(pose, RenderTypes.lines(), (entry, vc) -> {
			vc.addVertex(entry, 0.0F, e.getEyeHeight(), 0.0F)
					.setColor(0, 0, 255, 255)
					.setNormal(entry, (float) vec3.x, (float) vec3.y, (float) vec3.z)
					.setLineWidth(1.0F);
			vc.addVertex(entry,
							(float) (vec3.x * 2.0D),
							(float) (e.getEyeHeight() + vec3.y * 2.0D),
							(float) (vec3.z * 2.0D)
					).setColor(0, 0, 255, 255)
					.setNormal(entry, (float) vec3.x, (float) vec3.y, (float) vec3.z)
					.setLineWidth(1.0F);
		});
	}

	public static void renderPlayerHitbox(PoseStack pose, SubmitNodeCollector collector, Player e, double camx, double camy, double camz, float pTick) {
		double dx = Mth.lerp(pTick, e.xOld, e.getX()) - camx - e.getX();
		double dy = Mth.lerp(pTick, e.yOld, e.getY()) - camy - e.getY();
		double dz = Mth.lerp(pTick, e.zOld, e.getZ()) - camz - e.getZ();
		if (e.isInvisible()) {
			AABB base = e.getBoundingBox().move(dx, dy, dz);
			AABB hit = IDanmakuEntity.alterEntityHitBox(e, 0, 0).move(dx, dy, dz);
			renderLineBox(pose, collector, base, 0xFFFFFFFF);
			if (!base.equals(hit)) {
				renderLineBox(pose, collector, hit, 0xFFFF4040);
			}
		}
		AABB graze = IDanmakuEntity.alterEntityHitBox(e, 0, IDanmakuEntity.GRAZE_RANGE).move(dx, dy, dz);
		renderLineBox(pose, collector, graze, 0xFF40FF00);
	}

}
