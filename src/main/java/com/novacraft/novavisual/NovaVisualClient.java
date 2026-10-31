package com.novacraft.novavisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public final class NovaVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "novavisual";
    private static KeyBinding openMenuKey;
    private static double previousGamma = 1.0;
    private static boolean fullbrightWasApplied = false;
    private static final Set<Entity> highlightedEntities = new HashSet<>();

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novavisual.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        NovaEventMarker.register();
        NovaCombatWarning.register();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, helper, context) -> {
                    if (entityType == EntityType.PLAYER
                            && entityRenderer instanceof PlayerEntityRenderer<?, ?> playerRenderer) {
                        @SuppressWarnings("unchecked")
                        var playerContext = (net.minecraft.client.render.entity.feature.FeatureRendererContext<
                                net.minecraft.client.render.entity.state.PlayerEntityRenderState,
                                net.minecraft.client.render.entity.model.PlayerEntityModel>) playerRenderer;
                        helper.register(new NovaWingsFeature(playerContext));
                    }
                }
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new NovaVisualScreen());
                }
            }

            applyFullbright(client);
            applyEnemyEsp(client);
        });
    }

    private static void applyFullbright(MinecraftClient client) {
        if (client.options == null) return;

        if (NovaVisualSettings.fullbright) {
            if (!fullbrightWasApplied) {
                previousGamma = client.options.getGamma().getValue();
                fullbrightWasApplied = true;
            }
            client.options.getGamma().setValue(16.0);
        } else if (fullbrightWasApplied) {
            client.options.getGamma().setValue(previousGamma);
            fullbrightWasApplied = false;
        }
    }

    private static void applyEnemyEsp(MinecraftClient client) {
        if (!NovaVisualSettings.enemyEsp || client.world == null) {
            clearEnemyHighlights();
            return;
        }

        Set<Entity> currentlyHighlighted = new HashSet<>();
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof HostileEntity && entity.isAlive()) {
                entity.setGlowing(true);
                currentlyHighlighted.add(entity);
            }
        }

        for (Entity previous : highlightedEntities) {
            if (!currentlyHighlighted.contains(previous)) {
                previous.setGlowing(false);
            }
        }

        highlightedEntities.clear();
        highlightedEntities.addAll(currentlyHighlighted);
    }

    private static void clearEnemyHighlights() {
        for (Entity entity : highlightedEntities) {
            entity.setGlowing(false);
        }
        highlightedEntities.clear();
    }
}
