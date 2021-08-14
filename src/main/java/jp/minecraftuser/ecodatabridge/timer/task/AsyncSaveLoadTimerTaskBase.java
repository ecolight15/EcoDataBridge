
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用ベースクラス
 * @author ecolight
 */
public abstract class AsyncSaveLoadTimerTaskBase {
    protected final PluginFrame plg;
    protected final ConfigFrame conf;
    protected final Logger log;
    
    // 継承先でシングルトン実装する (基底クラス側でできないものだろうか)
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskBase(PluginFrame plg_) {
        plg = plg_;
        log = plg.getLogger();
        conf = plg.getDefaultConfig();
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
    abstract public void asyncThread(AsyncSaveLoadTimer thread, PlayerDataStore db, Connection con, PlayerDataPayload data) throws SQLException;
    
    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    abstract public void mainThread(AsyncSaveLoadTimer thread, PlayerDataPayload data);
}
