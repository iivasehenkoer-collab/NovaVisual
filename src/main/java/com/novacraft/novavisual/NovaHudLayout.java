package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Properties;

/** Persistent, per-widget HUD layout in normalized GUI coordinates. */
public final class NovaHudLayout {
    public enum Widget {
        COMPACT("Compact HUD"), KEYSTROKES("Keystrokes"), ARMOR("Armor durability"),
        EFFECTS("Potion effects"), RESOURCES("Player resources"), WORLD_INFO("World info"),
        COMPASS("Compass"), TOTEMS("Totem counter"), TARGET("PvP target"), WAYPOINT("Waypoint"),
        TOTEM_ALERT("Totem pop alert"), ARMOR_ALERT("Armor warning");

        private final String label;
        Widget(String label) { this.label = label; }
        public String label() { return label; }
    }

    public enum Profile {
        CUSTOM("Custom"), PVP("PvP"), ANARCHY("Anarchy"), SURVIVAL("Survival");
        private final String label;
        Profile(String label) { this.label = label; }
        public String label() { return label; }
    }

    public record Bounds(int x, int y, int width, int height, float scale, float opacity) {}

    private static final class Slot {
        float x = -1.0F;
        float y = -1.0F;
        float scale = 1.0F;
        float opacity = 1.0F;
    }

    private static final EnumMap<Widget, Slot> SLOTS = new EnumMap<>(Widget.class);
    private static Profile activeProfile = Profile.CUSTOM;

    static {
        for (Widget widget : Widget.values()) SLOTS.put(widget, new Slot());
    }

    private NovaHudLayout() {}

    public static Bounds bounds(Widget widget, int defaultX, int defaultY, int width, int height,
                                int screenWidth, int screenHeight) {
        Slot slot = SLOTS.get(widget);
        int scaledWidth = Math.max(1, Math.round(width * slot.scale));
        int scaledHeight = Math.max(1, Math.round(height * slot.scale));
        int maxX = Math.max(0, screenWidth - scaledWidth);
        int maxY = Math.max(0, screenHeight - scaledHeight);
        int x = slot.x < 0.0F ? clamp(defaultX, 0, maxX) : Math.round(slot.x * maxX);
        int y = slot.y < 0.0F ? clamp(defaultY, 0, maxY) : Math.round(slot.y * maxY);
        return new Bounds(clamp(x, 0, maxX), clamp(y, 0, maxY),
                scaledWidth, scaledHeight, slot.scale, slot.opacity);
    }

    public static Transform begin(DrawContext context, Widget widget, int defaultX, int defaultY,
                                  int width, int height, int screenWidth, int screenHeight) {
        Bounds bounds = bounds(widget, defaultX, defaultY, width, height, screenWidth, screenHeight);
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(bounds.x() - defaultX * bounds.scale(),
                bounds.y() - defaultY * bounds.scale());
        matrices.scale(bounds.scale(), bounds.scale());
        return new Transform(matrices);
    }

    public static final class Transform implements AutoCloseable {
        private final org.joml.Matrix3x2fStack matrices;
        private boolean closed;

        private Transform(org.joml.Matrix3x2fStack matrices) { this.matrices = matrices; }

        @Override
        public void close() {
            if (!closed) {
                matrices.popMatrix();
                closed = true;
            }
        }
    }

    public static Bounds previewBounds(Widget widget, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int x;
        int y;
        int itemWidth;
        int itemHeight;
        switch (widget) {
            case COMPACT -> {
                itemWidth = Math.min(210, Math.max(150, w - 16));
                itemHeight = 60;
                int anchor = Math.max(0, Math.min(3, NovaVisualSettings.hudAnchor));
                x = (anchor == 1 || anchor == 3) ? w - itemWidth - 8 : 8;
                y = (anchor == 2 || anchor == 3) ? h - itemHeight - 8 : 8;
            }
            case KEYSTROKES -> { x = 10; y = h - 76; itemWidth = 74; itemHeight = 70; }
            case ARMOR -> { x = 10; y = h - 122; itemWidth = 176; itemHeight = 34; }
            case EFFECTS -> { x = 8; y = 58; itemWidth = 176; itemHeight = 122; }
            case RESOURCES -> { x = Math.max(4, w - 188); y = Math.max(4, h - 122); itemWidth = 180; itemHeight = 92; }
            case WORLD_INFO -> { itemWidth = Math.min(300, Math.max(150, w - 290)); x = w / 2 - itemWidth / 2 - 55; y = h - 68; itemHeight = 40; }
            case COMPASS -> { itemWidth = Math.min(260, Math.max(1, w - 16)); x = (w - itemWidth) / 2; y = Math.min(64, Math.max(4, h - 44)); itemHeight = 40; }
            case TOTEMS -> { x = w - 100; y = h - 27; itemWidth = 92; itemHeight = 18; }
            case TARGET -> { itemWidth = Math.min(220, w - 16); x = (w - itemWidth) / 2; y = 8; itemHeight = 48; }
            case WAYPOINT -> { itemWidth = Math.min(292, w - 16); x = (w - itemWidth) / 2; y = Math.max(4, Math.min(108, h - 53)); itemHeight = 49; }
            case TOTEM_ALERT -> { itemWidth = Math.min(214, Math.max(1, w - 16)); x = (w - itemWidth) / 2; y = Math.max(4, Math.min(h - 46, h / 2 + 32)); itemHeight = 42; }
            case ARMOR_ALERT -> { itemWidth = Math.min(230, Math.max(1, w - 16)); x = (w - itemWidth) / 2; y = Math.max(4, Math.min(h - 42, h / 2 + 78)); itemHeight = 38; }
            default -> throw new IllegalStateException("Unknown HUD widget " + widget);
        }
        return bounds(widget, x, y, itemWidth, itemHeight, w, h);
    }

    public static Profile activeProfile() {
        return activeProfile;
    }

    /** Apply a complete ready-made widget and visibility setup. */
    public static void applyProfile(Profile profile) {
        if (profile == null || profile == Profile.CUSTOM) return;
        resetAll();

        NovaVisualSettings.compactHud = true;
        NovaVisualSettings.hudCoordinates = true;
        NovaVisualSettings.hudVitals = true;
        NovaVisualSettings.hudStatus = true;
        NovaVisualSettings.keystrokesHud = false;
        NovaVisualSettings.totemCounter = true;
        NovaVisualSettings.totemPopAlert = true;
        NovaVisualSettings.targetHud = false;
        NovaVisualSettings.targetRangeHud = false;
        NovaVisualSettings.armorDurabilityHud = true;
        NovaVisualSettings.armorDurabilityAlert = true;
        NovaVisualSettings.statusEffectHud = true;
        NovaVisualSettings.resourceBarsHud = false;
        NovaVisualSettings.healthVignette = true;
        NovaVisualSettings.worldInfoHud = false;
        NovaVisualSettings.compassHud = false;
        NovaVisualSettings.waypointHud = false;

        switch (profile) {
            case PVP -> {
                NovaVisualSettings.keystrokesHud = true;
                NovaVisualSettings.targetHud = true;
                NovaVisualSettings.targetRangeHud = true;
                setSlot(Widget.COMPACT, 0.02F, 0.02F, 0.90F, 0.95F);
                setSlot(Widget.TARGET, 0.50F, 0.02F, 0.90F, 0.95F);
                setSlot(Widget.EFFECTS, 0.80F, 0.12F, 0.86F, 0.92F);
                setSlot(Widget.KEYSTROKES, 0.02F, 0.68F, 0.92F, 0.95F);
                setSlot(Widget.ARMOR, 0.02F, 0.86F, 0.90F, 0.92F);
                setSlot(Widget.TOTEMS, 0.84F, 0.92F, 0.92F, 1.00F);
                setSlot(Widget.TOTEM_ALERT, 0.50F, 0.46F, 0.95F, 1.00F);
                setSlot(Widget.ARMOR_ALERT, 0.50F, 0.60F, 0.95F, 1.00F);
                NovaVisualSettings.accent = 0xFF39BDF8;
            }
            case ANARCHY -> {
                NovaVisualSettings.keystrokesHud = true;
                NovaVisualSettings.targetHud = true;
                NovaVisualSettings.targetRangeHud = true;
                NovaVisualSettings.resourceBarsHud = true;
                NovaVisualSettings.worldInfoHud = true;
                NovaVisualSettings.compassHud = true;
                NovaVisualSettings.waypointHud = true;
                setSlot(Widget.COMPACT, 0.02F, 0.02F, 0.86F, 0.94F);
                setSlot(Widget.TARGET, 0.50F, 0.02F, 0.90F, 0.94F);
                setSlot(Widget.COMPASS, 0.50F, 0.10F, 0.95F, 0.94F);
                setSlot(Widget.WAYPOINT, 0.50F, 0.21F, 0.90F, 0.94F);
                setSlot(Widget.EFFECTS, 0.80F, 0.17F, 0.86F, 0.92F);
                setSlot(Widget.KEYSTROKES, 0.02F, 0.68F, 0.86F, 0.94F);
                setSlot(Widget.ARMOR, 0.02F, 0.86F, 0.86F, 0.92F);
                setSlot(Widget.RESOURCES, 0.80F, 0.66F, 0.82F, 0.92F);
                setSlot(Widget.WORLD_INFO, 0.28F, 0.84F, 0.86F, 0.90F);
                setSlot(Widget.TOTEMS, 0.84F, 0.93F, 0.90F, 1.00F);
                setSlot(Widget.TOTEM_ALERT, 0.50F, 0.46F, 0.95F, 1.00F);
                setSlot(Widget.ARMOR_ALERT, 0.50F, 0.60F, 0.95F, 1.00F);
                NovaVisualSettings.accent = 0xFF9B7BFF;
            }
            case SURVIVAL -> {
                NovaVisualSettings.resourceBarsHud = true;
                NovaVisualSettings.worldInfoHud = true;
                NovaVisualSettings.compassHud = true;
                NovaVisualSettings.waypointHud = true;
                setSlot(Widget.COMPACT, 0.02F, 0.02F, 0.90F, 0.95F);
                setSlot(Widget.COMPASS, 0.50F, 0.10F, 0.96F, 0.95F);
                setSlot(Widget.WAYPOINT, 0.50F, 0.21F, 0.90F, 0.95F);
                setSlot(Widget.EFFECTS, 0.80F, 0.12F, 0.86F, 0.92F);
                setSlot(Widget.ARMOR, 0.02F, 0.86F, 0.90F, 0.92F);
                setSlot(Widget.RESOURCES, 0.80F, 0.66F, 0.86F, 0.94F);
                setSlot(Widget.WORLD_INFO, 0.18F, 0.84F, 0.86F, 0.92F);
                setSlot(Widget.TOTEMS, 0.84F, 0.93F, 0.92F, 1.00F);
                setSlot(Widget.TOTEM_ALERT, 0.50F, 0.46F, 0.95F, 1.00F);
                setSlot(Widget.ARMOR_ALERT, 0.50F, 0.60F, 0.95F, 1.00F);
                NovaVisualSettings.accent = 0xFF55D98B;
            }
            case CUSTOM -> { }
        }

        activeProfile = profile;
    }

    /** Manual widget changes switch the active preset indicator to Custom. */
    public static void markCustom() {
        activeProfile = Profile.CUSTOM;
    }

    public static void setSlot(Widget widget, float x, float y, float scale, float opacity) {
        Slot slot = SLOTS.get(widget);
        slot.x = clamp(x, 0.0F, 1.0F);
        slot.y = clamp(y, 0.0F, 1.0F);
        slot.scale = clamp(scale, 0.50F, 2.00F);
        slot.opacity = clamp(opacity, 0.20F, 1.00F);
    }

    public static void move(Widget widget, int x, int y, int width, int height,
                            int screenWidth, int screenHeight) {
        Slot slot = SLOTS.get(widget);
        int scaledWidth = Math.max(1, Math.round(width * slot.scale));
        int scaledHeight = Math.max(1, Math.round(height * slot.scale));
        int maxX = Math.max(0, screenWidth - scaledWidth);
        int maxY = Math.max(0, screenHeight - scaledHeight);
        slot.x = maxX == 0 ? 0.0F : clamp(x, 0, maxX) / (float) maxX;
        slot.y = maxY == 0 ? 0.0F : clamp(y, 0, maxY) / (float) maxY;
        markCustom();
    }

    public static void adjustScale(Widget widget, float amount) {
        Slot slot = SLOTS.get(widget);
        slot.scale = clamp(slot.scale + amount, 0.50F, 2.00F);
        markCustom();
    }

    public static void adjustOpacity(Widget widget, float amount) {
        Slot slot = SLOTS.get(widget);
        slot.opacity = clamp(slot.opacity + amount, 0.20F, 1.00F);
        markCustom();
    }

    public static void reset(Widget widget) {
        Slot slot = SLOTS.get(widget);
        slot.x = -1.0F;
        slot.y = -1.0F;
        slot.scale = 1.0F;
        slot.opacity = 1.0F;
        markCustom();
    }

    public static void resetAll() {
        for (Widget widget : Widget.values()) {
            Slot slot = SLOTS.get(widget);
            slot.x = -1.0F;
            slot.y = -1.0F;
            slot.scale = 1.0F;
            slot.opacity = 1.0F;
        }
        markCustom();
    }

    public static float scale(Widget widget) { return SLOTS.get(widget).scale; }
    public static float opacity(Widget widget) { return SLOTS.get(widget).opacity; }

    /** Applies the configured widget opacity to an ARGB fill or text color. */
    public static int color(Widget widget, int color) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * opacity(widget));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static void load(Properties values) {
        try {
            activeProfile = Profile.valueOf(values.getProperty("hudProfile", "CUSTOM").trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ignored) {
            activeProfile = Profile.CUSTOM;
        }
        for (Widget widget : Widget.values()) {
            Slot slot = SLOTS.get(widget);
            String prefix = "hud." + widget.name().toLowerCase(Locale.ROOT) + ".";
            slot.x = read(values, prefix + "x", -1.0F, -1.0F, 1.0F);
            slot.y = read(values, prefix + "y", -1.0F, -1.0F, 1.0F);
            slot.scale = read(values, prefix + "scale", 1.0F, 0.50F, 2.00F);
            slot.opacity = read(values, prefix + "opacity", 1.0F, 0.20F, 1.00F);
        }
    }

    public static void save(Properties values) {
        values.setProperty("hudProfile", activeProfile.name());
        for (Widget widget : Widget.values()) {
            Slot slot = SLOTS.get(widget);
            String prefix = "hud." + widget.name().toLowerCase(Locale.ROOT) + ".";
            values.setProperty(prefix + "x", Float.toString(slot.x));
            values.setProperty(prefix + "y", Float.toString(slot.y));
            values.setProperty(prefix + "scale", Float.toString(slot.scale));
            values.setProperty(prefix + "opacity", Float.toString(slot.opacity));
        }
    }

    private static float read(Properties values, String key, float fallback, float min, float max) {
        try {
            float value = Float.parseFloat(values.getProperty(key, Float.toString(fallback)).trim());
            return Float.isFinite(value) ? clamp(value, min, max) : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
}
