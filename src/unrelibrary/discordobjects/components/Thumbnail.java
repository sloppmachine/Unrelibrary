package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONBuilder;

public class Thumbnail extends Component {
    public final UnfurledMediaItem MEDIA;
    public final String DESCRIPTION;
    public final boolean SPOILER;

    public Thumbnail(UnfurledMediaItem media, String description, boolean spoiler) {
        super(11);
        this.MEDIA = media;
        this.DESCRIPTION = description;
        this.SPOILER = spoiler;
    }

    public String toJSON() {
        JSONBuilder toReturnBuilder = new JSONBuilder();
        toReturnBuilder.addIntProperty("type", TYPE);
        toReturnBuilder.addLiteralProperty("media", MEDIA.toJSON());
        toReturnBuilder.addStringProperty("description", DESCRIPTION);
        toReturnBuilder.addBooleanProperty("spoiler", SPOILER);
        return toReturnBuilder.build();
    }
}
