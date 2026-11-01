package com.novacraft.novavisual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class NovaVisualSettings {
    private static final String FILE_NAME = "novavisual.properties";

    private NovaVisualSettings() {}

    public static boolean animatedMenu = true;
    public static boolean compactHud = true;
    public static boolean damageFlash = true;
    public static boolean particles = false;
    public static boolean fullbright = false;
    public static boolean enemyEsp = false;
    public static boolean eventAutoMarker = true;
    public static boolean combatExitWarning = true;
    public static int accent = 0xFF39BDF8;
    public static boolean cosmeticsEnabled = true;
    public static boolean rgbWings = true;
    public static boolean rgbAura = false;
    public static boolean particleTrail = false;
    public static int wingStyle = 0;
    public static boolean optimizationEnabled = false;
    public static int optimizationPreset = 1;

    public static void load() {
        Path file = configPath();
        if (!Files.isRegularFile(file)) return;

        Properties values = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            values.load(input);
        } catch (IOException exception) {
            System.err.println("[NovaVisual] Could not read settings: " + exception.getMessage());
            return;
        }

        animatedMenu = bool(values, "animatedMenu", animatedMenu);
        compactHud = bool(values, "compactHud", compactHud);
        damageFlash = bool(values, "damageFlash", damageFlash);
        particles = bool(values, "particles", particles);
        fullbright = bool(values, "fullbright", fullbright);
        enemyEsp = bool(values, "enemyEsp", enemyEsp);
        eventAutoMarker = bool(values, "eventAutoMarker", eventAutoMarker);
        combatExitWarning = bool(values, "combatExitWarning", combatExitWarning);
        cosmeticsEnabled = bool(values, "cosmeticsEnabled", cosmeticsEnabled);
        rgbWings = bool(values, "rgbWings", rgbWings);
        rgbAura = bool(values, "rgbAura", rgbAura);
        particleTrail = bool(values, "particleTrail", particleTrail);
        optimizationEnabled = bool(values, "optimizationEnabled", optimizationEnabled);
        accent = parseHex(values.getProperty("accent"), accent);
        wingStyle = clamp(integer(values, "wingStyle", wingStyle), 0, 4);
        optimizationPreset = clamp(integer(values, "optimizationPreset", optimizationPreset), 0, 2);
    }

    public static void save() {
        Properties values = new Properties();
        values.setProperty("formatVersion", "1");
        values.setProperty("animatedMenu", Boolean.toString(animatedMenu));
        values.setProperty("compactHud", Boolean.toString(compactHud));
        values.setProperty("damageFlash", Boolean.toString(damageFlash));
        values.setProperty("particles", Boolean.toString(particles));
        values.setProperty("fullbright", Boolean.toString(fullbright));
        values.setProperty("enemyEsp", Boolean.toString(enemyEsp));
        values.setProperty("eventAutoMarker", Boolean.toString(eventAutoMarker));
        values.setProperty("combatExitWarning", Boolean.toString(combatExitWarning));
        values.setProperty("accent", Integer.toHexString(accent));
        values.setProperty("cosmeticsEnabled", Boolean.toString(cosmeticsEnabled));
        values.setProperty("rgbWings", Boolean.toString(rgbWings));
        values.setProperty("rgbAura", Boolean.toString(rgbAura));
        values.setProperty("particleTrail", Boolean.toString(particleTrail));
        values.setProperty("wingStyle", Integer.toString(wingStyle));
        values.setProperty("optimizationEnabled", Boolean.toString(optimizationEnabled));
        values.setProperty("optimizationPreset", Integer.toString(optimizationPreset));

        Path file = configPath();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                values.store(output, "NovaVisual client settings");
            }
        } catch (IOException exception) {
            System.err.println("[NovaVisual] Could not save settings: " + exception.getMessage());
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static boolean bool(Properties values, String key, boolean fallback) {
        return Boolean.parseBoolean(values.getProperty(key, Boolean.toString(fallback)));
    }

    private static int integer(Properties values, String key, int fallback) {
        try {
            return Integer.parseInt(values.getProperty(key, Integer.toString(fallback)).trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int parseHex(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return (int) Long.parseUnsignedLong(value.replace("#", "").trim(), 16);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
