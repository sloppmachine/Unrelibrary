package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;
import unrelibrary.formatting.JSONRepresentable;

// https://discord.com/developers/docs/components/reference#media-gallery
public class MediaGallery extends Component {
    public final Item[] ITEMS;
    
    public MediaGallery(Item[] items) {
        super(12);
        this.ITEMS = items;
    }

    public static class Item implements JSONRepresentable {
        public final UnfurledMediaItem MEDIA;
        public final String DESCRIPTION;
        public final boolean SPOILER;

        public Item(UnfurledMediaItem media, String description, boolean spoiler) {
            this.MEDIA = media;
            this.DESCRIPTION = description;
            this.SPOILER = spoiler;
        }

        public String toJSON() {
            JSONBuilder toReturnBuilder = new JSONBuilder();
            toReturnBuilder.addLiteralProperty("media", MEDIA.toJSON());
            toReturnBuilder.addStringProperty("description", DESCRIPTION);
            toReturnBuilder.addBooleanProperty("spoiler", SPOILER);
            return toReturnBuilder.build();
        }
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addLiteralProperty("items", JSONBuilder.buildArray(ITEMS));
        return toReturnBuilder.build();
    }
}
