package com.novacraft.novavisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
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
    private final long openedAt = System.currentTimeMillis();

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
        float entrance = NovaVisualSettings.animatedMenu ? easeOut(Math.min(1f, (System.currentTimeMillis()-openedAt)/420f)) : 1f;
        int l=12, r=width-12, t=76, b=height-28;
        ctx.fill(l-4,t-4,r+4,b+4,withAlpha(0x304CAEDC,entrance));
        ctx.fill(l,t,r,b,withAlpha(0xB9121D2D,entrance));
        ctx.fill(l+2,t+2,r-2,b-2,withAlpha(0x72283D55,entrance));
        ctx.fill(l+1,t+1,r-1,t+2,withAlpha(0x88FFFFFF,entrance));
        ctx.fill(l+12,t+42,r-12,t+43,0x558BD7F5);
        ctx.drawText(textRenderer,"N O V A   /   S P A T I A L   B R O W S E R",l+12,t+10,0xFFB9D0E5,false);
        ctx.drawText(textRenderer,title,l+12,t+17,0xFFFFFFFF,false);
        ctx.drawText(textRenderer,status,l+12,t+30,0xFF83CFF4,false);
        int y=t+48-scroll;
        for (String line: lines) {
            for (var wrapped:textRenderer.wrapLines(Text.literal(line),r-l-24)) {
                if (y>=t+44 && y<b-25) ctx.drawText(textRenderer,wrapped,l+12,y,0xFFE4EDF7,false);
                y+=11;
            }
        }
        int linkY=b-16, x=l+12;
        ctx.drawText(textRenderer,"LINKS",x,linkY,0xFF83D7FF,false); x+=42;
        for (int i=0;i<Math.min(4,links.size());i++) {
            String label=textRenderer.trimToWidth(links.get(i).label,100);
            ctx.drawText(textRenderer,label,x,linkY,0xFFB8E7FF,false); x+=textRenderer.getWidth(label)+12;
            if(x>r-100) break;
        }
        super.render(ctx,mx,my,delta);
        int bx=Math.max(12,width/2-220);
        glassButton(ctx,width-bx-58,18,58,20,"GO",mx,my);
        glassButton(ctx,bx,44,60,20,"BACK",mx,my);
        glassButton(ctx,bx+66,44,72,20,"REFRESH",mx,my);
        glassButton(ctx,bx+144,44,60,20,"HOME",mx,my);
        glassButton(ctx,width-bx-60,44,60,20,"CLOSE",mx,my);
    }

    private void glassButton(DrawContext ctx,int x,int y,int w,int h,String label,int mx,int my) {
        boolean hover=mx>=x&&mx<=x+w&&my>=y&&my<=y+h;
        int glow=hover?(0x70+(int)(35*(0.5+0.5*Math.sin(System.currentTimeMillis()/150.0)))):0x55;
        ctx.fill(x-2,y-2,x+w+2,y+h+2,hover?(glow<<24)|0x72D8FF:0x554B9CC8);
        ctx.fill(x,y,x+w,y+h,hover?0xE53C5873:0xC51B2B40);
        ctx.fill(x+2,y+1,x+w-2,y+2,0x66FFFFFF);
        ctx.fill(x+1,y+1,x+2,y+h-1,hover?0xFFB9EEFF:0x8878CFF2);
        ctx.drawCenteredText(textRenderer,label,x+w/2,y+6,0xFFFFFFFF);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int y=height-44, x=66;
        if(click.button()==0 && my>=y-3 && my<=y+12) {
            for(int i=0;i<Math.min(4,links.size());i++) {
                String label=textRenderer.trimToWidth(links.get(i).label,100);
                int w=textRenderer.getWidth(label);
                if(mx>=x && mx<=x+w) { navigate(links.get(i).url,true); return true; }
                x+=w+12; if(x>width-110) break;
            }
        }
        return super.mouseClicked(click,doubled);
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) {
        scroll=Math.max(0,Math.min(Math.max(0,lines.size()*11-(height-150)),scroll-(int)(vertical*24)));
        return true;
    }
    private static float easeOut(float t) { float p=1f-t; return 1f-p*p*p; }
    private static int withAlpha(int color,float amount) { return ((int)(((color>>>24)&255)*amount)<<24)|(color&0x00FFFFFF); }
    @Override public void close() { if(client!=null) client.setScreen(new NovaVisualScreen()); }
    @Override public boolean shouldPause() { return false; }
}
