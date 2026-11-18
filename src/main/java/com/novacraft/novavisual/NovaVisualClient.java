package com.novacraft.novavisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NovaVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "novavisual";
    private static KeyBinding openMenuKey;
    private static boolean fullbrightWasApplied = false;
    private static final Map<Entity, Boolean> highlightedEntities = new HashMap<>();

    @Override
    public void onInitializeClient() {
        NovaVisualSettings.load();
        NovaWaypointStore.load();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            NovaVisualSettings.save();
            NovaWaypointStore.save();
        });

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novavisual.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        NovaEventMarker.register();
        NovaCombatWarning.register();
        NovaHitMarker.register();
        NovaVisualEffects.register();
        NovaWaypointHud.register();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, helper, context) -> {
                    if (entityType == EntityType.PLAYER
                            && entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
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
                if (!NovaVisualSettings.fullbrightGammaCaptured) {
                    NovaVisualSettings.fullbrightRestoreGamma = Math.max(0.0,
                            Math.min(1.0, client.options.getGamma().getValue()));
                    NovaVisualSettings.fullbrightGammaCaptured = true;
                    NovaVisualSettings.save();
                }
                fullbrightWasApplied = true;
            }
            client.options.getGamma().setValue(1.0);
        } else if (fullbrightWasApplied || NovaVisualSettings.fullbrightGammaCaptured) {
            client.options.getGamma().setValue(NovaVisualSettings.fullbrightRestoreGamma);
            fullbrightWasApplied = false;
            NovaVisualSettings.fullbrightGammaCaptured = false;
            NovaVisualSettings.save();
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
                highlightedEntities.putIfAbsent(entity, entity.isGlowing());
                entity.setGlowing(true);
                currentlyHighlighted.add(entity);
            }
        }

        var iterator = highlightedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Boolean> previous = iterator.next();
            if (!currentlyHighlighted.contains(previous.getKey())) {
                previous.getKey().setGlowing(previous.getValue());
                iterator.remove();
            }
        }
    }

    private static void clearEnemyHighlights() {
        for (Map.Entry<Entity, Boolean> previous : highlightedEntities.entrySet()) {
            previous.getKey().setGlowing(previous.getValue());
        }
        highlightedEntities.clear();
    }
}
