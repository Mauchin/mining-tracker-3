package net.mauchin.mining_tracker;

import me.shedaniel.autoconfig.AutoConfig;
import net.mauchin.mining_tracker.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
    private Tracker tracker;
    private HomeManager homeManager;
    private Config.DisplayLocation selectedDisplayLocation;
    private ButtonWidget addButton = new ButtonWidget(MinecraftClient.getInstance().getWindow().getScaledWidth() /2-50,MinecraftClient.getInstance().getWindow().getScaledHeight() /2-20,100,20,new LiteralText("New Display Object"),null);
    private ButtonWidget configButton = new ButtonWidget(MinecraftClient.getInstance().getWindow().getScaledWidth() /2-50,MinecraftClient.getInstance().getWindow().getScaledHeight() /2,100,20,new LiteralText("Config"),(b) -> showConfigScreen());
    protected SettingsScreen(Text title,Tracker tracker,HomeManager homeManager) {
        super(title);
        this.tracker = tracker;
        this.homeManager = homeManager;
    }
    protected void init(){
        this.addDrawableChild(addButton);
        this.addDrawableChild(configButton);
        for (Config.DisplayLocation displayLocation:AutoConfig.getConfigHolder(Config.class).getConfig().displays){
            this.addDrawableChild(new ButtonWidget((int)this.tracker.getDisplayX(displayLocation,new LiteralText(this.tracker.getDisplayString(displayLocation,homeManager)))-10,
                    (int)this.tracker.getDisplayY(displayLocation)-2,10,10,new LiteralText("+"),(b)->{selectedDisplayLocation = displayLocation;}));
        }
    }
    public void showConfigScreen(){
        MinecraftClient.getInstance().setScreen(AutoConfig.getConfigScreen(Config.class,this).get());
    }
    public void tick(){
        //TODO
    }
    public void renderText(MatrixStack matrices){
        textRenderer.drawWithShadow(matrices,new LiteralText("Use w,a,s,d to move"),0,0,0xffffff);

    }

}
