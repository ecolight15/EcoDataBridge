
package jp.minecraftuser.ecodatabridge.commands;

import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EcoDataBridgeLeaveCommand extends CommandFrame{

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoDataBridgeLeaveCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(false);
        setAuthConsole(false);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodatabridge.leave";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // パラメータチェック:0のみ
        if (!checkRange(sender, args, 0, 0)) return true;

        // JOINのconfirmメッセージ
        Utl.sendPluginMessage(plg, sender, "サーバー間データ共有モードへの移行に際しての注意");
        Utl.sendPluginMessage(plg, sender, "&e- ");

        // 確認開始
        confirm(sender);

        return true;
    }
    
    /**
     * Confirmのaccept時の処理
     * @param sender
     */
    @Override
    public void acceptCallback(CommandSender sender) {
        // LEAVEのaccept処理
        // ToDo: 無効にする場合、インベントリの取り扱いによってはほかサーバー移動時に増加するかも
        AsyncSaveLoadTimer timer = (AsyncSaveLoadTimer) plg.getPluginTimer("data");
        PlayerDataPayload data = new PlayerDataPayload(plg, (Player) sender, PlayerDataPayload.Type.CMD_LEAVE);
        timer.sendData(data);
        Utl.sendPluginMessage(plg, sender, "Leaveを実行しました");
    }

    /**
     * Confirmのキャンセル時の処理
     * @param sender
     */
    @Override
    public void cancelCallback(CommandSender sender) {
        // LEAVEのcancel処理
        Utl.sendPluginMessage(plg, sender, "Leaveをキャンセルしました");
    }  
}
