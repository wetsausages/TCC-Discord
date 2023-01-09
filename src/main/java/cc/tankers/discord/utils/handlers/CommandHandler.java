package cc.tankers.discord.utils.handlers;

import cc.tankers.discord.general.RemindmeHandler;
import cc.tankers.discord.integrations.ClanEventHandler;
import cc.tankers.discord.integrations.ClanIntegrationHandler;
import cc.tankers.discord.integrations.ClanSQL;
import cc.tankers.discord.integrations.GameIntegrationHandler;
import cc.tankers.discord.moderation.MuteHandler;
import cc.tankers.discord.general.PollHandler;
import cc.tankers.discord.utils.EmbedUtil;
import cc.tankers.discord.utils.Logger;
import cc.tankers.discord.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommandHandler {
    static InteractionHandler i = new InteractionHandler();
    public static void RegisterCommands(JDA jda) {
        ClanSQL cSQL = new ClanSQL();
        List<CommandData> commandData = new ArrayList<>();
        // Admin commands
        // config
        commandData.add(Commands.slash("config", "Configure channels, roles and stuff for the bot")
                .addSubcommands(new SubcommandData("guild", "Set public and private guilds")
                        .addOptions(new OptionData(OptionType.STRING, "set", "Set public/private guild", true)
                                .addChoice("public", "public")
                                .addChoice("private", "private")))
                .addSubcommands(new SubcommandData("mod-roles", "Configure mod stuff")
                        .addOption(OptionType.ROLE, "add", "Add role to the mod role list")
                        .addOption(OptionType.ROLE, "remove", "Remove role from the mod role list")
                        .addOption(OptionType.BOOLEAN, "list", "List moderator roles"))
                .addSubcommands(new SubcommandData("mod-log", "Set moderation logging channel")
                        .addOption(OptionType.CHANNEL,"channel", "Channel to set as moderation log", true))
                .addSubcommands(new SubcommandData("poll", "Configure poll role and channel")
                        .addOption(OptionType.ROLE, "role", "Role to ping when poles are made")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to make new polls in"))
                .addSubcommands(new SubcommandData("tcc", "Set channels for data embeds")
                        .addOption(OptionType.CHANNEL, "players", "Channel for player data embed")
                        .addOption(OptionType.CHANNEL, "drops", "Channel for drop data embed")
                        .addOption(OptionType.CHANNEL, "approval", "Channel to send drop submissions to")
                        .addOption(OptionType.CHANNEL, "loot", "Channel to send loot embeds to (public)"))
                .addSubcommands(new SubcommandData("debug", "Toggle debugging mode")));

        // Mod commands
        // mute
        commandData.add(Commands.slash("mute", "Mute a user")
                .addOption(OptionType.USER, "user", "User to mute", true)
                .addOption(OptionType.STRING, "reason", "Reason for the mute", true)
                .addOption(OptionType.INTEGER, "duration", "Duration of mute")
                .addOptions(new OptionData(OptionType.STRING, "unit", "Unit of time for duration of mute")
                        .addChoice("s", "s")
                        .addChoice("m", "m")
                        .addChoice("h", "h")
                        .addChoice("d", "d")));

        // unmute
        commandData.add(Commands.slash("unmute", "Unmute a user")
                .addOption(OptionType.USER, "user", "User to unmute", true));

        // poll
        commandData.add(Commands.slash("poll", "Create a poll")
                .addOption(OptionType.STRING, "question", "Poll question", true)
                .addOption(OptionType.STRING, "responses", "Separate with `;`. Omit for YES/NO. "));

        // General
        // remindme
        commandData.add(Commands.slash("remindme", "Set a reminder")
                .addOption(OptionType.STRING, "content", "What do you need reminding?", true)
                .addOption(OptionType.INTEGER, "duration", "of time", true)
                .addOptions(new OptionData(OptionType.STRING, "unit", "of time", true)
                        .addChoice("s", "s")
                        .addChoice("m", "m")
                        .addChoice("h", "h")
                        .addChoice("d", "d")));

        // TANKERS CC
        // OSRS APIs
        commandData.add(Commands.slash("lookup", "Look up a player's stats")
                .addOption(OptionType.STRING, "playername", "Player's name to search", true));

        commandData.add(Commands.slash("price", "Look up an item's data")
                .addOption(OptionType.STRING, "item", "Item's name to search", true));

        // CC Handling
        List<OptionData> submitOptionData = new ArrayList<>();
        for (String boss : cSQL.GetBosses()) {
            List<Command.Choice> itemChoices = new ArrayList<>();
            for (String item : cSQL.GetItems()) {
                if (item.split(";")[2].equalsIgnoreCase(boss.split(";")[0])) {
                    String cleanItem = item.split(";")[0].toLowerCase().replace(" ", "-");
                    itemChoices.add(new Command.Choice(cleanItem, cleanItem));
                }
            }
            submitOptionData.add(new OptionData(OptionType.STRING, boss.split(";")[0].toLowerCase().replace(" ","-"), "Submit drops from " + boss.split(";")[0])
                    .addChoices(itemChoices));
        }
        commandData.add(Commands.slash("submit", "Submit a drop for clan points")
                .addOption(OptionType.ATTACHMENT, "screenshot", "Screenshot of the drop", true)
                .addOptions(submitOptionData)
                .addOption(OptionType.STRING, "teammates", "Party members"));

        commandData.add(Commands.slash("points", "Manually manage member's clan points")
                .addSubcommands(new SubcommandData("add", "Give points to a player")
                        .addOption(OptionType.USER, "member", "Member to give points to", true)
                        .addOption(OptionType.INTEGER, "points", "Number of points to give", true))
                .addSubcommands(new SubcommandData("remove", "Remove points from a player")
                        .addOption(OptionType.USER, "member", "Member to give points to", true)
                        .addOption(OptionType.INTEGER, "points", "Number of points to give", true))
                .addSubcommands(new SubcommandData("merge", "Merge data between two player entries (for name change correction)")
                        .addOption(OptionType.STRING, "old-name", "Previous name", true)
                        .addOption(OptionType.USER, "new-name", "Current name", true)));


        List<Command.Choice> bossChoices = new ArrayList<>();
        for (String boss : cSQL.GetBosses()) bossChoices.add(new Command.Choice(boss.split(";")[0], boss.split(";")[0]));

        List<Command.Choice> skillChoices = new ArrayList<>();
        String[] skills = {"Attack", "Strength", "Defence", "Ranged", "Prayer", "Magic", "Runecrafting", "Construction",
        "Hitpoints", "Agility", "Herblore", "Thieving", "Crafting", "Fletching", "Slayer", "Hunter",
        "Mining", "Smithing", "Fishing", "Cooking", "Firemaking", "Woodcutting", "Farming"};
        for (String skill : skills) skillChoices.add(new Command.Choice(skill, skill));

        commandData.add(Commands.slash("drops", "Handle drops to be counted for points")
                .addOption(OptionType.STRING, "add", "Item's name to add (requires boss)")
                .addOptions(new OptionData(OptionType.STRING, "boss", "Boss that drops the item")
                        .addChoices(bossChoices))
                .addOption(OptionType.STRING, "remove", "Item's name to remove")
                .addOption(OptionType.BOOLEAN, "list", "Show list of registered drops"));

        commandData.add(Commands.slash("boss", "Handle boss data")
                .addOption(OptionType.STRING, "add", "Boss's name to add")
                .addOption(OptionType.STRING, "remove", "Boss's name to remove")
                .addOption(OptionType.BOOLEAN, "list", "Show list of registered bosses"));

        commandData.add(Commands.slash("event", "Manage CC events")
                    .addOptions(new OptionData(OptionType.STRING, "set", "Start/stop the event", true)
                            .addChoice("start", "start")
                            .addChoice("stop", "stop"))
                    .addOptions(new OptionData(OptionType.STRING, "event", "Select which event to stop/start", true)
                            .addChoice("pvm-challenge", "pvm-challenge")
                            .addChoice("kots", "kots"))
                    .addOptions(new OptionData(OptionType.STRING, "boss", "Set the boss for the event")
                            .addChoices(bossChoices))
                    .addOptions(new OptionData(OptionType.STRING, "skill", "Set the skill for the event")
                            .addChoices(skillChoices)));

        // Register commands
        jda.updateCommands().addCommands(commandData).queue();
        System.out.println("[Tankers] [+] Registered " + commandData.toArray().length + " commands to " + jda.getGuilds());
    }

    public static void Handle(SlashCommandInteraction event) throws IOException, ExecutionException, InterruptedException {
        switch (event.getName()) {
            // Admin commands
            case "config" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "guild" -> HandleGuild(event);
                    case "mod-roles" -> HandleModRoles(event);
                    case "mod-log" -> HandleModLog(event);
                    case "poll" -> HandlePollConfig(event);
                    case "tcc" -> HandleTCCCommand(event);
                    case "debug" -> HandleDebug(event);
                }
            }

            // Mod commands
            case "mute" -> {
                if (!i.CheckPermission(event, 1)) return;
                MuteHandler.Mute(event);
            }
            case "unmute" -> {
                if (!i.CheckPermission(event, 1)) return;
                MuteHandler.Unmute(event);
            }
            case "poll" -> {
                if(!i.CheckPermission(event, 1)) return;
                PollHandler.Handle(event);
            }

            // General
            case "remindme" -> RemindmeHandler.RemindMe(event);

            // TANKERS CC
            case "lookup" -> GameIntegrationHandler.PlayerLookup(event);
            case "price" -> GameIntegrationHandler.ItemLookup(event);
            case "submit" -> ClanIntegrationHandler.SubmitDrop(event);

            case "points" -> {
                if(!i.CheckPermission(event, 2)) return;
                ClanIntegrationHandler.HandlePoints(event);
            }
            case "drops" -> {
                if(!i.CheckPermission(event, 2)) return;
                ClanIntegrationHandler.HandleItems(event);
            }
            case "boss" -> {
                if(!i.CheckPermission(event, 2)) return;
                ClanIntegrationHandler.HandleBosses(event);
            }
            case "event" -> {
                if (!i.CheckPermission(event, 1)) return;
                ClanEventHandler.HandleEvent(event);
            }
        }
    }

    // Config commands
    static void HandleGuild (SlashCommandInteraction event) {
        String guild = event.getOption("set").getAsString();
        if (guild.equals("public")) {
            Data.SetGuildPublic(event.getGuild());
            Logger.log("[+] Public guild updated to [" + event.getGuild().getName() + "].", 1);
        }
        else if (guild.equals("private")) {
            Data.SetGuildPrivate(event.getGuild());
            Logger.log("[+] Private guild updated to [" + event.getGuild().getName() + "].", 1);
        }
        else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Invalid syntax! Please use \"public\" or \"private\".");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Guilds updated.");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);

    }

    static void HandleModRoles (SlashCommandInteraction event) {
        Role addRole = null;
        Role removeRole = null;
        boolean list = false;
        try { addRole = event.getOption("add").getAsRole(); } catch (Exception ignored) {}
        try { removeRole = event.getOption("remove").getAsRole(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (addRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(addRole)) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + addRole.getName() + "** is already added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            } else {
                modRoles.add(addRole);
                Data.SetModRole(modRoles);
                Logger.log("[+] Role [" + addRole.getName() + "] added to mod roles list.", 1);
            }
        }

        if (removeRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(removeRole)) {
                modRoles.remove(removeRole);
                Data.SetModRole(modRoles);
                Logger.log("[+] Role [" + removeRole.getName() + "] removed from mod roles list.", 1);
            } else {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + removeRole.getName() + "** is not added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            }
        }

        if (list) {
            String blob = "";
            for (Role role : Data.GetModRoles(event.getJDA())) blob += role.getName() + "\n";
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("**Moderator roles:**\n" + blob);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Updated moderator roles.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        }
    }

    static void HandleModLog (SlashCommandInteraction event) {
        TextChannel channel = event.getOption("channel").getAsTextChannel();
        Data.SetModLogChannel(channel);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set mod log to **" + channel.getName() + "**");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
        Logger.log("[+] Channel [" + channel.getName() + "] set as mod log.", 1);
    }

    static void HandlePollConfig (SlashCommandInteraction event) {
        Role role = null;
        TextChannel channel = null;

        try { role = event.getOption("role").getAsRole(); } catch (Exception ignored) {}
        try { channel = event.getOption("channel").getAsTextChannel(); } catch (Exception ignored) {}

        if (role != null) {
            Data.SetPollRole(role);
            Logger.log("[+] Role [" + role.getName() + "] set as poll role.", 1);
        }
        if (channel != null) {
            Data.SetPollChannel(channel);
            Logger.log("[+] Channel [" + channel.getName() + "] set as poll channel.", 1);
        }

        if (role != null || channel != null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Poll config updated.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Failed to update poll config!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }
    }

    static void HandleTCCCommand (SlashCommandInteraction event) {
        EmbedBuilder eb = new EmbedBuilder().setTitle("[Tankers] Admin");

        TextChannel approvalChannel = null;
        TextChannel playerDataChannel = null;
        TextChannel dropDataChannel = null;
        TextChannel lootChannel = null;

        try { approvalChannel = event.getOption("approval").getAsTextChannel(); } catch (Exception ignored) {}
        try { playerDataChannel = event.getOption("players").getAsTextChannel(); } catch (Exception ignored) {}
        try { dropDataChannel = event.getOption("drops").getAsTextChannel(); } catch (Exception ignored) {}
        try { lootChannel = event.getOption("loot").getAsTextChannel(); } catch (Exception ignored) {}

        String desc = "";
        if (approvalChannel != null) {
            Data.SetApprovalChannel(approvalChannel);
            desc += "Set drop approval channel to **" + approvalChannel.getName() + "**\n";
            Logger.log("[+] Channel [" + approvalChannel.getName() + "] set as drop approval channel.", 1);
        }

        if (playerDataChannel != null) {
            Data.SetPlayerDataChannel(playerDataChannel);
            desc += "Set player data channel to **" + playerDataChannel.getName() + "**\n";
            Logger.log("[+] Channel [" + playerDataChannel.getName() + "] set as player data channel.", 1);

            MessageAction embedAction = playerDataChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("#PLAYERS#").build());
            try { Data.SetPlayerDataEmbed(embedAction.submit().get().getId()); }
            catch (Exception ignored) { eb.setDescription("[X] Failed to create base embed (player data)"); }
        }

        if (dropDataChannel != null) {
            Data.SetDropDataChannel(dropDataChannel);
            desc += "Set drop data channel to **" + dropDataChannel.getName() + "**";
            Logger.log("[+] Channel [" + dropDataChannel.getName() + "] set as drop data channel.", 1);

//            MessageAction embedAction = dropDataChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("#DROPS#").build());
//            try { Data.SetDropDataEmbed(embedAction.submit().get().getId()); }
//            catch (Exception ignored) { eb.setDescription("[X] Failed to create base embed (drop data)"); }
        }

        if (lootChannel != null) {
            Data.SetLootChannel(lootChannel);
            desc += "Set loot channel to **" + lootChannel.getName() + "**\n";
            Logger.log("[+] Channel [" + lootChannel.getName() + "] set as loot channel.", 1);
        }

        if (playerDataChannel != null || dropDataChannel != null) ClanIntegrationHandler.UpdatePlayerDataEmbed(event.getJDA());

        new EmbedUtil().ReplyEmbed(event, eb.setDescription(desc), true, false);
    }

    static void HandleDebug (SlashCommandInteraction event) {
        Data.SetDebug(!Data.GetDebug());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set config.debug to `" + Data.GetDebug() + "`");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
        Logger.log("[+] DEBUG set to [" + Data.GetDebug() + "].", 1);
    }
}
