package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NovaVisualEffects {
    private static final long DAMAGE_FLASH_MS = 360L;
    private static final int AURA_PARTICLE_COUNT = 10;
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
            client.particleManager.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.012, 0.0);
        }

        if (NovaVisualSettings.rgbAura && ticks % 5 == 0) {
            spawnRgbAura(client);
        }

        if (NovaVisualSettings.particleTrail && ticks % 3 == 0) {
            Vec3d velocity = client.player.getVelocity();
            double horizontalSpeedSquared = velocity.x * velocity.x + velocity.z * velocity.z;
            if (horizontalSpeedSquared > 0.0025) {
                double x = client.player.getX() - velocity.x * 0.65;
                double y = client.player.getY() + 0.15;
                double z = client.player.getZ() - velocity.z * 0.65;
                client.particleManager.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.015, 0.0);
            }
        }
    }

    /**
     * Draws a lightweight, locally rendered RGB dust ring around the player.
     * Ten particles are emitted every five ticks to keep the effect inexpensive.
     */
    private static void spawnRgbAura(MinecraftClient client) {
        float baseHue = (System.currentTimeMillis() % 6000L) / 6000.0F;
        double rotation = ticks * 0.045;
        double radius = 0.78;

        for (int i = 0; i < AURA_PARTICLE_COUNT; i++) {
            double angle = rotation + (Math.PI * 2.0 * i / AURA_PARTICLE_COUNT);
            float hue = (baseHue + (float) i / AURA_PARTICLE_COUNT) % 1.0F;
            int color = Color.HSBtoRGB(hue, 0.82F, 1.0F) & 0x00FFFFFF;
            DustParticleEffect dust = new DustParticleEffect(color, 0.85F);

            double x = client.player.getX() + Math.cos(angle) * radius;
            double y = client.player.getY() + 0.35
                    + Math.sin(angle * 2.0 + rotation) * 0.16;
            double z = client.player.getZ() + Math.sin(angle) * radius;
            client.particleManager.addParticle(dust, x, y, z, 0.0, 0.008, 0.0);
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

        List<String> lines = new ArrayList<>(3);
        List<Integer> colors = new ArrayList<>(3);
        if (NovaVisualSettings.hudCoordinates) {
            lines.add("XYZ  " + client.player.getBlockX() + "  "
                    + client.player.getBlockY() + "  " + client.player.getBlockZ());
            colors.add(0xFFEAF6FF);
        }
        if (NovaVisualSettings.hudVitals) {
            lines.add("HP  " + (int) Math.ceil(client.player.getHealth()) + "/"
                    + (int) Math.ceil(client.player.getMaxHealth()) + "    ARMOR  " + client.player.getArmor());
            colors.add(0xFF83D7FF);
        }
        if (NovaVisualSettings.hudStatus) {
            String dimension = client.world.getRegistryKey().getValue().getPath()
                    .replace('_', ' ').toUpperCase(Locale.ROOT);
            lines.add(dimension + "    " + client.getCurrentFps() + " FPS");
            colors.add(0xFFB6CBE0);
        }
        if (lines.isEmpty()) return;

        int textWidth = 0;
        for (String line : lines) {
            textWidth = Math.max(textWidth, client.textRenderer.getWidth(line));
        }
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        textWidth = Math.min(textWidth, Math.max(1, screenWidth - 34));

        int panelWidth = textWidth + 18;
        int panelHeight = 12 + lines.size() * 13;
        int anchor = Math.max(0, Math.min(3, NovaVisualSettings.hudAnchor));
        boolean right = anchor == 1 || anchor == 3;
        boolean bottom = anchor == 2 || anchor == 3;
        int panelX = right ? screenWidth - panelWidth - 8 : 8;
        int panelY = bottom ? screenHeight - panelHeight - 8 : 8;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xB9121D2D);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaVisualSettings.accent);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, NovaVisualSettings.accent);
        for (int i = 0; i < lines.size(); i++) {
            String line = client.textRenderer.trimToWidth(lines.get(i), textWidth);
            context.drawText(client.textRenderer, line, panelX + 8, panelY + 6 + i * 13,
                    colors.get(i), false);
        }
    }
}
