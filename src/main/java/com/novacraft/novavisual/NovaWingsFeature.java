package com.novacraft.novavisual;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.util.Map;

/**
 * Client-only cosmetic wings attached to the normal player render pipeline.
 * The mesh is built from real 3D cuboids and animated every frame.
 */
public final class NovaWingsFeature extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier WING_TEXTURE =
            Identifier.of("minecraft", "textures/entity/elytra.png");
    private static final RenderLayer WING_LAYER = RenderLayer.getEntityCutoutNoCull(WING_TEXTURE);

    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public NovaWingsFeature(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
        leftWing = createWing(false);
        rightWing = createWing(true);
        leftWing.setOrigin(2.0F, 0.0F, 2.0F);
        rightWing.setOrigin(-2.0F, 0.0F, 2.0F);
    }

    private static ModelPart createWing(boolean mirrored) {
        ModelPartBuilder mesh = ModelPartBuilder.create().uv(0, 0);
        if (!mirrored) {
            mesh.cuboid(0.0F, -2.0F, 0.0F, 12.0F, 18.0F, 2.0F)
                .cuboid(2.0F, 10.0F, -0.5F, 10.0F, 8.0F, 2.0F)
                .cuboid(4.0F, 16.0F, -1.0F, 8.0F, 7.0F, 2.0F)
                .cuboid(7.0F, 21.0F, -1.0F, 5.0F, 6.0F, 2.0F);
        } else {
            mesh.mirrored()
                .cuboid(-12.0F, -2.0F, 0.0F, 12.0F, 18.0F, 2.0F)
                .cuboid(-12.0F, 10.0F, -0.5F, 10.0F, 8.0F, 2.0F)
                .cuboid(-12.0F, 16.0F, -1.0F, 8.0F, 7.0F, 2.0F)
                .cuboid(-12.0F, 21.0F, -1.0F, 5.0F, 6.0F, 2.0F);
        }
        return new ModelPart(mesh.build(), Map.of());
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (!NovaVisualSettings.cosmeticsEnabled) return;

        float time = (System.currentTimeMillis() % 5000L) / 1000.0F;
        float flap = (float) Math.sin(time * 3.2F) * 0.22F;
        float idle = (float) Math.sin(time * 1.4F) * 0.035F;

        leftWing.pitch = idle;
        rightWing.pitch = idle;
        leftWing.roll = -0.18F - flap;
        rightWing.roll = 0.18F + flap;

        int color = wingColor();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 0.125F);
        queue.submitModelPart(leftWing, matrices, WING_LAYER, light, OverlayTexture.DEFAULT_UV,
                null, false, false, color, null, 0);
        queue.submitModelPart(rightWing, matrices, WING_LAYER, light, OverlayTexture.DEFAULT_UV,
                null, false, false, color, null, 0);
        matrices.pop();
    }

    private static int wingColor() {
        if (NovaVisualSettings.rgbWings) {
            float hue = (System.currentTimeMillis() % 6000L) / 6000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.82F, 1.0F) & 0x00FFFFFF);
        }
        return switch (NovaVisualSettings.wingStyle) {
            case 1 -> 0xFFF0F6FF;
            case 2 -> 0xFFE34B65;
            case 3 -> 0xFF9875FF;
            case 4 -> 0xFF42D9FF;
            default -> 0xFF6BCBFF;
        };
    }
}
