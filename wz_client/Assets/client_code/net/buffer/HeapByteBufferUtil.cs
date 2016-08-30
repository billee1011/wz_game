using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WzBuffer
{
    class HeapByteBufferUtil
    {

        public static void setInt(byte[] bytes, int index, int value)
        {
            bytes[index] = (byte)(value >> 24);
            bytes[index + 1] = (byte)(value >> 16);
            bytes[index + 2] = (byte)(value >> 8);
            bytes[index + 3] = (byte)(value);
        }

        public static void setByte(byte[] bytes , int index , int value)
        {
            bytes[index] = (byte)value;
        }

        public static void setBytes(byte[] bytes, int index, byte[] dst)
        {
            
        }

        public static void setBoolean(byte[] bytes , int index, bool value)
        {
            setByte(bytes, index, value ? 1 : 0);
        }

        public static void setShort(byte[] bytes, int index, int value)
        {
            bytes[index] = (byte)(value >> 8);
            bytes[index + 1] = (byte)value;
        }

        public static void setLong(byte[] bytes , int index , long value)
        {
            bytes[index] = (byte)(value >> 56);
            bytes[index + 1] = (byte)(value >> 48);
            bytes[index + 2] = (byte)(value >> 40);
            bytes[index + 3] = (byte)(value >> 32);
            bytes[index + 4] = (byte)(value >> 24);
            bytes[index + 5] = (byte)(value >> 16);
            bytes[index + 6] = (byte)(value >> 8);
            bytes[index + 7] = (byte)(value);
        }

        public static void setChar(byte[] bytes , int index , int value)
        {
            setShort(bytes, index, value);
        }

        public static void setFloat(byte[] bytes, int index, float value) {
            
        }

        public static void setDouble(byte[] bytes , int index , double value)
        {

        }

        public static byte getByte(byte[] bytes, int index) 
        {
            return bytes[index];
        }

        public static short getShort(byte[] bytes, int index)
        {
            return (short)(bytes[index] << 8 |
                bytes[index + 1]);
        }

        public static int getInt(byte[] bytes , int index)
        {
            return bytes[index] << 24 |
                   bytes[index + 1] << 16 |
                   bytes[index + 2] << 8 |
                   bytes[index + 3] ;
        }

        public static char getChar(byte[] bytes, int index)
        {
            return (char)(bytes[index] << 8 |
                bytes[index + 1]);
        }

        public static long getLong(byte[] bytes , int index)
        {
            return ((long)bytes[index]) << 56 |
                           ((long)bytes[index + 1]) << 48 |
                           ((long)bytes[index + 2]) << 40 |
                           ((long)bytes[index + 3]) << 32 |
                           ((long)bytes[index + 4]) << 24 |
                           ((long)bytes[index + 5]) << 16 |
                           ((long)bytes[index + 6]) << 8 |
                           (long)bytes[index + 7];
        }



    }
}
