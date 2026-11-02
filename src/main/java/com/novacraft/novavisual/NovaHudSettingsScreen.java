package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaHudSettingsScreen extends Screen {
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;

    public NovaHudSettingsScreen() {
        super(Text.literal("NovaVisual HUD Layout"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 110;
        int y = height / 2 - 38;

        addDrawableChild(toggleButton(x, y, "Coordinates",
                () -> NovaVisualSettings.hudCoordinates,
                value -> NovaVisualSettings.hudCoordinates = value));
        addDrawableChild(toggleButton(x, y + 28, "Health / Armor",
                () -> NovaVisualSettings.hudVitals,
                value -> NovaVisualSettings.hudVitals = value));
        addDrawableChild(toggleButton(x, y + 56, "Dimension / FPS",
                () -> NovaVisualSettings.hudStatus,
                value -> NovaVisualSettings.hudStatus = value));

        addDrawableChild(ButtonWidget.builder(Text.literal("Position: " + anchorName()), button -> {
            NovaVisualSettings.hudAnchor = (NovaVisualSettings.hudAnchor + 1) % 4;
            button.setMessage(Text.literal("Position: " + anchorName()));
        }).dimensions(x, y + 84, 220, 22).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(x, y + 114, 220, 22).build());
    }

    private ButtonWidget toggleButton(int x, int y, String label,
                                     java.util.function.BooleanSupplier getter,
                                     java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            button.setMessage(toggleText(label, next));
        }).dimensions(x, y, 220, 22).build();
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + "  ·  " + (enabled ? "ON" : "OFF"));
    }

    private String anchorName() {
        return switch (NovaVisualSettings.hudAnchor) {
            case 1 -> "Top Right";
            case 2 -> "Bottom Left";
            case 3 -> "Bottom Right";
            default -> "Top Left";
        };
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Use the custom opaque panel below instead of requesting vanilla blur.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xD908101B);

        int left = width / 2 - 150;
        int top = height / 2 - 120;
        int right = width / 2 + 150;
        int bottom = height / 2 + 120;
        context.fill(left - 4, top - 4, right + 4, bottom + 4, 0x5039BDF8);
        context.fill(left, top, right, bottom, 0xE8141D2B);
        context.fill(left + 2, top + 2, right - 2, bottom - 2, 0x7A26384D);
        context.fill(left + 1, top + 1, right - 1, top + 2, 0x88FFFFFF);
        context.drawText(textRenderer, "N O V A C R A F T   /   HUD CUSTOMIZATION",
                left + 16, top + 14, MUTED, false);
        context.drawText(textRenderer, "HUD Layout", left + 16, top + 31, WHITE, false);
        context.drawText(textRenderer, "Choose the data shown and its screen corner.",
                left + 16, top + 48, 0xFFB6CBE0, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        NovaVisualSettings.save();
        super.removed();
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
