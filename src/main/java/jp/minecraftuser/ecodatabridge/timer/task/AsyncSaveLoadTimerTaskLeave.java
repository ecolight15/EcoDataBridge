
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * タスク別処理分割用 LEAVE クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskLeave extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskLeave instance = null;
    public static final AsyncSaveLoadTimerTaskLeave getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskLeave(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskLeave(PluginFrame plg_) {
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
            // セーブ中だったら保留する
            if (((EcoDataBridgeConfig) conf).isSaveInventory(data.player)) {
                data.request_reset();
                // ToDo: リトライアウト処理入れないと無限ループするかも
                return;
            }
            // 有効かつセーブ中でなければ無効化する
            db.updateSetting(con, data.player.getUniqueId(), "enabled", "false");
            data.result = true;
        } else {
            // 設定は既に無効
            data.result = false;
            data.msg = "既に設定は無効です";
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
            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの無効化に成功しました");
        } else {
            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの無効化に失敗しました");
            if (data.msg != null) {
                Utl.sendPluginMessage(plg, data.player, data.msg);
            }
        }
    }

    

}
