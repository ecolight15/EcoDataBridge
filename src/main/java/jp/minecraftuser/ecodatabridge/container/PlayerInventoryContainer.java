
package jp.minecraftuser.ecodatabridge.container;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.gist.BukkitSerialization;
import jp.minecraftuser.ecodatabridge.gist.BukkitSerializationB;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * プレイヤーに属するインベントリ類をまとめるコンテナ
 * @author ecolight
 */
public class PlayerInventoryContainer {
    private final PluginFrame plg;
    private final Player p;
    private Gson gson = new Gson();
    
    public UUID uuid;
    public String inv;
    public String ender;
    public PlayerInventoryContainer(PluginFrame plg_, Player p_) {
        plg = plg_;
        p = p_;
        uuid = p.getUniqueId();
    }
    public PlayerInventoryContainer(PluginFrame plg_, Player p_, String inv_, String ender_) {
        plg = plg_;
        p = p_;
        uuid = p.getUniqueId();
        inv = inv_;
        ender = ender_;
    }
    
    public class InventoryData {
        public ArrayList<String> inv = new ArrayList<>();
        public ArrayList<String> armor = new ArrayList<>();
        public InventoryData(String[] data_) {
            for (String s : data_) {
                inv.add(s);
            }
        }
        public InventoryData(String[] inv_, String[] armor_) {
            for (String s : inv_) {
                inv.add(s);
            }
            for (String s : armor_) {
                armor.add(s);
            }
        }
    }
    public void save() {
        //InventoryData buf = new InventoryData(BukkitSerialization.playerInventoryToBase64(p.getInventory()));
        InventoryData buf = new InventoryData(
                BukkitSerializationB.inventoryToYaml(p.getInventory()),
                BukkitSerializationB.itemStacksToYaml(p.getInventory().getArmorContents())
        );

        inv = gson.toJson(buf);
        ender = gson.toJson(BukkitSerializationB.inventoryToYaml(p.getEnderChest()));
        Bukkit.getServer().getLogger().log(Level.INFO, "save inv:" + inv);
        Bukkit.getServer().getLogger().log(Level.INFO, "save ender:" + ender);
    }
    public void load() throws IOException {
        Bukkit.getServer().getLogger().log(Level.INFO, "load inv:" + inv);
        Bukkit.getServer().getLogger().log(Level.INFO, "load ender:" + ender);
        p.getInventory().clear();
        p.getEnderChest().clear();
        InventoryData buf = gson.fromJson(inv, InventoryData.class);
        p.getInventory().setContents(BukkitSerializationB.itemStacksFromYaml(buf.inv.toArray(new String[0])));
        p.getInventory().setArmorContents(BukkitSerializationB.itemStacksFromYaml(buf.armor.toArray(new String[0])));
        String[] buf2 = gson.fromJson(ender, String[].class);
        p.getEnderChest().setContents(BukkitSerializationB.itemStacksFromYaml(buf2));
//        InventoryData buf = gson.fromJson(inv, InventoryData.class);
//        p.getInventory().setContents(BukkitSerialization.itemStackArrayFromBase64(buf.inv.get(0)));
//        p.getInventory().setArmorContents(BukkitSerialization.itemStackArrayFromBase64(buf.inv.get(1)));
//        p.getEnderChest().setContents(BukkitSerialization.itemStackArrayFromBase64(ender));
    }
    public void load(String inv_, String ender_) throws IOException {
        Bukkit.getServer().getLogger().log(Level.INFO, "load_ inv:" + inv);
        Bukkit.getServer().getLogger().log(Level.INFO, "load_ ender:" + ender);
        p.getInventory().clear();
        p.getEnderChest().clear();
        InventoryData buf = gson.fromJson(inv_, InventoryData.class);
        p.getInventory().setContents(BukkitSerializationB.itemStacksFromYaml(buf.inv.toArray(new String[0])));
        p.getInventory().setArmorContents(BukkitSerializationB.itemStacksFromYaml(buf.armor.toArray(new String[0])));
        String[] buf2 = gson.fromJson(ender_, String[].class);
        p.getEnderChest().setContents(BukkitSerializationB.itemStacksFromYaml(buf2));
//        p.getInventory().clear();
//        p.getEnderChest().clear();
//        InventoryData buf = gson.fromJson(inv_, InventoryData.class);
//        p.getInventory().setContents(BukkitSerialization.itemStackArrayFromBase64(buf.inv.get(0)));
//        p.getInventory().setArmorContents(BukkitSerialization.itemStackArrayFromBase64(buf.inv.get(1)));
//        p.getEnderChest().setContents(BukkitSerialization.itemStackArrayFromBase64(ender));
    }
    @Override
    public String toString() {
        Bukkit.getLogger().log(Level.INFO, inv);
        StringBuilder sb = new StringBuilder("PlayerDataPayload->");
        if (p != null) {
            sb.append("[").append(p.getName()).append("]");
        }
        if (inv != null) sb.append(" inv:").append(inv);
        if (ender != null) sb.append(" ender:").append(ender);
        return sb.toString();
    }
}
