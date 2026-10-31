package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NovaEventMarker {
    private static final Pattern XZ = Pattern.compile(
            "(?i)x\\s*[:=]\\s*(-?\\d{1,6})[\\s,;|]+(?:y\\s*[:=]\\s*-?\\d{1,5}[\\s,;|]+)?z\\s*[:=]\\s*(-?\\d{1,6})"
    );
    private static final Pattern COORDINATES = Pattern.compile(
            "(?i)(?:координат\\w*|coords?)\\D{0,24}(-?\\d{1,6})\\D{1,16}(-?\\d{1,6})"
    );
    private static final String[] EVENT_WORDS = {
            "ивент", "событие", "event", "куб", "cube", "посылк", "package",
            "шахт", "mine", "золотая лихорадка", "typo", "опытный"
    };

    private static boolean hasMarker;
    private static String eventName = "HolyWorld event";
    private static int targetX;
    private static int targetZ;

    private NovaEventMarker() {}

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> detect(message));
        HudElementRegistry.addLast(Identifier.of("novavisual", "holyworld_event_marker"),
                (context, tickCounter) -> render(context));
    }

    private static void detect(Text message) {
        if (!NovaVisualSettings.eventAutoMarker || message == null) return;
        String raw = message.getString();
        String lower = raw.toLowerCase(Locale.ROOT);
        boolean eventMessage = false;
        for (String word : EVENT_WORDS) {
            if (lower.contains(word)) {
                eventMessage = true;
                break;
            }
        }
        if (!eventMessage) return;

        Matcher matcher = XZ.matcher(raw);
        if (!matcher.find()) matcher = COORDINATES.matcher(raw);
        if (!matcher.find(0)) return;

        try {
            targetX = Integer.parseInt(matcher.group(1));
            targetZ = Integer.parseInt(matcher.group(2));
            eventName = raw.replaceAll("\\s+", " ").trim();
            if (eventName.length() > 42) eventName = eventName.substring(0, 39) + "...";
            hasMarker = true;
        } catch (RuntimeException ignored) {
            // Ignore malformed or incomplete server announcements.
        }
    }

    private static void render(net.minecraft.client.gui.DrawContext context) {
        if (!NovaVisualSettings.eventAutoMarker || !hasMarker) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null) return;

        double dx = targetX + 0.5 - client.player.getX();
        double dz = targetZ + 0.5 - client.player.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relative = MathHelper.wrapDegrees(bearing - client.player.getYaw());
        String arrow = Math.abs(relative) < 18 ? "▲" : relative > 0 ? "◀" : "▶";

        String title = "HOLYWORLD EVENT  ·  " + arrow + "  " + distance + "m";
        String coords = eventName + "  [" + targetX + ", " + targetZ + "]";
        int x = Math.max(8, context.getScaledWindowWidth() - 246);
        int y = 12;
        context.fill(x - 5, y - 5, x + 238, y + 38, 0xB9121D2D);
        context.fill(x - 4, y - 4, x + 237, y - 3, 0x8878CFF2);
        context.fill(x - 5, y - 5, x - 3, y + 38, 0xAA39BDF8);
        context.drawText(client.textRenderer, title, x + 4, y + 1, 0xFF83D7FF, false);
        context.drawText(client.textRenderer, client.textRenderer.trimToWidth(coords, 224),
                x + 4, y + 16, 0xFFE4F2FF, false);
    }

    public static void clear() {
        hasMarker = false;
        eventName = "HolyWorld event";
    }
}
