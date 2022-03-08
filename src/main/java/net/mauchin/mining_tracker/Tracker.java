package net.mauchin.mining_tracker;

import me.shedaniel.autoconfig.AutoConfig;
import net.mauchin.mining_tracker.config.Config;
import net.mauchin.mining_tracker.config.DisplayItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.text.DecimalFormat;
import java.util.Objects;

public class Tracker {
    boolean breaker_enabled = false;
    boolean tick_enabled = false;
    boolean render_enabled = true;
    boolean has_been_reset = true;
    int breaker_time = 0;
    int time_passed_tick = 0;
    int start_dia_ore_count = 0;
    int start_dia_item_count = 0;

    void tick(ClientPlayerEntity player, Config config) {
        this.updateBreakerStatus(this.getEfficiencyLevel(player), config);
        if (tick_enabled) {
            this.time_passed_tick++;
        }
    }

    void startStopResumeCounting(ClientPlayerEntity player) {
        if (!this.tick_enabled && this.has_been_reset) {
            startCounting(player);
        } else if (!this.tick_enabled) {
            resumeCounting(player);
        } else {
            stopCounting(player);
        }
    }

    void resetCounting(ClientPlayerEntity player) {
        this.tick_enabled = false;
        this.start_dia_item_count = 0;
        this.start_dia_ore_count = 0;
        this.time_passed_tick = 0;
        this.has_been_reset = true;
        player.sendMessage(new LiteralText("Mining Tracker: \247eReset"), true);
    }

    void resumeCounting(ClientPlayerEntity player) {
        this.tick_enabled = true;
        player.sendMessage(new LiteralText("Mining Tracker: \247aResumed"), true);
    }

    void stopCounting(ClientPlayerEntity player) {
        this.tick_enabled = false;
        player.sendMessage(new LiteralText("Mining Tracker: \247cStopped"), true);
    }

    void startCounting(ClientPlayerEntity player) {
        this.start_dia_ore_count = getDiamondOreCount(player);
        this.start_dia_item_count = getDiamondItemCount(player);
        this.tick_enabled = true;
        this.has_been_reset = false;
        player.sendMessage(new LiteralText("Mining Tracker: \247aStarted"), true);
    }

    void toggleVisibility(ClientPlayerEntity player) {
        this.render_enabled = !this.render_enabled;
        if (player != null) {
            if (this.render_enabled) {
                player.sendMessage(new LiteralText("Mining Tracker: \247aShown"), true);
            } else {
                player.sendMessage(new LiteralText("Mining Tracker: \247cHidden"), true);
            }
        }
    }

    void render(MinecraftClient client, MatrixStack matrices, HomeManager homeManager, Config config) {
        if (this.render_enabled) {
            for (Config.DisplayLocation displayLocation : config.displays) {
                Text ds = new LiteralText(getDisplayString(displayLocation,homeManager));
                client.textRenderer.draw(matrices, ds, getDisplayX(displayLocation,ds), getDisplayY(displayLocation), 0);
            }
        }
    }

    public float getDisplayX(Config.DisplayLocation displayLocation, Text displayText){
        float x;
        if (displayLocation.usePercentage) {
            x = (float) MinecraftClient.getInstance().getWindow().getScaledWidth() * displayLocation.x / 100;
        }
        else{
            x = (float) displayLocation.x;
        }
        switch (displayLocation.alignment) {
            case CENTER -> {
                x -= (float) MinecraftClient.getInstance().textRenderer.getWidth(displayText) / 2;
            }
            case RIGHT -> {
                x -= (float) MinecraftClient.getInstance().textRenderer.getWidth(displayText);
            }
        }
        return x;
    }

    public float getDisplayY(Config.DisplayLocation displayLocation){
        float y;
        if (displayLocation.usePercentage){
            y = (float) MinecraftClient.getInstance().getWindow().getScaledHeight() * displayLocation.y / 100;
        }
        else{
            y = (float) displayLocation.y;
        }
        return y;
    }

    public String getDisplayString(Config.DisplayLocation displayLocation, HomeManager homeManager){
        StringBuilder s = new StringBuilder();
        MinecraftClient client = MinecraftClient.getInstance();
        Config config = AutoConfig.getConfigHolder(Config.class).getConfig();
        for (Config.DisplayLocation.DisplayItems displayItem : displayLocation.displayItems) {
            switch (displayItem.item) {
                case TIME -> s.append(getTimeString());
                case DIARATE -> s.append(getDiaRateString(client.player, config));
                case MONEY -> s.append(getMoneyString(client.player, config));
                case DURABILITY -> s.append(getPickaxeDurabilityString(client.player));
                case DIATOTAL -> s.append("\247b").append(getDiamondOreCount(client.player) - this.start_dia_ore_count);
                case BREAKER -> s.append(getBreakerString());
                case HOME -> s.append(homeManager.getString(this.breaker_enabled));
            }
            s.append(" ");
        }
        return s.toString();
    }

    /*TODO fix bugs
     *  3:config improve the looks*/
    String getTimeString() {
        String c;
        if (this.tick_enabled) {
            c = "\247a";
        } else {
            c = "\247f";
        }
        return c + (this.time_passed_tick / 72000) + ":" + new DecimalFormat("00").format((this.time_passed_tick % 72000) / 1200) + ":" + new DecimalFormat("00").format((this.time_passed_tick % 1200) / 20);
    }

    String getBreakerString() {
        if (!this.breaker_enabled && this.breaker_time <= 0) {
            return "\247e⚡ READY";
        } else if (this.breaker_enabled) {
            return "\247b\247l⚡ " + (this.breaker_time / 20 + 1) + "s";
        } else {
            return "\247f⚡ " + (this.breaker_time / 20 + 1) + "s";
        }
    }

    String getMarkerColor(boolean ticking) {
        if (ticking) {
            return "\247e";
        }
        return "\247c";
    }

    String getTextHighlightColor() {
        if (this.tick_enabled) {
            return "\247f";
        } else {
            return "\2477";
        }
    }

    String getDiaRateString(ClientPlayerEntity player, Config config) {
        String c;
        return getDiaColor(player, config) +
                new DecimalFormat("0.00").format(getDiaPerMinute(player)) +
                getTextHighlightColor() +
                "/min";
    }

    String getMoneyString(ClientPlayerEntity player, Config config) {
        switch (config.moneyDisplayMode) {
            case NORMAL -> {
                return getDiaColor(player, config) +
                        getValuePerHour(player, config) +
                        getTextHighlightColor() +
                        "/hour";
            }
            case K -> {
                return getDiaColor(player, config) +
                        getValuePerHour(player, config) / 1000 +
                        getTextHighlightColor() +
                        "k/hour";
            }
            case M -> {
                return getDiaColor(player, config) +
                        new DecimalFormat("0.00").format((double) getValuePerHour(player, config) / 1000000) +
                        getTextHighlightColor() +
                        "m/hour";
            }
        }
        return null;
    }

    String getDiaColor(ClientPlayerEntity player, Config config) {
        long value_per_hour = this.getValuePerHour(player, config);
        if (value_per_hour >= config.optimalMoneyPerHour) {
            return "\2479";
        } else if (value_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep) {
            return "\247b";
        } else if (value_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep * 2L) {
            return "\2472";
        } else if (value_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep * 3L) {
            return "\247a";
        } else if (value_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep * 4L) {
            return "\247e";
        } else if (value_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep * 5L) {
            return "\2476";
        } else {
            return "\247c";
        }
    }

    long getValuePerHour(ClientPlayerEntity player, Config config) {
        if (this.time_passed_tick != 0) {
            return ((long) (this.getDiamondOreCount(player) - this.start_dia_ore_count) * config.pricePerDiamondOre + (long) (this.getDiamondItemCount(player) - this.start_dia_item_count) * config.pricePerDiamondItem) * 3600 * 20 / this.time_passed_tick;
        }
        return 0;
    }

    double getDiaPerMinute(ClientPlayerEntity player) {
        if (this.time_passed_tick != 0) {
            return (double) (this.getDiamondOreCount(player) - this.start_dia_ore_count) * 60 * 20 / this.time_passed_tick;
        }
        return 0;
    }

    void updateBreakerStatus(int efficiency_level, Config config) {
        if (efficiency_level >= 10) {
            if (!this.breaker_enabled) {
                this.breaker_time = config.breakerLengthTicks;
            }
            this.breaker_enabled = true;

        } else if (this.breaker_enabled && this.breaker_time <= 0 && efficiency_level != 0) {
            this.breaker_enabled = false;
            this.breaker_time = config.breakerCooldownTicks;
        }
        if (this.breaker_time > 0) {
            this.breaker_time -= 1;
        }
    }

    int getEfficiencyLevel(ClientPlayerEntity player) {
        int efficiency_level = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (player != null && efficiency_level == 0 && player.getInventory().getStack(i).getEnchantments().getCompound(j).getString("id").contains("efficiency")
                        && (player.getInventory().getStack(i).getItem() == Items.NETHERITE_PICKAXE ||
                        player.getInventory().getStack(i).getItem() == Items.DIAMOND_PICKAXE ||
                        player.getInventory().getStack(i).getItem() == Items.IRON_PICKAXE ||
                        player.getInventory().getStack(i).getItem() == Items.STONE_PICKAXE ||
                        player.getInventory().getStack(i).getItem() == Items.GOLDEN_PICKAXE ||
                        player.getInventory().getStack(i).getItem() == Items.WOODEN_PICKAXE)) {
                    efficiency_level = player.getInventory().getStack(i).getEnchantments().getCompound(j).getInt("lvl");
                }
            }
        }
        return efficiency_level;
    }

    int getDiamondOreCount(ClientPlayerEntity player) {
        int dia_count = 0;
        for (int i = 0; i < 36; i++) {
            if (player != null && (player.getInventory().getStack(i).getItem() == Items.DIAMOND_ORE || player.getInventory().getStack(i).getItem() == Items.DEEPSLATE_DIAMOND_ORE)) {
                dia_count += player.getInventory().getStack(i).getCount();
            }
        }
        return dia_count;
    }

    int getDiamondItemCount(ClientPlayerEntity player) {
        int dia_count = 0;
        for (int i = 0; i < 36; i++) {
            if (player != null && player.getInventory().getStack(i).getItem() == Items.DIAMOND) {
                dia_count += player.getInventory().getStack(i).getCount();
            }
        }
        return dia_count;
    }

    String getPickaxeDurabilityString(ClientPlayerEntity player) {
        int pickaxe_durability = 100;
        if (player != null && (player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE ||
                player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE ||
                player.getMainHandStack().getItem() == Items.IRON_PICKAXE ||
                player.getMainHandStack().getItem() == Items.STONE_PICKAXE ||
                player.getMainHandStack().getItem() == Items.GOLDEN_PICKAXE ||
                player.getMainHandStack().getItem() == Items.WOODEN_PICKAXE)) {
            pickaxe_durability = (player.getMainHandStack().getMaxDamage() - player.getMainHandStack().getDamage()) * 100 / player.getMainHandStack().getMaxDamage();
        }
        if (pickaxe_durability <= 10) {
            return "\247c\247l[⛏]" + pickaxe_durability + "%";
        } else if (pickaxe_durability <= 20) {
            return "\247c⛏" + pickaxe_durability + "%";
        } else if (pickaxe_durability <= 35) {
            return "\247e⛏" + pickaxe_durability + "%";
        } else if (pickaxe_durability <= 50) {
            return "\247a⛏" + pickaxe_durability + "%";
        } else {
            return "\247b⛏" + pickaxe_durability + "%";
        }
    }
}

