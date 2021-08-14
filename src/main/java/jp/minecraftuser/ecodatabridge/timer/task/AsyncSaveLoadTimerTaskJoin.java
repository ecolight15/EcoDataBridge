
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.listener.SaveInventoryListener;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * タスク別処理分割用 JOIN クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskJoin extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskJoin instance = null;
    public static final AsyncSaveLoadTimerTaskJoin getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskJoin(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskJoin(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     * @throws java.sql.SQLException 
     */
    @Override
    public void asyncThread(AsyncSaveLoadTimer thread, PlayerDataStore db, Connection con, PlayerDataPayload data) throws SQLException {
        String enabled = db.getSetting(con, data.player.getUniqueId(), "enabled");
        if (enabled.equalsIgnoreCase("true")) {
            // 設定は既に有効
                data.result = false;
                data.msg = "既に設定は有効です";
            } else {
            // 無効であれば有効化する
            db.updateSetting(con, data.player.getUniqueId(), "enabled", "true");

            // ログインサーバを現在のサーバに設定
            data.serverName = conf.getString("server.name");
            db.updateSetting(con, data.player.getUniqueId(), "login-server", data.serverName);
            log.log(Level.INFO, "Join data bridge network.[{0}]", data.player.getName());

            data.result = true;
        }
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncSaveLoadTimer thread, PlayerDataPayload data) {
        // プレイヤーが現時点でオンラインであれば結果送信する。
        if (!data.player.isOnline()) return;
        if (data.result) {
            ((SaveInventoryListener) plg.getPluginListener("inventory")).updatePlayerServer(data.player.getUniqueId(), data.serverName);
            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に成功しました");
        } else {
            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に失敗しました");
            if (data.msg != null) {
                Utl.sendPluginMessage(plg, data.player, data.msg);
            }
        }
    }

    

}
