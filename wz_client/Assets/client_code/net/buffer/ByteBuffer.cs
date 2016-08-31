using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WzNet
{
    public class ByteBuffer
    {

        private byte[] bytes = null;

        private int writerIndex = 0;

        private int readerIndex = 0;

        public ByteBuffer(int capicity , ByteOrder order)
        {
            bytes = new byte[capicity];
        }

        public ByteBuffer(int capicity):this(capicity , ByteOrder.BIG)
        {
           
        }

        public ByteBuffer() : this(16, ByteOrder.BIG)
        {

        }

        public  int capicity()
        {
            return bytes.Length;
        }

        private void ensureWriteable(int length)
        {
            int minWriteAble = length + writerIndex;
            if( minWriteAble < capicity())
            {
                return;
            }
            int newCapacity = 64;
            while(newCapacity < minWriteAble)
            {
                newCapacity <<= 1;
            }
        }



        public  void writeByte(int value)
        {
            ensureWriteable(1);
            HeapByteBufferUtil.setByte(bytes, writerIndex, value);
            writerIndex++;
        }

        public void writeBytes(byte[] dst)
        {
            int length = dst.Length;
            ensureWriteable(length);

        }

        public void writeBytes(byte[] dst, int offset, int length)
        {
        }


        public  void writeInt(int value)
        {
            ensureWriteable(4);
            HeapByteBufferUtil.setInt(bytes, writerIndex, value);
            writerIndex += 4;
        }

        public void writeBoolean(bool value)
        {
            ensureWriteable(1);
            HeapByteBufferUtil.setBoolean(bytes, writerIndex, value);
            writerIndex++;
        }

        public void writeDouble(double value) {

        }

        public  void writeShort(int value)
        {
            ensureWriteable(2);
            HeapByteBufferUtil.setShort(bytes, writerIndex, value);
            writerIndex += 2;
        }

        public  void writeUtf8(string value)
        {
            if( value == null)
            {
                writeShort(0);
            }
            else
            {
                byte[] data = Encoding.UTF8.GetBytes(value);
                short length = (short)data.Length;
                writeShort(length);
                writeBytes(data);
            }
 
        }

        public  void writeFloat(float value)
        {
            ensureWriteable(4);
            HeapByteBufferUtil.setFloat(bytes, writerIndex, value);
            writerIndex += 4;
        }

        public void writeChar(int value)
        {
            ensureWriteable(2);
            HeapByteBufferUtil.setChar(bytes, writerIndex, value);
            writerIndex += 2;
        }

        public void writeLong(long value)
        {
            ensureWriteable(8);
            HeapByteBufferUtil.setLong(bytes, writerIndex, value);
            writerIndex += 8;
        }


        public int readerAbleBytes()
        {
            return capicity() - readerIndex;
        }

        public  int readByte()
        {
            byte value = HeapByteBufferUtil.getByte(bytes, readerIndex);
            readerIndex++;
            return value;
        }

        public  short readShort()
        {
            short value = HeapByteBufferUtil.getShort(bytes, readerIndex);
            readerIndex += 2;
            return value;
        }

        public  int readInt()
        {
            int value = HeapByteBufferUtil.getInt(bytes, readerIndex);
            readerIndex += 4;
            return value;
        }

        public  long readLong()
        {
            long value = HeapByteBufferUtil.getLong(bytes, readerIndex);
            readerIndex += 8;
            return value;
        }

        public  double readDouble()
        {
            return 0;
        }

        public  float readFloat()
        {
            float value = HeapByteBufferUtil.getFloat(bytes, readerIndex);
            readerIndex += 4;
            return value;
        }

        public  string readString()
        {
            return "1111";
        }

        public  char readChar()
        {
            char value = HeapByteBufferUtil.getChar(bytes, readerIndex);
            readerIndex += 2;
            return value;
        }

        public  byte[] getWriteBytes()
        {
            byte[] result = new byte[writerIndex];
            System.Array.Copy(bytes, result, writerIndex);
            return result;
        }
    }

    public enum ByteOrder {
        BIG,
        LITTLE
    }

}
