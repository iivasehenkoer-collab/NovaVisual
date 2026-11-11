package com.novacraft.novavisual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/** Persistent client-side collection of named waypoints. */
public final class NovaWaypointStore {
    public static final int MAX_WAYPOINTS = 12;
    private static final String FILE_NAME = "novavisual-waypoints.properties";
    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();
    private static int activeIndex = -1;

    private NovaWaypointStore() {}

    public record Waypoint(String name, int x, int y, int z, String dimension) {}

    public static void load() {
        WAYPOINTS.clear();
        activeIndex = -1;

        Path file = configPath();
        if (Files.isRegularFile(file)) {
            Properties values = new Properties();
            try (InputStream input = Files.newInputStream(file)) {
                values.load(input);
                int count = clamp(parseInt(values, "count", 0), 0, MAX_WAYPOINTS);
                for (int i = 0; i < count; i++) {
                    String prefix = "waypoint." + i + ".";
                    String name = sanitizeName(values.getProperty(prefix + "name", ""));
                    String dimension = sanitizeDimension(values.getProperty(prefix + "dimension", ""));
                    if (name.isBlank() || dimension.isBlank()) continue;

                    WAYPOINTS.add(new Waypoint(
                            name,
                            parseInt(values, prefix + "x", 0),
                            parseInt(values, prefix + "y", 0),
                            parseInt(values, prefix + "z", 0),
                            dimension
                    ));
                }
                if (!WAYPOINTS.isEmpty()) {
                    activeIndex = clamp(parseInt(values, "activeIndex", 0), 0, WAYPOINTS.size() - 1);
                }
            } catch (IOException exception) {
                System.err.println("[NovaVisual] Could not read waypoints: " + exception.getMessage());
            }
        }

        // Migrate the single-waypoint fields written by versions through 1.12.0.
        if (WAYPOINTS.isEmpty() && NovaVisualSettings.waypointSet
                && !NovaVisualSettings.waypointDimension.isBlank()) {
            WAYPOINTS.add(new Waypoint(
                    "Waypoint",
                    NovaVisualSettings.waypointX,
                    NovaVisualSettings.waypointY,
                    NovaVisualSettings.waypointZ,
                    sanitizeDimension(NovaVisualSettings.waypointDimension)
            ));
            activeIndex = 0;
            NovaVisualSettings.waypointSet = false;
            NovaVisualSettings.save();
            save();
        }
    }

    public static List<Waypoint> all() {
        return List.copyOf(WAYPOINTS);
    }

    public static Optional<Waypoint> active() {
        if (activeIndex < 0 || activeIndex >= WAYPOINTS.size()) return Optional.empty();
        return Optional.of(WAYPOINTS.get(activeIndex));
    }

    public static int activeIndex() {
        return activeIndex;
    }

    /** Adds a point or updates the existing point with the same name (case-insensitive). */
    public static boolean saveCurrent(String rawName, int x, int y, int z, String dimension) {
        String name = sanitizeName(rawName);
        String worldKey = sanitizeDimension(dimension);
        if (name.isBlank() || worldKey.isBlank()) return false;

        int existing = findByName(name);
        if (existing >= 0) {
            WAYPOINTS.set(existing, new Waypoint(name, x, y, z, worldKey));
            activeIndex = existing;
            save();
            return true;
        }

        if (WAYPOINTS.size() >= MAX_WAYPOINTS) return false;
        WAYPOINTS.add(new Waypoint(name, x, y, z, worldKey));
        activeIndex = WAYPOINTS.size() - 1;
        save();
        return true;
    }

    /** F8 updates the active point, or creates "Quick" when no point is active. */
    public static String updateActivePosition(int x, int y, int z, String dimension) {
        String worldKey = sanitizeDimension(dimension);
        if (worldKey.isBlank()) return null;

        if (activeIndex < 0 || activeIndex >= WAYPOINTS.size()) {
            return saveCurrent("Quick", x, y, z, worldKey) ? "Quick" : null;
        }

        Waypoint current = WAYPOINTS.get(activeIndex);
        WAYPOINTS.set(activeIndex, new Waypoint(current.name(), x, y, z, worldKey));
        save();
        return current.name();
    }

    public static boolean setActive(int index) {
        if (index < 0 || index >= WAYPOINTS.size()) return false;
        activeIndex = index;
        save();
        return true;
    }

    public static String remove(int index) {
        if (index < 0 || index >= WAYPOINTS.size()) return null;
        String removedName = WAYPOINTS.remove(index).name();

        if (WAYPOINTS.isEmpty()) {
            activeIndex = -1;
        } else if (activeIndex == index) {
            activeIndex = Math.min(index, WAYPOINTS.size() - 1);
        } else if (activeIndex > index) {
            activeIndex--;
        }

        save();
        return removedName;
    }

    public static String removeActive() {
        return remove(activeIndex);
    }

    public static void save() {
        Properties values = new Properties();
        values.setProperty("formatVersion", "1");
        values.setProperty("count", Integer.toString(WAYPOINTS.size()));
        values.setProperty("activeIndex", Integer.toString(activeIndex));

        for (int i = 0; i < WAYPOINTS.size(); i++) {
            Waypoint waypoint = WAYPOINTS.get(i);
            String prefix = "waypoint." + i + ".";
            values.setProperty(prefix + "name", waypoint.name());
            values.setProperty(prefix + "x", Integer.toString(waypoint.x()));
            values.setProperty(prefix + "y", Integer.toString(waypoint.y()));
            values.setProperty(prefix + "z", Integer.toString(waypoint.z()));
            values.setProperty(prefix + "dimension", waypoint.dimension());
        }

        Path file = configPath();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                values.store(output, "NovaVisual saved waypoints");
            }
        } catch (IOException exception) {
            System.err.println("[NovaVisual] Could not save waypoints: " + exception.getMessage());
        }
    }

    private static int findByName(String name) {
        for (int i = 0; i < WAYPOINTS.size(); i++) {
            if (WAYPOINTS.get(i).name().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private static String sanitizeName(String value) {
        if (value == null) return "";
        String stripped = value.strip();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < stripped.length() && result.length() < 24; i++) {
            char character = stripped.charAt(i);
            if (!Character.isISOControl(character)) result.append(character);
        }
        return result.toString().strip();
    }

    private static String sanitizeDimension(String value) {
        if (value == null) return "";
        String dimension = value.strip();
        return dimension.length() > 96 ? dimension.substring(0, 96) : dimension;
    }

    private static int parseInt(Properties values, String key, int fallback) {
        try {
            return Integer.parseInt(values.getProperty(key, Integer.toString(fallback)).trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}