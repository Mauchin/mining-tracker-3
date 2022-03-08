package net.mauchin.mining_tracker;

import net.mauchin.mining_tracker.config.Config;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeManager {
    List<Home> subHomeList = new ArrayList<>();
    Home mainHome;
    int command_delay = -1;
    BlockPos lastSetHomeLocation = new BlockPos(0,0,0);
    public HomeManager(Config config){
        for (String home_name : config.subhomes){
            subHomeList.add(new Home(home_name,false));
        }
        this.mainHome = new Home(config.mainhome,true);
    }
    public static class Home {
        String home_name;
        boolean is_used;
        boolean is_mainhome;

        public Home(String home_name, boolean is_mainhome) {
            this.home_name = home_name;
            this.is_used = false;
            this.is_mainhome = is_mainhome;
        }

        public void runSetHome(ClientPlayerEntity player) {
            this.is_used = true;
            player.sendChatMessage("/sethome " + this.home_name);
            player.sendMessage(new LiteralText("\2476Setting mainhome \247c" + this.home_name + "\2476..."), false);
        }

        public void runHome(ClientPlayerEntity player) {
            player.sendChatMessage("/home " + this.home_name);
            this.is_used = false;
            player.sendMessage(new LiteralText("\2476Teleporting to subhome \247c" + this.home_name + "\2476..."), false);
        }
    }
    public Home getFirstUsedHome(){
        for (Home home:this.subHomeList){
            if (home.is_used && !home.is_mainhome){
                return home;
            }
        }
        return this.mainHome;
    }
    public Home getFirstUnusedHome(){
        for (Home home:this.subHomeList){
            if (!home.is_used && !home.is_mainhome){
                return home;
            }
        }
        return this.mainHome;
    }
    public Home getLastUsedHome(){
        Home usedHome = this.mainHome;
        for (Home home:this.subHomeList){
            if (home.is_used && !home.is_mainhome){
                usedHome = home;
            }
        }
        return usedHome;
    }
    public Home getHome(String name){
        for (Home home:this.subHomeList){
            if (Objects.equals(home.home_name, name)){
                return home;
            }
        }
        return mainHome;
    }
    public void onHomeKeyPressed(ClientPlayerEntity player, boolean breaker_enabled){
        if (breaker_enabled){
            this.getFirstUsedHome().runHome(player);
        }
        else {
            if (this.lastSetHomeLocation.isWithinDistance(player.getBlockPos(),3.0d)){
                this.getLastUsedHome().runSetHome(player);
                player.sendMessage(new LiteralText("\2476Rerun of sethome detected..."), false);
            }
            else{
                this.getFirstUnusedHome().runSetHome(player);
            }
            lastSetHomeLocation = player.getBlockPos();
        }
    }
    public void tick(ClientPlayerEntity player){
        if (this.command_delay > 0){
            this.command_delay--;
        }
        if (this.command_delay == 0){
            this.mainHome.runSetHome(player);
            this.command_delay = -1;
        }
    }
    public void onSetHomeKeyPressed(ClientPlayerEntity player, Config config){
        this.getFirstUsedHome().runHome(player);
        this.command_delay = config.setMainHomeCooldown;
    }
    public String getString(boolean breaker_enabled){
        StringBuilder s = new StringBuilder();
        String used;
        String unused;
        if (breaker_enabled){used = "\247b";unused = "\2477";} else {unused = "\247a";used = "\2477";}
        for(Home home:this.subHomeList){
            if (home.is_used){
                s.append(used).append(home.home_name).append(" ");
            }
            else{
                s.append(unused).append(home.home_name).append(" ");
            }
        }
        return s.toString();
    }
}
