package common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by think on 2017/3/21.
 */
public enum LogCommand {

    WRITE_LOG(0x1001),
    LOG_PING(0x3002),
    ;

    private int value;

    LogCommand(int value) {
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    private static Map<Integer,LogCommand> idCmdCache = new ConcurrentHashMap<>();

    public static LogCommand getById(int id){
        if(idCmdCache.containsKey(id)){
            return idCmdCache.get(id);
        }
        for (LogCommand command : values()) {
            if(command.getValue() == id){
                return command;
            }
        }
        return null;
    }
}