
package jp.minecraftuser.ecodatabridge.timer;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecodatabridge.container.PlayerStatusContainer;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class PlayerDataPayload extends PayloadFrame {
    public boolean result = false;
    public Type type;
    public Type reloadtype;
    private PluginFrame plg;
    public ConcurrentHashMap<UUID, String> onlinePlayers;

    // 単一プレイヤー用
    public Player player;
    public PlayerInventoryContainer pic;
    public PlayerStatusContainer status;

    // 複数プレイヤー用
    public Player[] players;
    public HashMap<Player, PlayerInventoryContainer> pics;
    public HashMap<Player, PlayerStatusContainer> statuses;

    // その他実行種別ごとの必要な情報
    public boolean logout_flag = false;     // ログアウトセーブ時にログインサーバ情報を破棄する設定
    public String msg;                      // 
    public String serverName = "";          // ログインサーバ名
    //public PlayerDataPayload login_info;
    public boolean allow_event = false;     // イベントの処理を許可するかどうかの情報、ログイン時のペイロードを保存しておき参照する
    public String enabled = "false";        // ログイン時、プレイヤーのデータ連携が有効かどうかを示す値、格納テーブルの都合で文字列として処理する
    public long retryStart = 0;             // 他サーバでのプレイヤーデータ保存を待機してる間、プレイヤーデータの読み込みを待機する時間
    public PlayerDataPayload login_info = null;

    // 処理種別を追加した場合、AsyncSaveLoadTimer の initTask に処理クラスを登録すること
    public enum Type {
        NONE,
        RESET,
        CMD_JOIN,
        CMD_LEAVE,
        SAVE_DATA,
        SAVE_DATA_ALL,
        LOGIN,
        STOP,
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param player_
     * @param type_
     */
    public PlayerDataPayload(PluginFrame plg_, Player player_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        player = player_;
        pics = new HashMap<>();
        statuses = new HashMap<>();
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param players_
     * @param type_
     */
    public PlayerDataPayload(PluginFrame plg_, Player[] players_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        players = players_;
        pics = new HashMap<>();
        statuses = new HashMap<>();
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param type_
     */
    public PlayerDataPayload(PluginFrame plg_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        pics = new HashMap<>();
    }
    
    /**
     * メソッド実行時点でのプレイヤー情報を内部に保持する
     */
    public void saveSnapshot() {
        if (player != null) {
            pic = new PlayerInventoryContainer(plg, player);
            pic.save();
            status = new PlayerStatusContainer(plg, player);
            status.save();
        }
        if (players != null) {
            pics.clear();
            statuses.clear();
            for (Player p : players) {
                // インベントリ情報を保存
                PlayerInventoryContainer inv = new PlayerInventoryContainer(plg, p);
                inv.save();
                pics.put(p, inv);
                // ステータス情報を保存
                PlayerStatusContainer stat = new PlayerStatusContainer(plg, p);
                stat.save();
                statuses.put(p, stat);
            }
        }
    }
    
    /**
     * コマンドの再キュー指定を子スレッドから親スレッドに依頼する
     */
    public void request_reset() {
        reloadtype = type;
        type = Type.RESET;
    }
    
    /**
     * 依頼されたリセットを実行
     */
    public void reset() {
        type = reloadtype;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PlayerDataPayload->");
        if (player != null) {
            sb.append("[").append(player.getName()).append("]");
        }
        if (pic != null) {
            if (pic.inv != null) sb.append(" inv:").append(pic.inv);
            if (pic.ender != null) sb.append(" ender:").append(pic.ender);
        }
        return sb.toString();
    }
}
