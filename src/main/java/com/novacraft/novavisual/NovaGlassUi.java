package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;

/**
 * Shared visionOS-inspired glass primitives for NovaVisual screens.
 * Uses the GUI fill pipeline only; no shaders, mixins or external assets required.
 */
public final class NovaGlassUi {
    private NovaGlassUi() {}

    public static void drawBackdrop(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, 0xA908111E);
        drawGlow(context, width / 2, height / 2, Math.max(width, height) / 2, 0x2439BDF8);
        drawGlow(context, width / 3, height / 3, Math.max(90, width / 5), 0x169B7BFF);
    }

    public static void drawPanel(DrawContext context, int x, int y, int width, int height, int accent) {
        if (width <= 0 || height <= 0) return;
        int radius = Math.min(18, Math.min(width / 2, height / 2));
        roundedFill(context, x - 5, y - 5, width + 10, height + 10, radius + 4,
                (accent & 0x00FFFFFF) | 0x28000000);
        roundedFill(context, x - 2, y - 2, width + 4, height + 4, radius + 2, 0x4A8DDCFF);
        roundedFill(context, x, y, width, height, radius, 0xC91B2B3D);
        roundedFill(context, x + 1, y + 1, width - 2, height - 2, Math.max(1, radius - 1), 0xA92A3D50);
        roundedFill(context, x + 2, y + 2, width - 4, Math.max(2, height / 3), Math.max(1, radius - 2), 0x182FFFFF);
        context.fill(x + radius, y + 1, x + width - radius, y + 2, 0x88FFFFFF);
        context.fill(x + 1, y + radius, x + 2, y + height - radius, 0x557DCFF8);
    }

    public static void drawCard(DrawContext context, int x, int y, int width, int height, boolean selected) {
        int radius = Math.min(12, Math.min(width / 2, height / 2));
        roundedFill(context, x, y, width, height, radius, selected ? 0xA94D7897 : 0x795A7188);
        roundedFill(context, x + 1, y + 1, width - 2, height - 2,
                Math.max(1, radius - 1), selected ? 0x754A91B5 : 0x60304356);
        context.fill(x + radius, y + 1, x + width - radius, y + 2,
                selected ? 0x99E6F8FF : 0x55FFFFFF);
    }

    public static void drawDivider(DrawContext context, int x, int y, int width, int color) {
        context.fill(x, y, x + width, y + 1, color);
    }

    private static void drawGlow(DrawContext context, int centerX, int centerY, int radius, int color) {
        int safeRadius = Math.max(1, radius);
        for (int offset = -safeRadius; offset <= safeRadius; offset += 8) {
            double ratio = offset / (double) safeRadius;
            int halfWidth = (int) (safeRadius * Math.sqrt(Math.max(0.0, 1.0 - ratio * ratio)));
            int alpha = (color >>> 24) & 255;
            int fade = (int) (alpha * (1.0 - Math.abs(ratio)) * 0.16);
            context.fill(centerX - halfWidth, centerY + offset,
                    centerX + halfWidth, centerY + offset + 8, (fade << 24) | (color & 0x00FFFFFF));
        }
    }

    private static void roundedFill(DrawContext context, int x, int y, int width, int height,
                                    int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(width / 2, height / 2)));
        for (int row = 0; row < height; row++) {
            int inset = 0;
            if (r > 0 && row < r) {
                double dy = r - row - 0.5;
                inset = r - (int) Math.floor(Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            } else if (r > 0 && row >= height - r) {
                double dy = row - (height - r) + 0.5;
                inset = r - (int) Math.floor(Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            }
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }
}
