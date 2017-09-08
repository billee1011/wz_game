package util;


import actor.IActor;
import actor.ICallback;
import proto.Common;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtil {

	public static final Charset UTF8 = Charset.forName("UTF-8");

	public static final long UUID_MASK = 0x07FFFFFFFFFFFFFFL; // 59 bits,(5*10^17)
	public static final long DOUBLE_PRECISION_MASK = 0x001FFFFFFFFFFFFFL; // 53 bits, (10^16)

	public static final String paiHangSplit = "#;#,@;@";

	private static final AtomicInteger randomNum = new AtomicInteger(1);

	private static final boolean ACTOER_ASSERT_ON = true;

	public static long makeUUID(int itemid, int serverId, int charId) {
		String seed = serverId + "" + itemid + "" + charId + "";
		return makeUUID(seed);
	}

	public static long get53Long(long value) {
		return (value &= DOUBLE_PRECISION_MASK);
	}

	/**
	 * 只有53 bits, 保证传给double的时候,不会丢失精度
	 *
	 * @param seed
	 * @return
	 */
	public static long makeUUID(String seed) {
		try {
			seed = System.currentTimeMillis() + seed + Randomizer.nextInt() + " "
					+ Thread.currentThread().getId();
			byte[] digest = getMD5Bytes(seed.getBytes(UTF8));
			long result = 0;
			result = ((result << 8) | (digest[0] & 0xFF));
			result = ((result << 8) | (digest[1] & 0xFF));
			result = ((result << 8) | (digest[2] & 0xFF));
			result = ((result << 8) | (digest[3] & 0xFF));
			result = ((result << 8) | (digest[4] & 0xFF));
			result = ((result << 8) | (digest[5] & 0xFF));
			result = ((result << 8) | (digest[6] & 0xFF));
			result = ((result << 8) | (digest[7] & 0xFF));
			//result &= DOUBLE_PRECISION_MASK; // 53 bits
			result &= UUID_MASK;
			return result;
		} catch (Exception e) {
			return 0L;
		}
	}

	/**
	 * 只有53 bits, 保证传给double的时候,不会丢失精度
	 *
	 * @param seed
	 * @return
	 */
	public static long makeFixedUUID(String seed) {
		try {
			byte[] digest = getMD5Bytes(seed.getBytes(UTF8));
			long result = 0;
			result = ((result << 8) | (digest[0] & 0xFF));
			result = ((result << 8) | (digest[1] & 0xFF));
			result = ((result << 8) | (digest[2] & 0xFF));
			result = ((result << 8) | (digest[3] & 0xFF));
			result = ((result << 8) | (digest[4] & 0xFF));
			result = ((result << 8) | (digest[5] & 0xFF));
			result = ((result << 8) | (digest[6] & 0xFF));
			result = ((result << 8) | (digest[7] & 0xFF));
			// result &= DOUBLE_PRECISION_MASK; // 53 bits
			result &= UUID_MASK;
			return result;
		} catch (Exception e) {
			return 0L;
		}
	}

	public static void assertActorThread(IActor actor) {
		if (ACTOER_ASSERT_ON) {
			if (actor == null) {
				return;
			}
			Thread curr = Thread.currentThread();
			if (actor.getThreadId() != curr.getId()) {
				throw new IllegalStateException("CallerThread: " + curr.getName() + "/" + curr.getId()
						+ ", TargetThread: " + actor.getThreadName() + "/" + actor.getThreadId() + ", not match");
			}
		}
	}

	public static String getSystemProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null || (value = value.trim()).length() == 0) {
			value = defaultValue;
		}
		return value;
	}

	public static <T extends Comparable<T>> T limitMinMax(T target, T minValue, T maxValue) {
		if (minValue.compareTo(maxValue) > 0) {
			throw new IllegalArgumentException("minValue > maxValue");
		}
		if (target.compareTo(minValue) < 0)
			target = minValue;
		if (target.compareTo(maxValue) > 0)
			target = maxValue;
		return target;
	}

	public static int min(int... list) {
		int length = list.length;
		int min = list[0];
		for (int i = 1; i < length; i++) {
			if (min > list[i]) {
				min = list[i];
			}
		}
		return min;
	}

	public static byte[] getMD5Bytes(byte[] buffer) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.reset();
		md5.update(buffer);
		byte[] byteArray = md5.digest();
		return byteArray;
	}

	public static String getMD5(String source) {
		StringBuffer md5StrBuff = new StringBuffer();
		try {
			byte[] byteArray = getMD5Bytes(source.getBytes("utf-8"));
			String tmp = "";
			for (int i = 0; i < byteArray.length; i++) {
				tmp = Integer.toHexString(0xFF & byteArray[i]);
				if (tmp.length() == 1)
					md5StrBuff.append("0").append(tmp);
				else
					md5StrBuff.append(tmp);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5StrBuff.toString();
	}

	public static boolean isHexDigit(char ch) {
		return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
	}

	public static boolean isHexString(String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}
		for (int i = 0; i < s.length(); ++i) {
			if (!isHexDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static int getIpAddress(byte[] data) {
		if (data == null || data.length != 4) {
			throw new IllegalArgumentException();
		}
		return (((data[3] & 0xFF) << 24) | ((data[2] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[0] & 0xFF));
	}

	public static byte parseByte(String v) {
		return (byte) (Integer.parseInt(v) & 0xFF);
	}

	public static int getIpAddress(String data) {
		String[] segs = data.trim().split("\\.");
		if (segs.length != 4) {
			throw new IllegalArgumentException("invalid ip v4 address");
		}
		byte[] bytes = new byte[]{parseByte(segs[3]), parseByte(segs[2]), parseByte(segs[1]),
				parseByte(segs[0])};
		return getIpAddress(bytes);
	}

	public static <E, F> Pair<E, F> newPair(E left, F right) {
		return new Pair<E, F>(left, right);
	}

	public static <E> List<E> newSyncArrayList() {
		return Collections.synchronizedList(new ArrayList<E>());
	}

	public static <E> ArrayList<E> newArrayList() {
		return new ArrayList<E>();
	}

	public static <E> LinkedList<E> newLinkedList() {
		return new LinkedList<E>();
	}

	public static <E> List<E> newSyncLinkedList() {
		return Collections.synchronizedList(new LinkedList<E>());
	}

	public static <K, V> Map<K, V> newSyncArrayMap() {
		return Collections.synchronizedMap(new ArrayMap<K, V>());
	}

	public static <E> Set<E> newSyncHashSet() {
		return Collections.synchronizedSet(new HashSet<E>());
	}

	public static <K, V> ArrayMap<K, V> newArrayMap() {
		return new ArrayMap<K, V>();
	}

	public static <K, V> ArrayMap<K, V> newArrayMap(K key, V value) {
		ArrayMap<K, V> map = new ArrayMap<K, V>();
		map.put(key, value);
		return map;
	}

	public static Map<String, String> newParamsMap(String... kvs) {
		Map<String, String> map = new ArrayMap<String, String>();
		if (kvs == null) {
			return map;
		}
		for (int i = 0; (i + 1) < kvs.length; i += 2) {
			if (kvs[i] != null && kvs[i + 1] != null) {
				map.put(kvs[i], kvs[i + 1]);
			}
		}
		return map;
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	public static <K, V> Map<K, V> newLinkedHashMap(boolean accessOrder) {
		return new LinkedHashMap<K, V>(16, 0.75f, accessOrder);
	}

	public static <K, V> Map<K, V> newSyncLinkedHashMap() {
		return Collections.synchronizedMap(new LinkedHashMap<K, V>());
	}

	public static <K, V> Map<K, V> newSyncLinkedHashMap(boolean accessOrder) {
		return Collections.synchronizedMap(new LinkedHashMap<K, V>(16, 0.75f, accessOrder));
	}

	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> Map<K, V> newSyncHashMap() {
		return Collections.synchronizedMap(new HashMap<K, V>());
	}

	public static <K, V> Map<K, V> newWeakHashMap() {
		return new WeakHashMap<K, V>();
	}

	public static <E> PriorityQueue<E> newPriorityQueue() {
		return new PriorityQueue<E>();
	}

	public static <E> Set<E> newHashSet() {
		return new HashSet<E>();
	}

	public static <E> Set<E> newLinkedHashSet() {
		return new LinkedHashSet<E>();
	}

	public static byte[] getBlobBytes(Blob blob) {
		if (blob == null) {
			return null;
		}
		try {
			return blob.getBytes(1L, (int) blob.length());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (blob != null) {
				try {
					blob.free();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Object readObject(Blob blob) {
		byte[] buffer = getBlobBytes(blob);
		if (buffer == null || buffer.length == 0) {
			return null;
		}
		return readObject(buffer);
	}

	public static Object readObject(byte[] buffer) {
		return readObject(buffer, 0, buffer.length);
	}

	public static Object readObject(byte[] buffer, int offset, int length) {
		ByteArrayInputStream bytesInput = new ByteArrayInputStream(buffer, offset, length);
		ObjectInputStream inputStream = null;
		try {
			inputStream = new ObjectInputStream(bytesInput);
			return inputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static byte[] getObjectBytes(Object obj) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(buffer);
			outputStream.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
			buffer.reset();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
		return buffer.toByteArray();
	}

	public static int parseInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return def;
		}
	}

	public static List<Integer> parseIntList(String values, String separate) {
		List<Integer> ret = newArrayList();
		if (values != null) {
			String[] arr = values.split(separate);
			if (arr != null && arr.length > 0) {
				for (int i = 0; i < arr.length; i++) {
					int id = parseInt(arr[i], 0);
					ret.add(id);
				}
			}
		}
		return ret;
	}

	public static int parseIntFront(String value) {
		if (value == null) {
			return 0;
		}
		int ret = 0;
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) >= '0' && value.charAt(i) <= '9') {
				ret *= 10;
				ret += value.charAt(i) - '0';
			} else {
				break;
			}
		}
		return ret;
	}

	public static long parseLong(String value, long def) {
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			return def;
		}
	}

	public static int getDaysOffset(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		long utcMillis = c.getTimeInMillis() + c.get(Calendar.ZONE_OFFSET);
		return (int) (utcMillis / (24L * 3600 * 1000));
	}

	public static int getDayOfYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DAY_OF_YEAR);
	}

	public static int getWeekOffset(long from, long to) {
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTimeInMillis(from);
		Calendar toCal = Calendar.getInstance();
		toCal.setTimeInMillis(to);
		long fromWeekBegin = from - (getDayOfWeek(fromCal) - 1) * 24 * 36000 * 1000L - getSecondsOfDay(fromCal) * 1000L;
		long toWeekBegin = to - (getDayOfWeek(toCal) - 1) * 24 * 36000 * 1000L - getSecondsOfDay(toCal) * 1000L;
		long timeIntv = toWeekBegin - fromWeekBegin;
		return (int) ((timeIntv + 24 * 36000 * 1000L) / (7L * 24 * 36000 * 1000L));
	}

	public static long getMidnightToday(long now) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTimeInMillis() + 1;
	}

	public static long getMidnightYesterday(long now) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static <E> void shuffle(List<E> array) {
		if (array == null) {
			return;
		}
		for (int i = 0; i < array.size(); i++) {
			int pos = Randomizer.nextInt(array.size());
			E tmp = array.get(pos);
			if (pos != i) {
				array.set(pos, array.get(i));
				array.set(i, tmp);
			}
		}
	}

	private static ThreadLocal<SimpleDateFormat> localFormatFileName = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			return fmt;
		}
	};

	public static String getDateStrFileName(long timestamp) {
		SimpleDateFormat fmt = localFormatFileName.get();
		String name = fmt.format(new Date(timestamp));
		return name;
	}

	private static ThreadLocal<SimpleDateFormat> localFormat_ = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			return fmt;
		}
	};

	public static String getDateStr_(long timestamp) {
		String name = localFormat_.get().format(new Date(timestamp));
		return name;
	}

	private static ThreadLocal<SimpleDateFormat> localFormatSpace = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return fmt;
		}
	};

	public static int getCurrentSeconds() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	public static boolean getRandomStatus() {
		int data1 = (int) System.currentTimeMillis() % 1000;
		int value1 = data1 % 10 + (data1 / 10) % 10 + (data1 / 100) % 10;
		if (value1 < 27 / 2) {
			return false;
		}
		return true;
	}

	public static String getDateStrB(long timestamp) {
		SimpleDateFormat fmt = localFormatSpace.get();
		String name = fmt.format(new Date(timestamp));
		return name;
	}

	/**
	 * replace target with replacement in source
	 */
	public static String replaceIgnoreCase(CharSequence source, CharSequence target, CharSequence replacement) {
		return Pattern.compile(target.toString(), Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher(source)
				.replaceAll(Matcher.quoteReplacement(replacement.toString()));
	}

	/**
	 * replace #{key}, ignore case
	 *
	 * @param text
	 * @param params
	 * @return
	 */
	public static String replaceTemplate(String text, Map<String, String> params) {
		if (text != null && params != null && !params.isEmpty()) {
			for (String key : params.keySet()) {
				if (key == null) {
					continue;
				}
				String value = params.get(key);
				if (value != null) {
					text = replaceIgnoreCase(text, "#{" + key + "}", value);
				}
			}
		}
		return text;
	}

	public static int parseTimeInMinutes(String timestr) {
		int result = parseTimeInSeconds(timestr);
		return result < 0 ? result : (result / 60);
	}

	public static List<Integer> parseTimeListInSeconds(String timestr, String separator) {
		List<Integer> ret = MiscUtil.newArrayList();
		if (timestr == null || separator == null) {
			return ret;
		}
		String[] arr = timestr.split(separator);
		if (arr == null || arr.length == 0) {
			return ret;
		}
		for (int i = 0; i < arr.length; i++) {
			int sec = parseTimeInSeconds(arr[i]);
			ret.add(sec);
		}
		return ret;
	}

	/**
	 * @param timestr 23:34:28
	 * @return
	 */
	public static int parseTimeInSeconds(String timestr) {
		if (timestr == null || (timestr = timestr.trim()).length() == 0) {
			return -1;
		}
		String[] parts = timestr.split("[:]+");
		if (parts.length != 2 && parts.length != 3) {
			return -1;
		}
		int hour = MiscUtil.parseInt(parts[0], -1);
		int minute = MiscUtil.parseInt(parts[1], -1);
		int second = (parts.length < 3 ? 0 : MiscUtil.parseInt(parts[2], -1));
		if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60 || second < 0 || second >= 60) {
			return -1;
		}
		return hour * 3600 + minute * 60 + second;
	}

	public static int getMinutesOfDay() {
		return getSecondsOfDay() / 60;
	}

	public static int getSecondsOfDay() {
		Calendar now = Calendar.getInstance();
		return getSecondsOfDay(now);
	}

	public static int getSecondsOfDay(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return getSecondsOfDay(cal);
	}

	public static int getSecondsOfDay(Calendar cal) {
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int time = hour * 3600 + minute * 60 + second;
		return time;
	}

	public static String getStringOfSecondsInDay(int seconds) {
		int hour = seconds / 3600;
		int minute = seconds / 60 % 60;
		int second = seconds % 60;
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.getLeftPaddedStr(String.valueOf(hour), '0', 2));
		sb.append(":");
		sb.append(StringUtil.getLeftPaddedStr(String.valueOf(minute), '0', 2));
		sb.append(":");
		sb.append(StringUtil.getLeftPaddedStr(String.valueOf(second), '0', 2));
		return sb.toString();
	}

	/**
	 * 1(monday)~7(sunday)
	 */
	public static int getDayOfWeek() {
		Calendar now = Calendar.getInstance();
		return getDayOfWeek(now);
	}

	private static int getDayOfWeek(Calendar cal) {
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 0) {
			dayOfWeek = 7;
		}
		return dayOfWeek;
	}

	public static boolean isSameWeekDates(long time1, long time2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(new Date(time1));
		cal2.setTime(new Date(time2));
		long weekBegin1 = time1 - (getDayOfWeek(cal1) - 1) * (24 * 3600 * 1000L) - (getSecondsOfDay(cal1) * 1000L);
		long weekBegin2 = time2 - (getDayOfWeek(cal2) - 1) * (24 * 3600 * 1000L) - (getSecondsOfDay(cal2) * 1000L);
		if (Math.abs(weekBegin1 - weekBegin2) < (24 * 3600 * 1000L)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isSameDay(int time1, int time2) {
		return isSameDay(new Date(time1 * 1000), new Date(time2 * 1000));
	}

	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null && date2 == null) {
			return true;
		}
		if (date1 == null || date2 == null) {
			return false;
		}
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
				cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
			return true;
		}
		return false;
	}

	public static String randomStringCurrentTime() {
		long currentTime = getCurrentSeconds();
		long resultValue = currentTime << 10 + randomNum.getAndIncrement();
		return getMD5(String.valueOf(resultValue));
	}


	public static boolean isSameWeekDates(Date date1, Date date2) {
		if (date1 == null && date2 == null) {
			return true;
		}
		if (date1 == null || date2 == null) {
			return false;
		}
		return isSameWeekDates(date1.getTime(), date2.getTime());
	}

	public static String getPaiHangOutFileName(long time, String path, String file) {
		long currTime = time;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currTime);
		String name = path + "/" + file + "_" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
				+ "-" + c.get(Calendar.DATE) + ".log";
		return name;
	}

	public static long toLong(Object v) {
		if (v instanceof Number) {
			return ((Number) v).longValue();
		} else if (v instanceof String) {
			try {
				long ret = Long.parseLong((String) v);
				return ret;
			} catch (Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static int toInteger(Object v) {
		if (v instanceof Number) {
			return ((Number) v).intValue();
		} else if (v instanceof String) {
			try {
				int ret = Integer.parseInt((String) v);
				return ret;
			} catch (Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static List<Pair<Integer, Integer>> parseIntPairList(String listStr, String pairSep, String innerSep) {
		List<Pair<Integer, Integer>> ret = newArrayList();
		if (listStr == null || listStr.length() == 0) {
			return ret;
		}
		String[] arr = listStr.split(pairSep);
		if (arr == null || arr.length == 0) {
			return ret;
		}
		for (int i = 0; i < arr.length; i++) {
			String inner = arr[i];
			if (inner == null || inner.length() == 0) {
				continue;
			}
			String[] subArr = inner.split(innerSep);
			if (subArr == null || subArr.length < 2) {
				continue;
			}
			int key = parseInt(subArr[0], 0);
			int value = parseInt(subArr[1], 0);
			ret.add(new Pair<Integer, Integer>(key, value));
		}
		return ret;
	}

	public static String mergeIntList(List<Integer> list, String sep) {
		StringBuilder sb = new StringBuilder();
		if (list == null) {
			return sb.toString();
		}
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(sep);
			}
			sb.append(list.get(i));
		}
		return sb.toString();
	}

	public static long getPaihangToday(long now) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static byte[] writeObjectAsBuffer(Object data) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(data);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	public static List<Pair<Integer, Integer>> getPairList(String str, String one, String two) {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		String[] strs1 = str.split(one);
		for (String s : strs1) {
			String[] strs2 = s.split(two);
			Pair<Integer, Integer> pair = newPair(Integer.parseInt(strs2[0]), Integer.parseInt(strs2[1]));
			result.add(pair);
		}
		return result;
	}

	public static Pair<Integer, Integer> getPair(String str, String one) {
		String[] strs = str.split(one);
		if (strs.length != 2) {
			return null;
		}
		return new Pair<Integer, Integer>(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
	}

	public static List<Integer> getList(String str, String one) {
		List<Integer> result = new ArrayList<>();
		String[] strs = str.split(one);
		int length = strs.length;
		for (int i = 0; i < length; i++) {
			result.add(Integer.parseInt(strs[i]));
		}
		return result;
	}

	public static int randomKey(List<Pair<Integer, Integer>> list) {
		int totalWeight = 0;
		for (Pair<Integer, Integer> pair : list) {
			totalWeight += pair.getRight();
		}
		int randomNum = Randomizer.nextInt(totalWeight);
		int currentWeight = 0;
		for (Pair<Integer, Integer> pair : list) {
			currentWeight += pair.getRight();
			if (randomNum < currentWeight) {
				return pair.getLeft();
			}
		}
		return -1;
	}

	public static List<Three<Integer, Integer, Integer>> getPrimaryAttr(String str, String one, String two) {
		List<Three<Integer, Integer, Integer>> result = new ArrayList<>();
		String[] strs1 = str.split(",");
		int length = strs1.length;
		for (int i = 0; i < length; i++) {
			String[] strs2 = strs1[i].split("_");
			if (strs2.length != 3) {
				continue;
			}
			Three<Integer, Integer, Integer> element = new Three<>();
			element.setA(Integer.parseInt(strs2[0]));
			element.setB(Integer.parseInt(strs2[1]));
			element.setC(Integer.parseInt(strs2[2]));
			result.add(element);
		}
		return result;
	}


	public static List<FiveElement<Integer, Integer, Integer, Integer, Integer>> getDropItems(String str, String one, String two) {
		List<FiveElement<Integer, Integer, Integer, Integer, Integer>> result = new ArrayList<>();
		String[] strs1 = str.split(",");
		int length = strs1.length;
		for (int i = 0; i < length; i++) {
			String[] strs2 = strs1[i].split("_");
			if (strs2.length < 3) {
				continue;
			}
			FiveElement<Integer, Integer, Integer, Integer, Integer> element = new FiveElement<>();
			element.setA(Integer.parseInt(strs2[0]));
			element.setB(Integer.parseInt(strs2[1]));
			element.setC(Integer.parseInt(strs2[2]));
			if (strs2.length > 3) {
				element.setD(Integer.parseInt(strs2[3]));
			}
			if (strs2.length > 4) {
				element.setE(Integer.parseInt(strs2[4]));
			}
			result.add(element);
		}
		return result;
	}

	public static <T> void switchElementPosition(List<T> list, int index1, int index2) {
		T temp = list.get(index1);
		list.set(index1, list.get(index2));
		list.set(index2, temp);
	}


	public static Map<Integer, Integer> getMapOfString(String str, String one, String two) {
		Map<Integer, Integer> result = new HashMap<>();
		String[] strs1 = str.split(one);
		for (String s : strs1) {
			String[] strs2 = s.split(two);
			result.put(Integer.parseInt(strs2[0]), Integer.parseInt(strs2[1]));
		}
		return result;
	}

	public static void checkAndCallback(boolean check, ICallback callback) {
		checkAndCallback(check, callback, null);
	}

	public static void checkAndCallback(boolean check, ICallback callback, ICallback falseCallback) {
		if (check) {
			if (callback != null) {
				callback.onResult(null);
			}
		} else {
			if (falseCallback != null) {
				falseCallback.onResult(null);
			}
		}
	}


	public static String getCurrentTimeFormat() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		return format.format(new Date());
	}

	public static int getSecondsOfTimeStamp_ex(String timeStamp, String timeFromat) {
		if (timeStamp.equals("")) {
			timeStamp = getCurrentTimeFormat();
		}
		SimpleDateFormat sdf = new SimpleDateFormat(timeFromat);
		Date date = null;
		try {
			date = sdf.parse(timeStamp);
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
		if (null == date) {
			return -1;
		}
		Calendar calendar = Calendar.getInstance();
		if (null == calendar) {
			return -1;
		}
		calendar.setTime(date);
		return (int) (calendar.getTimeInMillis() / 1000);
	}

	public static long getTodayZeroTime(long now) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis() / 1000;
	}

	public static long getTimerPreDot(int day, long hour) {
		return getCurrentSeconds() - day * hour * 3600;
	}
}
