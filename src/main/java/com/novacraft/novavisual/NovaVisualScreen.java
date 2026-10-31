package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaVisualScreen extends Screen {
    private static final int PANEL = 0xE8141D2B;
    private static final int CYAN = 0xFF39BDF8;
    private static final int MUTED = 0xFF9CB2C8;

    public NovaVisualScreen() {
        super(Text.literal("NovaVisual"));
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = height / 2 - 82;
        int buttonWidth = 220;
        int x = center - buttonWidth / 2;

        addDrawableChild(toggleButton(x, top + 38, "Animated menu", () -> NovaVisualSettings.animatedMenu,
                value -> NovaVisualSettings.animatedMenu = value));
        addDrawableChild(toggleButton(x, top + 64, "Compact HUD", () -> NovaVisualSettings.compactHud,
                value -> NovaVisualSettings.compactHud = value));
        addDrawableChild(toggleButton(x, top + 90, "Damage flash", () -> NovaVisualSettings.damageFlash,
                value -> NovaVisualSettings.damageFlash = value));
        addDrawableChild(toggleButton(x, top + 116, "Ambient particles", () -> NovaVisualSettings.particles,
                value -> NovaVisualSettings.particles = value));
        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: Nova Blue"), button -> {
            NovaVisualSettings.accent = NovaVisualSettings.accent == CYAN ? 0xFF9B7BFF : CYAN;
            button.setMessage(Text.literal(NovaVisualSettings.accent == CYAN ? "Accent: Nova Blue" : "Accent: Nova Violet"));
        }).dimensions(x, top + 146, buttonWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(x, top + 174, buttonWidth, 20).build());
    }

    private ButtonWidget toggleButton(int x, int y, String label,
                                      java.util.function.BooleanSupplier getter,
                                      java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), b -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            b.setMessage(toggleText(label, next));
        }).dimensions(x, y, 220, 20).build();
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + "  ").append(Text.literal(enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 132;
        int top = height / 2 - 92;
        int right = width / 2 + 132;
        int bottom = height / 2 + 108;

        context.fill(left, top, right, bottom, PANEL);
        context.fill(left, top, right, top + 3, NovaVisualSettings.accent);
        context.fill(left + 12, top + 30, right - 12, top + 31, 0x5539BDF8);
        context.drawText(textRenderer, "NOVACRAFT / VISUAL ENGINE", left + 16, top + 11, MUTED, false);
        context.drawText(textRenderer, "NovaVisual", left + 16, top + 43, 0xFFFFFFFF, false);
        context.drawText(textRenderer, "CUSTOMIZE YOUR GAME", left + 16, bottom - 15, CYAN, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
