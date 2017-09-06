package util;

import java.io.IOException;

public interface IDataWriter {

    void writeByte(byte value) throws IOException;

    void writeShort(short value) throws IOException;

    void writeInt(int value) throws IOException;

    void writeLong(long value) throws IOException;

    void writeDouble(double value) throws IOException;

    void writeBytes(byte[] buffer) throws IOException;

    void writeBytes(byte[] buffer, int offset, int length) throws IOException;
    
    void writeUTF(String s) throws IOException;
}
