using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.IO;


namespace P3Net
{
    public delegate void OnConnectCallback();

    public enum NetStatus
    {
        SOCKET_TYEP_INIT = 0,
        SOCKET_TYEP_CONNECTING,
        SOCKET_TYEP_CONNECT_SUCCESSED,
        SOCKET_TYEP_CONNECT_FAILED,
        SOCKET_TYEP_GAMING,
    }

    public class OnRecvObj
    {
        public SocketRecvBuf m_RecvBuffer = null;
        public LogicRecvHandler m_LogicRecvHandler = null;
        public TSocket m_TSocket = null;
    }

    public class OnSendObj
    {
        public SocketSendBuf m_SendBuffer = null;
        public TSocket m_TSocket = null;
    }

    public class TSocket
    {
        //private static AutoResetEvent s_ConnectAutoEvent = new AutoResetEvent(false);

        private IPEndPoint m_IPAndPoint = null;
        private Socket m_Socket = null;        
        private OnRecvObj m_RecvData = null;

        private Boolean m_bConnectedFlag = false;   //-- OnConnect是否连接成功的标识
        private SocketError m_SocketError;          //-- 如果连接失败，可以通过这个查看具体错误信息。
        private NetStatus mSocketType = NetStatus.SOCKET_TYEP_INIT;

        //private OnConnectCallback m_OnConnectSuccessed  = null;
        //private OnConnectCallback m_OnConnectFailed     = null;
        //private SocketAsyncEventArgs connectCompleteArgs = null;
        //-TODO: 这个大小是DEFAULT_SOCKET_OUTPUT_BUFFERSIZE还是DISCONNECT_SOCKET_OUTPUT_BUFFERSIZE，有待商榷。
        
        SendBufferQueue m_SocketSendBufQueue = new SendBufferQueue(TNetConfig.DEFAULT_SOCKET_SEND_QUEUE_SIZE);   //-- 多个
        SocketRecvBufQueue m_SocketRecvBufQueue = new SocketRecvBufQueue();   //-- 一般为1个。

#region 构造/析构函数, init
        
        public TSocket()
        {            
        }
        ~TSocket()
        {
            Close();
        }

        //public void initCallback(OnConnectCallback successed, OnConnectCallback failed)
        //{
        //    m_OnConnectSuccessed = successed;
        //    m_OnConnectFailed = failed;
        //}
#endregion

#region 内部调用方法

        private static void OnConnect(object sender, SocketAsyncEventArgs e)
        {
            if (e.LastOperation != SocketAsyncOperation.Connect)
            {
                return;
            }

            TSocket tmpSocket = e.UserToken as TSocket;
            if (tmpSocket == null)
            {
                return;
            }

            tmpSocket.SetConnectFlag((e.SocketError == SocketError.Success));
            tmpSocket.SetSocketError(e.SocketError);
            //s_ConnectAutoEvent.Set();
        }

        private static void OnSend(object sender, SocketAsyncEventArgs e)
        {
            //Console.WriteLine("OnSend e.ToString():=[" + e.ToString() + "]");

            if (e == null)
            {
                return;
            }

            if (e.LastOperation != SocketAsyncOperation.Send)
            {
                return;
            }

            if (e.SocketError != SocketError.Success)
            {
                //-- log
                return;
            }

            OnSendObj sendObj = e.UserToken as OnSendObj;
            if (sendObj == null)
            {
                return;
            }

            if (sendObj.m_SendBuffer == null)
            {
                return;
            }

            if (sendObj.m_TSocket == null)
            {
                return;
            }
            
            if (e.BytesTransferred > 0)
            {
                sendObj.m_SendBuffer.AddTransferByteNum(e.BytesTransferred);
                if (!sendObj.m_SendBuffer.IsFinished())
                {
                    sendObj.m_TSocket.Send(sendObj.m_SendBuffer);
                    //-- test
                    MemoryStream memStream = sendObj.m_SendBuffer.GetMemoryStream();
                    if (memStream != null)
                    {
                        //Console.WriteLine("Send bytes continue:=[" + e.BytesTransferred + "] content:=[" + memStream.GetBuffer().ToString() + "]");
                    }
                    
                }
                else
                {
                    sendObj.m_SendBuffer.SetEmptyFlag(true);
                    //-- test
                    MemoryStream memStream = sendObj.m_SendBuffer.GetMemoryStream();
                    if (memStream != null)
                    {
                        //Console.WriteLine("Send bytes successed:=[" + e.BytesTransferred + "] content:=[" + memStream.GetBuffer().ToString() + "]");
                    }

                }                
            }

            //-- reuse
            e.Dispose();

            return;

        }

        private bool Send(SocketSendBuf sendBuff)
        {
            if (sendBuff == null)
            {
                return false;
            }

            MemoryStream sendMemStream = sendBuff.GetMemoryStream();
            if (sendMemStream == null)
            {
                return false;
            }

            if (sendMemStream.Length == 0)
            {
                return true;
            }

            if (sendBuff.IsFinished())
            {
                return true;
            }

            //-- 异步将发送缓冲区数据发送出去。
            OnSendObj   sendObj = new OnSendObj();
            sendObj.m_SendBuffer = sendBuff;
            sendObj.m_TSocket = this; 

            SocketAsyncEventArgs sendCompleteArgs = new SocketAsyncEventArgs();
            sendCompleteArgs.RemoteEndPoint = m_IPAndPoint;
            sendCompleteArgs.UserToken = sendObj;
            sendCompleteArgs.Completed += new EventHandler<SocketAsyncEventArgs>(OnSend);
            try
            {
                //-- 下面的以后放在一个pool中。
                byte[] tmpbyteArray = new byte[(int)sendMemStream.Length];
                Array.Copy(sendMemStream.GetBuffer(), tmpbyteArray, (int)sendMemStream.Length);
                sendCompleteArgs.SetBuffer(tmpbyteArray, 0, tmpbyteArray.Length);

                //sendCompleteArgs.SetBuffer(sendMemStream.GetBuffer(), sendBuff.GetTransferByteNum(), (int)sendMemStream.Length);

                //-- test log
                //Console.WriteLine("sendMemStream.Length:==[" + sendMemStream.Length + "]");
            }
            catch (System.Exception ex)
            {
                //-- log
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                return false;
            }

            //-: true,io pend. waiting callback
            try
            {
                if (!m_Socket.SendAsync(sendCompleteArgs))
                {
                    OnSend(null, sendCompleteArgs);
                }
            }
            catch (System.Exception ex)
            {
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
            }
            

            return true;

        }

        //-- 将从socket接收到的数据，拷贝到LogicRecvBuf中。
        private static void OnReceive(object sender, SocketAsyncEventArgs e)
        {
            if (e == null)
            {
                return;
            }

            if (e.LastOperation != SocketAsyncOperation.Receive)
            {
                return;
            }

            if (e.SocketError != SocketError.Success)
            {
                return;
            }

            OnRecvObj recvData = e.UserToken as OnRecvObj;
            if (recvData == null)
            {
                return;
            }

            if (recvData.m_RecvBuffer == null)
            {
                return;
            }

            if (recvData.m_LogicRecvHandler == null)
            {
                return;
            }

            if (recvData.m_TSocket == null)
            {
                return;
            }

            LogicRecvBufQueue logicRecvBufQueue = recvData.m_LogicRecvHandler.GetLogicRecvBufQueue();
            if (logicRecvBufQueue == null)
            {
                //-- log
                return;
            }

            LogicRecvBuffer logicRecvBuf = logicRecvBufQueue.GetEmptyBuf();
            if (logicRecvBuf == null)
            {
                //-- log
                return;
            }

            MemoryStream memStream = logicRecvBuf.GetMemoryStream();
            if (memStream == null)
            {
                //-- log
                return;
            }

            if (e.BytesTransferred > 0)
            {
                //-- test
                //Console.WriteLine("e.BytesTransferred:=[" + e.BytesTransferred + "]");

                //-- 拷贝数据要从memStream的头写入。
                memStream.Seek(0, SeekOrigin.Begin);
                memStream.Write(e.Buffer, 0, e.BytesTransferred);
                memStream.Seek(0, SeekOrigin.Begin);
                logicRecvBuf.SetUsedFlag(true);
                //-- 释放recvData.m_RecvBuffer
                recvData.m_RecvBuffer.SetUsedFlag(false);
                //-- 继续接收
                recvData.m_TSocket.RecvFromSocketBuf(recvData.m_LogicRecvHandler);
            }
            else
            {
                recvData.m_RecvBuffer.SetUsedFlag(false);
                //-- log查看是否接收缓冲区满了
				CommonDebugLog.Log("Server DisConnect!!!");
				NetClient.GetInstance().Disconnect();
            }
            return;
        }

#endregion

#region 属性get方法

        public Socket GetSocket()
        {
            return m_Socket;
        }

        public IPEndPoint GetIPEndPoint()
        {
            return m_IPAndPoint;
        }
        private void SetSocketError(SocketError socketError)
        {
            m_SocketError = socketError;
        }

        public SocketError GetSocketError()
        {
            return m_SocketError;
        }
        
        public NetStatus netStatus 
        {
            get { return mSocketType; }
            set {   
                  mSocketType = value;
            }
        }

#endregion

#region socket方法
        public bool IsConnected()
        {
            //-TODO: 不确定m_Socket.Connected是否一定准确，所以添加了m_bConnectedFlag的判断。			
            return ((m_Socket != null) && (m_Socket.Connected == true) && (m_bConnectedFlag == true));
        }

        public void SetConnectFlag(bool bConnectFlag)
        {
            m_bConnectedFlag = bConnectFlag;
            
            //-- 设置连接状态
            if (m_bConnectedFlag)
            {
                netStatus = NetStatus.SOCKET_TYEP_CONNECT_SUCCESSED;
                //m_OnConnectSuccessed();
            }
            else
            {
                netStatus = NetStatus.SOCKET_TYEP_CONNECT_FAILED;
                //m_OnConnectFailed();
            }

        }

        public void Reset()
        {
            if (IsConnected())
            {
                Close();
            }

            //-: 断开连接，发送/接受缓冲区都应该可以继续工作
            //s_ReceiveAutoEvent.Reset();
            //s_SendAutoEvent.Reset();
            //s_ConnectAutoEvent.Reset();
        }
        public bool Connect(String ip, Int32 port, Int32 retryCnt, Int32 overTimeMillSec)
        {
            ////-- 01. 设置connect连接状态
            ////-TODO: test
            //if (netStatus == NetStatus.SOCKET_TYEP_CONNECTING ||
            //    netStatus == NetStatus.SOCKET_TYEP_GAMING)
            //{
            //    return false;
            //}

            netStatus = NetStatus.SOCKET_TYEP_CONNECTING;

            //-- 考虑在逻辑层调用这个Connect的时候，下面的两个new的问题。
            Int32 iRetryCnt = 0;
            try
            {
                IPAddress ipAddress = IPAddress.Parse(ip);
                m_IPAndPoint = new IPEndPoint(ipAddress, port);
                m_Socket = new Socket(m_IPAndPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
				m_SocketSendBufQueue = new SendBufferQueue(TNetConfig.DEFAULT_SOCKET_SEND_QUEUE_SIZE);   //-- 多个
				m_SocketRecvBufQueue = new SocketRecvBufQueue();  

                //-- test 所以设置了发送缓冲区
                //try
                //{
                //    Int32 iTest01 = (Int32)m_Socket.GetSocketOption(SocketOptionLevel.Socket, SocketOptionName.SendBuffer);
                //    m_Socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.SendBuffer, 20);
                //    iTest01 = (Int32)m_Socket.GetSocketOption(SocketOptionLevel.Socket, SocketOptionName.SendBuffer);
                //}
                //catch (System.Exception ex)
                //{
                //    int i = 100; //-- for test assert
                //}
                
            }
            catch (System.Exception ex)
            {
                //-- log
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                return false;	
            }
            

__RETRY_CONNECT:
            Reset();

            SocketAsyncEventArgs connectCompleteArgs = new SocketAsyncEventArgs(); ;
            connectCompleteArgs.UserToken = this;
            connectCompleteArgs.RemoteEndPoint = m_IPAndPoint;
            connectCompleteArgs.Completed += new EventHandler<SocketAsyncEventArgs>(OnConnect);

            //-: true,io pend. waiting callback
            try
            {
                if (!m_Socket.ConnectAsync(connectCompleteArgs))
                {
                    OnConnect(null, connectCompleteArgs);
                }
            }
            catch (System.Exception ex)
            {
                if (++iRetryCnt < retryCnt)
                {
                    //log
                    goto __RETRY_CONNECT;
                }

                throw new Exception(ex.ToString());
            }

            return true;
        }
        
        public bool Connect(String ip, Int32 port)
        {
            bool bRet = false; 
            try
            {
                bRet = Connect(ip, port, TNetConfig.DEFAULT_SOCKET_CONNECT_RETRY_CNT, TNetConfig.DEFAULT_SOCKET_SEND_ASYNC_WAITING_TIME);
            }
            catch (System.Exception ex)
            {
                throw new Exception(ex.ToString());
            }
            return bRet;
        }

        public void DisConnect()
        {
            //-TODO: 明确看一下window phone8有没有异步Disconnect。
            //-: 第一次看是没有的。
            Close();
        }

        public void Close()
        {
            if (m_Socket != null && IsConnected())
            {
                m_Socket.Shutdown(SocketShutdown.Both);
                m_Socket.Close();
                m_Socket = null;
            }
			netStatus = NetStatus.SOCKET_TYEP_INIT;
            m_bConnectedFlag = false;

        }

        //-- 将逻辑发送缓冲区的数据转存到net层的发送缓冲区，开启异步发送过程。并立即返回。
        public bool SendToSocketBuf(LogicSendHandler logicSendHandler)
        {
            if (logicSendHandler == null)
            {
                return false;
            }

            //-TODO: 这里之后再考虑
            if (!IsConnected())
            {
                return true;
            }

            int iSendLength = logicSendHandler.SendBufSize();
            if (iSendLength == 0)
            {
                return true;
            }

            MemoryStream oMemStream = logicSendHandler.GetLogicSendBuf();
            if (oMemStream == null)
            {
                return false;
            }

            //-- 将逻辑层缓冲区转到发送缓冲区
            //-TODO: encode type
            SocketSendBuf sendBuff = m_SocketSendBufQueue.GetBuf();
            if (sendBuff == null)
            {
                return false;
            }
            MemoryStream sendMemStream = sendBuff.GetMemoryStream();
            if (sendMemStream == null)
            {
                return false;
            }
            //-- oMemStream.GetBuffer()不发生new byte[]操作。
            if (oMemStream.Length == 0)
            {
                //Console.WriteLine("Error:==[logicSendHandler.SendBufSize() != 0 && oMemStream.Length == 0]");
                return false;
            }
            try
            {
                sendMemStream.Write(oMemStream.GetBuffer(), 0, (int)oMemStream.Length);
            }
            catch (System.Exception ex)
            {
                //-- log
                CommonDebugLog.LogWarning("ex:=[" + ex.ToString() + "]");
                return false;
            }
            sendBuff.SetEmptyFlag(false);
            logicSendHandler.ClearUp();

            return Send(sendBuff);
        }

        public bool RecvFromSocketBuf(LogicRecvHandler recvHandler)
        {
            if (recvHandler == null)
            {
                return false;
            }

            if (!IsConnected())
            {
                return true;
            }

            SocketRecvBuf recvBuff = m_SocketRecvBufQueue.GetRecvBuf();
            //-- 说明已经创建了DEFAULT_RECV_QUEUE_SIZE SocketRecvBuf等待接收数据，所以返回。
            if (recvBuff == null)
            {
                return true;
            }

            MemoryStream recvMemStream = recvBuff.GetMemoryStream();
            if (recvMemStream == null)
            {
                return false;
            }

			//if ( recvMemStream.Length == 0 )
			//{
			//    // 如果长度为0，则认为服务器主动踢掉客户端;
			//    NetClient.GetInstance().Disconnect();
			//    return false;
			//}

            SocketAsyncEventArgs receiveCompleteArgs = new SocketAsyncEventArgs();
            if (    recvMemStream.Length < TNetConfig.DEFAULT_SOCKET_RECV_BUFFERSIZE    ||
                    recvMemStream.Capacity < TNetConfig.DEFAULT_SOCKET_RECV_BUFFERSIZE
            )
            {
                recvMemStream.SetLength(TNetConfig.DEFAULT_SOCKET_RECV_BUFFERSIZE);
                Array.Clear(recvMemStream.GetBuffer(), 0, (Int32)recvMemStream.Length);
            }
            receiveCompleteArgs.SetBuffer(recvMemStream.GetBuffer(), 0, (Int32)recvMemStream.Length);

            //-- test
            //Console.WriteLine("recvMemStream.Length:=[" + recvMemStream.Length + "]");

            recvBuff.SetUsedFlag(true);
            m_RecvData = new OnRecvObj();
            m_RecvData.m_RecvBuffer = recvBuff;
            m_RecvData.m_LogicRecvHandler = recvHandler;
            m_RecvData.m_TSocket = this;

            receiveCompleteArgs.UserToken = m_RecvData;
            receiveCompleteArgs.RemoteEndPoint = m_IPAndPoint;
            receiveCompleteArgs.Completed += new EventHandler<SocketAsyncEventArgs>(OnReceive);

            if (!m_Socket.ReceiveAsync(receiveCompleteArgs))
            {
                OnReceive(null, receiveCompleteArgs);
            }

            return true;
            ////-TODO: encode type
            //Array.Clear(m_CurrRecvBuff, 0, TNetConfig.DEFAULT_SOCKET_INPUT_BUFFERSIZE);
            //if (    m_ReceiveCompleteArgs.UserToken == null ||
            //        m_ReceiveCompleteArgs.UserToken.Equals(iStream) == false
            //)
            //{
            //    m_ReceiveCompleteArgs.UserToken = iStream;
            //}
            
            //if (    m_ReceiveCompleteArgs.RemoteEndPoint == null    ||
            //        m_ReceiveCompleteArgs.RemoteEndPoint.Equals(m_IPAndPoint) == false
            //)
            //{
            //    m_ReceiveCompleteArgs.RemoteEndPoint = m_IPAndPoint;
            //}

            //m_ReceiveCompleteArgs.SetBuffer(m_CurrRecvBuff, 0, TNetConfig.DEFAULT_SOCKET_INPUT_BUFFERSIZE);

            ////-: true,io pend. waiting callback
            //try
            //{
            //    if (m_Socket.ReceiveAsync(m_ReceiveCompleteArgs))
            //    {
            //        if (s_ReceiveAutoEvent.WaitOne(TNetConfig.DEFAULT_SOCKET_RECEIVE_ASYNC_WAITING_TIME))
            //        {
            //            RecvFromSocketBuf(iStream);
            //            int iTest01 = m_ReceiveCompleteArgs.BytesTransferred;
            //        }
            //        else
            //        {
            //            int iTest02 = m_ReceiveCompleteArgs.BytesTransferred;
            //            return true;
            //        }
            //    }
            //    else
            //    {
            //        return true;
            //    }
            //}
            //catch (System.Exception ex)
            //{
            //    Console.WriteLine(ex.ToString());
            //    return false;
            //}
            
            //return true;

        }


#endregion
      
    }
}
