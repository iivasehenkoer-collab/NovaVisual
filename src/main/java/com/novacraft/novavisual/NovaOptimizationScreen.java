package com.novacraft.novavisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaOptimizationScreen extends Screen {
    private static final String[] PRESETS = {"Low-end · 6 chunks", "Balanced · 10 chunks", "Quality · 16 chunks"};
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;

    public NovaOptimizationScreen() {
        super(Text.literal("Nova Optimization"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 112;
        int y = height / 2 - 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Preset: " + PRESETS[NovaVisualSettings.optimizationPreset]), button -> {
            NovaVisualSettings.optimizationPreset = (NovaVisualSettings.optimizationPreset + 1) % PRESETS.length;
            button.setMessage(Text.literal("Preset: " + PRESETS[NovaVisualSettings.optimizationPreset]));
        }).dimensions(x, y, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply preset"), button -> applyPreset())
                .dimensions(x, y + 26, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Restore recommended defaults"), button -> restoreDefaults())
                .dimensions(x, y + 52, 224, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(x, y + 82, 224, 20).build());
    }

    private void applyPreset() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;
        int view = switch (NovaVisualSettings.optimizationPreset) {
            case 0 -> 6;
            case 2 -> 16;
            default -> 10;
        };
        int simulation = switch (NovaVisualSettings.optimizationPreset) {
            case 0 -> 4;
            case 2 -> 10;
            default -> 6;
        };
        client.options.getViewDistance().setValue(view);
        client.options.getSimulationDistance().setValue(simulation);
        NovaVisualSettings.optimizationEnabled = true;
    }

    private void restoreDefaults() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;
        client.options.getViewDistance().setValue(12);
        client.options.getSimulationDistance().setValue(8);
        NovaVisualSettings.optimizationEnabled = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 150;
        int top = height / 2 - 112;
        int right = width / 2 + 150;
        int bottom = height / 2 + 100;
        context.fill(left - 4, top - 4, right + 4, bottom + 4, 0x5039BDF8);
        context.fill(left, top, right, bottom, 0xE8141D2B);
        context.fill(left + 2, top + 2, right - 2, bottom - 2, 0x7A26384D);
        context.fill(left + 1, top + 1, right - 1, top + 2, 0x88FFFFFF);
        context.drawText(textRenderer, "N O V A C R A F T   /   PERFORMANCE", left + 16, top + 14, MUTED, false);
        context.drawText(textRenderer, "Optimization Center", left + 16, top + 31, WHITE, false);
        context.drawText(textRenderer, "Choose a preset to tune render and simulation distance.", left + 16, top + 50, 0xFFB6CBE0, false);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            context.drawText(textRenderer, "Current render distance: " + mc.options.getViewDistance().getValue() + " chunks",
                    left + 16, top + 72, 0xFF83D7FF, false);
            context.drawText(textRenderer, "Current simulation distance: " + mc.options.getSimulationDistance().getValue() + " chunks",
                    left + 16, top + 86, 0xFF83D7FF, false);
        }
        context.drawText(textRenderer, "Presets change vanilla settings only; effects vary by PC and world.",
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
