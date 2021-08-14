
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用 SAVE クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskSave extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskSave instance = null;
    public static final AsyncSaveLoadTimerTaskSave getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskSave(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskSave(PluginFrame plg_) {
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
        // プレイヤーのインベントリ情報をDBに保存する
        log.log(Level.INFO, "Save player data[{0}]", data.player.getName());
        // プレイヤーのサーバ間データ連携は有効か？
        String enabled = db.getSetting(con, data.player.getUniqueId(), "enabled");
        if (enabled.equalsIgnoreCase("true")) {
            // ログイン結果がなしなら何もしない
            if (data.login_info == null) {
                log.log(Level.INFO, "Reject save player data. unload login information.");
                log.log(Level.INFO, "data= {0}", new Object[]{data.pic.toString()});
                data.result = false;
                return;
            }

            // ログイン結果がfalseなら何もしない
            if (data.login_info.result == false) {
                log.log(Level.INFO, "Reject save player data. failed login result.");
                log.log(Level.INFO, "data= {0}", new Object[]{data.pic.toString()});
                data.result = false;
                return;
            }

            // プレイヤーのログインサーバが一致しなければ何もしない
            if (!conf.getString("server.name").equals(data.login_info.serverName)) {
                log.log(Level.INFO, "Reject save player data. unmatch login server.[conf:{0}]<->[data:{1}]",
                        new Object[]{conf.getString("server.name"), data.login_info.serverName});
                log.log(Level.INFO, "data= {0}", new Object[]{data.pic.toString()});
                data.result = false;
                return;
            }
            // 保存
            db.updateInventory(con, data.pic);
            db.updateStatus(con, data.status);
            log.log(Level.INFO, "Save complete.");

            // ログアウト指定がある場合、該当プレイヤーのサーバ名を削除
            if (data.logout_flag) {
                db.updateSetting(con, data.player.getUniqueId(), "login-server", "");
                log.log(Level.INFO, "Update login information complete. clear login-server");
            }
            data.result = true;
        } else {
            log.log(Level.INFO, "Setting disabled.");
            data.result = false;
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
        log.log(Level.INFO, data.pic.toString());
        if (data.result) {
//            if (data.logout_flag) {
//                data.player.getEnderChest().clear();
//                data.player.getInventory().clear();
//            }
        }
        ((EcoDataBridgeConfig) conf).completeSaveInventory(data.player);
    }

    

}
