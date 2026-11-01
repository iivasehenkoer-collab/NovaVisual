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

        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: " + accentName()), button -> {
            NovaVisualSettings.accent = NovaVisualSettings.accent == CYAN ? 0xFF9B7BFF : CYAN;
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
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(rightX, firstY + step * 5, 220, 22).build());
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

    private String accentName() {
        return NovaVisualSettings.accent == CYAN ? "Nova Blue" : "Nova Violet";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float entrance = NovaVisualSettings.animatedMenu
                ? easeOut(Math.min(1f, (System.currentTimeMillis() - openedAt) / 420f)) : 1f;
        int center = width / 2;
        int left = center - 250;
        int top = height / 2 - 160;
        int right = center + 250;
        int bottom = height / 2 + 160;

        context.fill(left - 5, top - 5, right + 5, bottom + 5, withAlpha(0x302A9DCE, entrance));
        context.fill(left, top, right, bottom, withAlpha(0xE8141D2B, entrance));
        context.fill(left + 2, top + 2, right - 2, bottom - 2, withAlpha(0x7A26384D, entrance));
        context.fill(left + 1, top + 1, right - 1, top + 2, withAlpha(0x88FFFFFF, entrance));
        context.fill(left + 1, top + 1, left + 2, bottom - 1, withAlpha(0x5599DFFF, entrance));
        context.fill(left + 14, top + 48, right - 14, top + 49, 0x5595D9F5);
        context.drawText(textRenderer, "N O V A C R A F T   /   S P A T I A L   U I",
                left + 18, top + 13, MUTED, false);
        context.drawText(textRenderer, "NovaVisual", left + 18, top + 30, 0xFFFFFFFF, false);
        context.drawText(textRenderer, "Client-side visual toolkit  ·  Minecraft 1.21.11",
                left + 18, top + 42, 0xFFB6CBE0, false);

        super.render(context, mouseX, mouseY, delta);

        int leftX = center - 226;
        int rightX = center + 6;
        int firstY = top + 64;
        int step = 28;
        String[] labels = {
                "Animated menu", "Compact HUD", "Damage flash", "Ambient particles",
                "Fullbright", "Enemy ESP (hostile mobs)", "HolyWorld event marker", "PvP exit warning"
        };
        boolean[] enabled = {
                NovaVisualSettings.animatedMenu, NovaVisualSettings.compactHud,
                NovaVisualSettings.damageFlash, NovaVisualSettings.particles,
                NovaVisualSettings.fullbright, NovaVisualSettings.enemyEsp,
                NovaVisualSettings.eventAutoMarker, NovaVisualSettings.combatExitWarning
        };
        for (int i = 0; i < labels.length; i++) {
            glassButton(context, leftX, firstY + i * step, 220, 22,
                    labels[i] + "  ·  " + (enabled[i] ? "ON" : "OFF"), mouseX, mouseY);
        }

        String[] actions = {
                "Accent: " + accentName(), "OPTIMIZATION CENTER", "COSMETICS / SKIN STUDIO",
                "MODRINTH / MOD LIBRARY", "INTERNET / WEB", "DONE"
        };
        for (int i = 0; i < actions.length; i++) {
            glassButton(context, rightX, firstY + i * step, 220, 22,
                    actions[i], mouseX, mouseY);
        }
    }

    private void glassButton(DrawContext ctx, int x, int y, int w, int h,
                             String label, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, hover ? 0xAA72D8FF : 0x554B9CC8);
        ctx.fill(x, y, x + w, y + h, hover ? 0xE53C5873 : 0xC51B2B40);
        ctx.fill(x + 2, y + 1, x + w - 2, y + 2, 0x66FFFFFF);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, hover ? 0xFFB9EEFF : 0x8878CFF2);
        ctx.drawText(textRenderer, textRenderer.trimToWidth(label, w - 20),
                x + 12, y + 7, hover ? 0xFFFFFFFF : 0xFFE4F2FF, false);
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
