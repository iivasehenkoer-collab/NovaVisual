package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaVisualScreen extends Screen {
    private static final int CYAN = 0xFF39BDF8;
    private static final int MUTED = 0xFF9CB2C8;
    private final long openedAt = System.currentTimeMillis();

    public NovaVisualScreen() { super(Text.literal("NovaVisual")); }

    @Override
    protected void init() {
        int center = width / 2;
        int top = height / 2 - 160;
        int leftX = center - 226;
        int rightX = center + 6;
        int firstY = top + 64;
        int step = 28;

        addDrawableChild(toggleButton(leftX, firstY, "Animated menu",
                () -> NovaVisualSettings.animatedMenu, value -> NovaVisualSettings.animatedMenu = value));
        addDrawableChild(toggleButton(leftX, firstY + step, "Compact HUD",
                () -> NovaVisualSettings.compactHud, value -> NovaVisualSettings.compactHud = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 2, "Damage flash",
                () -> NovaVisualSettings.damageFlash, value -> NovaVisualSettings.damageFlash = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 3, "Ambient particles",
                () -> NovaVisualSettings.particles, value -> NovaVisualSettings.particles = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 4, "Fullbright",
                () -> NovaVisualSettings.fullbright, value -> NovaVisualSettings.fullbright = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 5, "Enemy ESP (hostile mobs)",
                () -> NovaVisualSettings.enemyEsp, value -> NovaVisualSettings.enemyEsp = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 6, "HolyWorld event marker",
                () -> NovaVisualSettings.eventAutoMarker, value -> NovaVisualSettings.eventAutoMarker = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 7, "PvP exit warning",
                () -> NovaVisualSettings.combatExitWarning, value -> NovaVisualSettings.combatExitWarning = value));
        addDrawableChild(toggleButton(leftX, firstY + step * 8, "Low-health vignette",
                () -> NovaVisualSettings.healthVignette, value -> NovaVisualSettings.healthVignette = value));

        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: " + accentName()), button -> {
            NovaVisualSettings.accent = NovaVisualSettings.accent == CYAN ? 0xFF9B7BFF : CYAN;
            NovaHudLayout.markCustom();
            NovaVisualSettings.save();
            button.setMessage(Text.literal("Accent: " + accentName()));
        }).dimensions(rightX, firstY, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Optimization Center"),
                button -> { if (client != null) client.setScreen(new NovaOptimizationScreen()); })
                .dimensions(rightX, firstY + step, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cosmetics / Skin Studio"),
                button -> { if (client != null) client.setScreen(new NovaCosmeticsScreen()); })
                .dimensions(rightX, firstY + step * 2, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Modrinth / Mod Library"),
                button -> { if (client != null) client.setScreen(new NovaModrinthScreen()); })
                .dimensions(rightX, firstY + step * 3, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Internet / Web"),
                button -> { if (client != null) client.setScreen(new NovaBrowserScreen()); })
                .dimensions(rightX, firstY + step * 4, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD / Layout"),
                button -> { if (client != null) client.setScreen(new NovaHudSettingsScreen()); })
                .dimensions(rightX, firstY + step * 5, 220, 22).build());
        addDrawableChild(toggleButton(rightX, firstY + step * 6, "Compass HUD",
                () -> NovaVisualSettings.compassHud, value -> NovaVisualSettings.compassHud = value));
        addDrawableChild(ButtonWidget.builder(Text.literal("Waypoint Manager"),
                button -> { if (client != null) client.setScreen(new NovaWaypointScreen(0)); })
                .dimensions(rightX, firstY + step * 7, 220, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(rightX, firstY + step * 8, 220, 22).build());
    }

    private ButtonWidget toggleButton(int x, int y, String label,
                                      java.util.function.BooleanSupplier getter,
                                      java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            if (label.equals("Compact HUD") || label.equals("Low-health vignette")
                    || label.equals("Compass HUD")) {
                NovaHudLayout.markCustom();
            }
            NovaVisualSettings.save();
            button.setMessage(toggleText(label, next));
        }).dimensions(x, y, 220, 22).build();
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + "  ·  " + (enabled ? "ON" : "OFF"));
    }

    private String accentName() {
        return NovaVisualSettings.accent == CYAN ? "Nova Blue" : "Nova Violet";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float entrance = NovaVisualSettings.animatedMenu
                ? easeOut(Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 260.0F)) : 1.0F;
        float scale = 0.985F + 0.015F * entrance;
        int center = width / 2;
        int verticalCenter = height / 2;

        NovaGlassUi.drawBackdrop(context, width, height);
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        try {
            matrices.translate(center, verticalCenter);
            matrices.scale(scale, scale);
            matrices.translate(-center, -verticalCenter);

            int left = center - 250;
            int top = verticalCenter - 160;
            int right = center + 250;
            int bottom = verticalCenter + 160;
            NovaGlassUi.drawPanel(context, left, top, right - left, bottom - top,
                    NovaVisualSettings.accent);
            NovaGlassUi.drawDivider(context, left + 16, top + 48, right - left - 32, 0x6695D9F5);
            context.drawText(textRenderer, "N O V A C R A F T   /   S P A T I A L   U I",
                    left + 18, top + 13, MUTED, false);
            context.drawText(textRenderer, "NovaVisual", left + 18, top + 30, 0xFFFFFFFF, false);
            context.drawText(textRenderer, "Client-side visual toolkit  ·  Minecraft 1.21.11",
                    left + 18, top + 42, 0xFFB6CBE0, false);

            // Render the real ButtonWidgets last. Decorative duplicate labels/cards used to
            // be painted over them here, making the controls look stacked and obscured.
            super.render(context, mouseX, mouseY, delta);
        } finally {
            matrices.popMatrix();
        }
    }

    private static float easeOut(float value) {
        float inverse = 1f - value;
        return 1f - inverse * inverse * inverse;
    }

    private static int withAlpha(int color, float amount) {
        return ((int) (((color >>> 24) & 255) * amount) << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public void removed() {
        NovaVisualSettings.save();
        super.removed();
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
