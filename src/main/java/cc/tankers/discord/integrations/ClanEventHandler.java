package cc.tankers.discord.integrations;

import cc.tankers.discord.utils.EmbedUtil;
import cc.tankers.discord.utils.Logger;
import cc.tankers.discord.utils.data.Data;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class ClanEventHandler {
    static Map<String, Integer> pcScoreboard = new HashMap<>();

    static File pcFile = new File("./events/pvmchallenge.json");

    static File kotsFile = new File("./events/kots.json");

    public static void Initialize(JDA jda) {
        if (!(new File("./events")).exists()) new File("./events").mkdir();
        if (!(new File("./events/pvmchallenge")).exists()) new File("./events/pvmchallenge").mkdir();
        if (!(new File("./events/kots")).exists()) new File("./events/kots").mkdir();
        if (!Data.GetPCBoss().equalsIgnoreCase("none")) LoadPC();
    }

    public static void HandleEvent(SlashCommandInteraction event) {
        EmbedBuilder eb;
        switch (event.getSubcommandName()) {
            case "pvm-challenge":
                if (event.getOption("set").getAsString().equals("start")) {
                    if (!Data.GetPCBoss().equals("none")) {
                        EmbedBuilder embedBuilder = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge already running!").setFooter("Do `/events pvm-challenge stop` to stop it.");
                        (new EmbedUtil()).ReplyEmbed(event, embedBuilder, true, true);
                        return;
                    }
                    try {
                        String boss = event.getOption("boss").getAsString();
                        Data.SetPCBoss(boss);
                        EmbedBuilder dataEB = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge").setDescription("**Boss:** " + boss + "\n\n## rules ##");
                        String embedID = "";
                        MessageAction embedAction = Data.GetEventDataChannel(event.getJDA()).sendMessageEmbeds(dataEB.build());
                        try {
                            embedID = ((Message)embedAction.submit().get()).getId();
                        } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        Data.SetEventDataEmbed(embedID);
                        pcFile.createNewFile();
                        EmbedBuilder embedBuilder1 = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge").setDescription("**Event started!**\nBoss: " + boss);
                        (new EmbedUtil()).ReplyEmbed(event, embedBuilder1, true, false);
                    } catch (Exception e) {
                        EmbedBuilder embedBuilder = (new EmbedBuilder()).setTitle("[Tankers] Failed to start PvM Challenge!").setFooter("Make sure you're setting the boss when starting.");
                        (new EmbedUtil()).ReplyEmbed(event, embedBuilder, true, true);
                    }
                    break;
                }
                if (Data.GetPCBoss().equalsIgnoreCase("none")) {
                    EmbedBuilder embedBuilder = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge isn't running!").setFooter("Do `/events pvm-challenge set:start boss:[boss]` to start it.");
                    (new EmbedUtil()).ReplyEmbed(event, embedBuilder, true, true);
                    return;
                }
                ClosePC(event.getJDA());
                Data.GetEventDataChannel(event.getJDA()).deleteMessageById(Data.GetEventDataEmbed()).queue();
                Data.SetEventDataEmbed("");
                eb = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge").setDescription("**Event ended!**");
                (new EmbedUtil()).ReplyEmbed(event, eb, true, false);
                break;
        }
    }

    public static void SubmitDrop(JDA jda, String[] players, int points) {
        for (String player : players) {
            if (pcScoreboard.containsKey(player)) {
                pcScoreboard.put(player, pcScoreboard.get(player).intValue() + points);
            } else {
                pcScoreboard.put(player, points);
            }
        }
        pcScoreboard = Sort(pcScoreboard);
        String playerBlob = "";
        String pointsBlob = "";
        for (String key : pcScoreboard.keySet()) {
            playerBlob = playerBlob.concat(key + "\n");
            pointsBlob = pointsBlob.concat("" + pcScoreboard.get(key) + "\n");
        }
        EmbedBuilder dataEB = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge").setDescription("**Boss:** " + Data.GetPCBoss() + "\n## rules ##\n").addField("Player:", playerBlob, true).addField("Points:", pointsBlob, true);
        Data.GetEventDataChannel(jda).editMessageEmbedsById(Data.GetEventDataEmbed(), new MessageEmbed[] { dataEB.build() }).queue();
        SavePC();
    }

    public static void ClosePC(JDA jda) {
        File newPCFile = new File("./events/pvmchallenge/" + Data.GetPCBoss() + "-" + LocalDate.now() + ".json");
        try {
            (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValue(newPCFile, pcScoreboard);
            Logger.log("[+] PvM Challenge data archived in `./events/`.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to archive PvM Challenge data!\n" + e, 1);
        }
        String playerBlob = "";
        String pointsBlob = "";
        int count = 0;
        for (String key : pcScoreboard.keySet()) {
            if (count == 3)
                break;
            playerBlob = playerBlob.concat(key + "\n");
            pointsBlob = pointsBlob.concat("" + pcScoreboard.get(key) + "\n");
            count++;
        }
        EmbedBuilder dataEB = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge - " + Data.GetPCBoss()).addField("Player:", playerBlob, true).addField("Points:", pointsBlob, true);
        Data.GetEventLBChannel(jda).sendMessageEmbeds(dataEB.build(), new MessageEmbed[0]).queue();
        Data.SetPCBoss("none");
        pcFile.delete();
    }

    static void SavePC() {
        try {
            (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValue(pcFile, pcScoreboard);
            Logger.log("[+] PvM Challenge data saved.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to save PvM Challenge data!\n" + e, 1);
        }
    }

    static void LoadPC() {
        MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        try {
            pcScoreboard = new ObjectMapper().readValue(pcFile, type);
            Logger.log("[+] PvM Challenge data loaded.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to load PvM Challenge data!\n" + e, 1);
        }
    }

    public void StartKOTS() {}

    public static <K, V extends Comparable<? super V>> Map<K, V> Sort(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }
}
