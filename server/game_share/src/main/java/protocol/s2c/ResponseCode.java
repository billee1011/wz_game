package protocol.s2c;

public enum ResponseCode {
    PONG(2),
    LOGIN_IN_VALID_LOGIN(0x0101),
    LOGIN_LOGIN_NO_ROLE(0x0102),;

    ResponseCode(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return this.value;
    }
}
