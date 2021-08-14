
package jp.minecraftuser.ecodatabridge.container;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.UUID;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class PlayerStaticsContainer {
    private PluginFrame plg;
    private Player p;
    private Gson gson = new Gson();
    
    public PlayerStaticsContainer(PluginFrame plg_, Player p_) {
        plg = plg_;
        p = p_;
    }
    public PlayerStaticsContainer(PluginFrame plg_, String name) {
        plg = plg_;
        p = plg.getServer().getOfflinePlayer(name).getPlayer();
    }
    public PlayerStaticsContainer(PluginFrame plg_, UUID uid) {
        plg = plg_;
        p = plg.getServer().getPlayer(uid);
    }
    
    class Data {
        public String namespace;
        public String key;
        public ArrayList<String> criteria = new ArrayList<>();
        public Data(Player p_) {
//            p_.getAdvancementProgress(a)
        }
    }
    public void save() {
//        Statistic.values().
        for (Statistic s : Statistic.values()) {
            int i = p.getStatistic(s);
            switch (s) {
                // GENERAL
                case TALKED_TO_VILLAGER:
                case CAULDRON_FILLED:
                case INTERACT_WITH_STONECUTTER:
                case ARMOR_CLEANED:
                case ENDERCHEST_OPENED:
                case HOPPER_INSPECTED:
                case MOB_KILLS:
                case FLY_ONE_CM:
                case CLEAN_SHULKER_BOX:
                case INTERACT_WITH_LECTERN:
                case PLAY_ONE_MINUTE:
                case INTERACT_WITH_GRINDSTONE:
                case DAMAGE_TAKEN:
                case DAMAGE_DEALT_RESISTED:
                case WALK_ON_WATER_ONE_CM:
                case DAMAGE_DEALT:
                case NOTEBLOCK_PLAYED:
                case TRADED_WITH_VILLAGER:
                case PIG_ONE_CM:
                case CHEST_OPENED:
                case LEAVE_GAME:
                case INTERACT_WITH_CARTOGRAPHY_TABLE:
                case DISPENSER_INSPECTED:
                case BEACON_INTERACTION:
                case FURNACE_INTERACTION:
                case DEATHS:
                case SLEEP_IN_BED:
                case INTERACT_WITH_CAMPFIRE:
                case INTERACT_WITH_ANVIL:
                case TIME_SINCE_DEATH:
                case SPRINT_ONE_CM:
                case INTERACT_WITH_SMOKER:
                case BANNER_CLEANED:
                case BREWINGSTAND_INTERACTION:
                case CLIMB_ONE_CM:
                case FISH_CAUGHT:
                case MINECART_ONE_CM:
                case CRAFTING_TABLE_INTERACTION:
                case WALK_UNDER_WATER_ONE_CM:
                case DROP_COUNT:
                case BELL_RING:
                case FLOWER_POTTED:
                case TIME_SINCE_REST:
                case HORSE_ONE_CM:
                case JUMP:
                case DAMAGE_ABSORBED:
                case SWIM_ONE_CM:
                case BOAT_ONE_CM:
                case CAULDRON_USED:
                case ITEM_ENCHANTED:
                case RECORD_PLAYED:
                case FALL_ONE_CM:
                case PLAYER_KILLS:
                case SHULKER_BOX_OPENED:
                case DAMAGE_BLOCKED_BY_SHIELD:
                case DROPPER_INSPECTED:
                case RAID_WIN:
                case DAMAGE_DEALT_ABSORBED:
                case INTERACT_WITH_BLAST_FURNACE:
                case TRAPPED_CHEST_TRIGGERED:
                case NOTEBLOCK_TUNED:
                case WALK_ONE_CM:
                case ANIMALS_BRED:
                case RAID_TRIGGER:
                case SNEAK_TIME:
                case AVIATE_ONE_CM: // エリトラ？
                case CROUCH_ONE_CM:
                case INTERACT_WITH_LOOM:
                case DAMAGE_RESISTED:
                case OPEN_BARREL:
                case CAKE_SLICES_EATEN:
                    
                // ITEMS
                case MINE_BLOCK:
                case BREAK_ITEM:
                case CRAFT_ITEM:
                case USE_ITEM:
                case PICKUP:
                case DROP:
                
                // MOB
                case KILL_ENTITY:
                case ENTITY_KILLED_BY:
            }
        }
    }
}
