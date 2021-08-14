
package jp.minecraftuser.ecodatabridge.listener;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import jp.minecraftuser.ecodatabridge.config.EcoDataBridgeConfig;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecodatabridge.timer.AsyncSaveLoadTimer;
import jp.minecraftuser.ecodatabridge.timer.PlayerDataPayload;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * プレイヤーデータセーブListenerクラス
 * @author ecolight
 */
public class SaveInventoryListener extends ListenerFrame {
    private final HashMap<UUID, PlayerDataPayload> loginServer;
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public SaveInventoryListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        loginServer = new HashMap<>();
        // サーバリロード等によるlogin情報喪失に備えて、コンストラクタ実行時点のOnlineプレイヤーに対してログイン処理をコールしておく
        // この場合、タイマーのインスタンスがいないのでここで生成する。
        AsyncSaveLoadTimer timer = AsyncSaveLoadTimer.getInstance(plg, "data");
        for(Player p : plg.getServer().getOnlinePlayers()) {
            PlayerDataPayload data = new PlayerDataPayload(plg, p, PlayerDataPayload.Type.LOGIN);
            data.pic = new PlayerInventoryContainer(plg, p);
            timer.sendData(data);
            registerPlayerServer(p.getUniqueId(), data);
        }
    }

    /**
     * プレイヤーログイン開始でインベントリを退避の上で空にしておく
     * @param player 
     */
// ログアウト時に消すよう修正
//    @EventHandler
//    private void PlayerLogin(PlayerLoginEvent e) {
//        Player p = e.getPlayer();
//        ((EcoDataBridgeConfig) conf).backupInventory(p);
//        p.getEnderChest().clear();
//        p.getInventory().clear();
//    }

    /**
     * プレイヤーのログイン確定後の処理
     * memo: 同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     */
    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        // Joinでサーバー入りが確定しているのでDBから非同期でロードを掛ける
        // まずはログイン情報の照会から開始
        Player p = e.getPlayer();
        AsyncSaveLoadTimer timer = (AsyncSaveLoadTimer) plg.getPluginTimer("data");
        PlayerDataPayload data = new PlayerDataPayload(plg, p, PlayerDataPayload.Type.LOGIN);
        data.pic = new PlayerInventoryContainer(plg, p);
        timer.sendData(data);
        // 結果が入っていない状態ではあるが、この時点でマップにインスタンスを保管しておく
        registerPlayerServer(p.getUniqueId(), data);
    }
    
    /**
     * ログイン情報の登録
     * @param uid
     * @param server 
     */
    public void registerPlayerServer(UUID uid, PlayerDataPayload server) {
        // ログイン情報を保存
        if (loginServer.containsKey(uid)) {
            loginServer.remove(uid);
        }
        loginServer.put(uid, server);
        log.log(Level.INFO, "Register player login information.[{0}]", server.player.getName());
    }
    
    /**
     * ログインサーバ情報の更新
     * @param uid
     * @param server 
     */
    public void updatePlayerServer(UUID uid, String server) {
        if (loginServer.containsKey(uid)) {
            loginServer.get(uid).serverName = server;
        }
    }

    /**
     * プレイヤーのイベント処理を許可する
     * @param uid 
     */
    public void allowEvent(UUID uid) {
        if (loginServer.containsKey(uid)) {
            loginServer.get(uid).allow_event = true;
        }
    }
    
    /**
     * プレイヤーのイベント処理が許可されているか確認する
     * @param uid
     * @return 
     */
    private boolean isAllowEvent(UUID uid) {
        if (loginServer.containsKey(uid)) {
            return loginServer.get(uid).allow_event;
        }
        return false;
    }
    
    /**
     * ・新規ユーザー
     * DB情報なし → ロードなし
     * 
     * ・既存ユーザー新規ログイン
     * DB情報有り → ログイン情報なし → ロード実行
     * 
     * ・既存ユーザーリログ
     * DB情報有り → ログイン情報有り → 待機 → ログイン情報なし → ロード実行
     * 
     * ・既存ユーザーサーバダウン
     * DB情報有り → ログイン情報有り → 待機 → (タイムアウト？) → ログイン情報なし → ロード実行
     * ※サーバダウンしたサーバのデータが最新
     * ※ダウンサーバーに一旦転送する？その場合ログイン情報有りの状態でリログ相当になるので待機してもだめなので一定時間でファイルデータ優先で動かすか
     * 
     * 
     * ・既存ユーザーサーバ移動
     * DB情報有り → ログイン情報有り → 待機 → ログイン情報なし → ロード実行
     * 
     * ・既存ユーザーサーバ移動中切断
     * DB情報有り → ログイン情報有り → ...切断... → ログイン情報なし → ロード実行
     * ※サーバは正常なのでセーブはされる
     * 
     */
    
    /**
     * ログイン情報不正ならインベントリクリック全般抑止する
     * @param e 
     */
    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        // ログイン情報がなし、またはイベント処理許可がfalseならばインベントリクリックは処理しない
        if (!isAllowEvent(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }
    /**
     * ログイン情報不正なら矢の回収を抑止する
     * @param e 
     */
    @EventHandler
    public void PlayerPickupArrowEvent(PlayerPickupArrowEvent e) {
        // プレイヤーが矢を拾った際のイベント、クリエイティブで発射した矢や無限弓の矢はクリエイティブで拾えるがその場合は発生しない
        // ログイン情報がなし、またはイベント処理許可がfalseならばアイテム取得は処理しない
        if (!isAllowEvent(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
    /**
     * ログイン情報不正ならアイテムの回収を抑止する
     * @param e 
     */
    @EventHandler
    public void EntityPickupItemEvent(EntityPickupItemEvent e) {
        // プレイヤーやその他エンティティが物を拾ったイベント、プレイヤーのピックアップイベントも併発する
        if (e.getEntity() instanceof Player) {
            // ログイン情報がなし、またはイベント処理許可がfalseならばアイテム取得は処理しない
            if (!isAllowEvent(e.getEntity().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * インベントリセーブ
     * @param p 
     */
    private void InventorySave(Player player, boolean clear) {
        // ログイン情報があり、かつ結果がtrueの場合のみ保存処理を呼び出す
        // サーバーログイン処理が終わりきっていない状態でのログアウトをガードする目的
        // →リロードでログイン情報が消えてしまうのが問題
        // →ログイン状態のチェック周りはTimer側に寄せることにする※serverNameが自サーバであることでログイン済みであることとする
        // 設定無効の判定はtimer側で判断
        PlayerDataPayload data = null;
        data = new PlayerDataPayload(plg, player, PlayerDataPayload.Type.SAVE_DATA);
        data.logout_flag = clear;
        //data.login_info = login;
        
        //if (login != null && login.result) {
            //data.serverName = login.serverName;
            // ログイン結果は付帯情報としてインスタンスを渡しておく
            data.login_info = loginServer.get(player.getUniqueId());
            data.saveSnapshot();
            ((EcoDataBridgeConfig) conf).startSaveInventory(player);
            AsyncSaveLoadTimer timer = (AsyncSaveLoadTimer) plg.getPluginTimer("data");
            log.log(Level.INFO, "send save request");
            timer.sendData(data);
//        } else {
//            data.saveSnapshot();
//            log.log(Level.SEVERE, "InventorySave failed.[{0}]", player.getName());
//            Utl.sendPluginMessage(plg, player, "ログイン情報が取得できなかったためインベントリ保存に失敗しました。");
//            Utl.sendPluginMessage(plg, player, "プレイヤー名と時刻、状況を管理者への連絡をお願いします。");
//            if (data != null) {
//                log.log(Level.SEVERE, "inv:{0}, ender:{1}", new Object[]{data.pic.inv, data.pic.ender});
//            }
//        }
    }

    /**
     * インベントリクローズ処理
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        // openinvの振る舞いは拾いきれないかも
        log.log(Level.INFO, "getPlayer():" + event.getPlayer().getName());
        log.log(Level.INFO, "getView().getPlayer():" + event.getView().getPlayer());
        if (event.getPlayer() instanceof Player) {
            InventorySave((Player) event.getPlayer(), false);
        }
    }

    /**
     * プレイヤーのサーバー退出処理
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        // セーブスタート
        log.log(Level.INFO, "Player quit save start");
        InventorySave(event.getPlayer(), true);
        // タイマの非同期保存後にインベントリクリアされる
        // @todo { と思ったけどここでクリアしないとサーバのプレイヤー保存に反映できないかも }
        event.getPlayer().getEnderChest().clear();
        event.getPlayer().getInventory().clear();
    }
         
}
