package com.novacraft.novavisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
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
    private static final long TOTEM_ALERT_MS = 2400L;
    private static final long TOTEM_DAMAGE_WINDOW_MS = 900L;
    private static final String[] ROMAN_LEVELS = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final String[] COMPASS_DIRECTIONS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static long damageFlashUntil;
    private static float previousHealth = -1.0F;
    private static int ticks;
    private static boolean leftMouseWasDown;
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Integer> fpsSamples = new ArrayDeque<>();
    private static int previousTotemCount = -1;
    private static int previousPlayerHurtTime;
    private static long recentDamageUntil;
    private static long totemAlertUntil;
    private static int sessionTotemPops;

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
            previousTotemCount = -1;
            previousPlayerHurtTime = 0;
            recentDamageUntil = 0L;
            totemAlertUntil = 0L;
            sessionTotemPops = 0;
            return;
        }

        long now = System.currentTimeMillis();
        float health = client.player.getHealth();
        boolean healthDropped = previousHealth >= 0.0F && health < previousHealth;
        if (healthDropped) {
            if (NovaVisualSettings.damageFlash) {
                damageFlashUntil = now + DAMAGE_FLASH_MS;
            }
            recentDamageUntil = now + TOTEM_DAMAGE_WINDOW_MS;
        }
        previousHealth = health;

        int hurtTime = client.player.hurtTime;
        if (hurtTime > previousPlayerHurtTime) {
            recentDamageUntil = now + TOTEM_DAMAGE_WINDOW_MS;
        }
        previousPlayerHurtTime = hurtTime;

        int totemCount = client.player.getInventory().count(Items.TOTEM_OF_UNDYING);
        if (previousTotemCount < 0) {
            previousTotemCount = totemCount;
        } else {
            if (totemCount < previousTotemCount
                    && now <= recentDamageUntil
                    && health <= 1.05F) {
                sessionTotemPops += previousTotemCount - totemCount;
                if (NovaVisualSettings.totemPopAlert) {
                    totemAlertUntil = now + TOTEM_ALERT_MS;
                }
                recentDamageUntil = 0L;
            }
            previousTotemCount = totemCount;
        }
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

        renderLowHealthVignette(context, client);

        if (client.options.hudHidden || client.currentScreen != null) return;
        renderCombatWidgets(context, client);
        renderTotemPopAlert(context, client);
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

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.COMPACT,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.COMPACT, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.COMPACT, NovaVisualSettings.accent));
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.COMPACT, NovaVisualSettings.accent));
        for (int i = 0; i < lines.size(); i++) {
            String line = client.textRenderer.trimToWidth(lines.get(i), textWidth);
            context.drawText(client.textRenderer, line, panelX + 8, panelY + 6 + i * 13,
                    NovaHudLayout.color(NovaHudLayout.Widget.COMPACT, colors.get(i)), false);
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
                            graphBottom, NovaHudLayout.color(NovaHudLayout.Widget.COMPACT, NovaVisualSettings.accent));
                    index++;
                }
            }
        }
        }

    }

    private static void renderTotemPopAlert(DrawContext context, MinecraftClient client) {
        if (!NovaVisualSettings.totemPopAlert) return;

        long remaining = totemAlertUntil - System.currentTimeMillis();
        if (remaining <= 0L) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        if (screenWidth < 120 || screenHeight < 100) return;

        int panelWidth = Math.min(214, screenWidth - 16);
        int panelHeight = 42;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.max(4, Math.min(screenHeight - panelHeight - 4,
                screenHeight / 2 + 32));
        float fade = Math.min(1.0F, remaining / 450.0F);
        int alpha = Math.max(0, Math.min(220, Math.round(220.0F * fade)));
        int background = (alpha << 24) | 0x00121D2D;
        int accent = (alpha << 24) | 0x00FF526B;
        int titleColor = (alpha << 24) | 0x00FFDCE2;
        int detailColor = (alpha << 24) | 0x00B6CBE0;

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.TOTEM_ALERT,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
            context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                    panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, accent));
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                    NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, background));
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 2,
                    NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, accent));

            String title = "TOTEM POP  ·  SESSION " + sessionTotemPops;
            String detail = "TOTEMS LEFT  "
                    + client.player.getInventory().count(Items.TOTEM_OF_UNDYING);
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(title, panelWidth - 16),
                    panelX + 8, panelY + 6,
                    NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, titleColor), false);
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(detail, panelWidth - 16),
                    panelX + 8, panelY + 19,
                    NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, detailColor), false);

            int barLeft = panelX + 8;
            int barRight = panelX + panelWidth - 8;
            int barY = panelY + 34;
            int barWidth = Math.max(1, barRight - barLeft);
            int filledWidth = Math.round(barWidth * remaining / (float) TOTEM_ALERT_MS);
            context.fill(barLeft, barY, barRight, barY + 3,
                    NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT,
                            (alpha << 24) | 0x0035465A));
            if (filledWidth > 0) {
                context.fill(barLeft, barY, barLeft + Math.min(barWidth, filledWidth), barY + 3,
                        NovaHudLayout.color(NovaHudLayout.Widget.TOTEM_ALERT, accent));
            }
        }
    }

    private static void renderLowHealthVignette(DrawContext context, MinecraftClient client) {
        if (!NovaVisualSettings.healthVignette || client.player == null || client.currentScreen != null) {
            return;
        }

        float maxHealth = client.player.getMaxHealth();
        if (maxHealth <= 0.0F) return;
        float healthRatio = clampRatio(client.player.getHealth() / maxHealth);
        if (healthRatio >= 0.35F) return;

        float severity = clampRatio((0.35F - healthRatio) / 0.35F);
        float pulse = 0.78F + 0.22F * (float) Math.sin(System.currentTimeMillis() * 0.008);
        int maxAlpha = Math.round(132.0F * severity * pulse);
        if (maxAlpha <= 0) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int bandThickness = Math.max(1, Math.min(4, Math.min(width, height) / 160));
        int layers = 12;

        for (int layer = 0; layer < layers; layer++) {
            float fade = (float) (layers - layer) / layers;
            int alpha = Math.round(maxAlpha * fade * fade);
            if (alpha <= 0) continue;

            int color = (alpha << 24) | 0x00E82442;
            int inset = layer * bandThickness;
            int innerWidth = width - inset;
            int innerHeight = height - inset;
            if (innerWidth <= inset || innerHeight <= inset) break;

            int bandEnd = Math.min(inset + bandThickness, innerHeight);
            context.fill(inset, inset, innerWidth, bandEnd, color);
            context.fill(inset, Math.max(inset, height - inset - bandThickness),
                    innerWidth, height - inset, color);
            context.fill(inset, inset, Math.min(inset + bandThickness, innerWidth),
                    innerHeight, color);
            context.fill(Math.max(inset, width - inset - bandThickness), inset,
                    width - inset, innerHeight, color);
        }
    }

    private static void renderCombatWidgets(DrawContext context, MinecraftClient client) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        if (NovaVisualSettings.keystrokesHud) {
            int x = 10;
            int y = height - 76;
            try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.KEYSTROKES,
                    x, y, 74, 70, width, height)) {
            drawKey(context, client, x + 25, y, "W", client.options.forwardKey.isPressed());
            drawKey(context, client, x, y + 25, "A", client.options.leftKey.isPressed());
            drawKey(context, client, x + 25, y + 25, "S", client.options.backKey.isPressed());
            drawKey(context, client, x + 50, y + 25, "D", client.options.rightKey.isPressed());
            drawKey(context, client, x, y + 50, "LMB", GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);
            drawKey(context, client, x + 50, y + 50, "RMB", GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);
            }
        }
        if (NovaVisualSettings.armorDurabilityHud) {
            renderArmorDurability(context, client);
        }
        if (NovaVisualSettings.statusEffectHud) {
            renderStatusEffects(context, client);
        }
        if (NovaVisualSettings.resourceBarsHud) {
            renderResourceBarsHud(context, client);
        }
        if (NovaVisualSettings.worldInfoHud) {
            renderWorldInfoHud(context, client);
        }
        if (NovaVisualSettings.compassHud) {
            renderCompassHud(context, client);
        }

        if (NovaVisualSettings.totemCounter) {
            int count = client.player.getInventory().count(Items.TOTEM_OF_UNDYING);
            String label = "TOTEMS  " + count;
            int x = width - client.textRenderer.getWidth(label) - 18;
            int y = height - 22;
            try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.TOTEMS,
                    x - 7, y - 5, Math.max(28, width - 8 - (x - 7)), 18, width, height)) {
            context.fill(x - 7, y - 5, width - 8, y + 13, NovaHudLayout.color(NovaHudLayout.Widget.TOTEMS, 0xB9121D2D));
            context.fill(x - 7, y - 5, width - 8, y - 3, NovaHudLayout.color(NovaHudLayout.Widget.TOTEMS, NovaVisualSettings.accent));
            context.drawText(client.textRenderer, label, x, y, NovaHudLayout.color(NovaHudLayout.Widget.TOTEMS, 0xFFEAF6FF), false);
            }
        }
        if (NovaVisualSettings.targetHud && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target && target.isAlive()) {
            String name = target.getDisplayName().getString();
            String health = String.format(Locale.ROOT, "%.1f / %.1f HP",
                    target.getHealth(), target.getMaxHealth());
            String range = String.format(Locale.ROOT, "RANGE  %.1f m",
                    target.distanceTo(client.player));

            int contentWidth = Math.max(client.textRenderer.getWidth(name),
                    client.textRenderer.getWidth(health));
            if (NovaVisualSettings.targetRangeHud) {
                contentWidth = Math.max(contentWidth, client.textRenderer.getWidth(range));
            }
            int maxPanelWidth = Math.max(1, width - 16);
            int minPanelWidth = Math.min(150, maxPanelWidth);
            int panelWidth = Math.min(maxPanelWidth,
                    Math.max(minPanelWidth, contentWidth + 20));
            boolean showRange = NovaVisualSettings.targetRangeHud;
            int panelHeight = showRange ? 48 : 39;
            int x = (width - panelWidth) / 2;
            int y = showRange ? 8 : 12;
            int textWidth = Math.max(1, panelWidth - 14);
            try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.TARGET,
                    x, y, panelWidth, panelHeight, width, height)) {
            context.fill(x, y, x + panelWidth, y + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xC9121D2D));
            context.fill(x, y, x + panelWidth, y + 2, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, NovaVisualSettings.accent));
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(name, textWidth),
                    x + 7, y + 7, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xFFFFFFFF), false);
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(health, textWidth),
                    x + 7, y + 20, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xFFB6CBE0), false);

            int barY = showRange ? y + 43 : y + 33;
            if (showRange) {
                context.drawText(client.textRenderer,
                        client.textRenderer.trimToWidth(range, textWidth),
                        x + 7, y + 31, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xFF7DD3FC), false);
            }
            int barLeft = x + 7;
            int barRight = Math.max(barLeft + 1, x + panelWidth - 7);
            context.fill(barLeft, barY, barRight, barY + 3, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xFF35465A));
            float ratio = target.getMaxHealth() <= 0 ? 0
                    : Math.max(0, Math.min(1, target.getHealth() / target.getMaxHealth()));
            context.fill(barLeft, barY, barLeft + (int) ((barRight - barLeft) * ratio),
                    barY + 3, NovaHudLayout.color(NovaHudLayout.Widget.TARGET, 0xFF55D98B));
            }
        }
    }



    private static void renderCompassHud(DrawContext context, MinecraftClient client) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int panelWidth = Math.min(260, Math.max(1, screenWidth - 16));
        int panelHeight = 40;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.min(64, Math.max(4, screenHeight - panelHeight - 4));
        int centerX = screenWidth / 2;

        float yaw = client.player.getYaw();
        float normalizedYaw = (yaw % 360.0F + 360.0F) % 360.0F;
        int headingDegrees = Math.round(normalizedYaw) % 360;
        int nearestDirection = Math.floorMod(Math.round(normalizedYaw / 45.0F), 8);
        String heading = String.format(Locale.ROOT, "HEADING  %03d°  ·  %s",
                headingDegrees, COMPASS_DIRECTIONS[nearestDirection]);

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.COMPASS,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0x5039BDF8));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, NovaVisualSettings.accent));

        float maxVisibleDegrees = Math.min(60.0F, Math.max(0.0F, panelWidth / 2.0F - 10.0F) / 2.0F);
        for (int direction = 0; direction < COMPASS_DIRECTIONS.length; direction++) {
            float markerYaw = direction * 45.0F;
            float difference = (markerYaw - normalizedYaw + 540.0F) % 360.0F - 180.0F;
            if (Math.abs(difference) > maxVisibleDegrees) continue;

            int markerX = centerX + Math.round(difference * 2.0F);
            String label = COMPASS_DIRECTIONS[direction];
            int labelWidth = client.textRenderer.getWidth(label);
            int labelX = markerX - labelWidth / 2;
            int color = Math.abs(difference) < 22.5F ? NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0xFF7DD3FC) : NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0xFFEAF6FF);
            context.drawText(client.textRenderer, label, labelX, panelY + 5, color, false);
        }

        for (int offset = -60; offset <= 60; offset += 15) {
            if (Math.abs(offset) > maxVisibleDegrees) continue;
            int tickX = centerX + Math.round(offset * 2.0F);
            int tickHeight = offset % 45 == 0 ? 8 : 4;
            int tickColor = offset == 0 ? NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, NovaVisualSettings.accent) : NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0xFF60758B);
            context.fill(tickX, panelY + 17, tickX + 1, panelY + 17 + tickHeight, tickColor);
        }

        context.fill(centerX - 1, panelY + 16, centerX + 2, panelY + 27, NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, NovaVisualSettings.accent));
        String trimmedHeading = client.textRenderer.trimToWidth(heading, Math.max(1, panelWidth - 12));
        int headingWidth = client.textRenderer.getWidth(trimmedHeading);
        context.drawText(client.textRenderer, trimmedHeading,
                centerX - headingWidth / 2, panelY + 28, NovaHudLayout.color(NovaHudLayout.Widget.COMPASS, 0xFFB6CBE0), false);
        }

    }

    private static void renderWorldInfoHud(DrawContext context, MinecraftClient client) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        // Reserve a lane between the lower-left key/armor widgets and the lower-right resource panel.
        // Hide this wide information card at very narrow GUI widths rather than drawing over other HUDs.
        if (screenWidth < 400) return;
        int panelWidth = Math.min(300, screenWidth - 290);
        int panelHeight = 40;
        int panelX = screenWidth / 2 - panelWidth / 2 - 55;
        int panelY = Math.max(4, screenHeight - panelHeight - 28);

        var playerPos = client.player.getBlockPos();
        String dimension = client.world.getRegistryKey().getValue().getPath()
                .replace('_', ' ').toUpperCase(Locale.ROOT);
        String biome = client.world.getBiome(playerPos).getKey()
                .map(key -> key.getValue().getPath())
                .orElse("unknown")
                .replace('_', ' ').toUpperCase(Locale.ROOT);
        int playerLight = client.world.getLightLevel(playerPos);
        int chunkX = client.player.getBlockX() >> 4;
        int chunkZ = client.player.getBlockZ() >> 4;

        String environmentLine = "DIM  " + dimension + "  ·  BIOME  " + biome
                + "  ·  LIGHT  " + playerLight + "  ·  CHUNK  " + chunkX + ", " + chunkZ;
        String targetLine = "TARGET  —";
        if (client.crosshairTarget instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            var targetPos = hit.getBlockPos();
            String blockName = client.world.getBlockState(targetPos).getBlock()
                    .getName().getString().toUpperCase(Locale.ROOT);
            int targetLight = client.world.getLightLevel(targetPos);
            if (NovaVisualSettings.targetRangeHud) {
                double distance = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ())
                        .distanceTo(new Vec3d(targetPos.getX() + 0.5,
                                targetPos.getY() + 0.5, targetPos.getZ() + 0.5));
                targetLine = String.format(Locale.ROOT,
                        "TARGET  RANGE  %.1f m  ·  %s  ·  XYZ  %d %d %d  ·  LIGHT  %d",
                        distance, blockName, targetPos.getX(), targetPos.getY(),
                        targetPos.getZ(), targetLight);
            } else {
                targetLine = "TARGET  " + blockName + "  ·  XYZ  "
                        + targetPos.getX() + " " + targetPos.getY() + " " + targetPos.getZ()
                        + "  ·  LIGHT  " + targetLight;
            }
        }

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.WORLD_INFO,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, 0x5039BDF8));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, NovaVisualSettings.accent));
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, NovaVisualSettings.accent));

        int textMaxWidth = Math.max(1, panelWidth - 16);
        context.drawText(client.textRenderer,
                client.textRenderer.trimToWidth(environmentLine, textMaxWidth),
                panelX + 8, panelY + 7, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, 0xFFEAF6FF), false);
        context.drawText(client.textRenderer,
                client.textRenderer.trimToWidth(targetLine, textMaxWidth),
                panelX + 8, panelY + 21, NovaHudLayout.color(NovaHudLayout.Widget.WORLD_INFO, 0xFFB6CBE0), false);
        }

    }

    private static void renderResourceBarsHud(DrawContext context, MinecraftClient client) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int panelWidth = 180;
        int panelHeight = 92;
        int panelX = Math.max(4, screenWidth - panelWidth - 8);
        int panelY = Math.max(4, screenHeight - panelHeight - 30);

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.RESOURCES,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0x5039BDF8));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, NovaVisualSettings.accent));
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, NovaVisualSettings.accent));
        context.drawText(client.textRenderer, "PLAYER STATUS", panelX + 8, panelY + 6,
                NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFF9CB2C8), false);

        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        float healthRatio = maxHealth <= 0.0F ? 0.0F : clampRatio(health / maxHealth);
        int healthColor = healthRatio <= 0.25F ? NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFFF526B)
                : healthRatio <= 0.50F ? NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFFFC857) : NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFF55D98B);
        drawResourceRow(context, client, panelX, panelY + 18, panelWidth,
                "HEALTH", String.format(Locale.ROOT, "%.1f / %.0f", health, maxHealth),
                healthRatio, healthColor);

        int armor = Math.max(0, client.player.getArmor());
        drawResourceRow(context, client, panelX, panelY + 34, panelWidth,
                "ARMOR", armor + " / 20", clampRatio(armor / 20.0F),
                armor <= 4 ? NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFFFC857) : NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFF39BDF8));

        int food = client.player.getHungerManager().getFoodLevel();
        float saturation = client.player.getHungerManager().getSaturationLevel();
        drawResourceRow(context, client, panelX, panelY + 50, panelWidth,
                "FOOD", String.format(Locale.ROOT, "%d / 20  ·  %.1f SAT", food, saturation),
                clampRatio(food / 20.0F), food <= 6 ? NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFFF526B) : NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFFFC857));

        float xpProgress = clampRatio(client.player.experienceProgress);
        String xpValue = String.format(Locale.ROOT, "LV %d  ·  %d%%",
                client.player.experienceLevel, Math.round(xpProgress * 100.0F));
        drawResourceRow(context, client, panelX, panelY + 66, panelWidth,
                "EXPERIENCE", xpValue, xpProgress, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, NovaVisualSettings.accent));
        }

    }

    private static void drawResourceRow(DrawContext context, MinecraftClient client,
                                       int panelX, int rowY, int panelWidth,
                                       String label, String value, float ratio, int barColor) {
        context.drawText(client.textRenderer, label, panelX + 8, rowY + 1,
                NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFF9CB2C8), false);
        String trimmedValue = client.textRenderer.trimToWidth(value, panelWidth - 66);
        int valueWidth = client.textRenderer.getWidth(trimmedValue);
        context.drawText(client.textRenderer, trimmedValue,
                panelX + panelWidth - 8 - valueWidth, rowY + 1, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFFEAF6FF), false);

        int barLeft = panelX + 8;
        int barRight = panelX + panelWidth - 8;
        int barY = rowY + 11;
        int barWidth = barRight - barLeft;
        int filledWidth = Math.round(barWidth * clampRatio(ratio));
        context.fill(barLeft, barY, barRight, barY + 3, NovaHudLayout.color(NovaHudLayout.Widget.RESOURCES, 0xFF35465A));
        if (filledWidth > 0) {
            context.fill(barLeft, barY, barLeft + filledWidth, barY + 3, barColor);
        }
    }

    private static float clampRatio(float ratio) {
        return Math.max(0.0F, Math.min(1.0F, ratio));
    }

    private static void renderStatusEffects(DrawContext context, MinecraftClient client) {
        List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
        if (effects.isEmpty()) return;

        effects.sort((first, second) -> Long.compare(effectSortDuration(first), effectSortDuration(second)));

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int panelWidth = 176;
        int visibleRows = Math.min(8, effects.size());
        int overflow = effects.size() - visibleRows;
        int panelHeight = 22 + visibleRows * 20 + (overflow > 0 ? 14 : 0);
        int anchor = Math.max(0, Math.min(3, NovaVisualSettings.hudAnchor));
        boolean compactHudOnRight = NovaVisualSettings.compactHud && (anchor == 1 || anchor == 3);
        int panelX = compactHudOnRight ? 8 : screenWidth - panelWidth - 8;
        int panelY = 58;

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.EFFECTS,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0x5039BDF8));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, NovaVisualSettings.accent));

        String header = "ACTIVE EFFECTS  ·  " + effects.size();
        context.drawText(client.textRenderer, client.textRenderer.trimToWidth(header, panelWidth - 14),
                panelX + 7, panelY + 7, NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFF9CB2C8), false);

        for (int i = 0; i < visibleRows; i++) {
            StatusEffectInstance instance = effects.get(i);
            int rowY = panelY + 22 + i * 20;
            int rgb = instance.getEffectType().value().getColor();
            int effectColor = 0xFF000000 | (rgb & 0x00FFFFFF);

            context.fill(panelX + 4, rowY + 1, panelX + panelWidth - 4, rowY + 19, 0x321D2B40);
            context.fill(panelX + 7, rowY + 6, panelX + 13, rowY + 13,
                    NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, effectColor));

            String name = instance.getEffectType().value().getName().getString()
                    + " " + romanLevel(instance.getAmplifier());
            String duration = formatEffectDuration(instance);
            int durationWidth = client.textRenderer.getWidth(duration);
            int maxNameWidth = Math.max(12, panelWidth - 36 - durationWidth);
            String trimmedName = client.textRenderer.trimToWidth(name, maxNameWidth);
            int durationX = panelX + panelWidth - 8 - durationWidth;

            context.drawText(client.textRenderer, trimmedName, panelX + 17, rowY + 5,
                    NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFFEAF6FF), false);
            context.drawText(client.textRenderer, duration, durationX, rowY + 5,
                    effectDurationColor(instance), false);
        }

        if (overflow > 0) {
            int moreY = panelY + 22 + visibleRows * 20;
            context.drawText(client.textRenderer, "+" + overflow + " more effects",
                    panelX + 8, moreY + 3, NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFF9CB2C8), false);
        }
        }

    }

    private static long effectSortDuration(StatusEffectInstance instance) {
        return instance.isInfinite() ? Long.MAX_VALUE : Math.max(0L, instance.getDuration());
    }

    private static String formatEffectDuration(StatusEffectInstance instance) {
        if (instance.isInfinite()) return "∞";
        long totalSeconds = (Math.max(0L, (long) instance.getDuration()) + 19L) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + ":" + (seconds < 10L ? "0" : "") + seconds;
    }

    private static int effectDurationColor(StatusEffectInstance instance) {
        if (instance.isInfinite()) return NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFFB6CBE0);
        if (instance.getDuration() <= 100) return NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFFFF526B);
        if (instance.getDuration() <= 200) return NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFFFFC857);
        return NovaHudLayout.color(NovaHudLayout.Widget.EFFECTS, 0xFFB6CBE0);
    }

    private static String romanLevel(int amplifier) {
        int level = Math.max(1, amplifier + 1);
        return level <= ROMAN_LEVELS.length ? ROMAN_LEVELS[level - 1] : Integer.toString(level);
    }

    private static void renderArmorDurability(DrawContext context, MinecraftClient client) {
        ItemStack[] armor = {
                client.player.getEquippedStack(EquipmentSlot.HEAD),
                client.player.getEquippedStack(EquipmentSlot.CHEST),
                client.player.getEquippedStack(EquipmentSlot.LEGS),
                client.player.getEquippedStack(EquipmentSlot.FEET)
        };
        String[] slotLabels = {"H", "C", "L", "B"};
        int panelX = 10;
        int panelY = context.getScaledWindowHeight() - 122;
        int panelWidth = 176;
        int panelHeight = 34;

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.ARMOR,
                panelX, panelY, panelWidth, panelHeight, context.getScaledWindowWidth(), context.getScaledWindowHeight())) {
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                panelY + panelHeight + 2, NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, 0x5039BDF8));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, 0xB9121D2D));
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, NovaVisualSettings.accent));

        for (int i = 0; i < armor.length; i++) {
            ItemStack stack = armor[i];
            int slotX = panelX + 4 + i * 42;
            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX + 10, panelY + 3);
            } else {
                context.drawText(client.textRenderer, slotLabels[i], slotX + 15, panelY + 7,
                        NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, 0xFF8FA6BC), false);
            }

            String durability = stack.isDamageable()
                    ? Integer.toString(Math.max(0, 100 - stack.getDamage() * 100 / stack.getMaxDamage())) + "%"
                    : "—";
            int textWidth = client.textRenderer.getWidth(durability);
            context.drawText(client.textRenderer, durability, slotX + (34 - textWidth) / 2,
                    panelY + 20, stack.isDamageable() ? NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, durabilityColor(stack)) : NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, 0xFF8FA6BC), false);

            if (stack.isDamageable()) {
                int barX = slotX + 1;
                int barY = panelY + 30;
                int barWidth = 34;
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                int filled = (int) Math.round(barWidth * (double) remaining / stack.getMaxDamage());
                context.fill(barX, barY, barX + barWidth, barY + 2, NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, 0xFF35465A));
                context.fill(barX, barY, barX + filled, barY + 2, NovaHudLayout.color(NovaHudLayout.Widget.ARMOR, durabilityColor(stack)));
            }
        }
        }

    }

    private static int durabilityColor(ItemStack stack) {
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) return 0xFF8FA6BC;
        double remaining = (double) (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage();
        if (remaining <= 0.20) return 0xFFFF526B;
        if (remaining <= 0.50) return 0xFFFFC857;
        return 0xFF55D98B;
    }

    private static void drawKey(DrawContext context, MinecraftClient client, int x, int y, String label, boolean pressed) {
        int boxWidth = label.length() > 1 ? 24 : 22;
        context.fill(x, y, x + boxWidth, y + 20, NovaHudLayout.color(NovaHudLayout.Widget.KEYSTROKES, pressed ? 0xE539BDF8 : 0xB9121D2D));
        context.fill(x, y, x + boxWidth, y + 2, NovaHudLayout.color(NovaHudLayout.Widget.KEYSTROKES, pressed ? 0xFFFFFFFF : NovaVisualSettings.accent));
        int textWidth = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, label, x + (boxWidth - textWidth) / 2, y + 6,
                NovaHudLayout.color(NovaHudLayout.Widget.KEYSTROKES, 0xFFFFFFFF), false);
    }

}
