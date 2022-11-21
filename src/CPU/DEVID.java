package CPU;

public enum DEVID {
    KEYBOARD (0),
    PRINTER (1),
    CARD_READER (2);

    private int id;

    DEVID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
