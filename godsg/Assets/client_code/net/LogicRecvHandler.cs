using System;
using System.IO;
using System.Net.Sockets;

namespace P3Net
{
    using System.Collections.Generic;
    using PacketID_t = UInt16;

    public class LogicRecvBuffer
    {
        private MemoryStream m_MemoryStream;
        private bool m_bUsedFlag = false;
        private Object m_LockerForUsedFlag = new Object();

        //-- test
        public Int32 m_indexInPool = -1;


        public LogicRecvBuffer()
        {
            m_MemoryStream = new MemoryStream();
        }

        ~LogicRecvBuffer()
        {
            m_MemoryStream.Dispose();
        }

        public void SetUsedFlag(bool bFlag)
        {
            lock (m_LockerForUsedFlag)
            {
                if (m_bUsedFlag != bFlag)
                {
                    m_bUsedFlag = bFlag;
                }
            }
        }

        public bool GetUsedFlag()
        {
            lock (m_LockerForUsedFlag)
            {
                return m_bUsedFlag;
            }
        }

        public MemoryStream GetMemoryStream()
        {
            return m_MemoryStream;
        }

        public void Clear()
        {
            m_MemoryStream.SetLength(0);

            if (m_MemoryStream.Length != 0)
            {
                Array.Clear(m_MemoryStream.GetBuffer(), 0, (int)m_MemoryStream.Length);
            }
            m_MemoryStream.Seek(0, SeekOrigin.Begin);
            m_bUsedFlag = false;
        }
    }

    public class LogicRecvBufQueue
    {
        private List<LogicRecvBuffer> m_RecvList = null;
        private Int32 m_iEmptyPos = 0;      
        private Int32 m_iUsedPos = 0;      
        private Int32 m_iQueueSize = 0;

        private Object m_LockerForRecvList = new Object();

        public LogicRecvBufQueue(Int32 iQueueSize)
        {
            m_RecvList = new List<LogicRecvBuffer>();
            m_iQueueSize = iQueueSize;
            Resize();
        }

        private void Resize()
        {
            LogicRecvBuffer buffer = null;
            for (UInt32 index = 0; index < m_iQueueSize; ++index)
            {
                buffer = new LogicRecvBuffer();
                if (buffer == null)
                {
                    //-- 抛出异常
                }
                m_RecvList.Add(buffer);
            }
        }

        public LogicRecvBuffer GetEmptyBuf()
        {
            lock(m_LockerForRecvList)
            {
                LogicRecvBuffer buffer = null;
                //-- 01. 在当前list中查找free的。
                for (Int32 index = 0; index < m_RecvList.Count; ++index, m_iEmptyPos++)
                {
                    if (m_iEmptyPos >= m_RecvList.Count)
                    {
                        m_iEmptyPos = 0;
                    }
                    try
                    {
                        buffer = m_RecvList[m_iEmptyPos];
                    }
                    catch (System.Exception ex)
                    {
                        //-- log记录异常
                        //-- 从开始找到一个可以free的。
                        CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                        m_iEmptyPos = 0;
                        goto __NEW_LOGICSENDBUFFER;
                    }

                    if (buffer.GetUsedFlag() == true)
                    {
                        continue;
                    }

                    buffer.Clear();

                    //-- test
                    buffer.m_indexInPool = m_iEmptyPos;

                    m_iEmptyPos++;
                    if (m_iEmptyPos >= m_RecvList.Count)
                    {
                        m_iEmptyPos = 0;
                    }

                    

                    return buffer;
                }

__NEW_LOGICSENDBUFFER:
                //-- 02. 没有找到则需要添加
                if (    (buffer == null) ||
                        (buffer != null) && (buffer.GetUsedFlag() == true)
                )
                {
                    buffer = new LogicRecvBuffer();
                    //-TODO: 下面这种情况就SB了。。。
                    if (buffer == null)
                    {
                        //-- log 怎么处理。
                    }
                    m_RecvList.Add(buffer);
                }

                buffer.Clear();
                m_iEmptyPos++;
                if (m_iEmptyPos >= m_RecvList.Count)
                {
                    m_iEmptyPos = 0;
                }

                //-- test
                buffer.m_indexInPool = (m_RecvList.Count - 1);

                return buffer;
            } //-- end_lock(m_LockerForRecvList)
        }

        public LogicRecvBuffer GetUsedBuf()
        {
            lock(m_LockerForRecvList)
            {
                LogicRecvBuffer buffer = null;
                //-- 01. 在当前list中查找free的。
                for (Int32 index = 0; index < m_RecvList.Count; ++index, m_iUsedPos++)
                {
                    if (m_iUsedPos >= m_RecvList.Count)
                    {
                        m_iUsedPos = 0;
                    }
                    try
                    {
                        buffer = m_RecvList[m_iUsedPos];
                    }
                    catch (System.Exception ex)
                    {
                        //-- log记录异常
                        //-- 从开始找到一个可以free的。
                        CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                        m_iUsedPos = 0;
                        goto __NO_USED_LOGICSENDBUFFER;
                    }

                    if (buffer.GetUsedFlag() == false)
                    {
                        continue;
                    }

                    //-- test
                    buffer.m_indexInPool = m_iUsedPos;

                    m_iUsedPos++;
                    if (m_iUsedPos >= m_RecvList.Count)
                    {
                        m_iUsedPos = 0;
                    }


                   buffer.GetMemoryStream().Seek(0, SeekOrigin.Begin);
                    return buffer;
                }

__NO_USED_LOGICSENDBUFFER:
                //-- 02. 没有找到,则返回null
                return null;
            }
        }

    }

    public class LogicRecvHandler
    {

#region 数据区

        private TSocket m_Socket;
        private LogicRecvBufQueue m_LogicRecvBufQueue = new LogicRecvBufQueue(TNetConfig.DEFAULT_LOGIC_RECV_QUEUE_SIZE);
        private MemoryStream m_LogicRemainingBuf = new MemoryStream();
        private BinaryReader m_BinaryReader = null; //-- 反序列化Packet的辅助变量

#endregion

#region 构造函数

        public LogicRecvHandler(TSocket socket)
        {
            m_Socket = socket;
            m_LogicRemainingBuf = new MemoryStream();
        }

        ~LogicRecvHandler()
        {
            m_BinaryReader.Close();
            m_LogicRemainingBuf.Dispose();
        }

#endregion

#region 属性get/add

        public LogicRecvBufQueue GetLogicRecvBufQueue()
        {
            return m_LogicRecvBufQueue;
        }

#endregion

#region stream 操作

        public bool ProcessPacket(ref byte serverMsgIndex)
        {
            if (m_LogicRecvBufQueue == null)
            {
                return false;
            }

            //-- 判断是否有待处理的逻辑层消息缓冲数据
            LogicRecvBuffer logicRecvBuf = m_LogicRecvBufQueue.GetUsedBuf();
            if (logicRecvBuf == null)
            {
                return true;
            }

            MemoryStream memStream = logicRecvBuf.GetMemoryStream();
            if (memStream == null)
            {
                return false;
            }

            //-- test

            if (memStream.Length == memStream.Position)
            {
                //Console.WriteLine("memStream.Length == memStream.Position");
            }

             //-- 如果m_RemainingStream长度不为0，则之后处理的memStream的前面要加上m_RemainingStream。
            if (m_LogicRemainingBuf.Length != 0)
            {
                m_LogicRemainingBuf.Write(memStream.GetBuffer(), 0, (Int32)memStream.Length);
                Array.Clear(memStream.GetBuffer(), 0, (Int32)memStream.Length);
                memStream.Write(m_LogicRemainingBuf.GetBuffer(), 0, (Int32)m_LogicRemainingBuf.Length);
                memStream.Seek(0, SeekOrigin.Begin);
                Array.Clear(m_LogicRemainingBuf.GetBuffer(), 0, (Int32)m_LogicRemainingBuf.Length);
                m_LogicRemainingBuf.SetLength(0);
            }

            //-- memStream中的消息反序列化操作
            m_BinaryReader = new BinaryReader(memStream, System.Text.Encoding.ASCII);

            Int32 iTmpHead = 0;
            Int32 iTmpTotalSize = (Int32)memStream.Length;
            Int32 iTmpDeltaLenth = 0;
            PacketID_t tmpPacketID = (PacketID_t)PACKET_TYPE.PACKET_TYPE_NONE;
            Int16 iTmpPacketUInt32 = 0;

            while (true)
            {
                iTmpDeltaLenth = iTmpTotalSize;
                memStream.Seek(iTmpHead, SeekOrigin.Begin);
                iTmpDeltaLenth -= iTmpHead;
                UnityEngine.Debug.Log("the head is " + iTmpHead + " and the  deltaLength is " + iTmpDeltaLenth);
                //-- 如果剩余的消息长度小于Header，则放入m_RemainingStream中。等待下次帧循环来处理
                if (iTmpDeltaLenth < PacketUtil.PACKET_HEADER_SIZE)
                {
                    if (iTmpDeltaLenth > 0)
                    {
                        m_LogicRemainingBuf.Write(memStream.GetBuffer(), iTmpHead, iTmpDeltaLenth);
                    }
                    logicRecvBuf.SetUsedFlag(false);
                    break;
                }
                UnityEngine.Debug.Log("the temp delta length is " + iTmpDeltaLenth);

                iTmpPacketUInt32 = m_BinaryReader.ReadInt16();                ;
                //-- 消息没有接收完全。
                if (iTmpDeltaLenth < iTmpPacketUInt32)
                {
                    if (iTmpDeltaLenth > 0)
                    {
                        m_LogicRemainingBuf.Write(memStream.GetBuffer(), iTmpHead, iTmpDeltaLenth);
                    }                    
                    logicRecvBuf.SetUsedFlag(false);
                    Console.WriteLine("消息没有接收完全");
                    break;
                }

                try
                {
                    tmpPacketID = m_BinaryReader.ReadUInt16();
                    UnityEngine.Debug.Log("THE PACKET ID IS ====================" + tmpPacketID);
                }
                catch (System.Exception ex)
                {
                    CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                }


                Packet packet = PacketFacotry.GetInstance().CreatePacket(tmpPacketID);
                if (packet == null)
                {
                    //-- 抛出异常,根据PacketID创建Packet失败
                    //-- 略过这个出错的消息，并继续处理
                    iTmpHead += iTmpPacketUInt32;
					CommonDebugLog.LogWarning(string.Format("CreatePacket Failed packetID = {0}", tmpPacketID));
					//Console.WriteLine("packet == null");
                    continue;
                }

                try
                {
                    if (!packet.ReadPacketBody(m_BinaryReader, (Int16)(iTmpPacketUInt32 - 4)))
                    {
						Console.WriteLine("packet.ReadPacketBody( m_BinaryReader) == false");
                        //-- log
                    }
                    UnityEngine.Debug.LogError("begin process packet ");
                    PacketFacotry.GetInstance().ProcessPacket(packet);

					NetClient.GetInstance().RemoveWatingPackets((PACKET_TYPE)packet.GetPacketID());
                }
				catch (System.Exception ex)
                {
					CommonDebugLog.LogWarning(ex.ToString());
				}

				//-- 执行成功，set iTmpHead
				iTmpHead += (Int32)(iTmpPacketUInt32);
               
            } //-- end_while (true)
            return true;

        }

        public bool Fill()
        {
            return m_Socket.RecvFromSocketBuf(this);
        }

#endregion

    }
}
