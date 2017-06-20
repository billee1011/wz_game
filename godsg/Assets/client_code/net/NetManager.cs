using System;
using System.IO;
using System.Net.Sockets;

namespace P3Net
{
	using PacketID_t = System.UInt16;

	public class NetManager
	{
		private TSocket m_Socket = null;
		private LogicRecvHandler m_LogicRecvHandler = null;
		private LogicSendHandler m_LogicSendHandler = null;

		private OnConnectCallback m_OnConnectSuccessed = null;
		private OnConnectCallback m_OnConnectFailed = null;

		private byte m_serverMsgIndex = 0;

		public NetManager()
		{
			m_Socket = new TSocket();
			m_LogicRecvHandler = new LogicRecvHandler(m_Socket);
			m_LogicSendHandler = new LogicSendHandler(m_Socket);
		}

		~NetManager()
		{
			ClearUp();
		}

		public void initCallback(OnConnectCallback successed, OnConnectCallback failed)
		{
			m_OnConnectSuccessed = successed;
			m_OnConnectFailed = failed;
			//m_Socket.initCallback(successed, failed);
		}

		#region 属性方法get/set

		TSocket getTSocket()
		{
			return m_Socket;
		}

		public SocketError GetSocketError()
		{
			return m_Socket.GetSocketError();
		}

		#endregion

		#region 网络层方法

		public void Disconnect()
		{
			//-TODO: log
			if (m_Socket != null)
			{
				m_Socket.Close();
				//m_Socket = null;
			}
		}

		public bool Connect(String ip, int port)
		{
			//Console.WriteLine("NetManager connect Time:=[" + System.DateTime.Now.TimeOfDay.ToString() + "]");
			bool bRet = false;
			//if (m_Socket == null)
			//{
			//    m_Socket = new TSocket();
			//}
			try
			{
				bRet = m_Socket.Connect(ip, port);
			}
			catch (System.Exception ex)
			{
				throw new Exception(ex.ToString());
			}

			return bRet;
		}
		#endregion

		#region 消息流程
		private bool ProcessInput()
		{
			if (m_LogicRecvHandler == null)
			{
				//-- assert
				return false;
			}
			return m_LogicRecvHandler.Fill();
		}

		private bool ProcessOutput()
		{
			if (m_LogicSendHandler == null)
			{
				//-- assert
				return false;
			}
			return m_LogicSendHandler.Flush();
		}

		private bool ProcessCommand()
		{
			if (m_LogicRecvHandler == null)
			{
				//-- assert
				return false;
			}
			return m_LogicRecvHandler.ProcessPacket(ref m_serverMsgIndex);

		}

		//-- 检测连接状态，调用相关回调函数。
		public void CheckConnect()
		{
			if (m_Socket.netStatus == NetStatus.SOCKET_TYEP_INIT ||
					m_Socket.netStatus == NetStatus.SOCKET_TYEP_GAMING
			)
			{
				return;
			}

			if (m_Socket.netStatus == NetStatus.SOCKET_TYEP_CONNECT_SUCCESSED)
			{
				m_Socket.netStatus = NetStatus.SOCKET_TYEP_GAMING;
				m_OnConnectSuccessed();
			}
			else if (m_Socket.netStatus == NetStatus.SOCKET_TYEP_CONNECT_FAILED)
			{
				m_Socket.netStatus = NetStatus.SOCKET_TYEP_INIT;
				m_OnConnectFailed();
			}
			return;

		}

		//-- 这里应该有个频率？
		//-- 这里先不进行重新连接。
		public void Update()
		{
			//-- 检测connect状态，供外部调用
			CheckConnect();

			if (!ProcessInput())
			{
				//-- 处理相关失败流程
			}

			if (!ProcessOutput())
			{
				//-- 处理相关失败流程
			}

			if (!ProcessCommand())
			{
				//-- 处理相关失败流程
			}

			return;

		}

		public bool IsConnected()
		{
			if (m_Socket == null)
			{
				return false;
			}

			return m_Socket.IsConnected();
		}

		public bool PushPacket(Packet packet)
		{
			if (m_Socket.IsConnected() == false)
			{
				//-- 这里应该return false,并且重新连接。
				return false;
			}

			if (m_LogicSendHandler == null)
			{
				//-TODO: new throw
				return false;
			}
			packet.SetPacketIndex(m_serverMsgIndex);

			// 不再检测大小了;
			int bodyLength = m_LogicSendHandler.WritePacket(packet);
			//Int32 nWriteSize = m_LogicSendHandler.WritePacket(packet);
			//if (nWriteSize != packet.GetPacketBodySize())
			//{
			//    //-TODO: log
	        CommonDebugLog.LogWarning("the body length is " + bodyLength);
			//}
			return true;

		}

		#endregion

		public void ClearUp()
		{
			Disconnect();
			m_Socket = null;
			m_LogicRecvHandler = null;
			m_LogicSendHandler = null;
			m_serverMsgIndex = 0;
		}

		//-- test：下面的功能，之后会移动到其他位置。
		public void Init()
		{
			//CWEnterWorldFactory.AddPacketFactory();
			//WCEnterWorldFactory.AddPacketFactory();
		}

	}
}
