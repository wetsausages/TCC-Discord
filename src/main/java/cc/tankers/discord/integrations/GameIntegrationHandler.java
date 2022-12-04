package cc.tankers.discord.integrations;

import cc.tankers.discord.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameIntegrationHandler {
    public static void PlayerLookup(SlashCommandInteraction event) {
        String name = event.getOption("playername").getAsString();
        int points = 0;
        for (String player : new ClanSQL().GetMembers()) {
            if (name.equalsIgnoreCase(player.split(";")[0])) points = Integer.parseInt(player.split(";")[1]);
        }
        String requestURL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + name;
        try {
            String[] data = request(requestURL).split("\n");
            String[] dataMap = {"Overall", "Attack", "Hitpoints", "Mining", "Strength", "Agility", "Smithing",
            "Defence", "Herblore", "Fishing", "Ranged", "Thieving", "Cooking", "Prayer", "Crafting", "Firemaking",
            "Magic", "Fletching", "Woodcutting", "Runecrafting", "Slayer", "Farming", "Construction", "Hunter"};
            EmbedBuilder eb = new EmbedBuilder().setTitle(name + " - " + points + " points");
            for (int i = 1; i < 24; i++) eb.addField(dataMap[i], data[i].split(",")[1], true);
            eb.addField(dataMap[0], data[0].split(",")[1], true);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } catch (IOException e) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[OSRS] " + name)
                    .setDescription("Unable to reach highscores.");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }
    }

    public static void ItemLookup(SlashCommandInteraction event) {
        Pattern wikiExchangeValuePattern = Pattern.compile(".*id=\"GEPrice\">(.*?)<.*?");
        Pattern cleanNamePattern = Pattern.compile("content=\"Exchange:(.*?)\".*?");
        String item = event.getOption("item").getAsString().toLowerCase();
        String requestURL = "https://oldschool.runescape.wiki/w/Exchange:" + item;

        try {
            //Make request
            String content = request(requestURL);

            //Get nicer name
            Matcher a = cleanNamePattern.matcher(content);
            if (a.find()) item = a.group(1);

            //Get GE value/clan points
            Matcher b = wikiExchangeValuePattern.matcher(content);
            if (b.find()) {
                String value = b.group(1);
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[OSRS] " + item)
                        .addField("Price", value, true)
                        .addField("Points", String.valueOf(Integer.parseInt(value.replace(",",""))/1000000), true)
                        .setDescription("[More information](https://oldschool.runescape.wiki/w/Exchange:" + item.replace(" ", "_") + ")");
                new EmbedUtil().ReplyEmbed(event, eb, true, false);
            } else {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[OSRS] " + item)
                        .setDescription("Unable to find " + item + " data.");
                new EmbedUtil().ReplyEmbed(event, eb, true, true);
            }
        } catch (IOException e) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[OSRS] " + item)
                    .setDescription("Unable to reach wiki.");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }

    }

    public static String request(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        } finally {
            is.close();
        }
    }
}
