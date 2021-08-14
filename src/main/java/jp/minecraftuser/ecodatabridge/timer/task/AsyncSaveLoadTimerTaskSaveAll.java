
package jp.minecraftuser.ecodatabridge.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 SAVE クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskSaveAll extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskSaveAll instance = null;
    public static final AsyncSaveLoadTimerTaskSaveAll getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskSaveAll(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskSaveAll(PluginFrame plg_) {
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
        int i = 1;
        while (true) {
            try {
                for (Player plbuf : data.players) {
                    log.log(Level.INFO, "Save player data[{0}]", plbuf.getName());

                    // 設定が有効であれば保存する
                    String enabled = db.getSetting(con, plbuf.getUniqueId(), "enabled");
                    if (!enabled.equalsIgnoreCase("true")) {
                        log.log(Level.INFO, "Player[{0}] Setting disabled.", plbuf.getName());
                        continue;
                    }

                    // プレイヤーのサーバ名が現在のサーバ名でなければ何もしない
                    // 空はログアウトが正常に済んだユーザーか既に保存済みのユーザ
                    // Todo:ログイン処理異常ユーザはセーブして良いのか？
                    String server_name = db.getSetting(con, plbuf.getUniqueId(), "login-server");
                    if (!conf.getString("server.name").equals(server_name)) {
                        log.log(Level.INFO, "Player[{0}] reject server name.[db:{1}]<->[conf:{2}]",
                                new Object[]{plbuf.getName(), db.getSetting(con, plbuf.getUniqueId(), "login-server"), conf.getString("server.name")});
                        continue;
                    }

                    // プレイヤー情報アップデート
                    db.updateInventory(con, data.pics.get(plbuf));
                    db.updateStatus(con, data.statuses.get(plbuf));
                    log.log(Level.INFO, "Save complete.");

                    // ログアウトで該当プレイヤーのサーバ名を削除、
                    db.updateSetting(con, plbuf.getUniqueId(), "login-server", "");
                    log.log(Level.INFO, "Update login information complete. clear login-server");
                }
                // 最後まで保存できたのでリトライループを抜ける
                log.log(Level.INFO, "All player save complete.");
                data.result = true;
                break;
            } catch (SQLException e) {
                // リトライ回数内であればリトライする
                log.log(Level.INFO, "[retry:{0}]Player save failed. {1}",
                        new Object[]{i, e.getLocalizedMessage()});
                if (i <= 10) {
                    try {
                        // DBロック状態を想定して3秒程度待つ/リトライ10回で最大30秒 Todo:パラメタ化しても良いが使用箇所増えたら考える
                        Thread.sleep(3000);
                    } catch (InterruptedException se) {
                        log.log(Level.INFO, "Thread sleep failed. retry:{0}", i);
                    }
                    i++;
                } else {
                    // 失敗
                    log.log(Level.SEVERE, "All player save retry over");
                    for (Player plbuf : data.players) {
                        PlayerInventoryContainer picbuf = data.pics.get(plbuf);
                        log.log(Level.INFO, "player[{0}({1}]/inv:{2}/ender:{3}", 
                                new Object[]{plbuf.getName(), plbuf.getUniqueId().toString(),
                                    picbuf.inv, picbuf.ender});
                    }
                    break;
                }
            }
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
            // 全体セーブ時はプラグインdisable時なので一旦全削除で良い
            //if (data.logout_flag) {
            for (Player plbuf : data.players) {
                plbuf.getEnderChest().clear();
                plbuf.getInventory().clear();
            }
            //}
        }
        ((EcoDataBridgeConfig) conf).completeSaveInventory(data.player);
    }
}
