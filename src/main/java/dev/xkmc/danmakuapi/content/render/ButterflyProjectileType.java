package dev.xkmc.danmakuapi.content.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xkmc.fastprojectileapi.entity.SimplifiedProjectile;
import dev.xkmc.fastprojectileapi.render.ProjectileRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Consumer;

public record ButterflyProjectileType(Identifier overlay, DisplayType display, int period)
		implements RenderableDanmakuType<ButterflyProjectileType, ButterflyProjectileType.Ins> {

	@Override
	public void start(SubmitNodeCollector collector, PoseStack pose, List<Ins> list) {
		collector.submitCustomGeometry(pose, DanmakuRenderStates.danmaku(overlay, display()), (entry, vc) -> {
			BulkDataWriter writer = new BulkDataWriter(vc, list.size());
			for (var e : list) {
				e.tex(writer);
			}
			writer.flush();
		});
	}

	@Override
	public void create(Consumer<Ins> holder, ProjectileRenderer<?> r, SimplifiedProjectile e, PoseStack pose, float pTick) {
		pose.rotate(Axis.YP.rotationDegrees(-Mth.lerp(pTick, e.yRotO, e.getYRot())));
		pose.rotate(Axis.XP.rotationDegrees(Mth.lerp(pTick, e.xRotO, e.getXRot())));
		float time = Math.abs((e.tickCount + pTick) / period % 1 * 4 - 2) - 1;
		float angle = 60f;
		int col = DanmakuRenderStates.fading(display, -1, r, e);
		{
			pose.pushPose();
			pose.rotate(Axis.ZP.rotationDegrees(time * angle));
			PoseStack.Pose mat = pose.last();
			Matrix4f m4 = new Matrix4f(mat.pose());
			holder.accept(new Ins(m4, col, false));
			pose.popPose();
		}
		{
			pose.pushPose();
			pose.rotate(Axis.ZP.rotationDegrees(time * -angle));
			PoseStack.Pose mat = pose.last();
			Matrix4f m4 = new Matrix4f(mat.pose());
			holder.accept(new Ins(m4, col, true));
			pose.popPose();
		}
	}

	public record Ins(Matrix4f m4, int color, boolean right) {

		public void tex(BulkDataWriter vc) {
			float x0 = 0;
			float x1 = .5f;
			if (right) {
				x0 += 0.5f;
				x1 += 0.5f;
			}
			vertex(vc, m4, x1, 1, x1, 0, color);
			vertex(vc, m4, x1, 0, x1, 1, color);
			vertex(vc, m4, x0, 0, x0, 1, color);
			vertex(vc, m4, x0, 1, x0, 0, color);
		}

		private static void vertex(BulkDataWriter vc, Matrix4f m4, float x, float y, float u, float v, int color) {
			vc.addVertex(m4, x - 0.5F, 0.0F, y - 0.5F, u, v, color);
		}

	}
}
