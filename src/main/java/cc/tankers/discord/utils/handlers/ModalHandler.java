package cc.tankers.discord.utils.handlers;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class ModalHandler {
    public static void Handle (ModalInteractionEvent event) {
        String[] id = event.getModalId().split("-");
        switch (id[0]) {
//            case "ticket" -> {
//                switch (id[1]) {
//                    case "temp" -> TicketBuilder.GetModalData(event);
//                }
//            }
        }
    }
}
