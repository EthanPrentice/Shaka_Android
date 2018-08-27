package ethanprentice.com.partyplaylist.adt;

public enum PlayerStatus {

    PLAYING  (1001),
    PAUSED   (1002),
    STOPPED  (1003),
    ERROR    (1004);


    private int val;
    PlayerStatus (int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }

}
