package cc.tankers.discord.integrations;

import cc.tankers.discord.utils.EmbedUtil;
import cc.tankers.discord.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClanIntegrationHandler {
    static ClanSQL sql = new ClanSQL();
    public static void SubmitDrop (SlashCommandInteraction event) {
        // Get command data
        String screenshot = event.getOption("screenshot").getAsAttachment().getUrl();
        List<OptionMapping> optList = event.getOptions();
        if (optList.size() < 2) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Submit Drop")
                    .setDescription("Not enough info! Please include a screenshot and the name of the drop by using the boss options.");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        String item = "";
        for (Iterator<OptionMapping> it = optList.iterator(); it.hasNext(); ) {
            OptionMapping curOpt = it.next();
            System.out.println(curOpt.getName() + ", " + curOpt.getAsString());
            if (!curOpt.getName().contains("teammate") && !curOpt.getName().contains("screenshot")) item = curOpt.getAsString().toLowerCase().replace("-", " ");
        }

        if (item.equals("")) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Submit Drop")
                    .setDescription("Item missing!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }

        List<String> players = new ArrayList<String>();

        try {
            String s = event.getMember().getNickname();
            if (s == null) s = event.getUser().getName();
            if (s != null) players.add(s);
        } catch (Exception ignored) { }

        for (int i = 1; i < 10; i++) {
            try {
                String p = event.getOption("teammate-" + i).getAsMember().getNickname();
                if (p == null) p = event.getOption("teammate-" + i).getAsUser().getName();
                if (p != null) players.add(p);
            } catch (Exception ignored) { }
        }

        // Validate item
        List<String> itemsList = new ArrayList<>();
        for (String i : sql.GetItems()) itemsList.add(i.toLowerCase().split(";")[0]);
        if (!itemsList.contains(item.toLowerCase())) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Submit Drop")
                    .setDescription("Item **" + item + "** is not registered as a drop for clan points!")
                    .setFooter("If you think this is an error, please contact an administrator.");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        // Get points
        Pattern wikiExchangeValuePattern = Pattern.compile(".*id=\"GEPrice\">(.*?)<.*?");
        String requestURL = "https://oldschool.runescape.wiki/w/Exchange:" + item;
        String content = "";
        String price = "";
        int points = 0;
        try { content = GameIntegrationHandler.request(requestURL); }
        catch (IOException e) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Submit Drop")
                    .setDescription("Unable to reach Wiki Exchange - please try resubmitting later!")
                    .setFooter(requestURL);
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        Matcher m = wikiExchangeValuePattern.matcher(content);
        if (m.find()) {
            try {
                price = m.group(1).replace(",", "");
                points = Integer.parseInt(price)/1000000;
                if (points < 1) points = 1;
            }
            catch (Exception ignored) { points = 1; }
        }

        // Validate players (add if row not exists)
        List<String> playerList = new ArrayList<>();
        for (String i : sql.GetMembers()) playerList.add(i.split(";")[0]);
        for (String player : players) if (!playerList.contains(player)) sql.AddMember(player);

        // Valid submission
        String newPlayersList = "";
        for (String player : players) newPlayersList += player + ", ";
        newPlayersList = newPlayersList.substring(0,(newPlayersList.length()-2));
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[Tankers] Submit Drop")
                .addField("Item:", item, true)
                .addField("Points:", String.valueOf(points), true)
                .addField("Players:", newPlayersList, false);
        new EmbedUtil().ReplyEmbed(event, eb, true, false);

        // Send for approval
        TextChannel approvalChannel = Data.GetApprovalChannel(event.getJDA());
        EmbedBuilder approvalEB = new EmbedBuilder()
                .setTitle("[Admin] Validate Submission")
                .setImage(screenshot)
                .addField("Item:", item, true)
                .addField("Points:", String.valueOf(points), true)
                .addField("Players:", newPlayersList, false);

        // Store embed ID to delete later
        String embedID = "";
        MessageAction embedAction = approvalChannel.sendMessageEmbeds(approvalEB.build());
        try { embedID = embedAction.submit().get().getId(); }
        catch (InterruptedException | ExecutionException e) {  throw new RuntimeException(e); }

        // Send buttons
        Button button0 = Button.primary("submit-approve-" + item + "-" + newPlayersList + "-" + points + "-" + embedID, "Approve");
        Button button1 = Button.danger("submit-deny-" + embedID, "Deny");
        Message message = new MessageBuilder()
                .setContent(" ")
                .setActionRows(ActionRow.of(button0, button1))
                .build();
        approvalChannel.sendMessage(message).queue();

        // Send drop to loot channel
        EmbedBuilder lootEB = new EmbedBuilder()
                .setTitle("[Tankers] Loot")
                .setImage(screenshot)
                .addField("Item:", item, true)
                .addField("Players:", newPlayersList, true)
                .addField("Value:", String.format("%,d", Integer.parseInt(price)) + " gp", false);
        Data.GetLootChannel(event.getJDA()).sendMessageEmbeds(lootEB.build()).queue();
    }

    public static void ApproveSubmission (ButtonInteractionEvent event) {
        String item = event.getButton().getId().split("-")[2];
        String[] players = event.getButton().getId().split("-")[3].split(", ");
        int value = Integer.parseInt(event.getButton().getId().split("-")[4]);

        for (String player : players) sql.AddPoints(player, value);
        sql.IncrementItemCount(item);

        for (String i : sql.GetItems()) {
            if (i.contains(item)) item = i;
        }

        UpdatePlayerDataEmbed(event.getJDA());
        UpdateDropDataEmbed(event.getJDA(), item.split(";")[2]);

        Data.GetApprovalChannel(event.getJDA()).deleteMessageById(event.getButton().getId().split("-")[5]).queue();
        event.getMessage().delete().queue();
    }

    public static void DenySubmission (ButtonInteractionEvent event) {
        Data.GetApprovalChannel(event.getJDA()).deleteMessageById(event.getButton().getId().split("-")[2]).queue();
        event.getMessage().delete().queue();
    }

    public static void HandleItems (SlashCommandInteraction event) {
        List<String> itemsList = new ArrayList<>();
        for (String i : sql.GetItems()) itemsList.add(i.toLowerCase().split(";")[0]); //.replace("#", "'"));

        String toAdd = null;
        String boss = null;
        String toRemove = null;
        boolean list = false;

        try { toAdd = event.getOption("add").getAsString(); } catch (Exception ignored) {}
        try { boss = event.getOption("boss").getAsString(); } catch (Exception ignored) {}
        try { toRemove = event.getOption("remove").getAsString(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (toAdd != null) {
            if (boss == null) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[Tankers] Drop Config")
                        .setDescription("Boss required when adding drops!");
                new EmbedUtil().ReplyEmbed(event, eb, true, true);
                return;
            }
            String[] items = event.getOption("add").getAsString().split(";");
            for (String item : items) {
                item = item.toLowerCase();
                if (!itemsList.contains(item)) sql.AddItem(item, boss);
            }
        }
        if (toRemove != null) {
            String[] items = event.getOption("remove").getAsString().split(";");
            for (String item : items) {
                item = item.toLowerCase();
                if (itemsList.contains(item)) sql.RemoveItem(item);
            }
        }

        if (list) {
            String newItemsList = "";
            for (String item : sql.GetItems()) newItemsList += item.split(";")[0] + "\n";
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Drop Config")
                    .setDescription("**Items**:\n" + newItemsList);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);

            // Really bad
            for (String _boss : sql.GetBosses()) UpdateDropDataEmbed(event.getJDA(), _boss.split(";")[0]);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Drop Config")
                    .setDescription("Drop list updated. Use `/drops list:true` to see updated list of registered drops.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        }

        if (toAdd != null && toRemove == null) UpdateDropDataEmbed(event.getJDA(), boss);
        if (toAdd != null || toRemove != null) {
            UpdatePlayerDataEmbed(event.getJDA());
            UpdateDropOptions(event.getJDA());
        }
    }

    public static void HandlePoints (SlashCommandInteraction event) {
        switch (event.getSubcommandName()) {
            case "add" -> {
                String player = null;
                try {
                    player = event.getOption("member").getAsMember().getNickname();
                    if (player == null) player = event.getOption("member").getAsUser().getName();
                } catch (Exception ignored) { }
                int value = event.getOption("points").getAsInt();
                sql.AddPoints(player, value);
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[Tankers] Add Points")
                        .setDescription("Gave **" + value + "** points to **" + player + "**");
                new EmbedUtil().ReplyEmbed(event, eb, true, false);
            }
            case "remove" -> {
                String player = null;
                try {
                    player = event.getOption("member").getAsMember().getNickname();
                    if (player == null) player = event.getOption("member").getAsUser().getName();
                } catch (Exception ignored) { }
                int value = event.getOption("points").getAsInt();
                sql.RemovePoints(player, value);
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[Tankers] Remove Points")
                        .setDescription("Removed **" + value + "** points from **" + player + "**");
                new EmbedUtil().ReplyEmbed(event, eb, true, false);
            }
            case "merge" -> {
                String newName = null;
                try {
                    newName = event.getOption("new-name").getAsMember().getNickname();
                    if (newName == null) newName = event.getOption("new-name").getAsUser().getName();
                } catch (Exception ignored) { }

                try {
                    String oldData = sql.GetMember(event.getOption("old-name").getAsString());
                    sql.AddPoints(newName, Integer.parseInt(oldData.split(";")[1]));
                    sql.RemoveMember(oldData.split(";")[0]);

                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[Tankers] Merge Points")
                            .setDescription("Merged **" + oldData.split(";")[0] + "** points with **" + newName + "**");
                    new EmbedUtil().ReplyEmbed(event, eb, true, false);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        UpdatePlayerDataEmbed(event.getJDA());
    }

    public static void HandleBosses (SlashCommandInteraction event) {
        TextChannel dropChannel = Data.GetDropDataChannel(event.getJDA());
        List<String> bossList = new ArrayList<>();
        for (String i : sql.GetBosses()) bossList.add(i.split(";")[0].toLowerCase());

        String toAdd = null;
        String toRemove = null;
        boolean list = false;

        try { toAdd = event.getOption("add").getAsString(); } catch (Exception ignored) {}
        try { toRemove = event.getOption("remove").getAsString(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (toAdd != null) {
            String[] bosses = event.getOption("add").getAsString().split(";");
            for (String boss : bosses) {
                if (!bossList.contains(boss.toLowerCase())) {
                    String embedID = "";
                    MessageAction embedAction = dropChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle(boss)
                            .setColor(Color.CYAN)
                            .build());
                    try { embedID = embedAction.submit().get().getId(); }
                    catch (InterruptedException | ExecutionException e) {  throw new RuntimeException(e); }
                    sql.AddBoss(boss, embedID);
                }
            }
        }
        if (toRemove != null) {
            String[] bosses = event.getOption("remove").getAsString().split(";");
            for (String boss : bosses) {
                if (bossList.contains(boss)) {
                    dropChannel.deleteMessageById(boss.split(";")[1]).queue();
                    sql.RemoveBoss(boss);
                }
            }
        }

        if (list) {
            String newBossList = "";
            for (String boss : sql.GetBosses()) newBossList += boss.split(";")[0] + "\n";
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Boss Config")
                    .setDescription("**Bosses**:\n" + newBossList);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);

        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[Tankers] Boss Config")
                    .setDescription("Boss list updated. Use `/boss list:true` to see updated list of registered bosses.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        }

        if (toAdd != null || toRemove != null) {
            UpdateBossOptions(event.getJDA());
        }
    }

    public static void UpdatePlayerDataEmbed (JDA jda) {
        //Player data
        TextChannel playerChannel = Data.GetPlayerDataChannel(jda);
        String[] playerBlob = new String[2];
        playerBlob[0] = playerBlob[1] = "";
        int c = 1;
        for (String player : sql.GetMembers()) {
            if (player.contains("null")) continue;
            playerBlob[0] += c + ". " + player.split(";")[0] + "\n";
            playerBlob[1] += player.split(";")[1] + "\n";
            c++;
        }
        EmbedBuilder playerEB = new EmbedBuilder()
                .setTitle("Member List")
                .setDescription("**How to submit:**\nIn any channel, use the `/submit` command. A screenshot and item name are required. Select the item name by using the boss options. See " + Data.GetDropDataChannel(jda).getAsMention() + " for category names. You can also include teammates with the teammate options and tagging their discord.\n\n" +
                        "Rank points are allocated based on current GE value of the drop at a rate of 1 point per 1 mil gp value. Points are given in full to all team members. Drops are also only valid if ALL team members are part of the clan. \n" +
                        "**NOTE:** please contact an administrator if you change your name!\n\n" +
                        "**Rankings**\nDragon - 10000\nRunite - 5000\nAdamant - 3000\nMithril - 1500\nSteel - 400\nIron - 150\nBronze - 50")
                .addField("Player", playerBlob[0], true)
                .addField("Points", playerBlob[1], true)
                .addField(" ", " ", true)
                .setColor(Color.MAGENTA);
        playerChannel.editMessageEmbedsById(Data.GetPlayerDataEmbed(jda), playerEB.build()).queue();
    }

    public static void UpdateDropDataEmbed (JDA jda, String boss) {
        TextChannel dropChannel = Data.GetDropDataChannel(jda);

        for (String b : sql.GetBosses()) {
            if (b.contains(boss)) boss = b;
        }
        String[] bossData = boss.split(";");

        String itemList = "";
        String countList = "";
        for (String item : sql.GetItems()) {
            if (item.split(";")[2].equalsIgnoreCase(bossData[0])) {
                itemList = itemList.concat(item.split(";")[0] + "\n");
                countList = countList.concat(item.split(";")[1] + "\n");
            }
        }

        if (bossData[1].equals("none")) {
            String embedID = "none";
            MessageAction embedAction = dropChannel.sendMessageEmbeds(new EmbedBuilder().setTitle(bossData[0]).build());
            try { embedID = embedAction.submit().get().getId(); }
            catch (InterruptedException | ExecutionException e) {  throw new RuntimeException(e); }
            sql.UpdateBoss(bossData[0], embedID);
            bossData[1] = embedID;
        }

        EmbedBuilder dropEB = new EmbedBuilder()
                .setTitle(bossData[0])
                .addField("Item:", itemList, true)
                .addField("Count:", countList, true)
                .setColor(Color.CYAN);
        dropChannel.editMessageEmbedsById(bossData[1], dropEB.build()).queue();
    }

    public static void UpdateBossOptions (JDA jda) {
        List<Command.Choice> choices = new ArrayList<>();
        for (String boss : sql.GetBosses()) choices.add(new Command.Choice(boss.split(";")[0], boss.split(";")[0]));

        List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.STRING, "add", "Item's name to add"));
        optionData.add(new OptionData(OptionType.STRING, "boss", "Boss that drops the item").addChoices(choices));
        optionData.add(new OptionData(OptionType.STRING, "remove", "Item's name to remove"));
        optionData.add(new OptionData(OptionType.BOOLEAN, "list", "Show list of registered drops"));

        try {
            List<Command> commandList = jda.retrieveCommands().submit().get();
            for (Command cmd : commandList) {
                if (cmd.getName().equals("drops")) {
                    cmd.editCommand().clearOptions().queue();
                    cmd.editCommand().addOptions(optionData).queue();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void UpdateDropOptions (JDA jda) {

        List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.ATTACHMENT, "screenshot", "Screenshot of the drop", true));

        for (String boss : sql.GetBosses()) {
            List<Command.Choice> itemChoices = new ArrayList<>();
            for (String item : sql.GetItems()) {
                if (item.split(";")[2].equalsIgnoreCase(boss)) {
                    String cleanItem = item.split(";")[0].toLowerCase().replace(" ", "-");
                    itemChoices.add(new Command.Choice(cleanItem, cleanItem));
                }
            }
            optionData.add(new OptionData(OptionType.STRING, boss.split(";")[0].toLowerCase().replace(" ","-"), "Submit drops from " + boss)
                    .addChoices(itemChoices));
        }

        optionData.add(new OptionData(OptionType.USER, "teammate-1", "1st party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-2", "2nd party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-3", "3rd party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-4", "4th party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-5", "5th party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-6", "6th party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-7", "7th party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-8", "8th party member"));
        optionData.add(new OptionData(OptionType.USER, "teammate-9", "9th party member"));

        try {
            List<Command> commandList = jda.retrieveCommands().submit().get();
            for (Command cmd : commandList) {
                if (cmd.getName().equals("submit")) {
                    cmd.editCommand().clearOptions().queue();
                    cmd.editCommand().addOptions(optionData).queue();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
