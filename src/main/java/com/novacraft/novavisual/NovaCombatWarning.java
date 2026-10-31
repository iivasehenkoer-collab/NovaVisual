package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side combat activity indicator. This is an estimate based on observed
 * damage events; only the server can authoritatively report a server combat tag.
 */
public final class NovaCombatWarning {
    private static final long COMBAT_WINDOW_MS = 15_000L;
    private static long combatUntil;
    private static float previousHealth = -1.0F;
    private static final Map<UUID, Integer> previousHurtTimes = new HashMap<>();

    private NovaCombatWarning() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            previousHealth = -1.0F;
            previousHurtTimes.clear();
            combatUntil = 0L;
            return;
        }
        if (!NovaVisualSettings.combatExitWarning) {
            combatUntil = 0L;
            return;
        }

        float health = client.player.getHealth();
        if (previousHealth >= 0.0F && health < previousHealth) markCombat();
        previousHealth = health;

        for (PlayerEntity other : client.world.getPlayers()) {
            if (other == client.player) continue;
            int hurtTime = other.hurtTime;
            int previous = previousHurtTimes.getOrDefault(other.getUuid(), 0);
            if (hurtTime > previous) markCombat();
            previousHurtTimes.put(other.getUuid(), hurtTime);
        }
        previousHurtTimes.keySet().removeIf(uuid ->
                client.world.getPlayers().stream().noneMatch(p -> p.getUuid().equals(uuid)));
    }

    private static void markCombat() {
        combatUntil = System.currentTimeMillis() + COMBAT_WINDOW_MS;
    }

    public static boolean isCombatActive() {
        return NovaVisualSettings.combatExitWarning && System.currentTimeMillis() < combatUntil;
    }

    private static void render(DrawContext context) {
        if (!isCombatActive()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        long seconds = Math.max(1L, (combatUntil - System.currentTimeMillis() + 999L) / 1000L);
        String message = "PVP COMBAT  ·  Avoid disconnecting!  ·  " + seconds + "s";
        int width = client.textRenderer.getWidth(message);
        int x = (context.getScaledWindowWidth() - width) / 2;
        int y = 12;
        context.fill(x - 10, y - 5, x + width + 10, y + 15, 0xD52B1018);
        context.fill(x - 10, y - 5, x + width + 10, y - 3, 0xFFFF526B);
        context.drawText(client.textRenderer, message, x, y, 0xFFFFDCE2, true);
    }
}
