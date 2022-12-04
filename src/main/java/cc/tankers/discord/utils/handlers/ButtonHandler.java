package cc.tankers.discord.utils.handlers;

import cc.tankers.discord.integrations.ClanIntegrationHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ButtonHandler {
    public static void Handle (ButtonInteractionEvent event) {
        String[] id = event.getButton().getId().split("-");
        switch (id[0]) {
            case "submit" -> {
                switch (id[1]) {
                    case "approve" -> ClanIntegrationHandler.ApproveSubmission(event);
                    case "deny" -> ClanIntegrationHandler.DenySubmission(event);
                }
            }
        }
    }
}
