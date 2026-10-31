package com.novacraft.novavisual;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class NovaModrinthScreen extends Screen {
    private static final String API = "https://api.modrinth.com/v2";
    private static final String GAME_VERSION = "1.21.11";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    private final List<Project> projects = new ArrayList<>();
    private TextFieldWidget search;
    private String status = "Search Modrinth for Fabric mods";
    private boolean busy;
    private int page;

    private record Project(String id, String title, String description, String author, int downloads) {}

    public NovaModrinthScreen() { super(Text.literal("Modrinth")); }

    @Override protected void init() {
        int left = Math.max(12, width / 2 - 210);
        search = new TextFieldWidget(textRenderer, left, 22, Math.max(100, width - 2 * left - 64), 20, Text.literal("Search mods"));
        search.setMaxLength(100);
        addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("Search"), b -> searchMods())
                .dimensions(width - left - 58, 22, 58, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            if (client != null) client.setScreen(new NovaVisualScreen());
        }).dimensions(left, height - 30, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Previous"), b -> {
            if (page > 0 && !busy) { page--; searchMods(); }
        }).dimensions(width / 2 - 78, height - 30, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next"), b -> {
            if (!busy && projects.size() == 6) { page++; searchMods(); }
        }).dimensions(width / 2 + 6, height - 30, 72, 20).build());
        searchMods();
    }

    private void searchMods() {
        if (busy) return;
        String query = search == null ? "" : search.getText().trim();
        busy = true;
        status = "Searching Modrinth...";
        projects.clear();
        String encoded = URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        String facets = URLEncoder.encode("[[\"project_type:mod\"],[\"categories:fabric\"],[\"versions:" + GAME_VERSION + "\"]]", java.nio.charset.StandardCharsets.UTF_8);
        String target = API + "/search?query=" + encoded + "&facets=" + facets + "&limit=6&offset=" + (page * 6) + "&index=relevance";
        request(target).thenApply(body -> {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonArray hits = root.getAsJsonArray("hits");
            List<Project> found = new ArrayList<>();
            for (JsonElement element : hits) {
                JsonObject item = element.getAsJsonObject();
                found.add(new Project(str(item, "project_id"), str(item, "title"),
                        str(item, "description"), str(item, "author"),
                        item.has("downloads") ? item.get("downloads").getAsInt() : 0));
            }
            return found;
        }).whenComplete((found, error) -> MinecraftClient.getInstance().execute(() -> {
            busy = false;
            if (error != null) status = "Search failed: " + rootMessage(error);
            else {
                projects.addAll(found);
                status = found.isEmpty() ? "No compatible Fabric mods found for Minecraft " + GAME_VERSION
                        : "Results for Minecraft " + GAME_VERSION + " · Fabric · " + found.size() + " mods";
                rebuildResults();
            }
        }));
    }

    private void rebuildResults() {
        clearChildren();
        int left = Math.max(12, width / 2 - 210);
        search = new TextFieldWidget(textRenderer, left, 22, Math.max(100, width - 2 * left - 64), 20, Text.literal("Search mods"));
        search.setMaxLength(100);
        addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("Search"), b -> { page = 0; searchMods(); })
                .dimensions(width - left - 58, 22, 58, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            if (client != null) client.setScreen(new NovaVisualScreen());
        }).dimensions(left, height - 30, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Previous"), b -> { if (page > 0 && !busy) { page--; searchMods(); } })
                .dimensions(width / 2 - 78, height - 30, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next"), b -> { if (!busy && projects.size() == 6) { page++; searchMods(); } })
                .dimensions(width / 2 + 6, height - 30, 72, 20).build());
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int y = 76 + i * 42;
            addDrawableChild(ButtonWidget.builder(Text.literal("Install"), b -> install(p))
                    .dimensions(width - left - 70, y + 2, 70, 20).build());
        }
    }

    private void install(Project project) {
        if (busy) return;
        busy = true;
        status = "Finding a compatible version of " + project.title + "...";
        String target = API + "/project/" + project.id + "/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22" + GAME_VERSION + "%22%5D";
        request(target).thenApply(body -> {
            JsonArray versions = JsonParser.parseString(body).getAsJsonArray();
            if (versions.isEmpty()) throw new IllegalStateException("No Fabric version for Minecraft " + GAME_VERSION);
            JsonObject version = versions.get(0).getAsJsonObject();
            JsonArray files = version.getAsJsonArray("files");
            JsonObject selected = null;
            for (JsonElement element : files) {
                JsonObject file = element.getAsJsonObject();
                if (file.has("primary") && file.get("primary").getAsBoolean()) { selected = file; break; }
            }
            if (selected == null && !files.isEmpty()) selected = files.get(0).getAsJsonObject();
            if (selected == null) throw new IllegalStateException("Modrinth did not provide a downloadable file");
            String filename = str(selected, "filename");
            String downloadUrl = str(selected, "url");
            if (!filename.toLowerCase(Locale.ROOT).endsWith(".jar")) throw new IllegalStateException("The selected file is not a JAR");
            URI uri = URI.create(downloadUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || !(uri.getHost().equals("cdn.modrinth.com") || uri.getHost().endsWith(".modrinth.com")))
                throw new IllegalStateException("Untrusted download host");
            return new Download(filename, downloadUrl);
        }).thenCompose(download -> HTTP.sendAsync(HttpRequest.newBuilder(URI.create(download.url))
                .timeout(Duration.ofSeconds(60)).header("User-Agent", "NovaVisual/1.3.0 (Modrinth integration)")
                .GET().build(), HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200) throw new IllegalStateException("Download failed: HTTP " + response.statusCode());
                    if (response.body().length < 4 || response.body()[0] != 'P' || response.body()[1] != 'K')
                        throw new IllegalStateException("Downloaded file is not a valid ZIP/JAR archive");
                    return new Downloaded(download.filename, response.body());
                })).thenAccept(downloaded -> {
            try {
                Path mods = MinecraftClient.getInstance().runDirectory.toPath().resolve("mods").toAbsolutePath().normalize();
                Files.createDirectories(mods);
                String safeName = downloaded.filename.replaceAll("[^A-Za-z0-9._-]", "_");
                Path destination = mods.resolve(safeName).normalize();
                if (!destination.startsWith(mods)) throw new IOException("Invalid file path");
                if (Files.exists(destination)) {
                    String base = safeName.substring(0, safeName.length() - 4);
                    destination = mods.resolve(base + "-modrinth-" + System.currentTimeMillis() + ".jar");
                }
                Files.write(destination, downloaded.bytes);
            } catch (IOException e) { throw new RuntimeException(e); }
        }).whenComplete((ignored, error) -> MinecraftClient.getInstance().execute(() -> {
            busy = false;
            status = error == null ? "Installed " + project.title + " · restart Minecraft to load it"
                    : "Install failed: " + rootMessage(error);
        }));
    }

    private record Download(String filename, String url) {}
    private record Downloaded(String filename, byte[] bytes) {}

    private static CompletableFuture<String> request(String target) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(target)).timeout(Duration.ofSeconds(25))
                .header("User-Agent", "NovaVisual/1.3.0 (https://github.com/iivasehenkoer-collab/NovaVisual)")
                .header("Accept", "application/json").GET().build();
        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) throw new IllegalStateException("Modrinth API HTTP " + response.statusCode());
                    return response.body();
                });
    }

    private static String str(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }
    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "Network error" : current.getMessage();
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        int left = Math.max(12, width / 2 - 210), right = width - left;
        int top = 54, bottom = height - 40;
        ctx.fill(left, top, right, bottom, 0xEF101B2A);
        ctx.fill(left, top, right, top + 3, NovaVisualSettings.accent);
        ctx.drawText(textRenderer, "MODRINTH MOD BROWSER", left + 12, top + 12, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, status, left + 12, top + 29, 0xFF9CB2C8, false);
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int y = 76 + i * 42;
            ctx.fill(left + 8, y - 4, right - 8, y + 35, 0xAA1D2B3D);
            ctx.drawText(textRenderer, textRenderer.trimToWidth(p.title, right - left - 112), left + 16, y, 0xFFFFFFFF, false);
            String detail = p.author + " · " + compact(p.downloads) + " downloads";
            ctx.drawText(textRenderer, textRenderer.trimToWidth(detail, right - left - 112), left + 16, y + 13, 0xFF9CB2C8, false);
        }
        ctx.drawText(textRenderer, "Fabric · Minecraft " + GAME_VERSION + " · Powered by Modrinth", left + 12, bottom - 14, 0xFF39BDF8, false);
        super.render(ctx, mx, my, delta);
    }

    private static String compact(int value) {
        if (value >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0);
        if (value >= 1_000) return String.format(Locale.ROOT, "%.1fK", value / 1_000.0);
        return Integer.toString(value);
    }

    @Override public void close() { if (client != null) client.setScreen(new NovaVisualScreen()); }
    @Override public boolean shouldPause() { return false; }
}
