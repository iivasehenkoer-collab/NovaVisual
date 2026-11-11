package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Displays the selected entry from the persistent multi-waypoint collection. */
public final class NovaWaypointHud {
    private static KeyBinding setWaypointKey;
    private static KeyBinding clearWaypointKey;

    private NovaWaypointHud() {}

    public static void register() {
        setWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novavisual.set_waypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                KeyBinding.Category.MISC
        ));
        clearWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novavisual.clear_waypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(NovaWaypointHud::tick);
        HudElementRegistry.addLast(Identifier.of("novavisual", "waypoint_hud"),
                (context, tickCounter) -> render(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.currentScreen != null) return;

        while (setWaypointKey.wasPressed()) {
            if (client.player == null || client.world == null) continue;

            var position = client.player.getBlockPos();
            String name = NovaWaypointStore.updateActivePosition(
                    position.getX(),
                    position.getY(),
                    position.getZ(),
                    client.world.getRegistryKey().getValue().toString()
            );

            String message = name == null
                    ? "NovaVisual: waypoint list is full"
                    : String.format(Locale.ROOT, "NovaVisual: updated %s at %d %d %d",
                    name, position.getX(), position.getY(), position.getZ());
            client.player.sendMessage(Text.literal(message), true);
        }

        while (clearWaypointKey.wasPressed()) {
            String removed = NovaWaypointStore.removeActive();
            if (client.player != null) {
                String message = removed == null
                        ? "NovaVisual: no active waypoint"
                        : "NovaVisual: deleted waypoint " + removed;
                client.player.sendMessage(Text.literal(message), true);
            }
        }
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NovaVisualSettings.waypointHud || client.player == null || client.world == null
                || client.options.hudHidden || client.currentScreen != null) {
            return;
        }

        var activeWaypoint = NovaWaypointStore.active();
        if (activeWaypoint.isEmpty()) return;
        NovaWaypointStore.Waypoint waypoint = activeWaypoint.get();

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        if (screenWidth < 120 || screenHeight < 90) return;

        int panelWidth = Math.min(292, screenWidth - 16);
        int panelHeight = 49;
        int panelX = (screenWidth - panelWidth) / 2;
        int preferredY = NovaVisualSettings.compassHud ? 108 : 64;
        int panelY = Math.max(4, Math.min(preferredY, screenHeight - panelHeight - 4));

        String firstLine;
        String secondLine;
        int statusColor = 0xFF7DD3FC;
        String currentDimension = client.world.getRegistryKey().getValue().toString();
        String waypointName = waypoint.name().toUpperCase(Locale.ROOT);

        if (!currentDimension.equals(waypoint.dimension())) {
            firstLine = waypointName + "  ·  DIMENSION MISMATCH";
            secondLine = "Saved in " + shortDimension(waypoint.dimension())
                    + "  ·  XYZ " + waypoint.x() + " " + waypoint.y() + " " + waypoint.z();
            statusColor = 0xFFFFC857;
        } else {
            double dx = waypoint.x() + 0.5 - client.player.getX();
            double dz = waypoint.z() + 0.5 - client.player.getZ();
            int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float relative = MathHelper.wrapDegrees(bearing - client.player.getYaw());

            String arrow;
            String side;
            if (Math.abs(relative) < 18.0F) {
                arrow = "▲";
                side = "AHEAD";
            } else if (Math.abs(relative) > 162.0F) {
                arrow = "▼";
                side = "BEHIND";
            } else if (relative > 0.0F) {
                arrow = "◀";
                side = "LEFT";
            } else {
                arrow = "▶";
                side = "RIGHT";
            }

            firstLine = distance <= 2
                    ? waypointName + "  ·  ARRIVED"
                    : String.format(Locale.ROOT, "%s  %s  %dm  ·  %s",
                    waypointName, arrow, distance, side);
            int deltaY = waypoint.y() - client.player.getBlockY();
            secondLine = String.format(Locale.ROOT, "XYZ  %d %d %d  ·  ΔY %+d",
                    waypoint.x(), waypoint.y(), waypoint.z(), deltaY);
        }

        String controls = "F8  UPDATE ACTIVE     F9  DELETE ACTIVE";
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, 0x5039BDF8);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xB9121D2D);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaVisualSettings.accent);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, NovaVisualSettings.accent);

        int textWidth = Math.max(1, panelWidth - 16);
        context.drawText(client.textRenderer,
                client.textRenderer.trimToWidth(firstLine, textWidth),
                panelX + 8, panelY + 6, statusColor, false);
        context.drawText(client.textRenderer,
                client.textRenderer.trimToWidth(secondLine, textWidth),
                panelX + 8, panelY + 20, 0xFFEAF6FF, false);
        context.drawText(client.textRenderer,
                client.textRenderer.trimToWidth(controls, textWidth),
                panelX + 8, panelY + 35, 0xFF9CB2C8, false);
    }

    private static String shortDimension(String dimension) {
        if (dimension == null || dimension.isBlank()) return "UNKNOWN";
        int separator = dimension.indexOf(':');
        String path = separator >= 0 ? dimension.substring(separator + 1) : dimension;
        return path.replace('_', ' ').toUpperCase(Locale.ROOT);
    }
}