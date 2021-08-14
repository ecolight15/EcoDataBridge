
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用 STOP クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskStop extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskStop instance = null;
    public static final AsyncSaveLoadTimerTaskStop getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskStop(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskStop(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     */
    @Override
    public void asyncThread(AsyncSaveLoadTimer thread, PlayerDataStore db, Connection con, PlayerDataPayload data) {
        log.log(Level.INFO, "start thread stop");
        thread.stop();
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncSaveLoadTimer thread, PlayerDataPayload data) {
        // 実装なし
    }

    

}
