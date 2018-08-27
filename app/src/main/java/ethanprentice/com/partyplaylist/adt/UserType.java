package ethanprentice.com.partyplaylist.adt;

public enum UserType {

    PLAYER   (1001),
    USER     (1002),
    INACTIVE (1003);

    private int val;
    UserType (int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }

}