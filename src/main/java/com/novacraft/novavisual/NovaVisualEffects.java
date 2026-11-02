package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public final class NovaVisualEffects {
    private static final long DAMAGE_FLASH_MS = 360L;
    private static final int AURA_PARTICLE_COUNT = 10;
    private static final long CPS_WINDOW_MS = 1000L;
    private static final int GRAPH_SAMPLES = 32;
    private static long damageFlashUntil;
    private static float previousHealth = -1.0F;
    private static int ticks;
    private static boolean leftMouseWasDown;
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Integer> fpsSamples = new ArrayDeque<>();

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
            leftMouseWasDown = false;
            leftClicks.clear();
            fpsSamples.clear();
            return;
        }

        float health = client.player.getHealth();
        if (NovaVisualSettings.damageFlash && previousHealth >= 0.0F && health < previousHealth) {
            damageFlashUntil = System.currentTimeMillis() + DAMAGE_FLASH_MS;
        }
        previousHealth = health;

        long now = System.currentTimeMillis();
        boolean leftMouseDown = client.currentScreen == null
                && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        if (leftMouseDown && !leftMouseWasDown) {
            leftClicks.addLast(now);
        }
        leftMouseWasDown = leftMouseDown;
        pruneClicks(now);

        ticks++;
        if (ticks % 4 == 0) {
            fpsSamples.addLast(Math.max(0, client.getCurrentFps()));
            while (fpsSamples.size() > GRAPH_SAMPLES) fpsSamples.removeFirst();
        }

        if (NovaVisualSettings.particles && ticks % 6 == 0) {
            double angle = ticks * 0.19;
            double x = client.player.getX() + Math.cos(angle) * 0.7;
            double y = client.player.getY() + 0.35 + (ticks % 12) * 0.035;
            double z = client.player.getZ() + Math.sin(angle) * 0.7;
            client.particleManager.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.012, 0.0);
        }

        if (NovaVisualSettings.rgbAura && ticks % 5 == 0) spawnRgbAura(client);

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

    private static void pruneClicks(long now) {
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > CPS_WINDOW_MS) {
            leftClicks.removeFirst();
        }
    }

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
            double y = client.player.getY() + 0.35 + Math.sin(angle * 2.0 + rotation) * 0.16;
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

        if (client.options.hudHidden || client.currentScreen != null) return;
        renderCombatWidgets(context, client);
        if (!NovaVisualSettings.compactHud) return;

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
            String ping = "—";
            if (client.getNetworkHandler() != null) {
                var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (entry != null) ping = entry.getLatency() + " ms";
            }
            lines.add(dimension + "    " + client.getCurrentFps() + " FPS    "
                    + ping + " PING    " + leftClicks.size() + " CPS");
            colors.add(0xFFB6CBE0);
        }
        if (lines.isEmpty()) return;

        int textWidth = 0;
        for (String line : lines) textWidth = Math.max(textWidth, client.textRenderer.getWidth(line));
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        textWidth = Math.min(textWidth, Math.max(1, screenWidth - 34));

        int panelWidth = textWidth + 18;
        int graphHeight = NovaVisualSettings.hudStatus ? 24 : 0;
        int panelHeight = 12 + lines.size() * 13 + graphHeight;
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

        if (graphHeight > 0) {
            int graphTop = panelY + 12 + lines.size() * 13 + 3;
            int graphBottom = panelY + panelHeight - 4;
            int graphWidth = panelWidth - 16;
            int sampleCount = fpsSamples.size();
            if (sampleCount > 0) {
                int index = 0;
                for (int fps : fpsSamples) {
                    int barX = panelX + 8 + index * graphWidth / GRAPH_SAMPLES;
                    int nextX = panelX + 8 + (index + 1) * graphWidth / GRAPH_SAMPLES;
                    int barHeight = Math.max(1, Math.min(graphBottom - graphTop,
                            fps * (graphBottom - graphTop) / 240));
                    context.fill(barX, graphBottom - barHeight, Math.max(barX + 1, nextX - 1),
                            graphBottom, NovaVisualSettings.accent);
                    index++;
                }
            }
        }
    }

    private static void renderCombatWidgets(DrawContext context, MinecraftClient client) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        if (NovaVisualSettings.keystrokesHud) {
            int x = 10;
            int y = height - 76;
            drawKey(context, client, x + 25, y, "W", client.options.forwardKey.isPressed());
            drawKey(context, client, x, y + 25, "A", client.options.leftKey.isPressed());
            drawKey(context, client, x + 25, y + 25, "S", client.options.backKey.isPressed());
            drawKey(context, client, x + 50, y + 25, "D", client.options.rightKey.isPressed());
            drawKey(context, client, x, y + 50, "LMB", GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);
            drawKey(context, client, x + 50, y + 50, "RMB", GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);
        }
        if (NovaVisualSettings.totemCounter) {
            int count = client.player.getInventory().count(Items.TOTEM_OF_UNDYING);
            String label = "TOTEMS  " + count;
            int x = width - client.textRenderer.getWidth(label) - 18;
            int y = height - 22;
            context.fill(x - 7, y - 5, width - 8, y + 13, 0xB9121D2D);
            context.fill(x - 7, y - 5, width - 8, y - 3, NovaVisualSettings.accent);
            context.drawText(client.textRenderer, label, x, y, 0xFFEAF6FF, false);
        }
        if (NovaVisualSettings.targetHud && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target && target.isAlive()) {
            String name = target.getDisplayName().getString();
            String health = String.format(Locale.ROOT, "%.1f / %.1f HP", target.getHealth(), target.getMaxHealth());
            int panelWidth = Math.max(150, Math.max(client.textRenderer.getWidth(name), client.textRenderer.getWidth(health)) + 20);
            int x = (width - panelWidth) / 2;
            int y = 12;
            context.fill(x, y, x + panelWidth, y + 39, 0xC9121D2D);
            context.fill(x, y, x + panelWidth, y + 2, NovaVisualSettings.accent);
            context.drawText(client.textRenderer, client.textRenderer.trimToWidth(name, panelWidth - 12), x + 7, y + 7, 0xFFFFFFFF, false);
            context.drawText(client.textRenderer, health, x + 7, y + 20, 0xFFB6CBE0, false);
            int barLeft = x + 7;
            int barRight = x + panelWidth - 7;
            context.fill(barLeft, y + 33, barRight, y + 36, 0xFF35465A);
            float ratio = target.getMaxHealth() <= 0 ? 0 : Math.max(0, Math.min(1, target.getHealth() / target.getMaxHealth()));
            context.fill(barLeft, y + 33, barLeft + (int)((barRight - barLeft) * ratio), y + 36, 0xFF55D98B);
        }
    }

    private static void drawKey(DrawContext context, MinecraftClient client, int x, int y, String label, boolean pressed) {
        int boxWidth = label.length() > 1 ? 24 : 22;
        context.fill(x, y, x + boxWidth, y + 20, pressed ? 0xE539BDF8 : 0xB9121D2D);
        context.fill(x, y, x + boxWidth, y + 2, pressed ? 0xFFFFFFFF : NovaVisualSettings.accent);
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label, x + (boxWidth - textWidth) / 2, y + 6, 0xFFFFFFFF, false);
    }

}
