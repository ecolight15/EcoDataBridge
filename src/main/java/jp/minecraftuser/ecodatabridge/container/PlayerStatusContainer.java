
package jp.minecraftuser.ecodatabridge.container;

import com.google.gson.Gson;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class PlayerStatusContainer {
    private PluginFrame plg;
    private Player p;
    public UUID uuid;
    private Gson gson = new Gson();
    public String statusStore = "";
    
    public PlayerStatusContainer(PluginFrame plg_, Player p_) {
        plg = plg_;
        p = p_;
        uuid = p.getUniqueId();
    }
    public PlayerStatusContainer(PluginFrame plg_, Player p_, String stat_) {
        plg = plg_;
        p = p_;
        uuid = p.getUniqueId();
        statusStore = stat_;
    }
    
    class AdvancementData {
        public float exp;
        public double health;
        public double health_scale;
        public boolean health_scaled;
        public int level;
        public float exhaustion;
        public int food_level;
        public int total_exp;
    }
    
    public void save() {
        AdvancementData data = new AdvancementData();
        
        data.exp = p.getExp();
        data.health = p.getHealth();
        data.health_scale = p.getHealthScale();
        data.health_scaled = p.isHealthScaled();
        data.level = p.getLevel();
        data.exhaustion = p.getExhaustion();
        data.food_level = p.getFoodLevel();
        // Todo: ツールバーのカーソル位置
        // Todo: プレイヤーリスト（全サーバ共通
        // Todo: static 統計  同期は諦めて各サーバ実績を別途参照できるようにしたほうが良いか
        data.total_exp = p.getTotalExperience();

        // リストをまとめてjson化して保存する
        statusStore = gson.toJson(data);
        plg.getLogger().log(Level.INFO, "save json:"+statusStore+")\n");
    }
    public void load() {
        plg.getLogger().log(Level.INFO, "load json:"+statusStore+")\n");
        AdvancementData data = gson.fromJson(statusStore, AdvancementData.class);

        p.setExp(data.exp);
        p.setHealth(data.health);
        p.setHealthScale(data.health_scale);
        p.setHealthScaled(data.health_scaled);
        p.setLevel(data.level);
        p.setExhaustion(data.exhaustion);
        p.setFoodLevel(data.food_level);
        // Todo: ツールバーのカーソル位置
        // Todo: プレイヤーリスト（全サーバ共通
        // Todo: static 統計  同期は諦めて各サーバ実績を別途参照できるようにしたほうが良いか
        p.setTotalExperience(data.total_exp);
    }
}
