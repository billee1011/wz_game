package date;

public enum IndexType {

    DAILY(1), // 
    WEEKLY(2), //
    MONTHLY(3), //
    ;

    private final int value;

    private IndexType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static IndexType getByValue(int value) {
        for (IndexType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

}
