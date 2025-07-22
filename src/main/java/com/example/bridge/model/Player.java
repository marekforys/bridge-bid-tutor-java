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

    public Player getPartner() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
                throw new IllegalStateException();
        }
    }
}
