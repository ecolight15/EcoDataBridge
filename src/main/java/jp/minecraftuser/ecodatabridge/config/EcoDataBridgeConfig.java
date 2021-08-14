
package jp.minecraftuser.ecodatabridge.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * デフォルトコンフィグクラス
 * @author ecolight
 */
public class EcoDataBridgeConfig extends ConfigFrame{
    protected HashMap<UUID, PlayerInventoryContainer> loginStack;
    protected HashMap<UUID, Boolean> save_start;

    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     */
    public EcoDataBridgeConfig(PluginFrame plg_) {
        super(plg_);
        loginStack = new HashMap<>();
        save_start = new HashMap<>();
    }
    
    /**
     * ログイン時のインベントリを退避しておく
     * @param p 
     */
    public void backupInventory(Player p) {
        UUID uid = p.getUniqueId();

        // 既に格納済みの場合は消してから退避する
        if (loginStack.containsKey(uid)) {
            loginStack.remove(uid);
        }

        // 格納する
        PlayerInventoryContainer con = new PlayerInventoryContainer(plg, p);
        con.save();
        loginStack.put(uid, con);
    }

    /**
     * ログイン時のインベントリ退避してあったものを書き戻す
     * @param p
     */
    public void restoreInventory(Player p) {
        UUID uid = p.getUniqueId();

        // 格納済みのデータがない場合は何もしない
        if (!loginStack.containsKey(uid)) {
            return;
        }

        try {
            // 取り出してリストアする
            loginStack.get(uid).load();
        } catch (IOException ex) {
            Logger.getLogger(EcoDataBridgeConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * ログアウト時のセーブ開始をマークする
     * @param p
     */
    public void startSaveInventory(Player p) {
        UUID uid = p.getUniqueId();

        if (save_start.containsKey(uid)) {
            save_start.remove(uid);
        }
        save_start.put(uid, Boolean.TRUE);
    }    
    
    /**
     * ログアウト時のセーブ完了をマークする
     * @param p
     */
    public void completeSaveInventory(Player p) {
        UUID uid = p.getUniqueId();

        if (save_start.containsKey(uid)) {
            save_start.remove(uid);
        }
    }    
    
    /**
     * セーブ中かどうか
     * @param p
     * @return 
     */
    public boolean isSaveInventory(Player p) {
        UUID uid = p.getUniqueId();

        if (!save_start.containsKey(uid)) return false;
        return save_start.get(uid);
    }    
}
