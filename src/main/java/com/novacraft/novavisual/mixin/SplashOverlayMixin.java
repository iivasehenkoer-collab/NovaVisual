package com.novacraft.novavisual.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NovaVisual branded resource-loading overlay.
 * Draws at TAIL so vanilla SplashOverlay still owns reload progress and lifecycle.
 */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void novavisual$drawLoadingScreen(DrawContext context, int mouseX, int mouseY,
                                               float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        long time = System.currentTimeMillis();

        // Opaque branded layer hides the vanilla splash without replacing its lifecycle.
        context.fill(0, 0, width, height, 0xFF080F1C);
        context.fill(0, height / 2 - 74, width, height / 2 + 74, 0xFF0B1728);
        context.fill(0, height / 2 - 74, width, height / 2 - 72, 0xFF183B58);

        int centerX = width / 2;
        int centerY = height / 2;
        int glow = (int) (Math.sin(time / 420.0) * 18.0 + 42.0);
        context.fill(centerX - 34, centerY - 58, centerX + 34, centerY + 10,
                (Math.min(255, glow + 70) << 24) | 0x0039BDF8);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("N"), centerX, centerY - 49, 0xFF72D8FF);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("N O V A V I S U A L"), centerX, centerY + 25, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("A NEW WAY TO PLAY"), centerX, centerY + 43, 0xFF8FAFCB);

        int barWidth = Math.min(220, width - 48);
        int barLeft = centerX - barWidth / 2;
        int barTop = centerY + 72;
        context.fill(barLeft, barTop, barLeft + barWidth, barTop + 3, 0xFF20364D);
        int segmentWidth = Math.max(28, barWidth / 4);
        int travel = Math.max(1, barWidth - segmentWidth);
        int offset = (int) ((time / 8L) % (travel * 2L));
        if (offset > travel) offset = travel * 2 - offset;
        context.fill(barLeft + offset, barTop, barLeft + offset + segmentWidth,
                barTop + 3, 0xFF39BDF8);

        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("Preparing Minecraft..."), centerX, barTop + 14, 0xFFB6CBE0);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("NOVACRAFT  /  SPATIAL EDITION"), centerX, height - 24, 0xFF526D86);
    }
}
