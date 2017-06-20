/********************************************************************
	created:	2014/09/18
	created:	18:9:2014   17:01
	filename: 	F:\csharp_client\Client\Client\LogicSendHandler.cs
	file path:	F:\csharp_client\Client\Client

	author:		wangjinyu
	
	purpose:	逻辑层的发送消息。
                01. 将逻辑层的消息序列化到逻辑层的发送缓冲区中。
                02. 帧循环中,将逻辑发送缓冲区中的数据，发送到socket的发送缓冲区中。

*********************************************************************/
using System;
using System.IO;
using System.Net.Sockets;

namespace P3Net
{
    public class LogicSendHandler
    {
#region 数据区

        private TSocket m_Socket;
        private MemoryStream m_LogicSendBuf = null;
        private BinaryWriter m_BinaryWriter = null;

#endregion

#region 构造/析构函数
        
        public LogicSendHandler(TSocket socket)
        {
            m_Socket = socket;
            m_LogicSendBuf = new MemoryStream(TNetConfig.DEFAULT_SOCKET_OUTPUT_BUFFERSIZE);
            m_BinaryWriter = new BinaryWriter(m_LogicSendBuf, System.Text.Encoding.ASCII);
        }

        ~LogicSendHandler()
        {
            ClearUp();
            m_LogicSendBuf.Dispose();
        }

#endregion
        
#region 属性get/add

        public MemoryStream GetLogicSendBuf()
        {
            return m_LogicSendBuf;
        }

        public Int32 SendBufSize()
        {
            return (Int32)m_LogicSendBuf.Length;
        }

#endregion

#region stream 操作

        //-- 将消息序列化到逻辑发送缓冲区
        public Int32 WritePacket(Packet packet)
        {
            Int32 iSizeBeforeWrite = SendBufSize();

            //packet.WritePacketTest(ref m_BinaryWriter, m_LogicMemStream);
            packet.WritePacket(ref m_BinaryWriter);

            //-- 返回Packet body length
            //-- 方便调试
            Int32 resultLength = SendBufSize() - iSizeBeforeWrite - PacketUtil.PACKET_HEADER_SIZE;
            return resultLength;
        }

        //-- 将逻辑发送缓冲区的数据，拷贝到socket的发送缓冲区。
        public bool Flush()
        {
            return m_Socket.SendToSocketBuf(this);
        }

        //-- 清空该发送缓冲区的数据
        public void ClearUp()
        {
            if (m_LogicSendBuf.Length > 0)
            {
                Array.Clear(m_LogicSendBuf.GetBuffer(), 0, (Int32)m_LogicSendBuf.Length);
            }
            m_LogicSendBuf.SetLength(0);
            m_LogicSendBuf.Seek(0, SeekOrigin.Begin);
        }

#endregion
    }
}
