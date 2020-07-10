package net.crashcraft.whipclaim.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.crashcraft.whipclaim.visualize.api.VisualColor;
import net.crashcraft.whipclaim.visualize.api.VisualType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class GlobalConfig extends BaseConfig{
    public static String visual_type;
    public static boolean visual_use_highest_block;
    public static HashMap<UUID, Material> visual_menu_items;

    public static EnumWrappers.TitleAction visual_alert_type;
    public static int visual_alert_fade_in;
    public static int visual_alert_duration;
    public static int visual_alert_fade_out;

    private static void loadVisual(){
        visual_type = config.getString("visualization.visual-type", "glow");

        visual_menu_items = new HashMap<>();
        for (World world : Bukkit.getWorlds()){
            visual_menu_items.put(world.getUID(), Material.getMaterial(config.getString("visualize.visualize-claim-items." + world.getName(), Material.OAK_FENCE.name())));
        }

        visual_use_highest_block = config.getBoolean("visualization.visual-use-highest-block", false);
        visual_alert_type = EnumWrappers.TitleAction.valueOf(config.getString("visualization.alert.type", EnumWrappers.TitleAction.ACTIONBAR.name()));
        visual_alert_fade_in = config.getInt("visualization.alert.fade-in", 10);
        visual_alert_duration = config.getInt("visualization.alert.duration", 1);
        visual_alert_fade_out = config.getInt("visualization.alert.fade-out", 10);

        for (VisualColor color : VisualColor.values()){
            Material material = Material.getMaterial(config.getString("visualization.visual-colors." + color.name(), Material.ORANGE_CONCRETE.name()));

            if (material == null){
                log("Invalid material for visualization.visual-colors." + color.name());
                material = Material.ORANGE_CONCRETE;
            }

            color.setMaterial(material);
        }
    }

    public static HashMap<PlayerTeleportEvent.TeleportCause, Integer> teleportCause;

    private static void loadTeleport(){
        // 0 | NONE  - diable, 1 | BLOCK - enable check with blocking, 2 | RELOCATE - enable check with relocating
        teleportCause = new HashMap<>();
        for (PlayerTeleportEvent.TeleportCause cause : PlayerTeleportEvent.TeleportCause.values()){
            String value = config.getString("events.teleport." + cause.name(), "block");

            switch (value.toLowerCase()){
                case "none":
                    teleportCause.put(cause, 0);
                case "block":
                    teleportCause.put(cause, 1);
                case "relocate":
                    teleportCause.put(cause, 2);
                default:
                    //Bad value default to good one
                    logError("Invalid value for events.teleport." + cause.name() + ", defaulting to `block`");
                    teleportCause.put(cause, 1);
            }
        }
    }

    public static double money_per_block;
    public static ArrayList<UUID> disabled_worlds;

    private static void miscValues(){
        money_per_block = config.getDouble("money-per-block", 0.01);
        disabled_worlds = new ArrayList<>();
        for (String s : config.getStringList("disabled-worlds")){
            World world = Bukkit.getWorld(s);
            if (world == null){
                logError("World name was invalid or the world was not loaded into memory");
                continue;
            }

            disabled_worlds.add(world.getUID());
        }
    }
}
