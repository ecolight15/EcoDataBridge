
package jp.minecraftuser.ecodatabridge.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeSaveCommand.pic;
import static jp.minecraftuser.ecodatabridge.commands.EcoDataBridgeSaveCommand.pac;
import jp.minecraftuser.ecodatabridge.container.PlayerAdvancementContainer;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EcoDataBridgeコマンドクラス
 * @author ecolight
 */
public class EcoDataBridgeLoadCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoDataBridgeLoadCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodatabridge.load";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        
        // インベントリ情報
        if (!pic.containsKey(p.getUniqueId())) {
            Utl.sendPluginMessage(plg, sender, "インベントリ情報が保存されていません");
        }
        PlayerInventoryContainer ic = pic.get(p.getUniqueId());
        try {
            ic.load();
            Utl.sendPluginMessage(plg, sender, "インベントリ情報を読み込みました");
        } catch (IOException ex) {
            Logger.getLogger(EcoDataBridgeLoadCommand.class.getName()).log(Level.SEVERE, null, ex);
            Utl.sendPluginMessage(plg, sender, "インベントリ情報の読み込みに失敗しました");
        }

        // 実績情報
        if (!pac.containsKey(p.getUniqueId())) {
            Utl.sendPluginMessage(plg, sender, "実績情報が保存されていません");
        }
        PlayerAdvancementContainer ac = pac.get(p.getUniqueId());
        ac.load();
        Utl.sendPluginMessage(plg, sender, "実績情報を読み込みました");
        return true;
    }
    
}
