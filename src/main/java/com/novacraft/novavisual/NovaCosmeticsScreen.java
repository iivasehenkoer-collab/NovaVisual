package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaCosmeticsScreen extends Screen {
    private static final String[] STYLES = {"RGB Wings", "Angel Wings", "Demon Wings", "Dragon Wings", "Neon Wings"};
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;
    private final long openedAt = System.currentTimeMillis();

    public NovaCosmeticsScreen() {
        super(Text.literal("NovaVisual Cosmetics"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 104;
        int y = height / 2 + 42;
        addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + STYLES[NovaVisualSettings.wingStyle]), button -> {
            NovaVisualSettings.wingStyle = (NovaVisualSettings.wingStyle + 1) % STYLES.length;
            button.setMessage(Text.literal("Style: " + STYLES[NovaVisualSettings.wingStyle]));
        }).dimensions(x, y, 208, 20).build());
        addDrawableChild(toggleButton(x, y + 24, "3D wings", () -> NovaVisualSettings.cosmeticsEnabled,
                value -> NovaVisualSettings.cosmeticsEnabled = value));
        addDrawableChild(toggleButton(x, y + 48, "RGB animation", () -> NovaVisualSettings.rgbWings,
                value -> NovaVisualSettings.rgbWings = value));
        addDrawableChild(toggleButton(x, y + 72, "RGB aura", () -> NovaVisualSettings.rgbAura,
                value -> NovaVisualSettings.rgbAura = value));
        addDrawableChild(toggleButton(x, y + 96, "Particle trail", () -> NovaVisualSettings.particleTrail,
                value -> NovaVisualSettings.particleTrail = value));
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(x, y + 124, 208, 20).build());
    }

    private ButtonWidget toggleButton(int x, int y, String label,
                                      java.util.function.BooleanSupplier getter,
                                      java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            button.setMessage(toggleText(label, next));
        }).dimensions(x, y, 208, 20).build();
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + "  ·  " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        NovaGlassUi.drawBackdrop(context, width, height);
        int left = width / 2 - 142;
        int top = height / 2 - 132;
        int right = width / 2 + 142;
        int bottom = height / 2 + 200;
        NovaGlassUi.drawPanel(context, left, top, right - left, bottom - top,
                NovaVisualSettings.accent);
        context.drawText(textRenderer, "N O V A C R A F T   /   C O S M E T I C S", left + 16, top + 14, MUTED, false);
        context.drawText(textRenderer, "Skin Studio", left + 16, top + 31, WHITE, false);
        context.drawText(textRenderer, "Preview  ·  " + STYLES[NovaVisualSettings.wingStyle], left + 16, top + 48, 0xFF83D7FF, false);

        int color = NovaVisualSettings.rgbWings ? rainbowColor() : 0xFF6BCBFF;
        drawWingPreview(context, width / 2, top + 118, color);
        context.drawText(textRenderer, "Live 3D wings · attached to player", width / 2 - 44, top + 192, MUTED, false);
        context.drawText(textRenderer, "Client-side cosmetic · visible with NovaVisual installed",
                width / 2 - 126, top + 207, 0xFFB6CBE0, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawWingPreview(DrawContext context, int centerX, int centerY, int color) {
        int accent = NovaVisualSettings.wingStyle == 1 ? 0xFFEAF6FF
                : NovaVisualSettings.wingStyle == 2 ? 0xFFE34B65
                : NovaVisualSettings.wingStyle == 3 ? 0xFF8C6BFF : color;
        int dark = NovaVisualSettings.wingStyle == 2 ? 0xFF501B38 : 0xFF234A70;
        if (NovaVisualSettings.rgbAura) {
            int aura = (color & 0x00FFFFFF) | 0x55330000;
            context.fill(centerX - 86, centerY - 62, centerX + 86, centerY + 65, aura);
        }
        context.fill(centerX - 10, centerY - 34, centerX + 10, centerY + 42, 0xFF26384D);
        for (int i = 0; i < 5; i++) {
            int offset = i * 13;
            int feather = Math.max(10, 56 - i * 8);
            context.fill(centerX - 18 - offset - feather, centerY - 30 + offset / 2,
                    centerX - 13 - offset, centerY - 22 + offset / 2 + feather, dark);
            context.fill(centerX + 13 + offset, centerY - 30 + offset / 2,
                    centerX + 18 + offset + feather, centerY - 22 + offset / 2 + feather, dark);
            context.fill(centerX - 17 - offset - feather, centerY - 28 + offset / 2,
                    centerX - 14 - offset, centerY - 24 + offset / 2 + feather, accent);
            context.fill(centerX + 14 + offset, centerY - 28 + offset / 2,
                    centerX + 17 + offset + feather, centerY - 24 + offset / 2 + feather, accent);
        }
        context.fill(centerX - 7, centerY - 30, centerX + 7, centerY + 38, 0xFFB7CBE0);
        context.fill(centerX - 5, centerY - 28, centerX + 5, centerY + 36, 0xFF26384D);
    }

    private int rainbowColor() {
        float hue = ((System.currentTimeMillis() - openedAt) % 6000L) / 6000f;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.78f, 1f) & 0x00FFFFFF);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new NovaVisualScreen());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
