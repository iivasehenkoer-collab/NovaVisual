package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class NovaWaypointScreen extends Screen {
    private static final int WHITE = 0xFFEAF6FF;
    private static final int MUTED = 0xFF9CB2C8;
    private final int requestedStart;
    private TextFieldWidget nameField;
    private String feedback = "";

    public NovaWaypointScreen(int pageStart) {
        super(Text.literal("NovaVisual Waypoints"));
        this.requestedStart = Math.max(0, pageStart);
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(380, width - 12);
        int panelHeight = Math.min(312, height - 8);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int innerX = left + 12;
        int innerWidth = panelWidth - 24;
        boolean compact = panelHeight < 280;

        int fieldY = top + 54;
        int saveWidth = Math.min(92, Math.max(76, innerWidth / 3));
        int fieldWidth = innerWidth - saveWidth - 6;
        nameField = new TextFieldWidget(textRenderer, innerX, fieldY,
                fieldWidth, 20, Text.literal("Waypoint name"));
        nameField.setMaxLength(24);
        nameField.setPlaceholder(Text.literal("Home, Base, Event, Farm..."));
        addDrawableChild(nameField);
        nameField.setFocused(true);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save here"), button -> saveCurrentPosition())
                .dimensions(innerX + fieldWidth + 6, fieldY, saveWidth, 20).build());

        int rowStart = top + (compact ? 94 : 98);
        int visibleRows = Math.max(1, Math.min(6,
                (panelHeight - (compact ? 160 : 150)) / 24));
        List<NovaWaypointStore.Waypoint> points = NovaWaypointStore.all();
        int start = Math.min(requestedStart, Math.max(0, points.size() - visibleRows));
        int end = Math.min(points.size(), start + visibleRows);

        for (int index = start; index < end; index++) {
            final int waypointIndex = index;
            NovaWaypointStore.Waypoint point = points.get(waypointIndex);
            int rowY = rowStart + (waypointIndex - start) * 24;
            boolean active = waypointIndex == NovaWaypointStore.activeIndex();
            String label = (active ? "● " : "Set active: ")
                    + point.name() + "  ·  " + point.x() + ", " + point.y() + ", " + point.z();
            int deleteWidth = 64;
            int selectWidth = innerWidth - deleteWidth - 6;

            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                NovaWaypointStore.setActive(waypointIndex);
                if (client != null) client.setScreen(new NovaWaypointScreen(start));
            }).dimensions(innerX, rowY, selectWidth, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> {
                NovaWaypointStore.remove(waypointIndex);
                if (client != null) client.setScreen(new NovaWaypointScreen(start));
            }).dimensions(innerX + selectWidth + 6, rowY, deleteWidth, 20).build());
        }

        int pageY = rowStart + visibleRows * 24 + 2;
        int center = width / 2;
        if (start > 0) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Previous"), button -> {
                if (client != null) client.setScreen(new NovaWaypointScreen(Math.max(0, start - visibleRows)));
            }).dimensions(center - 104, pageY, 98, 20).build());
        }
        if (end < points.size()) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Next"), button -> {
                if (client != null) client.setScreen(new NovaWaypointScreen(start + visibleRows));
            }).dimensions(center + 6, pageY, 98, 20).build());
        }

        int backY = top + panelHeight - 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(center - 50, backY, 100, 20).build());
    }

    private void saveCurrentPosition() {
        if (client == null || client.player == null || client.world == null) {
            feedback = "Join a world before saving a waypoint.";
            return;
        }

        String name = nameField == null ? "" : nameField.getText().strip();
        if (name.isBlank()) {
            feedback = "Enter a name for this waypoint.";
            return;
        }

        BlockPos position = client.player.getBlockPos();
        boolean saved = NovaWaypointStore.saveCurrent(
                name,
                position.getX(),
                position.getY(),
                position.getZ(),
                client.world.getRegistryKey().getValue().toString()
        );

        if (!saved) {
            feedback = "Waypoint limit reached (" + NovaWaypointStore.MAX_WAYPOINTS + ").";
            return;
        }

        client.player.sendMessage(Text.literal("NovaVisual: saved waypoint " + name), true);
        client.setScreen(new NovaWaypointScreen(0));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // The opaque NovaVisual panel replaces the vanilla blur background.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        NovaGlassUi.drawBackdrop(context, width, height);

        int panelWidth = Math.min(380, width - 12);
        int panelHeight = Math.min(312, height - 8);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        NovaGlassUi.drawPanel(context, left, top, panelWidth, panelHeight,
                NovaVisualSettings.accent);

        context.drawText(textRenderer, "N O V A C R A F T   /   NAVIGATION",
                left + 14, top + 10, MUTED, false);
        context.drawText(textRenderer, "Waypoint Manager",
                left + 14, top + 26, WHITE, false);
        context.drawText(textRenderer,
                "Save named locations; select one to drive the live HUD.",
                left + 14, top + 40, 0xFFB6CBE0, false);

        List<NovaWaypointStore.Waypoint> points = NovaWaypointStore.all();
        context.drawText(textRenderer,
                "SAVED POINTS  " + points.size() + " / " + NovaWaypointStore.MAX_WAYPOINTS,
                left + 14, top + 82, MUTED, false);

        if (points.isEmpty()) {
            context.drawText(textRenderer, "No waypoints yet. Enter a name and press Save here.",
                    left + 14, top + 104, 0xFFEAF6FF, false);
        }

        if (!feedback.isBlank()) {
            context.drawText(textRenderer, textRenderer.trimToWidth(feedback, panelWidth - 28),
                    left + 14, bottom - 44, 0xFFFFC857, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        NovaWaypointStore.save();
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