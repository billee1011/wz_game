package data.logdata;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import network.AbstractHandlers;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by think on 2017/3/21.
 */
public interface ILogInfo {
    void read(AbstractHandlers.MessageHolder<MessageLite> messageContainer);

    MessageLite write();

    void save() throws SQLException;

    default String getTableName(String table, long time) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
        return table + fmt.format(new Date(time * 1000));
    }
}
