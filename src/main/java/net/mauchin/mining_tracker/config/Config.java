package net.mauchin.mining_tracker.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.io.CharArrayWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@me.shedaniel.autoconfig.annotation.Config(name = "MiningTracker")
public class Config implements ConfigData {
    public int pricePerDiamondOre = 560;
    public int pricePerDiamondItem = 100;
    public int optimalMoneyPerHour = 500000;
    public int moneyPerHourStep = 25000;
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MoneyDisplayMode moneyDisplayMode = MoneyDisplayMode.K;
    public int breakerCooldownTicks = 2380;
    public int breakerLengthTicks = 440;
    public int setMainHomeCooldown = 10;
    public String mainhome = "w";
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public List<String> subhomes = new ArrayList<>() {{
        add("d");
        add("d1");
        add("d2");
    }};
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public List<DisplayLocation> displays = new ArrayList<>() {{
        add(new DisplayLocation());
    }};

    public static class DisplayLocation {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Alignment alignment = Alignment.CENTER;
        public List<DisplayItems> displayItems = new ArrayList<>() {{
            add(new DisplayItems() {{
                item = DisplayItem.TIME;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.DIARATE;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.MONEY;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.BREAKER;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.DURABILITY;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.DIATOTAL;
            }});
            add(new DisplayItems() {{
                item = DisplayItem.HOME;
            }});
        }};

        public static class DisplayItems {
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public DisplayItem item = DisplayItem.TIME;
        }

        public boolean usePercentage = true;
        public int x = 50;
        public int y = 75;

    }
}

