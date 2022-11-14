package cc.tankers.discord.utils.listeners;

import cc.tankers.discord.utils.Logger;
import cc.tankers.discord.utils.handlers.CommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CommandEvent extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            CommandHandler.Handle(event);
            Logger.log("[c] Command [" + event.getCommandString() + "] by [" + event.getUser() + "]", 1);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}