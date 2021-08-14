
package jp.minecraftuser.ecodatabridge.container;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class PlayerAdvancementContainer {
    private PluginFrame plg;
    private Player p;
    private Gson gson = new Gson();
    public String advancementStore = "";
    
    public PlayerAdvancementContainer(PluginFrame plg_, Player p_) {
        plg = plg_;
        p = p_;
    }
    public PlayerAdvancementContainer(PluginFrame plg_, String name) {
        plg = plg_;
        p = plg.getServer().getOfflinePlayer(name).getPlayer();
    }
    public PlayerAdvancementContainer(PluginFrame plg_, UUID uid) {
        plg = plg_;
        p = plg.getServer().getPlayer(uid);
    }
    
    class AdvancementData {
        public String namespace;
        public String key;
        public ArrayList<String> criteria = new ArrayList<>();
        public AdvancementData(Advancement ad) {
            namespace = ad.getKey().getNamespace();
            key = ad.getKey().getKey();
        }
    }
    class AdvancementList {
        public ArrayList<AdvancementData> advancementList;
        public AdvancementList() {
            advancementList = new ArrayList<>();
        }
    }
    public void save() {
        AdvancementList list = new AdvancementList();
        Bukkit.getServer().advancementIterator().forEachRemaining(a -> {
            AdvancementData d = new AdvancementData(a);
            AdvancementProgress pro = p.getAdvancementProgress(a);
            for (String aw : pro.getAwardedCriteria()) {
                // 取得済みアワード
                d.criteria.add(aw);
                plg.getLogger().log(Level.INFO, "criteria:"+aw+"\n");
            }
            if (d.criteria.size() != 0) {
                // 1件でも取得済みアワードがあればリストに加える
                // 1件もない場合にはAdvancementごと対象外
                list.advancementList.add(d);
                plg.getLogger().log(Level.INFO, "advancement:"+d.key+"("+d.namespace+")\n");
            }
        });
        // リストをまとめてjson化して保存する
        advancementStore = gson.toJson(list);
        plg.getLogger().log(Level.INFO, "save json:"+advancementStore+")\n");
    }
    public void load() {
        plg.getLogger().log(Level.INFO, "load json:"+advancementStore+")\n");
        AdvancementList list = gson.fromJson(advancementStore, AdvancementList.class);
        for (AdvancementData d : list.advancementList) {
            AdvancementProgress prog = p.getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.minecraft(d.key)));
            plg.getLogger().log(Level.INFO, "advancement:"+d.key+"("+d.namespace+")\n");
            for (String s: d.criteria) {
                prog.awardCriteria(s);
                plg.getLogger().log(Level.INFO, "criteria:"+s+"\n");
            }
        }
    }
}
