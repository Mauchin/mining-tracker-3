package net.mauchin.mining_tracker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Collections;
import java.util.Locale;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum DisplayItem {
    TIME, DIARATE, MONEY, DURABILITY, DIATOTAL, BREAKER, HOME;

    /*public Text toText(){
        return new TranslatableText("text.autoconfig.MiningTracker.displayItem" + this.name().toLowerCase(Locale.ROOT));
    }
    private static final DisplayItem[] VALUES = values();
    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfig.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                        entryBuilder.startSelector(
                                        new TranslatableText(i13n),
                                        VALUES,
                                        getUnsafely(field, config, getUnsafely(field, defaults)))
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .setNameProvider(DisplayItem::toText)
                                .build()
                ), DisplayItem.class);
    }*/
}
