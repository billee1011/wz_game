package util;

import java.io.IOException;

public interface IDataReader {

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    double readDouble() throws IOException;

    void readBytes(byte[] buffer) throws IOException;

    void readBytes(byte[] buffer, int offset, int length) throws IOException;
    
    String readUTF() throws IOException;
}
