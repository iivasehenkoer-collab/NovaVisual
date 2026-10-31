package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaVisualScreen extends Screen {
    private static final int PANEL = 0xE8141D2B;
    private static final int CYAN = 0xFF39BDF8;
    private static final int MUTED = 0xFF9CB2C8;
    private final long openedAt = System.currentTimeMillis();

    public NovaVisualScreen() { super(Text.literal("NovaVisual")); }

    @Override protected void init() {
        int center=width/2, top=height/2-132, bw=220, x=center-bw/2;
        addDrawableChild(toggleButton(x,top+38,"Animated menu",()->NovaVisualSettings.animatedMenu,v->NovaVisualSettings.animatedMenu=v));
        addDrawableChild(toggleButton(x,top+62,"Compact HUD",()->NovaVisualSettings.compactHud,v->NovaVisualSettings.compactHud=v));
        addDrawableChild(toggleButton(x,top+86,"Damage flash",()->NovaVisualSettings.damageFlash,v->NovaVisualSettings.damageFlash=v));
        addDrawableChild(toggleButton(x,top+110,"Ambient particles",()->NovaVisualSettings.particles,v->NovaVisualSettings.particles=v));
        addDrawableChild(toggleButton(x,top+134,"Fullbright",()->NovaVisualSettings.fullbright,v->NovaVisualSettings.fullbright=v));
        addDrawableChild(toggleButton(x,top+158,"Enemy ESP",()->NovaVisualSettings.enemyEsp,v->NovaVisualSettings.enemyEsp=v));
        addDrawableChild(toggleButton(x,top+184,"HolyWorld event marker",()->NovaVisualSettings.eventAutoMarker,v->NovaVisualSettings.eventAutoMarker=v));\n        addDrawableChild(toggleButton(x,top+198,"PvP exit warning",()->NovaVisualSettings.combatExitWarning,v->NovaVisualSettings.combatExitWarning=v));
        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: Nova Blue"),b->{
            NovaVisualSettings.accent=NovaVisualSettings.accent==CYAN?0xFF9B7BFF:CYAN;
            b.setMessage(Text.literal(NovaVisualSettings.accent==CYAN?"Accent: Nova Blue":"Accent: Nova Violet"));
        }).dimensions(x,top+210,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Optimization Center"),b->{if(client!=null)client.setScreen(new NovaOptimizationScreen());})
                .dimensions(x,top+236,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cosmetics  /  Skin Studio"),b->{if(client!=null)client.setScreen(new NovaCosmeticsScreen());})
                .dimensions(x,top+260,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Modrinth"),b->{if(client!=null)client.setScreen(new NovaModrinthScreen());})
                .dimensions(x,top+284,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Internet"),b->{if(client!=null)client.setScreen(new NovaBrowserScreen());})
                .dimensions(x,top+332,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,top+332,bw,20).build());
    }

    private ButtonWidget toggleButton(int x,int y,String label,java.util.function.BooleanSupplier getter,java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(toggleText(label,getter.getAsBoolean()),b->{
            boolean next=!getter.getAsBoolean(); setter.accept(next); b.setMessage(toggleText(label,next));
        }).dimensions(x,y,220,20).build();
    }
    private Text toggleText(String label,boolean enabled) {
        return Text.literal(label+"  ").append(Text.literal(enabled?"ON":"OFF"));
    }

    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta) {
        renderBackground(context,mouseX,mouseY,delta);
        float entrance = NovaVisualSettings.animatedMenu ? easeOut(Math.min(1f, (System.currentTimeMillis()-openedAt)/420f)) : 1f;
        int left=width/2-(int)(156*entrance),top=height/2-(int)(150*entrance),right=width/2+(int)(156*entrance),bottom=height/2+(int)(242*entrance);
        // Layered translucent surfaces create a spatial-glass look without requiring shaders.
        context.fill(left-5,top-5,right+5,bottom+5,withAlpha(0x302A9DCE, entrance));
        context.fill(left,top,right,bottom,withAlpha(0xB9101828, entrance));
        context.fill(left+2,top+2,right-2,bottom-2,withAlpha(0x7A26384D, entrance));
        context.fill(left+1,top+1,right-1,top+2,withAlpha(0x88FFFFFF, entrance));
        context.fill(left+1,top+1,left+2,bottom-1,withAlpha(0x5599DFFF, entrance));
        context.fill(left+14,top+48,right-14,top+49,0x5595D9F5);
        context.fill(left+14,top+49,left+15,bottom-18,0x2269C9F5);
        context.drawText(textRenderer,"N O V A C R A F T   /   S P A T I A L   U I",left+18,top+13,MUTED,false);
        context.drawText(textRenderer,"NovaVisual",left+18,top+30,0xFFFFFFFF,false);
        context.drawText(textRenderer,"A calmer, more immersive Minecraft workspace",left+18,top+42,0xFFB6CBE0,false);
        super.render(context,mouseX,mouseY,delta);
        int x=width/2-110, y=height/2-132;
        glassButton(context,x,y+38,220,20,"Animated menu   ·   "+(NovaVisualSettings.animatedMenu?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+62,220,20,"Compact HUD   ·   "+(NovaVisualSettings.compactHud?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+86,220,20,"Damage flash   ·   "+(NovaVisualSettings.damageFlash?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+110,220,20,"Ambient particles   ·   "+(NovaVisualSettings.particles?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+134,220,20,"Fullbright   ·   "+(NovaVisualSettings.fullbright?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+158,220,20,"Enemy ESP   ·   "+(NovaVisualSettings.enemyEsp?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+184,220,20,"HolyWorld event marker   ·   "+(NovaVisualSettings.eventAutoMarker?"ON":"OFF"),mouseX,mouseY);\n        glassButton(context,x,y+198,220,20,"PvP exit warning   ·   "+(NovaVisualSettings.combatExitWarning?"ON":"OFF"),mouseX,mouseY);
        glassButton(context,x,y+210,220,20,"Accent: "+(NovaVisualSettings.accent==CYAN?"Nova Blue":"Nova Violet"),mouseX,mouseY);
        glassButton(context,x,y+236,220,20,"OPTIMIZATION CENTER",mouseX,mouseY);
        glassButton(context,x,y+260,220,20,"COSMETICS  /  SKIN STUDIO",mouseX,mouseY);
        glassButton(context,x,y+284,220,20,"MODRINTH  /  MOD LIBRARY",mouseX,mouseY);
        glassButton(context,x,y+308,220,20,"INTERNET  /  WEB",mouseX,mouseY);
        glassButton(context,x,y+332,220,20,"DONE",mouseX,mouseY);
    }

    private void glassButton(DrawContext ctx,int x,int y,int w,int h,String label,int mx,int my) {
        boolean hover=mx>=x&&mx<=x+w&&my>=y&&my<=y+h;
        ctx.fill(x-1,y-1,x+w+1,y+h+1,hover?0xAA72D8FF:0x554B9CC8);
        ctx.fill(x,y,x+w,y+h,hover?0xE53C5873:0xC51B2B40);
        ctx.fill(x+2,y+1,x+w-2,y+2,0x66FFFFFF);
        ctx.fill(x+1,y+1,x+2,y+h-1,hover?0xFFB9EEFF:0x8878CFF2);
        ctx.drawText(textRenderer,label,x+12,y+6,hover?0xFFFFFFFF:0xFFE4F2FF,false);
    }
    private static float easeOut(float t) { float p=1f-t; return 1f-p*p*p; }
    private static int withAlpha(int color,float amount) { return ((int)(((color>>>24)&255)*amount)<<24)|(color&0x00FFFFFF); }
    @Override public void close(){if(client!=null)client.setScreen(null);}
    @Override public boolean shouldPause(){return false;}
}
