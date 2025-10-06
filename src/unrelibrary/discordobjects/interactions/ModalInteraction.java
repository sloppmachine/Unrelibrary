package unrelibrary.discordobjects.interactions;

import unrelibrary.discordobjects.GuildMember;
import unrelibrary.discordobjects.User;

public class ModalInteraction extends Interaction {
    public final Data DATA;

    public ModalInteraction(long id, int type, String token, User user, GuildMember guildMember, long channelID, Data data) {
        super(id, type, token, user, guildMember, channelID);
        this.DATA = data;
    }

    public static class Data {
        public final String CUSTOM_ID;
        public final ModalComponent[] MODAL_COMPONENTS; // this is a special type of field which exists for the return values the components give. what the hell discord

        public Data(String customID, ModalComponent[] modalComponents) {
            this.CUSTOM_ID = customID;
            this.MODAL_COMPONENTS = modalComponents;
        }


        public static class ModalComponent {
            // as of now, labels are the only component type in a modal that sends this type of response
            public final int TYPE;
            public final int ID;
            public final ModalComponentSubmission MODAL_COMPONENT_SUBMISSION;

            public ModalComponent(int type, int id, ModalComponentSubmission modalComponentSubmission) {
                this.TYPE = type;
                this.ID = id;
                this.MODAL_COMPONENT_SUBMISSION = modalComponentSubmission;
            }

            public static class ModalComponentSubmission {
                // every label can contain only one modal submission, either text input or string select
                public final int TYPE;
                public final int ID;
                public final String CUSTOM_ID;
                public final String[] VALUES;
                public final String VALUE;

                public ModalComponentSubmission(int type, int id, String customID, String[] values, String value) {
                    this.TYPE = type;
                    this.ID = id;
                    this.CUSTOM_ID = customID;
                    this.VALUES = values;
                    this.VALUE = value;
                }
            }
        }
    }
}
