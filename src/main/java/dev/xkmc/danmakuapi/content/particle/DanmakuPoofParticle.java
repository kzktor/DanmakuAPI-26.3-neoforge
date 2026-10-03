package dev.xkmc.danmakuapi.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

// 26.3: @OnlyIn no longer strips members at runtime, so it only produced a startup warning
// (NeoForge OnlyInWarningsHandler). The nested Provider is client-only and is only referenced
// from the client registration path, so the annotation is simply gone.
public class DanmakuPoofParticle extends DustParticleBase<DanmakuPoofParticleOptions> {

	protected DanmakuPoofParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, DanmakuPoofParticleOptions options, SpriteSet sprites) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
		float f = this.random.nextFloat() * 0.4F + 0.6F;
		this.rCol = this.randomizeColor(options.getColor().x(), f);
		this.gCol = this.randomizeColor(options.getColor().y(), f);
		this.bCol = this.randomizeColor(options.getColor().z(), f);
	}

	@Override
	protected int getLightCoords(float pTick) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	public static class Provider implements ParticleProvider<DanmakuPoofParticleOptions> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		public Particle createParticle(DanmakuPoofParticleOptions type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new DanmakuPoofParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type, this.sprites);
		}
	}

}