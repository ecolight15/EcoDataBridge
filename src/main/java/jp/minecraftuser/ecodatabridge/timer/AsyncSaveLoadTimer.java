
package jp.minecraftuser.ecodatabridge.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskBase;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskJoin;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskLeave;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskLogin;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskReset;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskSave;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskSaveAll;
import jp.minecraftuser.ecodatabridge.timer.task.AsyncSaveLoadTimerTaskStop;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * 非同期プレイヤーデータ保存クラス
 * @author ecolight
 */
public class AsyncSaveLoadTimer extends AsyncProcessFrame {
    // 処理種別ごとの制御をクラス化
    HashMap<PlayerDataPayload.Type, AsyncSaveLoadTimerTaskBase> tasktable;
    // リスナ生成時点でインスタンスが必要になったのでシングルトン化しておく
    private static AsyncSaveLoadTimer instance = null;
    public static final AsyncSaveLoadTimer getInstance(PluginFrame plg_, String name_) {
        if (instance == null) {
            instance = new AsyncSaveLoadTimer(plg_, name_);
        }
        return instance;
    }

    /**
     * 親スレッド用コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public AsyncSaveLoadTimer(PluginFrame plg_, String name_) {
        super(plg_, name_);
        initTask();
    }

    /**
     * 子スレッド用コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     * @param frame_ 子スレッド用フレーム
     */
    public AsyncSaveLoadTimer(PluginFrame plg_, String name_, AsyncFrame frame_) {
        super(plg_, name_, frame_);
        initTask();
    }
    
    /**
     * タスク処理用のクラスインスタンスを生成
     * 各クラスのシングルトンインスタンス取得して格納する
     */
    private void initTask() {
        tasktable = new HashMap<>();
        tasktable.put(PlayerDataPayload.Type.CMD_JOIN, AsyncSaveLoadTimerTaskJoin.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.CMD_LEAVE, AsyncSaveLoadTimerTaskLeave.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.LOGIN, AsyncSaveLoadTimerTaskLogin.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.RESET, AsyncSaveLoadTimerTaskReset.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.SAVE_DATA, AsyncSaveLoadTimerTaskSave.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.SAVE_DATA_ALL, AsyncSaveLoadTimerTaskSaveAll.getInstance(plg));
        tasktable.put(PlayerDataPayload.Type.STOP, AsyncSaveLoadTimerTaskStop.getInstance(plg));
    }
    
    /**
     * セーブ・ロードスレッド停止待ち合わせ
     * @throws InterruptedException 
     */
    public synchronized void timerWait() throws InterruptedException {
        log.log(Level.INFO, "Wait for thread stop.");
        wait();
        log.log(Level.INFO, "Detect thread stop.");
    }

    /**
     * セーブ・ロードスレッド停止 
     */
    public synchronized void timerStop() {
        log.log(Level.INFO, "Notify thread stop.");
        notifyAll();
        log.log(Level.INFO, "Call thread cancel.");
        cancel();
    }

    /**
     * 子スレッドから親スレッドへの停止指示用
     */
    public void stop() {
        log.log(Level.INFO, "start send parent timer stop");
        ((AsyncSaveLoadTimer) parentFrame).timerStop();
    }
    
    /**
     * Data加工子スレッド側処理
     * @param data_ ペイロードインスタンス
     */
    @Override
    protected void executeProcess(PayloadFrame data_) {
        PlayerDataPayload data = (PlayerDataPayload) data_;
        PlayerDataStore db = (PlayerDataStore) plg.getDB("player");
        Connection con;
        try {
            con = db.connect();
        } catch (SQLException ex) {
            Logger.getLogger(AsyncSaveLoadTimer.class.getName()).log(Level.SEVERE, null, ex);
            // 処理結果を返送
            data.result = false;
            receiveData(data);
            return;
        }
        
        try {
            tasktable.get(data.type).asyncThread(this, db, con, data);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "getSQLState:{0}", e.getSQLState());
            log.log(Level.SEVERE, "getErrorCode:{0}", e.getErrorCode());
            log.log(Level.SEVERE, "getErrorCode:{0}", e.getMessage());
            // データベース制御失敗したので再度試みる
            data.request_reset();
            data.result = false;
        }

        try {
            try {
                con.commit();
            } catch (SQLException e) {
                data.request_reset();
            }
            con.close();
        } catch (SQLException ex1) {
            Logger.getLogger(AsyncSaveLoadTimer.class.getName()).log(Level.SEVERE, null, ex1);
        }
        
        // 処理結果を返送
        receiveData(data);
    }

    /**
     * Data加工後親スレッド側処理
     * @param data_ ペイロードインスタンス
     */
    @Override
    protected void executeReceive(PayloadFrame data_) {
        PlayerDataPayload data = (PlayerDataPayload) data_;
        tasktable.get(data.type).mainThread(this, data);
    }

    /**
     * 継承クラスの子スレッド用インスタンス生成
     * 親子間で共有リソースがある場合、マルチスレッドセーフな作りにすること
     * synchronizedにする、スレッドセーフ対応クラスを使用するなど
     * @return AsyncFrame継承クラスのインスタンス
     */
    @Override
    protected AsyncFrame clone() {
        return new AsyncSaveLoadTimer(plg, name, this);
    }
  
}
