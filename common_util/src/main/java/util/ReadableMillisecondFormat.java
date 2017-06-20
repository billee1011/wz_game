package util;

public class ReadableMillisecondFormat {

    public long milliseconds;

    public ReadableMillisecondFormat(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int getSeconds(boolean withMinutes) {
        return (int) Math.floor(((double) milliseconds / 1000) % (withMinutes ? 60 : 0));
    }

    public int getMinutes(boolean withHours) {
        if (getSeconds(false) == 0) {
            return 0;
        }
        return (int) Math.floor((getSeconds(false) / 60) % (withHours ? 60 : 0));
    }

    public int getHours() {
        if (getMinutes(false) == 0) {
            return 0;
        }
        return (int) Math.floor(getMinutes(false) / 60);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getHours() > 0) {
            sb.append(getHours() + " hours,");
        }
        if (getMinutes(false) > 0) {
            sb.append(getMinutes(true) + " minutes,");
        }
        sb.append(sb.length() > 0 ? " and " : "");
        sb.append(getSeconds(true) + " seconds.");
        return sb.toString();
    }
}
