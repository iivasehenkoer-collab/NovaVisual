package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Mouse-driven editor for independently movable HUD widgets. */
public final class NovaHudEditorScreen extends Screen {
    private static final int MUTED = 0xFF9CB2C8;
    private static NovaHudLayout.Widget selected = NovaHudLayout.Widget.COMPACT;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public NovaHudEditorScreen() { super(Text.literal("NovaVisual HUD Editor")); }

    @Override
    protected void init() {
        int buttonWidth = 46;
        int gap = 3;
        int total = buttonWidth * 6 + gap * 5;
        int startX = Math.max(3, (width - total) / 2);
        int y = Math.max(3, height - 24);
        addAction(startX, y, buttonWidth, "S-", () -> {
            NovaHudLayout.adjustScale(selected, -0.05F); NovaVisualSettings.save();
        });
        addAction(startX + buttonWidth + gap, y, buttonWidth, "S+", () -> {
            NovaHudLayout.adjustScale(selected, 0.05F); NovaVisualSettings.save();
        });
        addAction(startX + 2 * (buttonWidth + gap), y, buttonWidth, "O-", () -> {
            NovaHudLayout.adjustOpacity(selected, -0.10F); NovaVisualSettings.save();
        });
        addAction(startX + 3 * (buttonWidth + gap), y, buttonWidth, "O+", () -> {
            NovaHudLayout.adjustOpacity(selected, 0.10F); NovaVisualSettings.save();
        });
        addAction(startX + 4 * (buttonWidth + gap), y, buttonWidth, "Reset", () -> {
            NovaHudLayout.reset(selected); NovaVisualSettings.save();
        });
        addAction(startX + 5 * (buttonWidth + gap), y, buttonWidth, "Done",
                () -> { if (client != null) client.setScreen(new NovaHudSettingsScreen()); });
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
        context.drawText(textRenderer, "Drag a widget to move it  ·  S-/S+ size  ·  O-/O+ opacity",
                left, 32, 0xFFEAF6FF, false);
        String info = selected.label() + "   " + Math.round(NovaHudLayout.scale(selected) * 100)
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        for (NovaHudLayout.Widget widget : NovaHudLayout.Widget.values()) {
            NovaHudLayout.Bounds b = NovaHudLayout.previewBounds(widget, width, height);
            if (mouseX >= b.x() && mouseX <= b.x() + b.width()
                    && mouseY >= b.y() && mouseY <= b.y() + b.height()) {
                selected = widget;
                dragging = true;
                dragOffsetX = (int) mouseX - b.x();
                dragOffsetY = (int) mouseY - b.y();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            NovaHudLayout.Bounds b = NovaHudLayout.previewBounds(selected, width, height);
            NovaHudLayout.move(selected, (int) mouseX - dragOffsetX, (int) mouseY - dragOffsetY,
                    Math.max(1, Math.round(b.width() / b.scale())),
                    Math.max(1, Math.round(b.height() / b.scale())), width, height);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            NovaVisualSettings.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
