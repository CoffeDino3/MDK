package com.CoffeDino.lunacy.classes;


public enum SpellbladeElement {
    FIRE("fire", "Fire"),
    WATER("water", "Water"),
    LIGHTNING("lightning", "Lightning"),
    VOID("void", "Void"),
    EARTH("earth", "Earth"),
    WIND("wind", "Wind"),
    LIGHT("light", "Light"),
    ETHER("ether", "Ether"),
    BLOOD("blood", "Blood");

    private final String id;
    private final String displayName;

    SpellbladeElement(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    public static SpellbladeElement fromId(String id) {
        for (SpellbladeElement e : values()) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }
}