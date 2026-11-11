package com.novacraft.novavisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaOptimizationScreen extends Screen {
    private static final String[] PRESETS = {
            "Low-end · 6 chunks", "Balanced · 8 chunks", "Quality · 10 chunks"
    };
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;

    public NovaOptimizationScreen() {
        super(Text.literal("Nova Optimization"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 112;
        int y = height / 2 - 2;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Preset: " + PRESETS[NovaVisualSettings.optimizationPreset]), button -> {
                    NovaVisualSettings.optimizationPreset =
                            (NovaVisualSettings.optimizationPreset + 1) % PRESETS.length;
                    button.setMessage(Text.literal("Preset: "
                            + PRESETS[NovaVisualSettings.optimizationPreset]));
                    NovaVisualSettings.save();
                }).dimensions(x, y, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply safe preset"), button -> applyPreset())
                .dimensions(x, y + 26, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Safe Mode / Restore defaults"), button -> restoreDefaults())
                .dimensions(x, y + 52, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(x, y + 82, 224, 20).build());
    }

    private void applyPreset() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;

        int view = switch (NovaVisualSettings.optimizationPreset) {
            case 0 -> 6;
            case 2 -> 10;
            default -> 8;
        };
        int simulation = switch (NovaVisualSettings.optimizationPreset) {
            case 0 -> 4;
            case 2 -> 6;
            default -> 5;
        };

        client.options.getViewDistance().setValue(view);
        client.options.getSimulationDistance().setValue(simulation);
        NovaVisualSettings.optimizationEnabled = true;
        NovaVisualSettings.save();
    }

    private void restoreDefaults() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;

        client.options.getViewDistance().setValue(12);
        client.options.getSimulationDistance().setValue(8);
        NovaVisualSettings.optimizationEnabled = false;
        NovaVisualSettings.save();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Intentionally empty: the custom opaque background is drawn in render().
        // This prevents vanilla's per-frame blur pipeline from being requested.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw an opaque custom backdrop. Screen.renderBackground() can request
        // the vanilla blur pipeline, which may already have run earlier this frame.
        NovaGlassUi.drawBackdrop(context, width, height);

        int left = width / 2 - 150;
        int top = height / 2 - 112;
        int right = width / 2 + 150;
        int bottom = height / 2 + 100;
        NovaGlassUi.drawPanel(context, left, top, right - left, bottom - top,
                NovaVisualSettings.accent);
        context.drawText(textRenderer, "N O V A C R A F T   /   PERFORMANCE",
                left + 16, top + 14, MUTED, false);
        context.drawText(textRenderer, "Optimization Center", left + 16, top + 31, WHITE, false);
        context.drawText(textRenderer, "Conservative presets for stable gameplay.",
                left + 16, top + 50, 0xFFB6CBE0, false);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            context.drawText(textRenderer, "Current render distance: "
                            + mc.options.getViewDistance().getValue() + " chunks",
                    left + 16, top + 72, 0xFF83D7FF, false);
            context.drawText(textRenderer, "Current simulation distance: "
                            + mc.options.getSimulationDistance().getValue() + " chunks",
                    left + 16, top + 86, 0xFF83D7FF, false);
        }
        context.drawText(textRenderer, "Safe Mode restores 12 render / 8 simulation chunks.",
                left + 16, top + 112, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
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
