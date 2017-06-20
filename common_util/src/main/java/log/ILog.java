package log;

public interface ILog {

    boolean isAutoFlush();

    void setAutoFlush(boolean auto);

    void flush();

    void close();

    void logAndFlush(String target, Object... msg);

    void log(String target, Object... msg);

    void logLevelup(Object... msg);

    void logExp(Object... msg);

    void logZq(Object... msg);

    void logMeso(Object... msg);

    void logCShop(Object... msg);

    void logCash(Object... msg);

    void logItem(Object... msg);

    void logAttr(Object... msg);

    void logQuest(Object... msg);

    void logOnline(Object... msg);

    void logCheat(Object... msg);

    void logHeal(Object... msg);

    void logGm(Object... msg);

    void logSvr(Object... msg);

    void logSvrAndFlush(Object... msg);

    void logCharge(Object... msg);

}