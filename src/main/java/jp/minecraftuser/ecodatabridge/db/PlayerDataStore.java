
package jp.minecraftuser.ecodatabridge.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecodatabridge.container.PlayerInventoryContainer;
import jp.minecraftuser.ecodatabridge.container.PlayerStatusContainer;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.db.CTYPE;
import org.bukkit.entity.Player;


/**
 * プレイヤー固有ファイル保存
 * @author ecolight
 */
public class PlayerDataStore extends DatabaseFrame {

    public PlayerDataStore(PluginFrame plg_, String dbfilepath_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, dbfilepath_, name_);
    }

    public PlayerDataStore(PluginFrame plg_, String server_, String user_, String pass_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, server_, user_, pass_, dbname_, name_);
    }

    /**
     * データベース移行処理
     * 基底クラスからDBをオープンするインスタンスの生成時に呼ばれる
     * 
     * @throws SQLException
     */
    @Override
    protected void migrationData(Connection con) throws SQLException  {
        // 全体的にテーブル操作になるため、暗黙的コミットが走り失敗してもロールバックが効かない
        // 十分なテストの後にリリースするか、何らかの形で異常検知し、DBバージョンに従い元に戻せるようテーブル操作順を考慮する必要がある
        // 本処理においては取り敢えずロールバックは諦める
        
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、初版のテーブルのみ作成して終わり

                // インベントリ情報保管テーブル
                MessageFormat mf = new MessageFormat("CREATE TABLE IF NOT EXISTS inventory(most {0} NOT NULL, least {1} NOT NULL, inv {2} NOT NULL, ender {3} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[inventory].");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[inventory].");

                // ステータス情報保管テーブル
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS status(most {0} NOT NULL, least {1} NOT NULL, stat {2} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[status].");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[inventory].");

                // ユーザ設定保存テーブル（任意のkey:value保存用）
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS settings(most {0} NOT NULL, least {1} NOT NULL, key {2} NOT NULL, value {3} NOT NULL, PRIMARY KEY(most, least, key))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[settings].");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[settings].");

                // 実績保管用テーブル
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS advancement(most {0} NOT NULL, least {1} NOT NULL, adv {2} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[advancement].");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[advancement].");

                // 統計保管用テーブル
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS statistic(most {0} NOT NULL, least {1} NOT NULL, statistic {2} NOT NULL)");
                try {
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[statistic].");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[statistic].");

                log.log(Level.INFO, "{0}DataBase checked.", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion.");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "create {0} version {1}", new Object[]{name, dbversion});
            } else {
                // 既存DB引き継ぎの場合はdbversionだけ上げてv2->3の処理へ
                log.log(Level.INFO, "convert {0} version 1 -> 2 start", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion 1 -> 2.");
                    Logger.getLogger(PlayerDataStore.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "convert {0} version 1 -> 2 complete", name);
            }
        }
        // Version 2 -> 3
//        if (dbversion == 2) {
//            log.log(Level.INFO, "convert {0} version {1} -> {2} start", new Object[]{name, dbversion, dbversion + 1});
//            // ユーザー状態テーブル追加
//            MessageFormat mf = new MessageFormat("CREATE TABLE IF NOT EXISTS playerstats(most {0} NOT NULL, least {1} NOT NULL, logout {2} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
//            // ユーザー統計データデーブル追加
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS statstable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//            // ユーザー実績データテーブル追加
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS advtable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//            // 既存テーブルからlogoutを分離(playerstatsへ)、
//            renameTable("datatable", "datatable_");
//            mf = new MessageFormat("CREATE TABLE IF NOT EXISTS datatable(most {0} NOT NULL, least {1} NOT NULL, name {2} NOT NULL, size {3} NOT NULL, data {4} NOT NULL, PRIMARY KEY(most, least))");
//            executeStatement(mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.BLOB.get(jdbc)}));
//
//            PreparedStatement prep = con.prepareStatement("SELECT * FROM datatable_");
//            PreparedStatement prep2 = con.prepareStatement("REPLACE INTO datatable VALUES (?, ?, ?, ?, ?)");
//            PreparedStatement prep3 = con.prepareStatement("REPLACE INTO playerstats VALUES (?, ?, ?)");
//            try {
//                // 実行
//                ResultSet rs = prep.executeQuery();
//                try {
//                    // 結果取得
//                    while (rs.next()) {
//                        prep2.setLong(1, rs.getLong("most"));
//                        prep2.setLong(2, rs.getLong("least"));
//                        prep2.setString(3, rs.getString("name"));
//                        prep2.setLong(4, rs.getLong("size"));
//                        prep2.setBlob(5, rs.getBlob("data"));
//                        prep3.setLong(1, rs.getLong("most"));
//                        prep3.setLong(2, rs.getLong("least"));
//                        prep3.setLong(3, rs.getLong("logout"));
//                        prep2.executeUpdate();
//                        prep3.executeUpdate();
//                    }
//                } catch (SQLException ex) {
//                    // PreparedStatementがcloseできればカーソルリークしないはずだが、
//                    // 念のため確実にResultSetをcloseするようにしておく
//                    rs.close();
//                    // 投げなおして上位で異常検知させる
//                    throw ex;
//                }
//                // 後処理
//                rs.close();
//            } catch (SQLException ex) {
//                prep.close();
//                prep2.close();
//                prep3.close();
//                throw ex;
//                // ロールバックは上位のスーパークラスでやる
//            }
//            prep.close();
//            dropTable("datatable_");
//
//            updateSettingsVersion();
//            log.log(Level.INFO, "convert {0} version {1} -> {2} complete", new Object[]{name, dbversion - 1, dbversion});
//        }
    }

    /**
     *設定保存（プレイヤー指定）
     * @param con
     * @param uuid
     * @param key
     * @param value
     * @throws SQLException
     */
    public void updateSetting(Connection con, UUID uuid, String key, String value) throws SQLException {
        
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM settings WHERE most = ? AND least = ? AND key = ?");
        PreparedStatement prep2 = con.prepareStatement("UPDATE settings SET value = ? WHERE most = ? AND least = ? AND key = ?");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO settings VALUES(?, ?, ?, ?)");
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            prep1.setString(3, key);
            ResultSet rs = prep1.executeQuery();
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            
            if (hit) {
                // update
                prep2.setString(1, value);
                prep2.setLong(2, uuid.getMostSignificantBits());
                prep2.setLong(3, uuid.getLeastSignificantBits());
                prep2.setString(4, key);
                // 実行
                prep2.executeUpdate();
            } else { 
                // insert
                prep3.setLong(1, uuid.getMostSignificantBits());
                prep3.setLong(2, uuid.getLeastSignificantBits());
                prep3.setString(3, key);
                prep3.setString(4, value);
                // 実行
                prep3.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }
    
    /**
     * 設定取得
     * @param con 
     * @param uuid 
     * @param key 
     * @return  
     * @throws SQLException
     */
    public String getSetting(Connection con, UUID uuid, String key) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM settings WHERE most = ? AND least = ? AND key = ?");
        String result = "";
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            prep1.setString(3, key);
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    result = rs.getString("value");
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        return result;
    }

    /**
     * インベントリ保存
     * @param con
     * @param pic
     * @throws SQLException
     */
    public void updateInventory(Connection con, PlayerInventoryContainer pic) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM inventory WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("UPDATE inventory SET inv = ?, ender = ? WHERE most = ? AND least = ?");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO inventory VALUES(?, ?, ?, ?)");
        try {
            prep1.setLong(1, pic.uuid.getMostSignificantBits());
            prep1.setLong(2, pic.uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            long count = 0;
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            log.log(Level.INFO, "DB save :::::::::::::::::::::::::::::::::: "+pic.inv);
            log.log(Level.INFO, "DB save :::::::::::::::::::::::::::::::::: "+pic.ender);
            if (hit) {
                // update
                prep2.setString(1, pic.inv);
                prep2.setString(2, pic.ender);
                prep2.setLong(3, pic.uuid.getMostSignificantBits());
                prep2.setLong(4, pic.uuid.getLeastSignificantBits());
                // 実行
                prep2.executeUpdate();
            } else { 
                // insert
                prep3.setLong(1, pic.uuid.getMostSignificantBits());
                prep3.setLong(2, pic.uuid.getLeastSignificantBits());
                prep3.setString(3, pic.inv);
                prep3.setString(4, pic.ender);
                // 実行
                prep3.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }

    /**
     * ステータス保存
     * @param con
     * @param stat
     * @throws SQLException
     */
    public void updateStatus(Connection con, PlayerStatusContainer stat) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM status WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("UPDATE status SET stat = ? WHERE most = ? AND least = ?");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO status VALUES(?, ?, ?)");
        try {
            prep1.setLong(1, stat.uuid.getMostSignificantBits());
            prep1.setLong(2, stat.uuid.getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            long count = 0;
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            
            if (hit) {
                // update
                prep2.setString(1, stat.statusStore);
                prep2.setLong(2, stat.uuid.getMostSignificantBits());
                prep2.setLong(3, stat.uuid.getLeastSignificantBits());
                // 実行
                prep2.executeUpdate();
            } else { 
                // insert
                prep3.setLong(1, stat.uuid.getMostSignificantBits());
                prep3.setLong(2, stat.uuid.getLeastSignificantBits());
                prep3.setString(3, stat.statusStore);
                // 実行
                prep3.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }

    /**
     * プレイヤーインベントリロード
     * @param con 
     * @param p 
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public void loadPlayerInventory(Connection con, Player p) throws SQLException, IOException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM inventory WHERE most = ? AND least = ?");
        try {
            prep1.setLong(1, p.getUniqueId().getMostSignificantBits());
            prep1.setLong(2, p.getUniqueId().getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    PlayerInventoryContainer container = new PlayerInventoryContainer(plg, p);
                    container.load(rs.getString("inv"), rs.getString("ender"));
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
    }
    /**
     * プレイヤーインベントリロード
     * @param con 
     * @param p 
     * @return  
     * @throws SQLException
     */
    public PlayerInventoryContainer loadPlayerInventoryContainer(Connection con, Player p) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM inventory WHERE most = ? AND least = ?");
        PlayerInventoryContainer result = null;
        try {
            prep1.setLong(1, p.getUniqueId().getMostSignificantBits());
            prep1.setLong(2, p.getUniqueId().getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    result = new PlayerInventoryContainer(plg, p, rs.getString("inv"), rs.getString("ender"));
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        
        return result;
    }
    /**
     * プレイヤーインベントリロード
     * @param con 
     * @param p 
     * @return  
     * @throws SQLException
     */
    public PlayerStatusContainer loadPlayerStatusContainer(Connection con, Player p) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM status WHERE most = ? AND least = ?");
        PlayerStatusContainer result = null;
        try {
            prep1.setLong(1, p.getUniqueId().getMostSignificantBits());
            prep1.setLong(2, p.getUniqueId().getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    result = new PlayerStatusContainer(plg, p, rs.getString("stat"));
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        
        return result;
    }
}
