package com.example.bridge.model;

public enum Player {
    NORTH, EAST, SOUTH, WEST;

    public String getShortName() {
        switch (this) {
            case NORTH:
                return "N";
            case EAST:
                return "E";
            case SOUTH:
                return "S";
            case WEST:
                return "W";
            default:
                return this.name().substring(0, 1);
        }
    }

    public boolean isPartner(Player other) {
        if (this == NORTH || this == SOUTH) {
            return other == NORTH || other == SOUTH;
        }
        if (this == EAST || this == WEST) {
            return other == EAST || other == WEST;
        }
        return false;
    }

    public Player getPartner() {
        return values()[(this.ordinal() + 2) % 4];
    }

    public boolean isOpponent(Player other) {
        return this != other && !isPartner(other);
    }
}
