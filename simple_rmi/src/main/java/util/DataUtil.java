package util;

import data.IData;
import handler.Pair;
import handler.RmiObjectType;
import io.netty.buffer.ByteBuf;
import proxy.WzCallback;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangfang on 2017/5/22.
 */
public class DataUtil {
    public static String readUtf8(ByteBuf buffer) {
        int length = buffer.readShort();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes, 0, length);
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static Object readObject(ByteBuf buffer) {
        int type = buffer.readByte();
        RmiObjectType objectType = RmiObjectType.getByValue(type);
        switch (objectType) {
            case INT:
                return buffer.readInt();
            case STRING:
                return DataUtil.readUtf8(buffer);
            case SHORT:
                return buffer.readShort();
            case BOOLEAN:
                return buffer.readByte() == 0 ? false : true;
            case CALLBACK:
                // callback we need know the callback args num and callback thing
                return new WzCallback() {
                    @Override
                    public void onResult(Object o) {

                    }
                };
            case LIST:
                return readList(buffer);
            case OBJECT:
                return readData(buffer);
            case NULL:
                return null;
            default:
                throw new RuntimeException("un support type of object");
        }
    }

    public static List<Object> readList(ByteBuf buffer) {
        int size = buffer.readShort();
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(readObject(buffer));
        }
        return result;
    }

    public static Object readData(ByteBuf buffer) {
        String dataName = DataUtil.readUtf8(buffer);
        Class<?> classType = null;
        try {
            classType = Class.forName(dataName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("no such data class Exception");
        }
        Object obj = null;
        try {
            obj = classType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (false == obj instanceof IData) {
            throw new RuntimeException("unKnow type of data class Exception");
        }
        IData data = (IData) obj;
        ((IData) obj).read(buffer);
        return data;
    }

    public static void writeObject(ByteBuf buffer, Object o) {
        if (o == null) {
            buffer.writeByte(RmiObjectType.NULL.getValue());
            return;
        }
        String className = o.getClass().getSimpleName();
        className = className.equals("") ? "callback" : className;
        switch (className) {
            case "Integer":
                buffer.writeByte(RmiObjectType.INT.getValue());
                buffer.writeInt((Integer) o);
                break;
            case "String":
                buffer.writeByte(RmiObjectType.STRING.getValue());
                writeUtf8(buffer, (String) o);
                break;
            case "Boolean":
                buffer.writeByte(RmiObjectType.BOOLEAN.getValue());
                buffer.writeByte((boolean) o ? 1 : 0);
                break;
            case "Short":
                buffer.writeByte(RmiObjectType.SHORT.getValue());
                buffer.writeShort((Short) o);
                break;
            case "callback":
                buffer.writeByte(RmiObjectType.CALLBACK.getValue());
                //write nothing because the callback class does't have and fields;
                break;
            case "List":
                buffer.writeByte(RmiObjectType.LIST.getValue());
                writeList(buffer, (List) o);
                break;
            default:
                System.out.println("encode object type " + o);
                buffer.writeByte(RmiObjectType.OBJECT.getValue());
                ByteBuf buf = ((IData) o).write();
                buffer.writeBytes(buf);
                break;

        }
    }

    public static void writeUtf8(ByteBuf buffer, String str) {
        if (str.equals("")) {
            buffer.writeShort(0);
        } else {
            buffer.writeShort(str.length());
            buffer.writeBytes(str.getBytes(Charset.forName("utf-8")));
        }
    }

    public static void writeMap(ByteBuf buffer, List<Pair<String, Object>> map) {
        buffer.writeShort(map.size());
        map.forEach((e) -> {
            writeUtf8(buffer, e.getK());
            writeObject(buffer, e.getV());
        });
    }

    public static void writeList(ByteBuf buffer, List<Object> list) {
        int size = list.size();
        buffer.writeShort(size);
        for (int i = 0; i < size; i++) {
            writeObject(buffer, list.get(i));
        }
    }

}
