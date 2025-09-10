package net.thedragonskull.rodsawaken.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;

public class AwakenedEndRodGlitterParticles extends SimpleAnimatedParticle {

    AwakenedEndRodGlitterParticles(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites, int potionColor) {
        super(pLevel, pX, pY, pZ, pSprites, 0.0125F);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.quadSize *= 0.75F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setSpriteFromAge(pSprites);

        float r = ((potionColor >> 16) & 0xFF) / 255f;
        float g = ((potionColor >> 8) & 0xFF) / 255f;
        float b = (potionColor & 0xFF) / 255f;

        float[][] palette = {
                adjustRGB(r, g, b, 1.10f,  0.03f), // un pelín más brillante + leve shift
                adjustRGB(r, g, b, 0.92f, -0.03f), // un pelín más oscuro  + leve shift
                new float[]{r, g, b},              // color puro
                adjustRGB(r, g, b, 1.20f,  0.06f)  // un poco más vibrante
        };

        float[] selected = palette[random.nextInt(palette.length)];
        this.setColor(selected[0], selected[1], selected[2]);

        this.setFadeColor(potionColor);
    }

    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    /**
     * brightness: multiplica (1.0 = igual, >1 más brillante, <1 más oscuro)
     * shift: pequeño desplazamiento aditivo para romper la monotonía sin alejarse del color base
     */
    private static float[] adjustRGB(float r, float g, float b, float brightness, float shift) {
        r = clamp(r * brightness + shift);
        g = clamp(g * brightness + shift);
        b = clamp(b * brightness + shift);
        return new float[]{r, g, b};
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {

            BlockPos pos = BlockPos.containing(x, y, z);

            int baseColor = 0;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AwakenedEndRodBE rod) {
                baseColor = rod.getCombinedPotionColor();
            }

            if (baseColor == 0) {
                return null;
            }

            return new AwakenedEndRodGlitterParticles(level, x, y, z, dx, dy, dz, this.sprites, baseColor);
        }
    }
}
