package logic;


import java.util.Calendar;

/**
 * Created by Administrator on 2017/1/9.
 */
public class DailyFlush {
	private static DailyFlush ourInstance = new DailyFlush();

	public static DailyFlush getInstance() {
		return ourInstance;
	}

	private DailyFlush() {
	}

	//one time per second
	public void update() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		if (minute % 10 == 0 && second == 0) {
			flush10MinuteEvent(hour, minute);
		}
	}

	private void flush10MinuteEvent(int hour, int minute) {
		if (hour == 0 && minute == 0) {
		} else {
		}
	}

}