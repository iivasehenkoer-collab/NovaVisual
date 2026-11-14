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
    public static double fullbrightRestoreGamma = 0.5;
    public static boolean fullbrightGammaCaptured = false;
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
    public static boolean hudCoordinates = true;
    public static boolean hudVitals = true;
    public static boolean hudStatus = true;
    public static int hudAnchor = 0;
    public static boolean keystrokesHud = true;
    public static boolean totemCounter = true;
    public static boolean totemPopAlert = true;
    public static boolean targetHud = true;
    public static boolean targetRangeHud = true;
    public static boolean armorDurabilityHud = true;
    public static boolean armorDurabilityAlert = true;
    public static boolean statusEffectHud = true;
    public static boolean resourceBarsHud = true;
    public static boolean healthVignette = true;
    public static boolean worldInfoHud = true;
    public static boolean compassHud = true;
    public static boolean waypointHud = true;
    public static boolean waypointSet = false;
    public static int waypointX = 0;
    public static int waypointY = 0;
    public static int waypointZ = 0;
    public static String waypointDimension = "";

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
        fullbrightRestoreGamma = clampGamma(decimal(values, "fullbrightRestoreGamma", fullbrightRestoreGamma));
        fullbrightGammaCaptured = bool(values, "fullbrightGammaCaptured", fullbrightGammaCaptured);
        enemyEsp = bool(values, "enemyEsp", enemyEsp);
        eventAutoMarker = bool(values, "eventAutoMarker", eventAutoMarker);
        combatExitWarning = bool(values, "combatExitWarning", combatExitWarning);
        cosmeticsEnabled = bool(values, "cosmeticsEnabled", cosmeticsEnabled);
        rgbWings = bool(values, "rgbWings", rgbWings);
        rgbAura = bool(values, "rgbAura", rgbAura);
        particleTrail = bool(values, "particleTrail", particleTrail);
        optimizationEnabled = bool(values, "optimizationEnabled", optimizationEnabled);
        hudCoordinates = bool(values, "hudCoordinates", hudCoordinates);
        hudVitals = bool(values, "hudVitals", hudVitals);
        hudStatus = bool(values, "hudStatus", hudStatus);
        hudAnchor = clamp(integer(values, "hudAnchor", hudAnchor), 0, 3);
        keystrokesHud = bool(values, "keystrokesHud", keystrokesHud);
        totemCounter = bool(values, "totemCounter", totemCounter);
        totemPopAlert = bool(values, "totemPopAlert", totemPopAlert);
        targetHud = bool(values, "targetHud", targetHud);
        targetRangeHud = bool(values, "targetRangeHud", targetRangeHud);
        armorDurabilityHud = bool(values, "armorDurabilityHud", armorDurabilityHud);
        armorDurabilityAlert = bool(values, "armorDurabilityAlert", armorDurabilityAlert);
        statusEffectHud = bool(values, "statusEffectHud", statusEffectHud);
        resourceBarsHud = bool(values, "resourceBarsHud", resourceBarsHud);
        healthVignette = bool(values, "healthVignette", healthVignette);
        worldInfoHud = bool(values, "worldInfoHud", worldInfoHud);
        compassHud = bool(values, "compassHud", compassHud);
        waypointHud = bool(values, "waypointHud", waypointHud);
        waypointSet = bool(values, "waypointSet", waypointSet);
        waypointX = integer(values, "waypointX", waypointX);
        waypointY = integer(values, "waypointY", waypointY);
        waypointZ = integer(values, "waypointZ", waypointZ);
        waypointDimension = values.getProperty("waypointDimension", waypointDimension);
        if (waypointDimension.length() > 96) waypointDimension = waypointDimension.substring(0, 96);
        if (waypointDimension.isBlank()) waypointSet = false;
        NovaHudLayout.load(values);
        accent = parseHex(values.getProperty("accent"), accent);
        wingStyle = clamp(integer(values, "wingStyle", wingStyle), 0, 4);
        optimizationPreset = clamp(integer(values, "optimizationPreset", optimizationPreset), 0, 2);
    }

    public static void save() {
        Properties values = new Properties();
        values.setProperty("formatVersion", "13");
        values.setProperty("animatedMenu", Boolean.toString(animatedMenu));
        values.setProperty("compactHud", Boolean.toString(compactHud));
        values.setProperty("damageFlash", Boolean.toString(damageFlash));
        values.setProperty("particles", Boolean.toString(particles));
        values.setProperty("fullbright", Boolean.toString(fullbright));
        values.setProperty("fullbrightRestoreGamma", Double.toString(fullbrightRestoreGamma));
        values.setProperty("fullbrightGammaCaptured", Boolean.toString(fullbrightGammaCaptured));
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
        values.setProperty("hudCoordinates", Boolean.toString(hudCoordinates));
        values.setProperty("hudVitals", Boolean.toString(hudVitals));
        values.setProperty("hudStatus", Boolean.toString(hudStatus));
        values.setProperty("hudAnchor", Integer.toString(hudAnchor));
        values.setProperty("keystrokesHud", Boolean.toString(keystrokesHud));
        values.setProperty("totemCounter", Boolean.toString(totemCounter));
        values.setProperty("totemPopAlert", Boolean.toString(totemPopAlert));
        values.setProperty("targetHud", Boolean.toString(targetHud));
        values.setProperty("targetRangeHud", Boolean.toString(targetRangeHud));
        values.setProperty("armorDurabilityHud", Boolean.toString(armorDurabilityHud));
        values.setProperty("armorDurabilityAlert", Boolean.toString(armorDurabilityAlert));
        values.setProperty("statusEffectHud", Boolean.toString(statusEffectHud));
        values.setProperty("resourceBarsHud", Boolean.toString(resourceBarsHud));
        values.setProperty("healthVignette", Boolean.toString(healthVignette));
        values.setProperty("worldInfoHud", Boolean.toString(worldInfoHud));
        values.setProperty("compassHud", Boolean.toString(compassHud));
        values.setProperty("waypointHud", Boolean.toString(waypointHud));
        values.setProperty("waypointSet", Boolean.toString(waypointSet));
        values.setProperty("waypointX", Integer.toString(waypointX));
        values.setProperty("waypointY", Integer.toString(waypointY));
        values.setProperty("waypointZ", Integer.toString(waypointZ));
        values.setProperty("waypointDimension", waypointDimension);
        NovaHudLayout.save(values);

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

    private static double decimal(Properties values, String key, double fallback) {
        try {
            double parsed = Double.parseDouble(values.getProperty(key, Double.toString(fallback)).trim());
            return Double.isFinite(parsed) ? parsed : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static double clampGamma(double gamma) {
        return Math.max(0.0, Math.min(1.0, gamma));
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
