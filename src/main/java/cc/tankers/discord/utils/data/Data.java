package cc.tankers.discord.utils.data;

import cc.tankers.discord.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Data {
    private static Config config = new Config();

    static final File configFile = new File("./data/config.json");

    public void Load() {
        //Validate path
        new File("./data").mkdir();

        //Create config.json if needed
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Save();
                Logger.log("[+] config.json successfully created.", 1);
            } catch (IOException e) { Logger.log("[-] Failed to build config.json!\n" + e, 1); }
        }
        try {
            config = new ObjectMapper().readValue(configFile, Config.class);
            Logger.log("[+] Config loaded.", 1);
        } catch (IOException e) { Logger.log("[-] Failed to load config.json!\n" + e, 1); }
    }

    public static void Save() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Guild GetGuildPublic(JDA jda) {
        return jda.getGuildById(config.getGuildPublic());
    }

    public static void SetGuildPublic(Guild guild) {
        config.setGuildPublic(guild.getId());
        Save();
    }

    public static Guild GetGuildPrivate(JDA jda) {
        return jda.getGuildById(config.getGuildPrivate());
    }

    public static void SetGuildPrivate(Guild guild) {
        config.setGuildPrivate(guild.getId());
        Save();
    }

    public static ArrayList<Role> GetModRoles(JDA jda) {
        ArrayList<Role> roles = new ArrayList<>();
        String modRoles = config.getModRoles();
        if (modRoles.equals(""))
            return roles;
        for (String role : config.getModRoles().split(","))
            roles.add(jda.getRoleById(role));
        return roles;
    }

    public static void SetModRole(ArrayList<Role> roles) {
        String blob = "";
        if (roles.size() > 0) {
            for (Role role : roles)
                blob = blob + blob + ",";
            blob = blob.substring(0, blob.length() - 1);
        }
        config.setModRoles(blob);
        Save();
    }

    public static TextChannel GetModLogChannel(JDA jda) {
        return jda.getTextChannelById(config.getModChannel());
    }

    public static void SetModLogChannel(TextChannel channel) {
        config.setModChannel(channel.getId());
        Save();
    }

    public static TextChannel GetDataPublicChannel(JDA jda) {
        return jda.getTextChannelById(config.getDataPublicChannel());
    }

    public static void SetDataPublicChannel(TextChannel channel) {
        config.setDataPublicChannel(channel.getId());
        Save();
    }

    public static TextChannel GetDataPrivateChannel(JDA jda) {
        return jda.getTextChannelById(config.getDataPrivateChannel());
    }

    public static void SetDataPrivateChannel(TextChannel channel) {
        config.setDataPrivateChannel(channel.getId());
        Save();
    }

    public static Role GetPollRole(JDA jda) {
        return jda.getRoleById(config.getPollRole());
    }

    public static void SetPollRole(Role role) {
        config.setPollRole(role.getId());
        Save();
    }

    public static TextChannel GetPollChannel(JDA jda) {
        return jda.getTextChannelById(config.getPollChannel());
    }

    public static void SetPollChannel(TextChannel channel) {
        config.setPollChannel(channel.getId());
        Save();
    }

    public static TextChannel GetApprovalChannel(JDA jda) {
        return jda.getTextChannelById(config.getApprovalChannel());
    }

    public static void SetApprovalChannel(TextChannel channel) {
        config.setApprovalChannel(channel.getId());
        Save();
    }

    public static TextChannel GetPlayerDataChannel(JDA jda) {
        return jda.getTextChannelById(config.getPlayerDataChannel());
    }

    public static void SetPlayerDataChannel(TextChannel channel) {
        config.setPlayerDataChannel(channel.getId());
        Save();
    }

    public static TextChannel GetDropDataChannel(JDA jda) {
        return jda.getTextChannelById(config.getDropDataChannel());
    }

    public static void SetDropDataChannel(TextChannel channel) {
        config.setDropDataChannel(channel.getId());
        Save();
    }

    public static String GetPlayerDataEmbed(JDA jda) {
        return config.getPlayerDataEmbed();
    }

    public static void SetPlayerDataEmbed(String id) {
        config.setPlayerDataEmbed(id);
        Save();
    }

    public static TextChannel GetLootChannel(JDA jda) {
        return jda.getTextChannelById(config.getLootChannel());
    }

    public static void SetLootChannel(TextChannel channel) {
        config.setLootChannel(channel.getId());
        Save();
    }

    public static TextChannel GetEventDataChannel(JDA jda) {
        return jda.getTextChannelById(config.getEventDataChannel());
    }

    public static void SetEventDataChannel(TextChannel channel) {
        config.setEventDataChannel(channel.getId());
        Save();
    }

    public static String GetEventDataEmbed() {
        return config.getEventDataEmbed();
    }

    public static void SetEventDataEmbed(String id) {
        config.setEventDataEmbed(id);
        Save();
    }

    public static TextChannel GetEventLBChannel(JDA jda) {
        return jda.getTextChannelById(config.getEventLBChannel());
    }

    public static void SetEventLBChannel(TextChannel channel) {
        config.setEventLBChannel(channel.getId());
        Save();
    }

    public static String GetPCBoss() {
        return config.getPCBoss();
    }

    public static void SetPCBoss(String boss) {
        config.setPCBoss(boss);
        Save();
    }

    public static String GetKOTSSkill() {
        return config.getKOTSSkill();
    }

    public static void SetKOTSSkill(String skill) {
        config.setKOTSSkill(skill);
        Save();
    }

    public static boolean GetDebug() {
        return config.isDebug();
    }

    public static void SetDebug(boolean set) {
        config.setDebug(set);
        Save();
    }
}
