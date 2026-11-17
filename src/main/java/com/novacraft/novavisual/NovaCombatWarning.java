package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Client-side PvP activity estimate. It only reacts to damage/attack state
 * associated with another player; it cannot read an authoritative server tag.
 */
public final class NovaCombatWarning {
    private static final long COMBAT_WINDOW_MS = 15_000L;
    private static long combatUntil;
    private static float previousHealth = -1.0F;
    private static int previousLocalHurtTime = -1;
    private static Object trackedWorld;
    private static final Map<UUID, Integer> previousHurtTimes = new HashMap<>();

    private NovaCombatWarning() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(NovaCombatWarning::tick);
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            resetTracking();
            return;
        }

        PlayerEntity player = client.player;
        if (trackedWorld != client.world) {
            trackedWorld = client.world;
            combatUntil = 0L;
            previousHealth = player.getHealth();
            previousLocalHurtTime = player.hurtTime;
            previousHurtTimes.clear();
        }

        if (!NovaVisualSettings.combatExitWarning) {
            combatUntil = 0L;
            previousHealth = player.getHealth();
            previousLocalHurtTime = player.hurtTime;
            previousHurtTimes.clear();
            return;
        }

        float health = player.getHealth();
        int localHurtTime = player.hurtTime;
        boolean localWasHit = localHurtTime > previousLocalHurtTime || health < previousHealth;
        if (localWasHit && wasHitByAnotherPlayer(player)) {
            markCombat();
        }
        previousHealth = health;
        previousLocalHurtTime = localHurtTime;

        Set<UUID> presentPlayers = new HashSet<>();
        for (PlayerEntity other : client.world.getPlayers()) {
            if (other == player) continue;

            UUID uuid = other.getUuid();
            presentPlayers.add(uuid);
            int hurtTime = other.hurtTime;
            Integer previous = previousHurtTimes.put(uuid, hurtTime);

            // Only count a nearby player's fresh hurt animation when the local
            // player is recorded as its attacker. Unrelated fights no longer
            // put the user into a false combat state.
            if (previous != null && hurtTime > previous && wasHitByPlayer(other, player)) {
                markCombat();
            }
        }
        previousHurtTimes.keySet().removeIf(uuid -> !presentPlayers.contains(uuid));
    }

    private static boolean wasHitByAnotherPlayer(LivingEntity target) {
        LivingEntity attacker = target.getAttacker();
        if (attacker instanceof PlayerEntity && attacker != target) return true;

        PlayerEntity attackingPlayer = target.getAttackingPlayer();
        return attackingPlayer != null && attackingPlayer != target;
    }

    private static boolean wasHitByPlayer(LivingEntity target, PlayerEntity player) {
        return target.getAttacker() == player || target.getAttackingPlayer() == player;
    }

    private static void markCombat() {
        combatUntil = System.currentTimeMillis() + COMBAT_WINDOW_MS;
    }

    private static void resetTracking() {
        trackedWorld = null;
        combatUntil = 0L;
        previousHealth = -1.0F;
        previousLocalHurtTime = -1;
        previousHurtTimes.clear();
    }

    public static boolean isCombatActive() {
        return NovaVisualSettings.combatExitWarning && System.currentTimeMillis() < combatUntil;
    }

    private static void render(DrawContext context) {
        if (!isCombatActive()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        long remaining = Math.max(0L, combatUntil - System.currentTimeMillis());
        long seconds = Math.max(1L, (remaining + 999L) / 1000L);
        int screenWidth = context.getScaledWindowWidth();
        String message = "PVP ACTIVITY  ·  Avoid disconnecting!  ·  " + seconds + "s";
        message = client.textRenderer.trimToWidth(message, Math.max(1, screenWidth - 32));
        int textWidth = client.textRenderer.getWidth(message);
        int cardWidth = Math.min(screenWidth - 12, textWidth + 20);
        int x = (screenWidth - cardWidth) / 2;
        int y = 12;

        context.fill(x, y - 5, x + cardWidth, y + 20, 0xD52B1018);
        context.fill(x, y - 5, x + cardWidth, y - 3, 0xFFFF526B);
        context.drawText(client.textRenderer, message,
                (screenWidth - textWidth) / 2, y, 0xFFFFDCE2, true);

        int barLeft = x + 6;
        int barRight = x + cardWidth - 6;
        int barWidth = Math.max(0, barRight - barLeft);
        context.fill(barLeft, y + 16, barRight, y + 18, 0xFF63313D);
        int progressWidth = Math.round(barWidth * Math.min(1.0F, remaining / (float) COMBAT_WINDOW_MS));
        if (progressWidth > 0) {
            context.fill(barLeft, y + 16, barLeft + progressWidth, y + 18, 0xFFFF7184);
        }
    }
}
