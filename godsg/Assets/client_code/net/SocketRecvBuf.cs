
using System;
using System.Collections.Generic;
using System.IO;
namespace P3Net
{
    public class SocketRecvBuf
    {
        private MemoryStream m_MemoryStream;
        private bool m_bUsedFlag = false;

        private Object m_LockerForUsedFlag = new Object();

        //-- test
        public Int32 m_indexInPool = -1;


        public SocketRecvBuf()
        {
            m_MemoryStream = new MemoryStream(TNetConfig.DEFAULT_SOCKET_RECV_BUFFERSIZE);
        }

        ~SocketRecvBuf()
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
            if (m_MemoryStream.Length != 0)
            {
                Array.Clear(m_MemoryStream.GetBuffer(), 0, (int)m_MemoryStream.Length);
            }
            m_MemoryStream.Seek(0, SeekOrigin.Begin);
            m_MemoryStream.SetLength(0);
            m_bUsedFlag = false; 
        }
    }

    public class SocketRecvBufQueue
    {
        private List<SocketRecvBuf> m_RecvList = null;
        private Int32 m_iPos = 0;      //-- 当前可用的位置[0~size-1]
        private Object m_LockerForRecvList = new Object();

        public SocketRecvBufQueue()
        {
            m_RecvList = new List<SocketRecvBuf>();
            Resize();
        }

        ~SocketRecvBufQueue()
        {
            m_RecvList.Clear();
        }

        private void Resize()
        {
            SocketRecvBuf buffer = null;
            for (UInt32 index = 0; index < TNetConfig.DEFAULT_SOCKET_RECV_QUEUE_SIZE; ++index)
            {
                buffer = new SocketRecvBuf();
                if (buffer == null)
                {
                    //-- 抛出异常
                }
                m_RecvList.Add(buffer);
            }
        }

        public SocketRecvBuf GetRecvBuf()
        {
            lock (m_LockerForRecvList)
            {
                SocketRecvBuf buffer = null;

                //-- 01. 在当前list中查找free的。
                for (Int32 index = 0; index < m_RecvList.Count; ++index, m_iPos++)
                {
                    if (m_iPos >= m_RecvList.Count)
                    {
                        m_iPos = 0;
                    }
                    try
                    {
                        buffer = m_RecvList[m_iPos];
                    }
                    catch (System.Exception ex)
                    {
                        //-- log记录异常
                        //-- 从开始找到一个可以free的。
                        CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                        m_iPos = 0;
                        return null;
                    }

                    if (buffer.GetUsedFlag() == true)
                    {
                        continue;
                    }

                    buffer.Clear();

                    //-- test
                    buffer.m_indexInPool = m_iPos;

                    m_iPos++;
                    if (m_iPos >= m_RecvList.Count)
                    {
                        m_iPos = 0;
                    }
                    
                    return buffer;
                }

                //-- 02. 没有找到,则返回null。等待数据接收
                return null;
            }
        }

        public UInt32 GetBuffNum()
        {
            return (UInt32)m_RecvList.Count;
        }
    }
}
