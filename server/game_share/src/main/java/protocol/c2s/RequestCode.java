package protocol.c2s;

public enum RequestCode {
    PING(0X0001),
    LOGIN_BEGIN(100),
    LOGIN_REQUEST_LOGIN(101),
    LOGIN_CREATE_ROLE(102),
    ;


    RequestCode(int value) {
        this.value = value;
    }


    public int getValue() {
        return this.value;
    }


    private int value;

    public static RequestCode getByValue(int value) {
        for (RequestCode code : values()) {
            if (code.getValue() == value) {
                return code;
            }
        }
        return null;
    }

}
