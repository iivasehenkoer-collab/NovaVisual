package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaVisualScreen extends Screen {
    private static final int PANEL = 0xE8141D2B;
    private static final int CYAN = 0xFF39BDF8;
    private static final int MUTED = 0xFF9CB2C8;

    public NovaVisualScreen() { super(Text.literal("NovaVisual")); }

    @Override protected void init() {
        int center=width/2, top=height/2-132, bw=220, x=center-bw/2;
        addDrawableChild(toggleButton(x,top+38,"Animated menu",()->NovaVisualSettings.animatedMenu,v->NovaVisualSettings.animatedMenu=v));
        addDrawableChild(toggleButton(x,top+62,"Compact HUD",()->NovaVisualSettings.compactHud,v->NovaVisualSettings.compactHud=v));
        addDrawableChild(toggleButton(x,top+86,"Damage flash",()->NovaVisualSettings.damageFlash,v->NovaVisualSettings.damageFlash=v));
        addDrawableChild(toggleButton(x,top+110,"Ambient particles",()->NovaVisualSettings.particles,v->NovaVisualSettings.particles=v));
        addDrawableChild(toggleButton(x,top+134,"Fullbright",()->NovaVisualSettings.fullbright,v->NovaVisualSettings.fullbright=v));
        addDrawableChild(toggleButton(x,top+158,"Enemy ESP",()->NovaVisualSettings.enemyEsp,v->NovaVisualSettings.enemyEsp=v));
        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: Nova Blue"),b->{
            NovaVisualSettings.accent=NovaVisualSettings.accent==CYAN?0xFF9B7BFF:CYAN;
            b.setMessage(Text.literal(NovaVisualSettings.accent==CYAN?"Accent: Nova Blue":"Accent: Nova Violet"));
        }).dimensions(x,top+184,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Internet"),b->{if(client!=null)client.setScreen(new NovaBrowserScreen());})
                .dimensions(x,top+210,bw,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,top+234,bw,20).build());
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
        int left=width/2-132,top=height/2-142,right=width/2+132,bottom=height/2+154;
        context.fill(left,top,right,bottom,PANEL);
        context.fill(left,top,right,top+3,NovaVisualSettings.accent);
        context.fill(left+12,top+30,right-12,top+31,0x5539BDF8);
        context.drawText(textRenderer,"NOVACRAFT / VISUAL ENGINE",left+16,top+11,MUTED,false);
        context.drawText(textRenderer,"NovaVisual",left+16,top+43,0xFFFFFFFF,false);
        context.drawText(textRenderer,"CUSTOMIZE YOUR GAME",left+16,bottom-15,CYAN,false);
        super.render(context,mouseX,mouseY,delta);
    }
    @Override public void close(){if(client!=null)client.setScreen(null);}
    @Override public boolean shouldPause(){return false;}
}
