package unrelibrary.restapi;

import java.util.Map;
import java.util.function.Function;

import unrelibrary.discordobjects.interactions.Interaction;
import unrelibrary.discordobjects.interactions.ModalInteraction;
import unrelibrary.discordobjects.interactions.ComponentInteraction;

// update which custom ids to listen for in interactions
public class CustomIDListeningUpdate {
    public final Map<String, Function<ComponentInteraction, Interaction.CustomIDUpdatingResponse>> COMPONENT_NOTIFICATION_START_LISTENING;
    public final String[] COMPONENT_NOTIFICATION_STOP_LISTENING;
    public final Map<String, Function<ModalInteraction, Interaction.CustomIDUpdatingResponse>> MODAL_NOTIFICATION_START_LISTENING;
    public final String[] MODAL_NOTIFICATION_STOP_LISTENING;

    public CustomIDListeningUpdate(
        Map<String, Function<ComponentInteraction, Interaction.CustomIDUpdatingResponse>> componentNotificationsStartListening,
        String[] componentNotificationsStopListening,
        Map<String, Function<ModalInteraction, Interaction.CustomIDUpdatingResponse>> modalNotificationStartListening,
        String[] modalNotificationStopListening
    ) {
        this.COMPONENT_NOTIFICATION_START_LISTENING = componentNotificationsStartListening;
        this.COMPONENT_NOTIFICATION_STOP_LISTENING = componentNotificationsStopListening;
        this.MODAL_NOTIFICATION_START_LISTENING = modalNotificationStartListening;
        this.MODAL_NOTIFICATION_STOP_LISTENING = modalNotificationStopListening;
    }
}
