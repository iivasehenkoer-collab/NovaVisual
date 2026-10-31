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
    private String lastQuery = "";
    private boolean busy;
    private int page;
    private final long openedAt = System.currentTimeMillis();

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
        String query = search == null ? lastQuery : search.getText().trim();
        lastQuery = query;
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
        search.setText(lastQuery);
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
        super.render(ctx, mx, my, delta);
        float entrance = NovaVisualSettings.animatedMenu ? easeOut(Math.min(1f, (System.currentTimeMillis()-openedAt)/420f)) : 1f;
        int left = Math.max(12, width / 2 - 210), right = width - left;
        int top = 54, bottom = height - 40;
        ctx.fill(left - 4, top - 4, right + 4, bottom + 4, withAlpha(0x304CAEDC, entrance));
        ctx.fill(left, top, right, bottom, withAlpha(0xB9121D2D, entrance));
        ctx.fill(left + 2, top + 2, right - 2, bottom - 2, withAlpha(0x72283D55, entrance));
        ctx.fill(left + 1, top + 1, right - 1, top + 2, withAlpha(0x88FFFFFF, entrance));
        ctx.fill(left + 12, top + 47, right - 12, top + 48, 0x558BD7F5);
        ctx.drawText(textRenderer, "MODRINTH  /  DISCOVER", left + 14, top + 11, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, "Find your next world-changing mod", left + 14, top + 25, 0xFFB9D0E5, false);
        ctx.drawText(textRenderer, status, left + 14, top + 36, 0xFF83CFF4, false);
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int y = 76 + i * 42;
            ctx.fill(left + 8, y - 4, right - 8, y + 35, 0x554B9CC8);
            ctx.fill(left + 10, y - 2, right - 10, y + 33, 0xAA1B2C40);
            ctx.fill(left + 11, y - 1, right - 11, y, 0x55FFFFFF);
            ctx.drawText(textRenderer, textRenderer.trimToWidth(p.title, right - left - 112), left + 18, y + 1, 0xFFFFFFFF, false);
            String detail = p.author + "  ·  " + compact(p.downloads) + " downloads";
            ctx.drawText(textRenderer, textRenderer.trimToWidth(detail, right - left - 112), left + 18, y + 15, 0xFFB3C8DC, false);
            glassButton(ctx, right - 80, y + 2, 64, 20, "INSTALL", mx, my);
        }
        glassButton(ctx, width - left - 58, 22, 58, 20, "SEARCH", mx, my);
        glassButton(ctx, left, height - 30, 70, 20, "BACK", mx, my);
        glassButton(ctx, width / 2 - 78, height - 30, 72, 20, "PREV", mx, my);
        glassButton(ctx, width / 2 + 6, height - 30, 72, 20, "NEXT", mx, my);
        ctx.drawText(textRenderer, "FABRIC  ·  MINECRAFT " + GAME_VERSION + "  ·  POWERED BY MODRINTH",
                left + 12, bottom - 14, 0xFF83D7FF, false);
    }

    private void glassButton(DrawContext ctx, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int glow = hover ? (0x70 + (int)(35 * (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 150.0)))) : 0x55;
        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, hover ? (glow << 24) | 0x72D8FF : 0x554B9CC8);
        ctx.fill(x, y, x + w, y + h, hover ? 0xE53C5873 : 0xC51B2B40);
        ctx.fill(x + 2, y + 1, x + w - 2, y + 2, 0x66FFFFFF);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, hover ? 0xFFB9EEFF : 0x8878CFF2);
        ctx.drawText(textRenderer, label, x + (w - textRenderer.getWidth(label)) / 2, y + 6, 0xFFFFFFFF, false);
    }

    private static String compact(int value) {
        if (value >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0);
        if (value >= 1_000) return String.format(Locale.ROOT, "%.1fK", value / 1_000.0);
        return Integer.toString(value);
    }

    private static float easeOut(float t) { float p=1f-t; return 1f-p*p*p; }
    private static int withAlpha(int color,float amount) { return ((int)(((color>>>24)&255)*amount)<<24)|(color&0x00FFFFFF); }
    @Override public void close() { if (client != null) client.setScreen(new NovaVisualScreen()); }
    @Override public boolean shouldPause() { return false; }
}
