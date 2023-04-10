package cc.tankers.discord.integrations;

import cc.tankers.discord.utils.EmbedUtil;
import cc.tankers.discord.utils.Logger;
import cc.tankers.discord.utils.data.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.FutureTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class ClanEventHandler {
    static Map<String, Integer> pcScoreboard = new HashMap<>();
    static Map<String, Integer> kotsStarting = new HashMap<>();
    static Map<String, Integer> kotsGains = new HashMap<>();

    static File pcFile = new File("./events/pvmchallenge.json");

    static File kotsFile = new File("./events/kots.json");
    static Timer timer = new Timer();

    public static void Initialize(JDA jda) {
        if (!(new File("./events")).exists()) new File("./events").mkdir();
        if (!(new File("./events/pvmchallenge")).exists()) new File("./events/pvmchallenge").mkdir();
        if (!(new File("./events/kots")).exists()) new File("./events/kots").mkdir();
        if (!Data.GetPCBoss().equalsIgnoreCase("none")) LoadPC();
        if (!Data.GetKOTSSkill().equalsIgnoreCase("none")) LoadKOTS(jda);
    }

    public static void HandleEvent(SlashCommandInteraction event) {
        event.deferReply(true);
        switch (event.getOption("event").getAsString()) {
            case "pvm-challenge" -> {
                if (event.getOption("set").getAsString().equals("start")) {
                    // Check if already running
                    if (!Data.GetPCBoss().equals("none") || !Data.GetKOTSSkill().equals("none")) {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("[Tankers] An event is already running!")
                                .setFooter("Do `/events [event] stop` to stop it.");
                        new EmbedUtil().ReplyEmbed(event, embedBuilder, true, true);
                        return;
                    }
                    try {
                        // Set boss (start event)
                        pcScoreboard = new HashMap<>();
                        String boss = event.getOption("boss").getAsString();
                        Data.SetPCBoss(boss);
                        EmbedBuilder dataEB = new EmbedBuilder()
                                .setTitle("[Tankers] PvM Challenge")
                                .setDescription("**Boss:** " + boss + "\n**Rewards:**\n1st - 5.5m\n2nd - 3m\n3rd - 1.5m\n\nAny pet earned during that week gets an extra 10m!");
                        String embedID = "";
                        MessageAction embedAction = Data.GetEventDataChannel(event.getJDA()).sendMessageEmbeds(dataEB.build());
                        try {
                            embedID = embedAction.submit().get().getId();
                        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        Data.SetEventDataEmbed(embedID);
                        pcFile.createNewFile();
                        Data.GetEventDataChannel(event.getJDA()).upsertPermissionOverride(event.getGuild().getPublicRole()).setAllowed(Permission.VIEW_CHANNEL).queue();
                        EmbedBuilder embedBuilder1 = new EmbedBuilder()
                                .setTitle("[Tankers] PvM Challenge")
                                .setDescription("**Event started!**\nBoss: " + boss);
                        new EmbedUtil().ReplyEmbed(event, embedBuilder1, true, false);
                    } catch (Exception e) {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("[Tankers] Failed to start PvM Challenge!")
                                .setFooter("Make sure you're setting the boss when starting.");
                        new EmbedUtil().ReplyEmbed(event, embedBuilder, true, true);
                    }
                } else if (event.getOption("set").getAsString().equals("stop")) {
                    if (Data.GetPCBoss().equals("none")) {
                        EmbedBuilder embedBuilder = (new EmbedBuilder()).setTitle("[Tankers] PvM Challenge isn't running!").setFooter("Do `/events pvm-challenge set:start boss:[boss]` to start it.");
                        (new EmbedUtil()).ReplyEmbed(event, embedBuilder, true, true);
                        return;
                    }
                    ClosePC(event.getJDA());
                    Data.GetEventDataChannel(event.getJDA()).deleteMessageById(Data.GetEventDataEmbed()).queue();
                    Data.SetEventDataEmbed("");
                    Data.GetEventDataChannel(event.getJDA()).upsertPermissionOverride(event.getGuild().getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue();

                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[Tankers] PvM Challenge")
                            .setDescription("**Event ended!**");
                    new EmbedUtil().ReplyEmbed(event, eb, true, false);
                }
            }
            case "kots" -> {
                if (event.getOption("set").getAsString().equals("start")) {
                    if (!Data.GetKOTSSkill().equals("none") || !Data.GetPCBoss().equals("none")) {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("[Tankers] An event is already running!")
                                .setFooter("Do `/events [event] stop` to stop it.");
                        new EmbedUtil().ReplyEmbed(event, embedBuilder, true, true);
                        return;
                    }
                    try {
                        // Set skill (start event)
                        kotsStarting = new HashMap<>();
                        kotsGains = new HashMap<>();
                        String skill = event.getOption("skill").getAsString();
                        Data.SetKOTSSkill(skill);
                        EmbedBuilder dataEB = new EmbedBuilder()
                                .setTitle("[Tankers] King of the Skill")
                                .setDescription("**Skill:** " + skill + "\n**Rewards:**\n1st - 5.5m\n2nd - 3m\n3rd - 1.5m\n\n WE USE JAGEX HIGHSCORES! Your name has to be correct in `#" + Data.GetPlayerDataChannel(event.getJDA()).getName() + "` to participate! If you have points in the cc you're good. If you are new, just ask an admin!");
                        String embedID = "";
                        MessageAction embedAction = Data.GetEventDataChannel(event.getJDA()).sendMessageEmbeds(dataEB.build());
                        try {
                            embedID = embedAction.submit().get().getId();
                        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        Data.SetEventDataEmbed(embedID);
                        kotsFile.createNewFile();
                        Data.GetEventDataChannel(event.getJDA()).upsertPermissionOverride(event.getGuild().getPublicRole()).setAllowed(Permission.VIEW_CHANNEL).queue();
                        EmbedBuilder embedBuilder1 = new EmbedBuilder()
                                .setTitle("[Tankers] King of the Skill")
                                .setDescription("**Event started!**\nSkill: " + skill)
                                .setFooter("It will take a few minutes to fetch current XP and populate the event embed.");
                        new EmbedUtil().ReplyEmbed(event, embedBuilder1, true, false);
                        StartKOTS(event.getJDA());
                    } catch (Exception e) {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("[Tankers] Failed to start King of the Skill!")
                                .setFooter("Make sure you're setting the skill when starting.");
                        new EmbedUtil().SendEmbed(Data.GetEventDataChannel(event.getJDA()), embedBuilder, true);
                        System.out.println(e);
                    }
                } else {
                    if (Data.GetKOTSSkill().equals("none")) {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("[Tankers] King of the Skill isn't running!")
                                .setFooter("Do `/events kots set:start skill:[skill]` to start it.");
                        new EmbedUtil().ReplyEmbed(event, embedBuilder, true, true);
                        return;
                    }

                    EmbedBuilder ebEnd = new EmbedBuilder()
                            .setTitle("[Tankers] King of the Skill")
                            .setDescription("**Event ended!**")
                            .setFooter("May take a few minutes to fetch final XP values. See winner results in #" + Data.GetEventLBChannel(event.getJDA()).getName());
                    new EmbedUtil().ReplyEmbed(event, ebEnd, true, false);

                    CloseKOTS(event.getJDA());
                }
            }
        }
    }

    public static void SubmitDrop(JDA jda, List<String> players, int points) {
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
        EmbedBuilder dataEB = new EmbedBuilder()
                .setTitle("[Tankers] PvM Challenge")
                .setDescription("**Boss:** " + Data.GetPCBoss() + "\n**Rewards:**\n1st - 5.5m\n2nd - 3m\n3rd - 1.5m\n\nAny pet earned during that week gets an extra 10m!")
                .addField("Player:", playerBlob, true)
                .addField("Points:", pointsBlob, true);
        Data.GetEventDataChannel(jda).editMessageEmbedsById(Data.GetEventDataEmbed(), dataEB.build()).queue();
        SavePC();
    }

    public static void ClosePC(JDA jda) {
        pcScoreboard = Sort(pcScoreboard);
        File newPCFile = new File("./events/pvmchallenge/" + Data.GetPCBoss() + "-" + LocalDate.now() + ".json");
        try {
            (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValue(newPCFile, pcScoreboard);
            Logger.log("[+] PvM Challenge data archived in `./events/pvmchallenge/`.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to archive PvM Challenge data!\n" + e, 1);
        }

        String playerBlob = "";
        String pointsBlob = "";
        int count = 0;
        for (String key : pcScoreboard.keySet()) {
            if (count == 5)
                break;
            playerBlob = playerBlob.concat((count+1) + ". " + key + "\n");
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

    public static void StartKOTS(JDA jda) {
        String skill = Data.GetKOTSSkill();

        for (String member : new ClanSQL().GetMembers()) {
            member = member.split(";")[0];
            String requestURL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + member;
            try {
                String[] data = GameIntegrationHandler.request(requestURL).split("\n");
                String[] dataMap = { "Overall", "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking",
                        "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore",
                        "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction" };
                for (int i = 1; i < 24; i++) {
                    if (dataMap[i].equals(skill)) {
                        kotsStarting.put(member, Integer.parseInt(data[i].split(",")[2]));
                        break;
                    }
                }
            } catch (IOException e) { continue; };
        }
        SaveKOTS();
        UpdateKOTSData(jda);
    }

    public static void UpdateKOTSData (JDA jda) {
        FutureTask<Void> task = new FutureTask<>(() -> KOTSTimer(jda), null); // Create refresh timer
        String skill = Data.GetKOTSSkill();

        for (String key : kotsStarting.keySet()) {
            String requestURL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + key;
            try {
                String[] data = GameIntegrationHandler.request(requestURL).split("\n");
                String[] dataMap = { "Overall", "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking",
                        "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore",
                        "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction" };
                for (int i = 1; i < 24; i++) {
                    if (dataMap[i].equals(skill)) {
                        kotsGains.put(key, Integer.parseInt(data[i].split(",")[2]) - kotsStarting.get(key));
                        break;
                    }
                }
            } catch (IOException e) { new Thread(task).start(); }
        }

        kotsGains = Sort(kotsGains);

        // Update embeds
        String playerBlob = "";
        String startingXPBlob = "";
        String gainedXPBlob = "";
        for (String key : kotsGains.keySet()) {
            playerBlob = playerBlob.concat(key + "\n");
            startingXPBlob = startingXPBlob.concat("" + kotsStarting.get(key) + "\n");
            gainedXPBlob = gainedXPBlob.concat("" + kotsGains.get(key) + "\n");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        EmbedBuilder dataEB = new EmbedBuilder()
                .setTitle("[Tankers] King of the Skill")
                .setDescription("**Skill:** " + skill + "\n**Rewards:**\n1st - 5.5m\n2nd - 3m\n3rd - 1.5m\n\n WE USE JAGEX HIGHSCORES! Your name has to be correct in `#" + Data.GetPlayerDataChannel(jda).getName() + "` to participate! If you have points in the cc you're good. If you are new, just ask an admin!")
                .addField("Player:", playerBlob, true)
                .addField("Starting XP:", startingXPBlob, true)
                .addField("Gained XP:", gainedXPBlob, true)
                .setFooter("Last updated " + LocalDateTime.now().format(formatter));
        Data.GetEventDataChannel(jda).editMessageEmbedsById(Data.GetEventDataEmbed(),dataEB.build()).queue();

        new Thread(task).start();
    }

    static void KOTSTimer (JDA jda) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                UpdateKOTSData(jda);
            }
        };
        timer.schedule(task, 60000L);
    }

    public static void CloseKOTS(JDA jda) {
        UpdateKOTSData(jda);
        timer.cancel();
        kotsGains = Sort(kotsGains);

        Data.GetEventDataChannel(jda).deleteMessageById(Data.GetEventDataEmbed()).queue();
        Data.SetEventDataEmbed("");
        Data.GetEventDataChannel(jda).upsertPermissionOverride(Data.GetGuildPublic(jda).getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue();

        File newKOTSFile = new File("./events/kots/" + Data.GetKOTSSkill() + "-" + LocalDate.now() + ".json");
        Map<String, Map<String, Integer>> dataMap = new HashMap<>();
        for (String key : kotsGains.keySet()) {
            Map <String, Integer> innerMap = new HashMap<>();
            innerMap.put("startingXP", kotsStarting.get(key));
            innerMap.put("gainedXP", kotsGains.get(key));
            dataMap.put(key, innerMap);
        }

        try {
            (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValue(newKOTSFile, dataMap);
            Logger.log("[+] KOTS data archived in `./events/kots/`.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to archive KOTS data!\n" + e, 1);
        }

        String playerBlob = "";
        String pointsBlob = "";
        int count = 0;
        for (String key : kotsGains.keySet()) {
            if (kotsGains.get(key).equals(0)) continue;
            if (count == 3)
                break;
            playerBlob = playerBlob.concat((count+1) + ". " + key + "\n");
            pointsBlob = pointsBlob.concat("" + kotsGains.get(key) + "\n");
            count++;
        }
        EmbedBuilder dataEB = new EmbedBuilder()
                .setTitle("[Tankers] King of the Skill - " + Data.GetKOTSSkill())
                .addField("Player:", playerBlob, true)
                .addField("Gained XP:", pointsBlob, true);
        Data.GetEventLBChannel(jda).sendMessageEmbeds(dataEB.build()).queue();
        Data.SetKOTSSkill("none");
        kotsFile.delete();
    }

    static void SaveKOTS() {
        try {
            (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValue(kotsFile, kotsStarting);
            Logger.log("[+] KOTS data saved.", 1);
        } catch (IOException e) {
            Logger.log("[-] Failed to save KOTS data!\n" + e, 1);
        }
    }

    static void LoadKOTS(JDA jda) {
        MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        try {
            kotsStarting = new ObjectMapper().readValue(kotsFile, type);
            Logger.log("[+] KOTS data loaded.", 1);
            UpdateKOTSData(jda);
        } catch (IOException e) {
            Logger.log("[-] Failed to load KOTS data!\n" + e, 1);
        }
    }

    public static void AddMemberKOTS (SlashCommandInteraction event) {
        String skill = Data.GetKOTSSkill();
        if (skill.equals("none")) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Add KOTS Player")
                    .setDescription("No KOTS event running!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        String member = event.getOption("member").getAsMember().getNickname();
        if (kotsStarting.containsKey(member)) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Add KOTS Player")
                    .setDescription("Player already competing!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        String requestURL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + member;
        try {
            String[] data = GameIntegrationHandler.request(requestURL).split("\n");
            String[] dataMap = { "Overall", "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking",
                    "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore",
                    "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction" };

            for (int i = 1; i < 24; i++) {
                if (dataMap[i].equals(skill)) {
                    kotsStarting.put(member, Integer.parseInt(data[i].split(",")[2]));
                    break;
                }
            }

            SaveKOTS();
            UpdateKOTSData(event.getJDA());

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Add KOTS Player")
                    .setDescription("Player added to KOTS!");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } catch (IOException e) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Add KOTS Player")
                    .setDescription("Failed to add player to KOTS!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            Logger.log("Failed to add player to KOTS!\n" + e, 0);
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> Sort(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }
}
