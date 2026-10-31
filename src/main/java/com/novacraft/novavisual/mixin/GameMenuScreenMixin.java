package com.novacraft.novavisual.mixin;

import com.novacraft.novavisual.NovaCombatWarning;
import com.novacraft.novavisual.NovaCombatExitScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin {
    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void novavisual$confirmCombatExit(CallbackInfo ci) {
        if (NovaCombatWarning.isCombatActive()) {
            MinecraftClient.getInstance().setScreen(new NovaCombatExitScreen());
            ci.cancel();
        }
    }
}
