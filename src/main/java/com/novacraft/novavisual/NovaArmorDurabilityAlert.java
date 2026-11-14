package com.novacraft.novavisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Detects equipped armor crossing the low-durability threshold and renders a short HUD warning. */
public final class NovaArmorDurabilityAlert {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] SLOT_NAMES = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private static final int ALERT_THRESHOLD_PERCENT = 20;
    private static final long ALERT_DURATION_MS = 3200L;
    private static final ItemStack[] previousStacks = new ItemStack[SLOTS.length];
    private static final Item[] previousItems = new Item[SLOTS.length];
    private static final int[] previousDamage = new int[SLOTS.length];
    private static final int[] previousMaxDamage = new int[SLOTS.length];
    private static final boolean[] initialized = new boolean[SLOTS.length];

    private static long alertUntil;
    private static String alertDetail = "";

    private NovaArmorDurabilityAlert() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();
        for (int index = 0; index < SLOTS.length; index++) {
            ItemStack stack = client.player.getEquippedStack(SLOTS[index]);
            if (stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
                previousStacks[index] = null;
                previousItems[index] = null;
                previousDamage[index] = 0;
                previousMaxDamage[index] = 0;
                initialized[index] = false;
                continue;
            }

            Item item = stack.getItem();
            int damage = stack.getDamage();
            int maxDamage = stack.getMaxDamage();
            if (!initialized[index] || previousStacks[index] != stack || previousItems[index] != item
                    || previousMaxDamage[index] != maxDamage || damage < previousDamage[index]) {
                previousStacks[index] = stack;
                previousItems[index] = item;
                previousDamage[index] = damage;
                previousMaxDamage[index] = maxDamage;
                initialized[index] = true;
                continue;
            }

            int previousRemaining = remainingPercent(previousDamage[index], maxDamage);
            int remaining = remainingPercent(damage, maxDamage);
            if (NovaVisualSettings.armorDurabilityAlert
                    && previousRemaining > ALERT_THRESHOLD_PERCENT
                    && remaining <= ALERT_THRESHOLD_PERCENT
                    && damage > previousDamage[index]) {
                alertDetail = SLOT_NAMES[index] + "  ·  " + stack.getName().getString()
                        + "  ·  " + remaining + "% LEFT";
                alertUntil = now + ALERT_DURATION_MS;
            }

            previousDamage[index] = damage;
        }
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (!NovaVisualSettings.armorDurabilityAlert || alertUntil <= 0L
                || client.options.hudHidden || client.currentScreen != null) {
            return;
        }

        long remaining = alertUntil - System.currentTimeMillis();
        if (remaining <= 0L) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        if (screenWidth < 120 || screenHeight < 100) return;

        int panelWidth = Math.min(230, screenWidth - 16);
        int panelHeight = 38;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.max(4, Math.min(screenHeight - panelHeight - 4,
                screenHeight / 2 + 78));
        float fade = Math.min(1.0F, remaining / 550.0F);
        int alpha = Math.max(0, Math.min(224, Math.round(224.0F * fade)));
        int warning = (alpha << 24) | 0x00FFC857;
        int background = (alpha << 24) | 0x00121D2D;
        int titleColor = (alpha << 24) | 0x00FFE7B0;
        int detailColor = (alpha << 24) | 0x00EAF6FF;

        try (var hudTransform = NovaHudLayout.begin(context, NovaHudLayout.Widget.ARMOR_ALERT,
                panelX, panelY, panelWidth, panelHeight, screenWidth, screenHeight)) {
            context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2,
                    panelY + panelHeight + 2,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, warning));
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, background));
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 2,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, warning));

            context.drawText(client.textRenderer, "LOW ARMOR DURABILITY",
                    panelX + 8, panelY + 5,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, titleColor), false);
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(alertDetail, panelWidth - 16),
                    panelX + 8, panelY + 18,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, detailColor), false);

            int barLeft = panelX + 8;
            int barRight = panelX + panelWidth - 8;
            int barY = panelY + 31;
            int barWidth = Math.max(1, barRight - barLeft);
            int filledWidth = Math.round(barWidth * remaining / (float) ALERT_DURATION_MS);
            context.fill(barLeft, barY, barRight, barY + 3,
                    NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT,
                            (alpha << 24) | 0x0035465A));
            if (filledWidth > 0) {
                context.fill(barLeft, barY, barLeft + Math.min(barWidth, filledWidth), barY + 3,
                        NovaHudLayout.color(NovaHudLayout.Widget.ARMOR_ALERT, warning));
            }
        }
    }

    private static int remainingPercent(int damage, int maxDamage) {
        if (maxDamage <= 0) return 0;
        return Math.max(0, Math.min(100,
                Math.round((maxDamage - damage) * 100.0F / maxDamage)));
    }

    private static void reset() {
        for (int index = 0; index < SLOTS.length; index++) {
            previousStacks[index] = null;
            previousItems[index] = null;
            previousDamage[index] = 0;
            previousMaxDamage[index] = 0;
            initialized[index] = false;
        }
        alertUntil = 0L;
        alertDetail = "";
    }
}
