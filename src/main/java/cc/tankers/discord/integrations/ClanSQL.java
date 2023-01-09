package cc.tankers.discord.integrations;

import cc.tankers.discord.utils.data.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanSQL {
    public List<String> GetMembers () {
        List<String> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY points DESC;";
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                members.add(rs.getString("member") + ";" + rs.getInt("points"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return members;
    }

    public String GetMember (String name) {
        String member = "";
        String sql = "SELECT * FROM members WHERE member='"+name+"';";
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                member = rs.getString("member") + ";" + rs.getInt("points") + ";" + rs.getInt("id");
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return member;

    }

    public void AddMember (String name) {
        Execute("INSERT INTO members (member) VALUES ('"+name+"');");
    }

    public void RemoveMember (String name) {
        Execute("DELETE FROM members WHERE member='" + name +"';");
    }

    public List<String> GetItems () {
        List<String> items = new ArrayList<>();
        String sql = "SELECT * FROM items;";
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                items.add(rs.getString("item") + ";" + rs.getInt("count") + ";" + rs.getString("boss"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return items;
    }

    public void AddItem (String itemName, String bossName) {
        itemName = itemName.replace("'", "''");
        Execute("INSERT INTO items (item, boss) VALUES ('" + itemName + "','" + bossName + "');");
    }

    public void RemoveItem (String item) {
        item = item.replace("'", "''");
        Execute("DELETE FROM items WHERE item='" + item +"';");
    }

    public void AddPoints (String name, int value) {
        Execute("UPDATE members SET points=points+'" + value + "' WHERE member='" + name +"';");
    }

    public void RemovePoints (String name, int value) {
        Execute("UPDATE members SET points=points-'" + value + "' WHERE member='" + name +"';");
    }

    public void IncrementItemCount (String item) {
        item = item.replace("'", "''");
        Execute("UPDATE items SET count=count+1 WHERE item='" + item +"';");
    }

    public List<String> GetBosses () {
        List<String> bosses = new ArrayList<>();
        String sql = "SELECT * FROM bosses;";
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                bosses.add(rs.getString("boss") + ";" + rs.getString("embed_id"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return bosses;
    }

    public void AddBoss (String name, String embedId) {
        Execute("INSERT INTO bosses (boss, embed_id) VALUES ('" + name + "','" + embedId + "');");
    }

    public void RemoveBoss (String name) {
        Execute("DELETE FROM bosses WHERE boss='" + name +"';");
    }
    
    public void UpdateBoss (String name, String embedID) {
        Execute("UPDATE bosses SET embed_id='" + embedID + "' WHERE boss='" + name + "';");
    }
    
    void Execute (String sql) {
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
