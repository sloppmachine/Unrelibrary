package unrelibrary.discordobjects.components;

import unrelibrary.formatting.JSONRepresentable;

// there is no component that doesn't have a type, so this class is abstract
// https://discord.com/developers/docs/components/reference#component-object
public abstract class Component implements JSONRepresentable {
    public final int TYPE;

    public Component(int type) {
        this.TYPE = type;
    }
}
