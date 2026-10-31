package com.novacraft.novavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NovaCombatExitScreen extends Screen {
    public NovaCombatExitScreen() {
        super(Text.literal("PvP combat warning"));
    }

    @Override
    protected void init() {
        int buttonWidth = 190;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 + 18;

        addDrawableChild(ButtonWidget.builder(Text.literal("Всё равно выйти"), button -> {
            if (client != null) {
                client.disconnect(new TitleScreen(), false);
            }
        }).dimensions(x, y, buttonWidth, 22).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Остаться"), button -> {
            if (client != null) {
                client.setScreen(new GameMenuScreen(true));
            }
        }).dimensions(x, y + 30, buttonWidth, 22).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int center = width / 2;
        int top = height / 2 - 54;
        context.fill(center - 158, top - 18, center + 158, top + 112, 0xE8172233);
        context.fill(center - 158, top - 18, center + 158, top - 16, 0xFFFF526B);
        context.drawCenteredTextWithShadow(textRenderer, "§cОСТОРОЖНО: ВЫ В PvP", center, top, 0xFFFF667A);
        context.drawCenteredTextWithShadow(textRenderer,
                "Выход может привести к потере вещей или наказанию сервера.",
                center, top + 25, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                "Вы действительно хотите отключиться?",
                center, top + 42, 0xFFB9CBE0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
