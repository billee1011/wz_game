
using System;
using System.Collections.Generic;
using System.IO;

namespace P3Net
{
    public class SocketSendBuf
    {
        private MemoryStream m_MemoryStream = null;
        private bool m_bEmptyFlag = true;
        private Int32 m_iTransferNum = 0;

        public Object m_LockerForTransfer = new Object();
        public Object m_LockerForEmptyFlag = new Object();

        public SocketSendBuf()
        {
            m_MemoryStream = new MemoryStream();
        }

        ~SocketSendBuf()
        {
            m_MemoryStream.Dispose();
        }

        public void SetEmptyFlag(bool bFlag)
        {
            lock (m_LockerForEmptyFlag)
            {
                if (m_bEmptyFlag != bFlag)
                {
                    m_bEmptyFlag = bFlag;
                }
            }
        }

        public bool GetEmptyFlag()
        {
            lock (m_LockerForEmptyFlag)
            {
                return m_bEmptyFlag;
            }
        }

        public MemoryStream GetMemoryStream()
        {
            return m_MemoryStream;
        }

        public Int32 GetTransferByteNum()
        {
            lock (m_LockerForTransfer)
            {
                return m_iTransferNum;
            }
        }

        public bool IsFinished()
        {
            lock (m_LockerForTransfer)
            {
                return (m_iTransferNum == (int)m_MemoryStream.Length);
            }
        }

        public void AddTransferByteNum(Int32 iBytesTransferred)
        {
            lock (m_LockerForTransfer)
            {
                if (iBytesTransferred < 0)
                {
                    return;
                }
                
                m_iTransferNum += iBytesTransferred;
                if (m_iTransferNum > (int)m_MemoryStream.Length)
                {
                    //-- log
                    m_iTransferNum = (int)m_MemoryStream.Length;
                }
            }
        }

        public void Clear()
        {
            if (m_MemoryStream.Length != 0)
            {
                Array.Clear(m_MemoryStream.GetBuffer(), 0, (int)m_MemoryStream.Length);
            }
            m_MemoryStream.Seek(0, SeekOrigin.Begin);
            m_MemoryStream.SetLength(0);
            m_bEmptyFlag = true;
            m_iTransferNum = 0;
        }

    }
    
    public class SendBufferQueue
    {
        private List<SocketSendBuf> m_SendList = null;
        private Int32 m_iPos = 0;      //-- 当前可用的位置[0~size-1]
        private Int32 m_iQueueSize = 0;

        public SendBufferQueue(Int32 iQueueSize)
        {
            m_SendList = new List<SocketSendBuf>();
            m_iQueueSize = iQueueSize;
            Resize();
        }

        ~SendBufferQueue()
        {
            m_SendList.Clear();
        }

        private void Resize()
        {
            SocketSendBuf buffer = null;
            for (UInt32 index = 0; index < m_iQueueSize; ++index)
            {
                buffer = new SocketSendBuf();                
                if (buffer == null)
                {
                    //-- 抛出异常
                }
                m_SendList.Add(buffer);
            }
        }

        public SocketSendBuf GetBuf()
        {
            SocketSendBuf buffer = null;

            //-- 01. 在当前list中查找free的。
            for (Int32 index = 0; index < m_SendList.Count; ++index, m_iPos++)
            {
                if (m_iPos >= m_SendList.Count)
                {
                    m_iPos = 0;
                }
                try
                {
                    buffer = m_SendList[m_iPos];  
                }
                catch (System.Exception ex)
                {
                    //-- log记录异常
                    //-- 从开始找到一个可以free的。
                    //Console.WriteLine("ex:==[" + ex.ToString() + "]");
                    UnityEngine.Debug.LogWarning("ex:=[" + ex.ToString() + "]");
                    m_iPos = 0;
                    goto __NEW_SENDBUFFER;
                }

                if (buffer.GetEmptyFlag() == false)
                {
                    continue;
                }

                buffer.Clear();
                m_iPos++;
                if (m_iPos >= m_SendList.Count)
                {
                    m_iPos = 0;
                }
                return buffer;
            }

__NEW_SENDBUFFER:
            //-- 02. 没有找到则需要添加
            if (    (buffer == null)    ||
                    (buffer != null) && (buffer.GetEmptyFlag() == true)
            )
            {
                buffer = new SocketSendBuf();
                //-TODO: 下面这种情况就SB了。。。
                if (buffer == null)
                {
                    //-- log 怎么处理。
                }
                m_SendList.Add(buffer);
            }

            buffer.Clear();
            m_iPos++;
            if (m_iPos >= m_SendList.Count)
            {
                m_iPos = 0;
            }
            return buffer;
        }

        public UInt32 GetBuffNum()
        {
            return (UInt32)m_SendList.Count;
        }
    }
}
