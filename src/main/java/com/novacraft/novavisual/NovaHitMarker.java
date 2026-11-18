package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows a short crosshair marker only after a locally initiated hit is
 * reflected by client-visible damage and attacker state.
 */
public final class NovaHitMarker {
    private static final long DISPLAY_DURATION_MS = 420L;
    private static final int CONFIRM_WINDOW_TICKS = 10;
    private static final int MAX_PENDING_TARGETS = 6;

    private static final Map<UUID, PendingHit> PENDING_HITS = new LinkedHashMap<>();
    private static Object trackedWorld;
    private static long markerUntil;

    private NovaHitMarker() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (NovaVisualSettings.hitMarker
                    && player == client.player
                    && world == client.world
                    && entity instanceof LivingEntity target
                    && target != player) {
                PENDING_HITS.put(target.getUuid(), new PendingHit(
                        target, target.getHealth(), target.getAbsorptionAmount()));
                while (PENDING_HITS.size() > MAX_PENDING_TARGETS) {
                    Iterator<UUID> oldest = PENDING_HITS.keySet().iterator();
                    oldest.next();
                    oldest.remove();
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(NovaHitMarker::tick);
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }

        if (trackedWorld != client.world) {
            trackedWorld = client.world;
            PENDING_HITS.clear();
            markerUntil = 0L;
            return;
        }

        if (!NovaVisualSettings.hitMarker) {
            PENDING_HITS.clear();
            markerUntil = 0L;
            return;
        }

        PlayerEntity player = client.player;
        Iterator<Map.Entry<UUID, PendingHit>> iterator = PENDING_HITS.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingHit pending = iterator.next().getValue();
            LivingEntity target = pending.target;

            boolean damageChanged = target.getHealth() < pending.health - 0.001F
                    || target.getAbsorptionAmount() < pending.absorption - 0.001F;
            boolean attributedToLocalPlayer = target.getAttacker() == player
                    || target.getAttackingPlayer() == player;

            if (damageChanged && target.hurtTime > 0 && attributedToLocalPlayer) {
                markerUntil = System.currentTimeMillis() + DISPLAY_DURATION_MS;
                iterator.remove();
                continue;
            }

            pending.age++;
            if (pending.age >= CONFIRM_WINDOW_TICKS || target.isRemoved()) {
                iterator.remove();
            }
        }
    }

    private static void reset() {
        trackedWorld = null;
        PENDING_HITS.clear();
        markerUntil = 0L;
    }

    private static void render(DrawContext context) {
        if (!NovaVisualSettings.hitMarker) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options == null
                || client.options.hudHidden || client.currentScreen != null) {
            return;
        }

        long remaining = markerUntil - System.currentTimeMillis();
        if (remaining <= 0L) return;

        float fade = Math.min(1.0F, remaining / (float) DISPLAY_DURATION_MS);
        int alpha = Math.max(0, Math.min(255, Math.round(255.0F * fade)));
        int shadowAlpha = Math.round(alpha * 0.45F);
        int shadow = (shadowAlpha << 24) | 0x00101C24;
        int color = (alpha << 24) | 0x00A8FFD5;

        int centerX = context.getScaledWindowWidth() / 2;
        int centerY = context.getScaledWindowHeight() / 2;
        drawMarker(context, centerX, centerY, shadow, 1, 1);
        drawMarker(context, centerX, centerY, color, 0, 0);
    }

    private static void drawMarker(DrawContext context, int centerX, int centerY,
                                   int color, int offsetX, int offsetY) {
        for (int step = 3; step <= 7; step++) {
            int left = centerX - step + offsetX;
            int right = centerX + step - 1 + offsetX;
            int top = centerY - step + offsetY;
            int bottom = centerY + step - 1 + offsetY;

            context.fill(left, top, left + 2, top + 2, color);
            context.fill(right, top, right + 2, top + 2, color);
            context.fill(left, bottom, left + 2, bottom + 2, color);
            context.fill(right, bottom, right + 2, bottom + 2, color);
        }
    }

    private static final class PendingHit {
        private final LivingEntity target;
        private final float health;
        private final float absorption;
        private int age;

        private PendingHit(LivingEntity target, float health, float absorption) {
            this.target = target;
            this.health = health;
            this.absorption = absorption;
        }
    }
}
