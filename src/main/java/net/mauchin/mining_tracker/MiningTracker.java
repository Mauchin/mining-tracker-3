package net.mauchin.mining_tracker;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.mauchin.mining_tracker.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;


public class MiningTracker implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("mining_tracker");

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, Toml4jConfigSerializer::new);
        Tracker tracker = new Tracker();
        HomeManager homeManager = new HomeManager(AutoConfig.getConfigHolder(Config.class).getConfig());
        SettingsScreen settingsScreen = new SettingsScreen(new LiteralText("aaa"),tracker,homeManager);
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("homemanager").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(context -> {
            homeManager.getHome(StringArgumentType.getString(context, "name")).is_used = true;
            return 1;
        })));
        HudRenderCallback.EVENT.register((matrices, f) -> {
            Config config = AutoConfig.getConfigHolder(Config.class).getConfig();
            tracker.render(MinecraftClient.getInstance(), matrices, homeManager, config);
            if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen.getClass() == SettingsScreen.class){
                settingsScreen.renderText(matrices);

            }

        });
        KeyBinding key_reset = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.reset", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "mining_tracker.category.tracker"));
        KeyBinding key_toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "mining_tracker.category.tracker"));
        KeyBinding key_start = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.start", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "mining_tracker.category.tracker"));
        KeyBinding key_mainhome = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.mainhome", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "mining_tracker.category.tracker"));
        KeyBinding key_home = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.home", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "mining_tracker.category.tracker"));
        KeyBinding key_test = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.test", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "mining_tracker.category.tracker"));
        LOGGER.info("Mining Tracker Loaded!");
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Config config = AutoConfig.getConfigHolder(Config.class).getConfig();
            if (client.player != null) {
                while (key_toggle.wasPressed()) {
                    tracker.toggleVisibility(client.player);
                }
                while (key_reset.wasPressed()) {
                    tracker.resetCounting(client.player);
                }
                while (key_start.wasPressed()) {
                    tracker.startStopResumeCounting(client.player);
                }
                while (key_mainhome.wasPressed() && client.player != null) {
                    homeManager.onSetHomeKeyPressed(client.player, config);
                }
                while (key_home.wasPressed() && client.player != null) {
                    homeManager.onHomeKeyPressed(client.player, tracker.breaker_enabled);
                }
                while (key_test.wasPressed() && client.player != null){
                    client.setScreenAndRender(settingsScreen);
                }
                tracker.tick(client.player, config);
                homeManager.tick(client.player);
            }


        });


    }


}


