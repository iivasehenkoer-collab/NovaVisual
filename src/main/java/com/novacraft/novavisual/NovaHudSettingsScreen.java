package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaHudSettingsScreen extends Screen {
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;
    private static Page selectedPage = Page.LAYOUT;

    private enum Page {
        LAYOUT, COMBAT, STATUS
    }

    public NovaHudSettingsScreen() {
        super(Text.literal("NovaVisual HUD Layout"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int panelTop = panelTop();
        int buttonX = centerX - 110;
        int tabY = panelTop + 38;
        int rowStartY = panelTop + (height < 300 ? 56 : 64);
        int rowStep = height < 300 ? 19 : 24;

        addPageButton(buttonX, tabY, Page.LAYOUT, "Layout");
        addPageButton(buttonX + 74, tabY, Page.COMBAT, "Combat");
        addPageButton(buttonX + 148, tabY, Page.STATUS, "Status");

        switch (selectedPage) {
            case LAYOUT -> {
                addToggle(buttonX, rowStartY, "Coordinates",
                        () -> NovaVisualSettings.hudCoordinates,
                        value -> NovaVisualSettings.hudCoordinates = value);
                addToggle(buttonX, rowStartY + rowStep, "Health / Armor",
                        () -> NovaVisualSettings.hudVitals,
                        value -> NovaVisualSettings.hudVitals = value);
                addToggle(buttonX, rowStartY + rowStep * 2, "Dimension / FPS",
                        () -> NovaVisualSettings.hudStatus,
                        value -> NovaVisualSettings.hudStatus = value);

                addDrawableChild(ButtonWidget.builder(Text.literal("Position: " + anchorName()), button -> {
                    NovaVisualSettings.hudAnchor = (NovaVisualSettings.hudAnchor + 1) % 4;
                    NovaHudLayout.markCustom();
                    NovaVisualSettings.save();
                    button.setMessage(Text.literal("Position: " + anchorName()));
                }).dimensions(buttonX, rowStartY + rowStep * 3, 220, 20).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("Open HUD Editor"), button -> {
                    if (client != null) client.setScreen(new NovaHudEditorScreen());
                }).dimensions(buttonX, rowStartY + rowStep * 4, 220, 20).build());
                addBackButton(buttonX, rowStartY + rowStep * 5);
            }
            case COMBAT -> {
                addToggle(buttonX, rowStartY, "Keystrokes",
                        () -> NovaVisualSettings.keystrokesHud,
                        value -> NovaVisualSettings.keystrokesHud = value);
                addToggle(buttonX, rowStartY + rowStep, "Totem Counter",
                        () -> NovaVisualSettings.totemCounter,
                        value -> NovaVisualSettings.totemCounter = value);
                addToggle(buttonX, rowStartY + rowStep * 2, "Totem Pop Alert",
                        () -> NovaVisualSettings.totemPopAlert,
                        value -> NovaVisualSettings.totemPopAlert = value);
                addToggle(buttonX, rowStartY + rowStep * 3, "PvP Target HUD",
                        () -> NovaVisualSettings.targetHud,
                        value -> NovaVisualSettings.targetHud = value);
                addToggle(buttonX, rowStartY + rowStep * 4, "Target Range",
                        () -> NovaVisualSettings.targetRangeHud,
                        value -> NovaVisualSettings.targetRangeHud = value);
                addBackButton(buttonX, rowStartY + rowStep * 5);
            }
            case STATUS -> {
                addToggle(buttonX, rowStartY, "Armor Durability HUD",
                        () -> NovaVisualSettings.armorDurabilityHud,
                        value -> NovaVisualSettings.armorDurabilityHud = value);
                addToggle(buttonX, rowStartY + rowStep, "Low Durability Alert",
                        () -> NovaVisualSettings.armorDurabilityAlert,
                        value -> NovaVisualSettings.armorDurabilityAlert = value);
                addToggle(buttonX, rowStartY + rowStep, "Potion Effects HUD",
                        () -> NovaVisualSettings.statusEffectHud,
                        value -> NovaVisualSettings.statusEffectHud = value);
                addToggle(buttonX, rowStartY + rowStep * 2, "Player Resource Bars",
                        () -> NovaVisualSettings.resourceBarsHud,
                        value -> NovaVisualSettings.resourceBarsHud = value);
                addToggle(buttonX, rowStartY + rowStep * 3, "Low-health Vignette",
                        () -> NovaVisualSettings.healthVignette,
                        value -> NovaVisualSettings.healthVignette = value);
                addToggle(buttonX, rowStartY + rowStep * 4, "World Info HUD",
                        () -> NovaVisualSettings.worldInfoHud,
                        value -> NovaVisualSettings.worldInfoHud = value);
                addToggle(buttonX, rowStartY + rowStep * 5, "Waypoint HUD",
                        () -> NovaVisualSettings.waypointHud,
                        value -> NovaVisualSettings.waypointHud = value);
                addBackButton(buttonX, rowStartY + rowStep * 6);
            }
        }
    }

    private void addPageButton(int x, int y, Page page, String label) {
        String prefix = selectedPage == page ? "● " : "";
        addDrawableChild(ButtonWidget.builder(Text.literal(prefix + label), button -> {
            selectedPage = page;
            if (client != null) client.setScreen(new NovaHudSettingsScreen());
        }).dimensions(x, y, 72, 20).build());
    }

    private void addToggle(int x, int y, String label,
                           java.util.function.BooleanSupplier getter,
                           java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            NovaHudLayout.markCustom();
            NovaVisualSettings.save();
            button.setMessage(toggleText(label, next));
        }).dimensions(x, y, 220, 20).build());
    }

    private void addBackButton(int x, int y) {
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(x, y, 220, 20).build());
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

    private int panelHeight() {
        return selectedPage == Page.STATUS ? 260 : selectedPage == Page.LAYOUT ? 244 : 236;
    }

    private int panelTop() {
        int panelHeight = panelHeight();
        return Math.max(4, Math.min(height / 2 - panelHeight / 2, height - panelHeight - 4));
    }

    private int panelBottom() {
        return Math.min(panelTop() + panelHeight(), height - 4);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Opaque NovaVisual panel replaces vanilla background blur.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        NovaGlassUi.drawBackdrop(context, width, height);

        int left = width / 2 - 150;
        int top = panelTop();
        int right = width / 2 + 150;
        int bottom = panelBottom();
        NovaGlassUi.drawPanel(context, left, top, right - left, bottom - top,
                NovaVisualSettings.accent);

        context.drawText(textRenderer, "N O V A C R A F T   /   HUD CUSTOMIZATION",
                left + 16, top + 10, MUTED, false);
        context.drawText(textRenderer, "HUD Layout  ·  " + selectedPage.name(),
                left + 16, top + 26, WHITE, false);
        context.drawText(textRenderer, "Select a category or open the mouse-driven HUD editor.",
                left + 16, top + 42, 0xFFB6CBE0, false);

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
