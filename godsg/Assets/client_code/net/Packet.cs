namespace P3Net
{
    using System;
    using System.IO;
    using Google.Protobuf;

    //-- Packet处理后的返回结果。服务器端使用。
    public enum PACKET_EXE
	{
        PACKET_EXE_ERROR = 0,
        PACKET_EXE_BREAK,
        PACKET_EXE_CONTINUE,
        PACKET_EXE_NOTREMOVE,
        PACKET_EXE_NOTREMOVE_ERROR,
	}

    //-- Packet Status的类型。
    public enum PACKET_HEADER_STATUS
    {
        PACKET_HEADER_STATUS_UNCOMPRESS = 0,    //-- 不压缩
        PACKET_HEADER_STATUS_COMPRESS,          //-- 压缩
        PACKET_HEADER_STATUS_NUM,               //-- <= PACKET_MAX_STATUS
    }

    //-- Packet的常量，公共接口。
    public class PacketUtil
    {
        public static int PACKET_ID_LENGTH      = sizeof(UInt16);
        public static int UINT32_LENGTH         = sizeof(UInt32);
        public static Int32 PACKET_HEADER_SIZE  = PACKET_ID_LENGTH + UINT32_LENGTH; //-- 发送消息的header的长度
        public static Int32 PACKET_MAX_STATUS   = 16;                               //-- 包的最大status。m_PacketStatus < PACKET_MAX_STATUS
        public static Int32 PACKET_MAX_SIZE     = 1048576;                          //-- 包的最大size:2*20 = 1048576。m_PacketSize < PACKET_MAX_SIZE
        public static char CHAR_TERMINATOR      = '\0';
        //-- 将BYTE packetIndex，BYTE pakcetStatus，UINT32 packetSize 拼在一个packetUInt32中。
        //-- BYTE packetIndex， 占位：0xFF000000
        //-- BYTE pakcetStatus，占位：0x00F00000
        //-- UINT32 packetSize, 占位：0x000FFFFF
        public static byte PACKET_INDEX_OFFSET = 24;
        public static UInt32 PACKET_INDEX_MARK = 0X00FFFFFF;

        public static byte PACKET_STATUS_OFFSET = 20;
        public static UInt32 GET_PACKET_STATUS_MARK = 0X00FFFFFF;
        public static UInt32 SET_PACKET_STATUS_MARK = 0XFF0FFFFF;

        public static UInt32 GET_PACKET_LENGTH_MARK = 0X000FFFFF;
        public static UInt32 SET_PACKET_LENGTH_MARK = 0XFFF00000;
        
        public static UInt32 GetPacketIndex(UInt32 packetIndex)
        {
            return (packetIndex >> PACKET_INDEX_OFFSET);
        }
        public static void SetPacketIndex(ref UInt32 packetUInt32, UInt32 index)
        {
            packetUInt32 = (packetUInt32 & PACKET_INDEX_MARK) + index << PACKET_INDEX_OFFSET;
        }

        public static UInt32 GetPacketStates(UInt32 packetStatus)
        {
            return ((packetStatus & GET_PACKET_STATUS_MARK) >> PACKET_STATUS_OFFSET);
        }
        public static void SetPacketStatus(ref UInt32 packetUInt32, UInt32 packetStatus)
        {
            packetUInt32 = (packetUInt32 & SET_PACKET_STATUS_MARK | packetStatus << PACKET_STATUS_OFFSET);
        }

        public static UInt32 GetPacketBodyLength(UInt32 packetLength)
        {
            return (packetLength & GET_PACKET_LENGTH_MARK);
        }
        public static void SetPacketBodyLength(ref UInt32 packetUInt32, UInt32 packetLength)
        {
            packetUInt32 = (packetUInt32 & SET_PACKET_LENGTH_MARK) + packetLength;
        }

        public static void WriteString(BinaryWriter write, string inputStr)
        {
            if (string.IsNullOrEmpty(inputStr))
            {
                return;
            }

            byte[] byteArray = null;

            try
            {
                byteArray = System.Text.Encoding.UTF8.GetBytes(inputStr);
            }
            catch (System.Exception ex)
            {
                //Console.WriteLine("ex:=[" + ex.ToString() + "]");
                UnityEngine.Debug.LogWarning("ex:=[" + ex.ToString() + "]");
                return;
            }

            for (Int32 index = 0; index < byteArray.Length; ++index)
            {
                write.Write(byteArray[index]);
            }

        }

        public static string ReadString(BinaryReader read, Int32 iLength)
        {
            if (iLength <= 0)
            {
                return null;
            }

            byte[] byteArray = read.ReadBytes(iLength);
            if (byteArray == null)
            {
                return null;
            }

            string ret = System.Text.Encoding.UTF8.GetString(byteArray, 0, byteArray.Length);
            int iIndex = ret.IndexOf(CHAR_TERMINATOR);
            if (iIndex > 0)
            {
                ret = ret.Substring(0, iIndex);
            }
            return ret;
        }

        public static Int32 GetUTF8StringLength(string InputString)
        {
            if (string.IsNullOrEmpty(InputString))
            {
                return 0;
            }

            byte[] byteArray = null;
            try
            {
                byteArray = System.Text.Encoding.UTF8.GetBytes(InputString);
            }
            catch (System.Exception ex)
            {
                //-- log
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                return 0;
            }

            if (byteArray == null)
            {
                return 0;
            }

            return byteArray.Length;

        }

    }

    public class Packet
    {
        private PACKET_TYPE m_PacketID = PACKET_TYPE.PACKET_TYPE_NONE;//-- 消息header处理中无变化。
        private Byte m_PacketIndex  = 0;	//-- 当前消息的序列号, 目前并没被使用。消息head处理中无变化。					备注：目前没有参与收包的校验。如果参与，检测256个index是否够用。
        private Byte m_PacketStatus = 0;	//-- 当前0，无压缩。1，压缩。其他没有被使用。消息head处理中，大小由2*8变为2*4.	备注：必须<=MAX_PACKET_STATUS
        private IMessage message = null;
        public Packet(PACKET_TYPE packetID, IMessage message)
        {
            this.m_PacketID = packetID;
            this.message = message;
        }

#region 属性接口

        public PACKET_TYPE GetPacketID()
        {
            return m_PacketID;
        }
        public Byte GetPacketIndex()
        {
            return m_PacketIndex;
        }
        public void SetPacketIndex(Byte b)
        {
            m_PacketIndex = b;
        }
        public Byte GetPacketStatus()
        {
            return m_PacketStatus;
        }
        public void SetPacketStatus(Byte b)
        {
            m_PacketStatus = b;
        }

        public virtual UInt32 GetPacketBodySize()
        {
            throw new NotImplementedException();
        }

#endregion

#region 消息流程接口

        private bool WritePacketHeader(ref BinaryWriter oStream)
        {
            //-- 01. valid
            if (oStream == null)
            {
                return false;
            }

            //-- 02. write header
            //-- PacketID_t
            //-- UINT32:m_PacketIndex[0xff000000], m_PacketStatus[0x00f00000], m_PakcetSize[0x000fffff]
            Int16 packetUInt32 = 0;
            try
            {
                oStream.Write(packetUInt32);
            }
            catch (System.Exception ex)
            {
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
            }
            try
            {
                oStream.Write((UInt16)m_PacketID);                
            }
            catch (System.Exception ex)
            {
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
            }
            
            //PacketUtil.SetPacketIndex(ref packetUInt32, m_PacketIndex);
            //PacketUtil.SetPacketStatus(ref packetUInt32, m_PacketStatus);
            //PacketUtil.SetPacketBodyLength(ref packetUInt32, GetPacketBodySize())

            return true;
        }

        public  bool WritePacketBody(ref BinaryWriter oStream)
        {
            if(message != null)
            {
                oStream.Write(message.ToByteArray());
            }
            return true;
        }

        public bool WritePacket(ref BinaryWriter oStream)
        {
            Stream iStream = oStream.BaseStream;
            long packetUINT32Pos = iStream.Position;
            
            if (!WritePacketHeader(ref oStream))
            {
                return false;
            }
            long lengthHeader = iStream.Length;
            if (!WritePacketBody(ref oStream))
            {
                return false;
            }
            long endPos = iStream.Length;
            long bodyLength = endPos - sizeof(Int16);
            Int16 packetUInt32 = (Int16)bodyLength;

            try
            {
                oStream.Seek((Int32)packetUINT32Pos, SeekOrigin.Begin);
                oStream.Write(packetUInt32);
                oStream.Seek((Int32)endPos, SeekOrigin.Begin);
            }
            catch (System.Exception ex)
            {
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
            }

            return true;

        }

        public virtual bool ReadPacketBody( BinaryReader iStream, UInt32 iSize)
        {
            throw new NotImplementedException();
        }

        private void PrintPacketHeaderContect()
        {
            //-TODO: 临时方案，以后用客户端的log系统
            //Console.WriteLine("PacketID:==[" + m_PacketID + "][PacketIndex:="+m_PacketIndex+"][PacketStatus:="+m_PacketStatus+"]");
            return;
        }

        public virtual void PrintPacketBodyContent()
        {
            throw new NotImplementedException();
        }

        public void PrintPacketContent()
        {
            PrintPacketHeaderContect();
            PrintPacketBodyContent();
            return;
        }

        //-- test func
        //public bool WritePacketTest(ref BinaryWriter oStream, MemoryStream memStream)
        //{
        //    Int32 iLength01 = (Int32)memStream.Length;
        //    if (!WritePacketHeader(ref oStream))
        //    {
        //        return false;
        //    }
        //    Int32 iLength02 = (Int32)memStream.Length;
        //    return WritePacketBody(ref oStream, memStream);
        //}

        public virtual bool WritePacketBody(ref BinaryWriter oStream, MemoryStream memStream)
        {
            return true;
        }
#endregion
    }

}
