using System;
using System.Collections.Generic;
using System.Net.Sockets;
using P3GameClient;

namespace P3Net
{
	public class NetClient : Singleton<NetClient>
	{
		protected string mStrHostName;
		protected Int32 mPortID;

		protected OnConnectCallback OnSuccessedCallBack = null;
		protected OnConnectCallback OnFailedCallBack = null;

		private NetManager mNetManager = null;

		private string mAccount = null;


		protected List<HandlerBase> mHandlerList = new List<HandlerBase>();
		protected Dictionary<PACKET_TYPE, PACKET_TYPE> mWaitPackets = new Dictionary<PACKET_TYPE, PACKET_TYPE>();	// 有些协议发送完毕之后，必须等待服务器回应;
		protected List<PACKET_TYPE> mWatingPackets = new List<PACKET_TYPE>();	// 现在客户端正在等待哪些协议;

		protected List<Packet> mFailedPacketList = new List<Packet>();		// 因为连接断开，发送失败的协议;

		public List<PACKET_TYPE> watingPackets
		{
			get
			{
				return mWatingPackets;
			}
		}

		public void AddWatingPackets(PACKET_TYPE packetID)
		{
            //PACKET_TYPE waitPacket = PACKET_TYPE.PACKET_TYPE_NUM;
            //if (!mWaitPackets.TryGetValue(packetID, out waitPacket))
            //{
            //	return;
            //}

            //if (watingPackets.Contains(waitPacket))
            //{
            //	return;
            //}
            //mWatingPackets.Add(waitPacket);

            //OnWatingChange();
        }

        public void RemoveWatingPackets(PACKET_TYPE packetID)
		{
			if (!mWatingPackets.Contains(packetID))
			{
				return;
			}

			mWatingPackets.Remove(packetID);

			OnWatingChange();
		}

		protected void OnWatingChange()
		{
            //if (P3Net.NetClient.GetInstance().watingPackets.Count > 0)
            //{
            //    GameUIManager.instance.ShowWindow(PkgSetting.NameUILoading, true);
            //}
            //else
            //{
            //    GameUIManager.instance.ShowWindow(PkgSetting.NameUILoading, false);
            //}
        }

        private void init()
		{
			if (mNetManager == null)
			{
				mNetManager = new NetManager();
			}

			mNetManager.initCallback(OnConnectSuccessed, OnConnectFailed);
            InitHandlerList();
			RegisterHandler();
			RegisterWaitList();
		}

        private void InitHandlerList()
        {
            mHandlerList.Add(new LoginHandler());
        }

		protected void RegisterHandler()
		{
//          mHandlerList.Add(new LoginHandler());
            for (int i = 0; i < mHandlerList.Count; i++)
			{
				mHandlerList[i].RegisterPacket();
			}
		}

		protected void RegisterWaitList()
		{
		}

		public NetClient()
		{
			init();
		}

		public bool IsConnected()
		{
			return mNetManager.IsConnected();
		}

		public SocketError GetSocketError()
		{
			return mNetManager.GetSocketError();
		}

		//-TODO: test 保存的GUID，下面先保存account，之后接入login后在进行更改;
		public string account
		{
			get { return mAccount; }
			set
			{
				if (value != null)
					mAccount = value;
			}
		}

		//-TODO: 等到socket里面的connect的阻塞event给废除后，这里要把超时的回调暴露出来，供外面使用;
		public bool ConnectService(string hostName, Int32 port, OnConnectCallback successed, OnConnectCallback failed)
		{
			try
			{
				mStrHostName = hostName;
				mPortID = port;

				OnSuccessedCallBack = successed;
				OnFailedCallBack = failed;

				return mNetManager.Connect(hostName, port);
			}
			catch (System.Exception ex)
			{
				UnityEngine.Debug.LogWarning("[Connect 127.0.0.1:10000 is failed!] Reason:=[" + ex.ToString() + "]");
				return false;
			}
		}

		public void Disconnect()
		{
			mNetManager.Disconnect();
		}

		public void PushPacket(Packet packet)
		{
			if (!mNetManager.IsConnected())
			{
				// 连接断开的情况下，已经不能发送协议了（先暂时加成自动重连，以后按策划需求重新规划）;
				Disconnect();
				//LoginManager.GetInstance().ShowReConnectDialog();
				mFailedPacketList.Add(packet);
				return;
			}

			bool bFlag = mNetManager.PushPacket(packet);
			if (!bFlag)
			{
				CommonDebugLog.LogWarning("PushPacket Failed");
				return;
			}

			AddWatingPackets((PACKET_TYPE) packet.GetPacketID());
		}

		public void PushAllFailedPacket()
		{
			Packet[] packets = mFailedPacketList.ToArray();
			mFailedPacketList.Clear();

			for (int i = 0; i < packets.Length; i++)
			{
				PushPacket(packets[i]);
			}

		}

		//-TODO: 超时回调函数;
		public void PushPacket_Old(Packet packet)
		{
			bool bFlag = true;
			for (Int32 index = 0; index < 5; ++index)
			{
				bFlag = mNetManager.PushPacket(packet);
				if (bFlag == true)
				{
					break;
				}

				bFlag = ConnectService(mStrHostName, mPortID, OnSuccessedCallBack, OnConnectFailed);
				if (!bFlag)
				{
					break;
				}
			}

			if (!bFlag)
			{
				UnityEngine.Debug.LogWarning("[mNetManager.PushPacket(packet)] or [Connect()] failed!");
				return;
			}

			AddWatingPackets((PACKET_TYPE) packet.GetPacketID());
		}

		public void Update(float deltaTime)
		{
			mNetManager.Update();
		}

		private static Int32 m_iTryCnt = 0;
		void OnConnectFailed()
		{
			if (m_iTryCnt++ > TNetConfig.DEFAULT_SOCKET_CONNECT_RETRY_CNT)
			{
				m_iTryCnt = 0;
				CommonDebugLog.LogWarning("Connect failed in login!Socket Error:=(" + GetSocketError().ToString() + ")");

				if (OnFailedCallBack != null)
				{
					OnFailedCallBack();
					OnFailedCallBack = null;
				}

				return;
			}

			ConnectService(mStrHostName, mPortID, OnSuccessedCallBack, OnFailedCallBack);
			return;
		}

		void OnConnectSuccessed()
		{
			if (OnSuccessedCallBack != null)
			{
				OnSuccessedCallBack();
				OnSuccessedCallBack = null;
			}
		}

		void OnConnectFailed_PushPacket()
		{
			//-TODO: 关于消息，应该先本地保存到一个容器中。
			//-- 外部调用这个进行相关操作？
		}

		void OnConnectSuccessed_PushPacket()
		{
			//-TODO: 继续进行？
		}
	}
}
