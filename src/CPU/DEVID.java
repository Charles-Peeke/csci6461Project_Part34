/**
 * CSCI 6461 - Fall 2022
 * 
 * DEVID - Device ID Class
 * Constants for Keyboard, Printer (DEV CONSOLE), Card Reader
 */


package CPU;

public enum DEVID {
    KEYBOARD (0), PRINTER (1), CARD_READER (2);

    private int id;

    DEVID(int id) { this.id = id; }

    public int getId() { return id; }
}
