package date;


public enum OnlineType {

    DAILY_MINUTES(1), // 
    WEEKLY_DAYS(2), //
    MONTHLY_MINUTES(3), // 每月在线时间
    WEEKLY_MINUTES(4), // 每周在线时间
    ;

    private final int value;

    private OnlineType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public IndexType getIndexType() {
        switch (this) {
        case DAILY_MINUTES:
            return IndexType.DAILY;
        case WEEKLY_DAYS:
        case WEEKLY_MINUTES:
            return IndexType.WEEKLY;
        case MONTHLY_MINUTES:
            return IndexType.MONTHLY;
        }
        return null;
    }

    public static OnlineType getByValue(int value) {
        for (OnlineType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

}
