package date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class YearIndex {
    private IndexType type;
    private int year;
    private int index;

    public YearIndex(IndexType type, int year, int index) {
        this.type = type;
        this.year = year;
        this.index = index;
    }

    public IndexType getType() {
        return this.type;
    }

    public int getYear() {
        return this.year;
    }

    public int getIndex() {
        return this.index;
    }

    public void setType(IndexType type) {
        this.type = type;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof YearIndex) {
            YearIndex other = (YearIndex) obj;
            return type == other.type && year == other.year && index == other.index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.type.getValue() + this.index * 10 + this.year * 10000;
    }

    public YearIndex getLastWeek() {
        if (this.getType() != IndexType.WEEKLY) {
            throw new IllegalArgumentException("type = " + getType() + " != " + IndexType.WEEKLY);
        }
        if (getIndex() == 1) { // 跨年
            int lastYear = getYear() - 1;
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            String str = lastYear + "-12-31";
            try {
                date = fmt.parse(str);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Date: " + str, e);
            }
            YearIndex y = YearIndex.indexOfDate(getType(), date);
            return y;
        }
        else {
            YearIndex y = new YearIndex(getType(), getYear(), getIndex());
            y.setIndex(y.getIndex() - 1);
            return y;
        }
    }

    public YearIndex getLastMonth() {
        if (this.getType() != IndexType.MONTHLY) {
            throw new IllegalArgumentException("type = " + getType() + " != " + IndexType.MONTHLY);
        }
        YearIndex y = new YearIndex(getType(), getYear(), getIndex());
        if (y.getIndex() == 1) {
            y.setIndex(12);
            y.setYear(y.getYear() - 1);
        }
        else {
            y.setIndex(y.getIndex() - 1);
        }
        return y;
    }

    /**
     * <pre>
     * DAILY 返回一年中的第几天  (0-365)
     * MONTHLY 返回一年中的第几个月 (1-12)
     * WEEKLY 返回一年中的第几个星期 (1-xx)
     * </pre>
     * 
     * @param type
     * @param date
     * @return
     */
    public static YearIndex indexOfDate(IndexType type, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = 0;
        int index = 0;
        switch (type) {
        case DAILY:
            // 返回dayOfYear
            year = calendar.get(Calendar.YEAR);
            index = calendar.get(Calendar.DAY_OF_YEAR);
            break;
        case MONTHLY:
            // 返回monthOfYear
            year = calendar.get(Calendar.YEAR);
            index = calendar.get(Calendar.MONTH) + 1; // 1~12
            break;
        case WEEKLY:
            // 返回weekOfYear
            calendar.setMinimalDaysInFirstWeek(7);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            index = calendar.get(Calendar.WEEK_OF_YEAR);
            year = calendar.get(Calendar.YEAR);
            // 检查是不是去年剩余的周
            if (calendar.get(Calendar.MONTH) == 0 && index > 5) {
                --year;
            }
            break;
        }
        return new YearIndex(type, year, index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "|" + type + "|" + year + "|" + index;
    }

}
