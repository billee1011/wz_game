package util;

import java.util.Date;

/**
 * 
 */
public class StringUtil {

    public static String getDayStr(Date date) {
        int d = date.getDay();
        if (d == 0) {
            d = 7;
        }
        return String.valueOf(d);
    }

    public static String getMonthDateStr(Date date) {
        int m = date.getMonth() + 1;
        int d = date.getDate();
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtil.getLeftPaddedStr(m + "", '0', 2));
        builder.append(StringUtil.getLeftPaddedStr(d + "", '0', 2));
        return builder.toString();
    }

    public static String getYearMonthDateStr(Date date) {
        int y = date.getYear() + 1900;
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtil.getLeftPaddedStr(y + "", '0', 4));
        builder.append(getMonthDateStr(date));
        return builder.toString();
    }

    public static String getHourMinuteStr(Date date) {
        int h = date.getHours();
        int m = date.getMinutes();
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtil.getLeftPaddedStr(h + "", '0', 2));
        builder.append(StringUtil.getLeftPaddedStr(m + "", '0', 2));
        return builder.toString();
    }

    /**
     */
    public static String getLeftPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int x = in.getBytes().length; x < length; x++) {
            builder.append(padchar);
        }
        builder.append(in);
        return builder.toString();
    }

    /**
     */
    public static String getRightPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.getBytes().length; x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    /**
     */
    public static String joinStringFrom(String arr[], int start) {
        return joinStringFrom(arr, start, " ");
    }

    /**
     */
    public static String joinStringFrom(String arr[], int start, String sep) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(sep);
            }
        }
        return builder.toString();
    }

    /**
     */
    public static String makeEnumHumanReadable(String enumName) {
        StringBuilder builder = new StringBuilder(enumName.length() + 1);
        String[] words = enumName.split("_");
        for (String word : words) {
            if (word.length() <= 2) {
                builder.append(word);
            }
            else {
                builder.append(word.charAt(0));
                builder.append(word.substring(1).toLowerCase());
            }
            builder.append(' ');
        }
        return builder.substring(0, enumName.length());
    }

    /**
     */
    public static int countCharacters(String str, char chr) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == chr) {
                ret++;
            }
        }
        return ret;
    }

}