package log;


import util.MiscUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Calendar;

public class XLog implements ILog {

    private final static Charset kCharset = Charset.forName("UTF-8");

    private String fileName;
    private String realName;
    private PrintWriter pw;
    private long fileModifyTime = 0;
    private final Object kMutex = new Object();
    private boolean autoFlush = true;

    private static final String kLogDir;

    static {
        kLogDir = System.getProperty("coco.logdir", "log");
    }

    private XLog() {
    }

    public boolean isAutoFlush() {
        return this.autoFlush;
    }

    public void setAutoFlush(boolean auto) {
        this.autoFlush = auto;
    }

    public void flush() {
        try {
            if (pw != null) {
                pw.flush();
            }
        }
        catch (Throwable t) {
            // ignore
        }
    }

    public void close() {
        try {
            if (pw != null) {
                pw.close();
                realName = "";
                fileModifyTime = 0;
                pw = null;
            }
        }
        catch (Throwable t) {
            // ignore
        }
    }

    private void createPrintWriter() {
        try {
            synchronized (kMutex) {
                try {
                    long currTime = System.currentTimeMillis();
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(currTime);
                    String datestr = MiscUtil.getDateStrFileName(currTime).substring(0, 10);
                    String name = fileName + "_" + datestr + ".log";
                    fileModifyTime = currTime;
                    if (name.equals(realName)) {
                        pw.flush();
                        return;
                    }
                    if (pw != null) {
                        pw.close();
                    }
                    File logDir = new File(kLogDir);
                    if (!logDir.exists()) {
                        logDir.mkdirs();
                    }
                    String logName = kLogDir + File.separator + name;
                    OutputStream outputStream = new FileOutputStream(logName, true);
                    pw = new PrintWriter(new OutputStreamWriter(outputStream, kCharset));
                    realName = name;
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                    pw = null;
                    realName = "";
                }
            }
        }
        catch (Throwable t) {
            // ignore
        }
    }

    public static ILog emptyInst() {
        return new XLog();
    }

    public static ILog newInst(String fileName) {
        XLog log = new XLog();
        try {
            log.fileName = fileName;
            log.createPrintWriter();
        }
        catch (Throwable t) {
            // ignore
        }
        return log;
    }

    public void logAndFlush(String target, Object... msg) {
        log(target, msg);
        flush();
    }

    public void log(String target, Object... msg) {
        if (pw == null) {
            return;
        }
        try {
            synchronized (kMutex) {
                long currTime = System.currentTimeMillis();
                if (currTime - fileModifyTime >= 5 * 60 * 1000L) {
                    createPrintWriter();
                }
                if (pw != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(MiscUtil.getDateStr_(currTime)).append("|").append(target).append(",");
                    for (int i = 0; i < msg.length; ++i) {
                        builder.append(msg[i]).append(",");
                    }
                    String message = builder.toString();
                    pw.println(message);
                    if (autoFlush) {
                        pw.flush();
                    }
                    // 
                    System.err.println(message);
                }
            }
        }
        catch (Throwable t) {
            // ignore
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void logLevelup(Object... msg) {
        log(LogTarget.LEVELUP, msg);
    }

    public void logExp(Object... msg) {
        log(LogTarget.EXP, msg);
    }

    public void logZq(Object... msg) {
        log(LogTarget.ZQ, msg);
    }

    public void logMeso(Object... msg) {
        log(LogTarget.MESO, msg);
    }

    public void logCShop(Object... msg) {
        log(LogTarget.CSHOP, msg);
    }

    public void logCash(Object... msg) {
        log(LogTarget.CASH, msg);
    }

    public void logItem(Object... msg) {
        log(LogTarget.ITEM, msg);
    }

    public void logAttr(Object... msg) {
        log(LogTarget.ATTR, msg);
    }

    public void logQuest(Object... msg) {
        log(LogTarget.QUEST, msg);
    }

    public void logOnline(Object... msg) {
        log(LogTarget.ONLINE, msg);
    }

    public void logCheat(Object... msg) {
        log(LogTarget.CHEAT, msg);
    }

    public void logHeal(Object... msg) {
        log(LogTarget.HEAL, msg);
    }

    public void logGm(Object... msg) {
        log(LogTarget.GM, msg);
    }

    public void logSvr(Object... msg) {
        log(LogTarget.SERVER_INFO, msg);
    }

    public void logSvrAndFlush(Object... msg) {
        logAndFlush(LogTarget.SERVER_INFO, msg);
    }

    public void logCharge(Object... msg) {
        logAndFlush(LogTarget.CHARGE, msg);
    }
}
