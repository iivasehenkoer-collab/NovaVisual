package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;

import java.util.Locale;

public final class NovaVisualEffects {
    private static final long DAMAGE_FLASH_MS = 360L;
    private static long damageFlashUntil;
    private static float previousHealth = -1.0F;
    private static int ticks;

    private NovaVisualEffects() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(NovaVisualEffects::tick);
        HudElementRegistry.addLast(Identifier.of("novavisual", "compact_hud_and_effects"),
                (context, tickCounter) -> render(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            previousHealth = -1.0F;
            damageFlashUntil = 0L;
            ticks = 0;
            return;
        }

        float health = client.player.getHealth();
        if (NovaVisualSettings.damageFlash && previousHealth >= 0.0F && health < previousHealth) {
            damageFlashUntil = System.currentTimeMillis() + DAMAGE_FLASH_MS;
        }
        previousHealth = health;

        ticks++;
        if (NovaVisualSettings.particles && ticks % 6 == 0) {
            double angle = ticks * 0.19;
            double x = client.player.getX() + Math.cos(angle) * 0.7;
            double y = client.player.getY() + 0.35 + (ticks % 12) * 0.035;
            double z = client.player.getZ() + Math.sin(angle) * 0.7;
            client.world.addParticle(x, y, z, 0.0, 0.012, ParticleTypes.END_ROD);
        }
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        long now = System.currentTimeMillis();
        if (NovaVisualSettings.damageFlash && now < damageFlashUntil) {
            int alpha = Math.max(0, Math.min(76,
                    (int) (76.0F * (damageFlashUntil - now) / DAMAGE_FLASH_MS)));
            int tint = (alpha << 24) | 0x00FF344F;
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();
            context.fill(0, 0, width, 7, tint);
            context.fill(0, height - 7, width, height, tint);
            context.fill(0, 0, 7, height, tint);
            context.fill(width - 7, 0, width, height, tint);
        }

        if (!NovaVisualSettings.compactHud || client.options.hudHidden || client.currentScreen != null) {
            return;
        }

        int x = 8;
        int y = 8;
        String coordinates = "XYZ  " + client.player.getBlockX() + "  "
                + client.player.getBlockY() + "  " + client.player.getBlockZ();
        String vitals = "HP  " + (int) Math.ceil(client.player.getHealth()) + "/"
                + (int) Math.ceil(client.player.getMaxHealth()) + "    ARMOR  " + client.player.getArmor();
        String dimension = client.world.getRegistryKey().getValue().getPath()
                .replace('_', ' ').toUpperCase(Locale.ROOT);
        String status = dimension + "    " + client.getCurrentFps() + " FPS";

        int textWidth = Math.max(client.textRenderer.getWidth(coordinates),
                Math.max(client.textRenderer.getWidth(vitals), client.textRenderer.getWidth(status)));
        int right = x + textWidth + 18;
        context.fill(x - 5, y - 5, right, y + 43, 0xB9121D2D);
        context.fill(x - 5, y - 5, right, y - 3, NovaVisualSettings.accent);
        context.fill(x - 5, y - 5, x - 3, y + 43, NovaVisualSettings.accent);
        context.drawText(client.textRenderer, coordinates, x + 5, y + 1, 0xFFEAF6FF, false);
        context.drawText(client.textRenderer, vitals, x + 5, y + 14, 0xFF83D7FF, false);
        context.drawText(client.textRenderer, status, x + 5, y + 27, 0xFFB6CBE0, false);
    }
}
