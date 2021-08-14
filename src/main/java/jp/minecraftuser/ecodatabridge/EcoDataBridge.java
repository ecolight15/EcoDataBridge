
package jp.minecraftuser.ecodatabridge;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeCommand;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeJoinCommand;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeLeaveCommand;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeLoadCommand;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeReloadCommand;
import jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeSaveCommand;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.db.PlayerDataStore;
import jp.minecraftuser.ecodatabridge.listener.SaveInventoryListener;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * Todo: BukkitSerializationクラスはクラスインスタンスを無理矢理文字列変換してるので、
 *      同じバイナリのクラスインスタンスにしか戻せないかもしれない。
 */

/**
 * EcoDataBridgeプラグインメインクラス
 * @author ecolight
 */
public class EcoDataBridge extends PluginFrame {

    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
        // プラグイン停止時の全プレイヤーデータ保存処理
        // ログイン中プレイヤーのセーブをキューして、処理が完了するのを待つ
        // リロード時点でも最新のデータが保存されることになる
        AsyncSaveLoadTimer t = (AsyncSaveLoadTimer) getPluginTimer("data");
        PlayerDataPayload data = new PlayerDataPayload(this, getServer().getOnlinePlayers().toArray(new Player[getServer().getOnlinePlayers().size()]), PlayerDataPayload.Type.SAVE_DATA_ALL);
        data.saveSnapshot();
        t.sendData(data);
        data = new PlayerDataPayload(this, PlayerDataPayload.Type.STOP);
        log.log(Level.INFO, "start onDisable send Data");
        t.sendData(data);
        try {
            // 処理完了まで待機
            t.timerWait();
        } catch (InterruptedException ex) {
            Logger.getLogger(EcoDataBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
        disable();
    }

    
    /**
     * 設定初期化
     */
    @Override
    public void initializeConfig() {
        EcoDataBridgeConfig conf = new EcoDataBridgeConfig(this);
        // サーバ設定
        conf.registerString("server.name");
        // DB設定
        conf.registerBoolean("playerdb.use");
        conf.registerString("playerdb.db");
        conf.registerString("playerdb.name");
        conf.registerString("playerdb.server");
        conf.registerString("playerdb.user");
        conf.registerString("playerdb.pass");
        registerPluginConfig(conf);
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcoDataBridgeCommand(this, "edb");
        cmd.addCommand(new EcoDataBridgeReloadCommand(this, "reload"));
        cmd.addCommand(new EcoDataBridgeSaveCommand(this, "save"));
        cmd.addCommand(new EcoDataBridgeLoadCommand(this, "load"));
        cmd.addCommand(new EcoDataBridgeJoinCommand(this, "join"));
        cmd.addCommand(new EcoDataBridgeLeaveCommand(this, "leave"));
        registerPluginCommand(cmd);
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        registerPluginListener(new SaveInventoryListener(this, "inventory"));
    }

    /**
     * 定期実行タイマー初期化
     */
    @Override
    public void initializeTimer() {
        // Listenerが先に生成されるが、SaveInventoryListenerのコンストラクタでAsyncSaveLoadTimerが必要になったため
        // シングルトン化してListener側でインスタンス生成することにした。タイマーの起動はここで行う。
        AsyncSaveLoadTimer timer = AsyncSaveLoadTimer.getInstance(this, "data");
        registerPluginTimer(timer);
        timer.runTaskTimer(this, 0, 20);
    }

    /**
     * データベース初期化
     */
    @Override
    public void initializeDB() {
        EcoDataBridgeConfig conf = (EcoDataBridgeConfig) getDefaultConfig();
        try {
            if (conf.getBoolean("playerdb.use")) {
                if (conf.getString("playerdb.db").equalsIgnoreCase("sqlite")) {
                    registerPluginDB(new PlayerDataStore(this, conf.getString("playerdb.name"), "player"));
                } else if (conf.getString("playerdb.db").equalsIgnoreCase("mysql")) {
                    registerPluginDB(new PlayerDataStore(this,
                            conf.getString("playerdb.server"),
                            conf.getString("playerdb.user"),
                            conf.getString("playerdb.pass"),
                            conf.getString("playerdb.name"),
                            "player"));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EcoDataBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
