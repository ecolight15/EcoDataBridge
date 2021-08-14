
package jp.minecraftuser.ecodatabridge.commands;

import java.util.HashMap;
import java.util.UUID;
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
public class EcoDataBridgeSaveCommand extends CommandFrame {
    static public HashMap<UUID, PlayerInventoryContainer> pic = new HashMap<>();
    static public HashMap<UUID, PlayerAdvancementContainer> pac = new HashMap<>();
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoDataBridgeSaveCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        //setAuthBlock(true);
        //setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodatabridge.save";
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
        
        // インベントリセーブ
        PlayerInventoryContainer ic;
        if (pic.containsKey(p.getUniqueId())) {
            ic = pic.get(p.getUniqueId());
        } else {
            ic = new PlayerInventoryContainer(plg, p);
            pic.put(p.getUniqueId(), ic);
        }
        ic.save();
        Utl.sendPluginMessage(plg, sender, "インベントリ情報を書き込みました");
        
        // 統計保存
        
        // 実績保存
        // 起動時にサーバーでロードする必要がある実績一覧作成
        // プレイヤーログイン時にDBから該当advancementのcriteria追加
        // criteria完了時にセーブ
        PlayerAdvancementContainer ac;
        if (pac.containsKey((p.getUniqueId()))) {
            ac = pac.get(p.getUniqueId());
        } else {
            ac = new PlayerAdvancementContainer(plg, p);
            pac.put(p.getUniqueId(), ac);
        }
        ac.save();
        Utl.sendPluginMessage(plg, sender, "実績情報を書き込みました");

//        plg.getServer().getWorlds().listIterator().forEachRemaining(w -> log.log(Level.INFO, w.toString()));

                
//        Advancement a = p.getServer().getAdvancement(NamespacedKey.minecraft("adventure/kill_all_mobs"));
//        log.log(Level.INFO, "----------require criteria----------");
//        for (String s : a.getCriteria()) {
//            log.log(Level.INFO, s);
//        }
//        log.log(Level.INFO, "----------awarded criteria----------");
//        for (String s : p.getAdvancementProgress(a).getAwardedCriteria()) {
//            log.log(Level.INFO, s);
//        }
//        log.log(Level.INFO, "----------remain criteria----------");
//        for (String s : p.getAdvancementProgress(a).getRemainingCriteria()) {
//            log.log(Level.INFO, s);
//        }
//        if (args.length >= 2) {
//            if (args[0].equalsIgnoreCase("add")) {
//                p.getAdvancementProgress(a).awardCriteria(args[1]);
//            } else if (args[0].equalsIgnoreCase("del")) {
//                p.getAdvancementProgress(a).revokeCriteria(args[1]);
//                ;
//            }
//        }
        
        return true;
    }
    
}
