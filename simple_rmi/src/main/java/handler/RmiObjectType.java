package handler;

/**
 * Created by think on 2017/4/17.
 */
public enum RmiObjectType {
    BYTE(1),
    SHORT(2),
    INT(3),
    STRING(4),
    FLOAT(5),
    DOUBLE(6),
    BOOLEAN(7),
    LONG(8),
    CALLBACK(9),
    LIST(10),
    OBJECT(11),
    NULL(12),
    ;
    private int value;

    public static RmiObjectType getByValue(int value) {
        for (RmiObjectType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    RmiObjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
