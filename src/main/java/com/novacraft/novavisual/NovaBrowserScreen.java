package com.novacraft.novavisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class NovaBrowserScreen extends Screen {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(10)).build();
    private final Deque<String> history = new ArrayDeque<>();
    private final List<String> lines = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private TextFieldWidget address;
    private String url = "https://example.com", title = "NovaVisual Internet", status = "Enter a website address";
    private boolean loading;
    private int scroll;

    private record Link(String label, String url) {}

    public NovaBrowserScreen() { super(Text.literal("NovaVisual Internet")); }

    @Override protected void init() {
        int x = Math.max(12, width / 2 - 220);
        address = new TextFieldWidget(textRenderer, x, 18, Math.max(100, width - 2*x - 65), 20, Text.literal("Address"));
        address.setMaxLength(2048);
        address.setText(url);
        addDrawableChild(address);
        addDrawableChild(ButtonWidget.builder(Text.literal("Go"), b -> navigate(address.getText(), true))
                .dimensions(width - x - 58, 18, 58, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> back()).dimensions(x, 44, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), b -> load(url)).dimensions(x+66, 44, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Home"), b -> navigate("https://example.com", true)).dimensions(x+144, 44, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close()).dimensions(width-x-60, 44, 60, 20).build());
    }

    private void navigate(String input, boolean remember) {
        String next = input == null ? "" : input.trim();
        if (next.isEmpty()) return;
        if (!next.matches("(?i)^https?://.*")) next = "https://" + next;
        try {
            URI u = URI.create(next);
            if (u.getHost() == null || !Set.of("http", "https").contains(u.getScheme().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException();
        } catch (Exception e) { status = "Enter a valid HTTP or HTTPS address"; return; }
        if (remember && !url.equals(next)) history.push(url);
        url = next;
        if (address != null) address.setText(url);
        scroll = 0;
        load(url);
    }

    private void back() {
        if (!history.isEmpty()) {
            url = history.pop();
            if (address != null) address.setText(url);
            scroll = 0;
            load(url);
        }
    }

    private void load(String target) {
        if (loading) return;
        loading = true;
        status = "Loading " + target;
        lines.clear(); links.clear();
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(target)).timeout(Duration.ofSeconds(20))
                        .header("User-Agent", "NovaVisual/1.2.0").GET().build();
                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() >= 400) throw new IllegalStateException("HTTP " + res.statusCode());
                var doc = Jsoup.parse(res.body(), res.uri().toString());
                String body = doc.body() == null ? "" : doc.body().text();
                List<Link> found = new ArrayList<>();
                for (Element a : doc.select("a[href]")) {
                    String href = a.absUrl("href"), label = a.text().trim();
                    if (!href.isBlank() && !label.isBlank() && href.matches("(?i)^https?://.*") && found.size() < 40)
                        found.add(new Link(label, href));
                }
                return new Page(doc.title().isBlank() ? res.uri().getHost() : doc.title(), body, found,
                        "HTTP " + res.statusCode() + " · " + res.uri());
            } catch (Exception e) { throw new RuntimeException(e.getMessage() == null ? "Network error" : e.getMessage(), e); }
        }).whenComplete((page, error) -> MinecraftClient.getInstance().execute(() -> {
            loading = false;
            if (error != null) {
                Throwable c = error; while (c.getCause() != null) c = c.getCause();
                title = "Page unavailable"; status = "Could not load: " + c.getMessage();
                lines.clear(); lines.add("The page could not be loaded.");
                lines.add("Check your connection and website address.");
                lines.add("Some websites block lightweight browser requests.");
            } else {
                title = page.title; status = page.status; links.addAll(page.links);
                String text = page.body.replaceAll("\\s+", " ").trim();
                int max = Math.max(32, width - 64);
                while (!text.isEmpty()) {
                    int end = Math.min(max, text.length());
                    if (end < text.length() && text.lastIndexOf(' ', end) > 0) end = text.lastIndexOf(' ', end);
                    lines.add(text.substring(0, end)); text = text.substring(end).stripLeading();
                }
                if (lines.isEmpty()) lines.add("No readable text on this page.");
            }
        }));
    }

    private record Page(String title, String body, List<Link> links, String status) {}

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        int l=12, r=width-12, t=76, b=height-28;
        ctx.fill(l,t,r,b,0xEF101B2A); ctx.fill(l,t,r,t+3,NovaVisualSettings.accent);
        ctx.drawText(textRenderer,title,l+12,t+12,0xFFFFFFFF,false);
        ctx.drawText(textRenderer,status,l+12,t+28,0xFF9CB2C8,false);
        int y=t+48-scroll;
        for (String line: lines) {
            for (var wrapped:textRenderer.wrapLines(Text.literal(line),r-l-24)) {
                if (y>=t+44 && y<b-25) ctx.drawText(textRenderer,wrapped,l+12,y,0xFFE4EDF7,false);
                y+=11;
            }
        }
        int linkY=b-16, x=l+12;
        ctx.drawText(textRenderer,"Links:",x,linkY,0xFF39BDF8,false); x+=42;
        for (int i=0;i<Math.min(4,links.size());i++) {
            String label=textRenderer.trimToWidth(links.get(i).label,100);
            ctx.drawText(textRenderer,label,x,linkY,0xFF39BDF8,false); x+=textRenderer.getWidth(label)+12;
            if(x>r-100) break;
        }
        super.render(ctx,mx,my,delta);
    }

    @Override public boolean mouseClicked(double mx,double my,int button) {
        int y=height-44, x=54;
        if(button==0 && my>=y-3 && my<=y+12) {
            for(int i=0;i<Math.min(4,links.size());i++) {
                String label=textRenderer.trimToWidth(links.get(i).label,100);
                int w=textRenderer.getWidth(label);
                if(mx>=x && mx<=x+w) { navigate(links.get(i).url,true); return true; }
                x+=w+12; if(x>width-110) break;
            }
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) {
        scroll=Math.max(0,Math.min(Math.max(0,lines.size()*11-(height-150)),scroll-(int)(vertical*24)));
        return true;
    }
    @Override public void close() { if(client!=null) client.setScreen(new NovaVisualScreen()); }
    @Override public boolean shouldPause() { return false; }
}
