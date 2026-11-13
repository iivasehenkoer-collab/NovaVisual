package com.novacraft.novavisual;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Mouse-driven editor for independently movable HUD widgets. */
public final class NovaHudEditorScreen extends Screen {
    private static final int MUTED = 0xFF9CB2C8;
    private static NovaHudLayout.Widget selected = NovaHudLayout.Widget.COMPACT;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private double draggedOffsetX;
    private double draggedOffsetY;
    private ButtonWidget pvpProfileButton;
    private ButtonWidget anarchyProfileButton;
    private ButtonWidget survivalProfileButton;

    public NovaHudEditorScreen() { super(Text.literal("NovaVisual HUD Editor")); }

    @Override
    protected void init() {
        int buttonWidth = 46;
        int gap = 3;
        int total = buttonWidth * 6 + gap * 5;
        int startX = Math.max(3, (width - total) / 2);
        int y = Math.max(3, height - 24);
        addAction(startX, y, buttonWidth, "S-", () -> {
            NovaHudLayout.adjustScale(selected, -0.05F); NovaVisualSettings.save(); updateProfileButtons();
        });
        addAction(startX + buttonWidth + gap, y, buttonWidth, "S+", () -> {
            NovaHudLayout.adjustScale(selected, 0.05F); NovaVisualSettings.save(); updateProfileButtons();
        });
        addAction(startX + 2 * (buttonWidth + gap), y, buttonWidth, "O-", () -> {
            NovaHudLayout.adjustOpacity(selected, -0.10F); NovaVisualSettings.save(); updateProfileButtons();
        });
        addAction(startX + 3 * (buttonWidth + gap), y, buttonWidth, "O+", () -> {
            NovaHudLayout.adjustOpacity(selected, 0.10F); NovaVisualSettings.save(); updateProfileButtons();
        });
        addAction(startX + 4 * (buttonWidth + gap), y, buttonWidth, "Reset", () -> {
            NovaHudLayout.reset(selected); NovaVisualSettings.save(); updateProfileButtons();
        });
        addAction(startX + 5 * (buttonWidth + gap), y, buttonWidth, "Done",
                () -> { if (client != null) client.setScreen(new NovaHudSettingsScreen()); });

        int profileWidth = 94;
        int profileGap = 5;
        int profileX = (width - profileWidth * 3 - profileGap * 2) / 2;
        int profileY = 82;
        pvpProfileButton = createProfileButton(profileX, profileY, profileWidth, NovaHudLayout.Profile.PVP);
        anarchyProfileButton = createProfileButton(profileX + profileWidth + profileGap,
                profileY, profileWidth, NovaHudLayout.Profile.ANARCHY);
        survivalProfileButton = createProfileButton(profileX + (profileWidth + profileGap) * 2,
                profileY, profileWidth, NovaHudLayout.Profile.SURVIVAL);
        addDrawableChild(pvpProfileButton);
        addDrawableChild(anarchyProfileButton);
        addDrawableChild(survivalProfileButton);
        updateProfileButtons();
    }

    private ButtonWidget createProfileButton(int x, int y, int buttonWidth, NovaHudLayout.Profile profile) {
        return ButtonWidget.builder(profileText(profile), button -> {
            NovaHudLayout.applyProfile(profile);
            NovaVisualSettings.save();
            updateProfileButtons();
        }).dimensions(x, y, buttonWidth, 20).build();
    }

    private Text profileText(NovaHudLayout.Profile profile) {
        String prefix = NovaHudLayout.activeProfile() == profile ? "✓ " : "";
        return Text.literal(prefix + profile.label());
    }

    private void updateProfileButtons() {
        if (pvpProfileButton != null) pvpProfileButton.setMessage(profileText(NovaHudLayout.Profile.PVP));
        if (anarchyProfileButton != null) anarchyProfileButton.setMessage(profileText(NovaHudLayout.Profile.ANARCHY));
        if (survivalProfileButton != null) survivalProfileButton.setMessage(profileText(NovaHudLayout.Profile.SURVIVAL));
    }

    private void addAction(int x, int y, int buttonWidth, String label, Runnable action) {
        int safeX = Math.min(x, Math.max(3, width - buttonWidth - 3));
        addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> action.run())
                .dimensions(safeX, y, buttonWidth, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // The editor draws its own glass backdrop.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        NovaGlassUi.drawBackdrop(context, width, height);
        int panelWidth = Math.min(430, width - 12);
        NovaGlassUi.drawPanel(context, (width - panelWidth) / 2, 7, panelWidth, 70,
                NovaVisualSettings.accent);
        int left = (width - panelWidth) / 2 + 14;
        context.drawText(textRenderer, "N O V A C R A F T  /  HUD EDITOR", left, 16, MUTED, false);
        context.drawText(textRenderer, "Drag widgets  ·  Presets: PvP / Anarchy / Survival",
                left, 32, 0xFFEAF6FF, false);
        String info = NovaHudLayout.activeProfile().label() + "  /  " + selected.label() + "   " + Math.round(NovaHudLayout.scale(selected) * 100)
                + "% size   " + Math.round(NovaHudLayout.opacity(selected) * 100) + "% opacity";
        context.drawText(textRenderer, textRenderer.trimToWidth(info, panelWidth - 28),
                left, 49, NovaVisualSettings.accent, false);

        for (NovaHudLayout.Widget widget : NovaHudLayout.Widget.values()) {
            NovaHudLayout.Bounds b = NovaHudLayout.previewBounds(widget, width, height);
            boolean active = widget == selected;
            boolean hover = mouseX >= b.x() && mouseX <= b.x() + b.width()
                    && mouseY >= b.y() && mouseY <= b.y() + b.height();
            NovaGlassUi.drawCard(context, b.x(), b.y(), b.width(), b.height(), active || hover);
            context.fill(b.x() + 2, b.y() + 2, b.x() + b.width() - 2, b.y() + 4,
                    NovaHudLayout.color(widget, NovaVisualSettings.accent));
            String label = textRenderer.trimToWidth(widget.label(), Math.max(1, b.width() - 12));
            context.drawText(textRenderer, label, b.x() + 6,
                    b.y() + Math.max(6, b.height() / 2 - 4), active ? 0xFFFFFFFF : MUTED, false);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() != 0) return false;
        double mouseX = click.x();
        double mouseY = click.y();
        for (NovaHudLayout.Widget widget : NovaHudLayout.Widget.values()) {
            NovaHudLayout.Bounds b = NovaHudLayout.previewBounds(widget, width, height);
            if (mouseX >= b.x() && mouseX <= b.x() + b.width()
                    && mouseY >= b.y() && mouseY <= b.y() + b.height()) {
                selected = widget;
                dragging = true;
                dragStartX = b.x();
                dragStartY = b.y();
                draggedOffsetX = 0.0;
                draggedOffsetY = 0.0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging && click.button() == 0) {
            draggedOffsetX += offsetX;
            draggedOffsetY += offsetY;
            NovaHudLayout.Bounds b = NovaHudLayout.previewBounds(selected, width, height);
            NovaHudLayout.move(selected,
                    (int) Math.round(dragStartX + draggedOffsetX),
                    (int) Math.round(dragStartY + draggedOffsetY),
                    Math.max(1, Math.round(b.width() / b.scale())),
                    Math.max(1, Math.round(b.height() / b.scale())), width, height);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && dragging) {
            dragging = false;
            NovaVisualSettings.save();
            updateProfileButtons();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void removed() {
        NovaVisualSettings.save();
        super.removed();
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(new NovaHudSettingsScreen());
    }

    @Override
    public boolean shouldPause() { return false; }
}
