package cc.tankers.discord.utils.listeners;

import cc.tankers.discord.general.RemindmeHandler;
import cc.tankers.discord.moderation.MuteHandler;
import cc.tankers.discord.utils.Logger;
import cc.tankers.discord.utils.data.Data;
import cc.tankers.discord.utils.data.SQL;
import cc.tankers.discord.utils.handlers.CommandHandler;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger.Load();
        Logger.log("[+] Logger loaded.", 1);

        new Data().Load();
        new SQL().Load();

        MuteHandler.Load(event.getJDA());
        RemindmeHandler.Load(event.getJDA());
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        CommandHandler.RegisterCommands(event.getJDA());
    }
}
