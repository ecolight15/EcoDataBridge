
package jp.minecraftuser.ecodatabridge.timer.task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.listener.SaveInventoryListener;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * タスク別処理分割用 LOGIN クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimerTaskLogin extends AsyncSaveLoadTimerTaskBase {
    
    // シングルトン実装
    private static AsyncSaveLoadTimerTaskLogin instance = null;
    public static final AsyncSaveLoadTimerTaskLogin getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimerTaskLogin(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncSaveLoadTimerTaskLogin(PluginFrame plg_) {
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
        // インベントリ読み込み処理より前にすること
        // 設定が無効の場合はログイン情報が取得できた体でtrueでリターンする
        data.enabled = db.getSetting(con, data.player.getUniqueId(), "enabled");
        if (!data.enabled.equalsIgnoreCase("true")) {
            data.result = true;
            data.saveSnapshot();
            log.log(Level.INFO, "Skip login information loading. server name =[{0}]", data.serverName);
            return;
        }

        // セーブ実行中の場合には保留する
        if (((EcoDataBridgeConfig) conf).isSaveInventory(data.player)) {
            // 保留する場合は、一旦親スレッドに戻してから再度キューさせる
            data.request_reset();
            // ToDo: リトライアウト処理入れないと無限ループするかも
            return;
        }

        // ログインサーバ情報を取り出し
        data.serverName = db.getSetting(con, data.player.getUniqueId(), "login-server");
        // ログイン時、他サーバ名であればDBからのプレイヤーデータ読み込みをガード
        // ログイン時、自サーバ名であればDBからのプレイヤーデータ読み込みをガード
        // すでに入ってるサーバ名を維持するのでここでreturnで抜ける
        if (!data.serverName.equals("")) { // 前回正しくログアウトしていれば空
            // リトライチェック
            long now = System.currentTimeMillis();
            if (data.retryStart == 0) {
                data.retryStart = now;
            }
            // 最長60秒待機する
            if (now - data.retryStart > 60000) {
                // リトライアウト
                log.log(Level.INFO, "Reject load player data[{0}]", data.player.getName());
                log.log(Level.WARNING, "CurrentServer:{0} / OldServerName:{1}", new Object[]{conf.getString("server.name"), data.serverName});
                data.result = false;
            } else {
                // リトライ、再キュー
                data.request_reset();
                // Todo:timerの再キュー間隔で出るので出過ぎるかも
                log.log(Level.INFO, "Detected loading player data by another server. Skip load player data.[{0}][time={1} msec]", new Object[]{data.player.getName(), (now - data.retryStart)});
            }
            return;
        }

        // ★ユーザーログイン時、＋プラグイン起動時のオンラインユーザーについて、サーバー名を記録
        // 最終的に更新した or ロードしたサーバ情報はメインスレッド側に送って保管し、ガード処理に用いる
        data.serverName = conf.getString("server.name");
        db.updateSetting(con, data.player.getUniqueId(), "login-server", data.serverName);
        log.log(Level.INFO, "Update login information. server name =[{0}]", data.serverName);

        // プレイヤーのインベントリ情報/ステータス情報をDBから取り出す
        // インスタンスに触るのはメインスレッド側でするのでJson情報を返却するだけでloadはしない
        log.log(Level.INFO, "Load player data[{0}]", data.player.getName());
        data.pic = db.loadPlayerInventoryContainer(con, data.player);
        data.status = db.loadPlayerStatusContainer(con, data.player);
        log.log(Level.INFO, "Load complete.");

        data.result = true;

        //memo
        //　資源など削除されるサーバーでサーバーダウンした場合を想定
        //　　適当なデータに戻すと確定でitemdup可能な状況に
        //　　前提：サーバーダウンはほぼ予測不可
        //　　前提：問題となるのはサーバダウン後、ワールド削除まで該当プレイヤーがログインしないケース
        //　ワールド削除時点でログイン情報残ってるプレイヤーデータを残存するサーバワールドに移す？

    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncSaveLoadTimer thread, PlayerDataPayload data) {
        if (data.result) {
            // 現在もオンラインかどうかチェックして、オンラインであればロードする
            if (data.player.isOnline()) {
                try {
                    // 実際のプレイヤーインスタンスに読み込み
                    if (data.enabled.equalsIgnoreCase("true")) {
                        // インベントリ情報のロード
                        data.pic.load();
                        // ステータス情報のロード
                        data.status.load();
                    }
                    // ロード完了したタイミングでイベント処理を許可する
                    ((SaveInventoryListener) plg.getPluginListener("inventory")).allowEvent(data.player.getUniqueId());
                } catch (IOException ex) {
                    // いざというときの為に内容をログ
                    log.log(Level.SEVERE, "Failed load player data.");
                    log.log(Level.SEVERE, data.pic.toString());
                    log.log(Level.SEVERE, data.status.toString());
                    Logger.getLogger(AsyncSaveLoadTimer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Utl.sendPluginMessage(plg, data.player, "プレイヤーのログイン/インベントリ情報取得に失敗しました。");
            Utl.sendPluginMessage(plg, data.player, "プレイヤー名と時刻、状況を管理者への連絡をお願いします。");
        }
    }
}
