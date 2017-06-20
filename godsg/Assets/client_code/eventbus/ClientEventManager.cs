using System;
using System.Collections;
using System.Collections.Generic;

namespace P3GameClient
{
	public class ClientEventManager : Singleton<ClientEventManager>
	{
		static Int32 MaxEventCount = 512;
		static Int32 EventCount = 100;
		
		// 事件的关注处理回调列表;		
		public delegate void VoidDelegate(ClientEvent eve);

		// 所有的事件，和其处理函数;
		List<List<VoidDelegate>> mAllEvents = null;
		List<ClientEvent> mProcessEventsList = null;

		List<ClientEvent> GetProcessEvent()
		{
			if (mProcessEventsList == null)
			{
				mProcessEventsList = new List<ClientEvent>();
			}
			return mProcessEventsList;
		}

		public void ClearProcessEvent()
		{
			List<ClientEvent> eventList = GetProcessEvent();
			if (null == eventList)
			{
				return;
			}

			for (int i = 0; i < eventList.Count; i++)
			{
				ClientEvent proEvent = eventList[i];
				GameEventID type = proEvent.GetID();

				if (type >= 0 && type < GameEventID.GEIdCount)
				{
					List<VoidDelegate> mFuns = mAllEvents[(int)type];
					for (int index = 0; index < mFuns.Count; index++)
					{
						VoidDelegate function = mFuns[index];
						if (function != null)
						{
							function(proEvent);
						}
					}
				}

				DestoryEvent(eventList[i]);
			}

			mProcessEventsList.Clear();
		}

		// 事件的ID和name的映射表;
		Dictionary<string, GameEventID> mEventDefines = new Dictionary<string, GameEventID>();

		ObjectPool mEventPool = new ObjectPool();

		public ClientEventManager()
		{
			Init();
		}

		public void ResCache()
		{
			if (mEventPool != null)
			{
				mEventPool.RestPool();
			}
		}

		// 创建事件ID和name的映射表.
		void AddEventMapping(string name, GameEventID eventId)
		{
			if (mEventDefines.ContainsKey(name) == true)
			{
				CommonDebugLog.Log(" The event " + name + "already contains a definition for EventMapping");
			}
			else if (mEventDefines.ContainsValue(eventId) == true)
			{
				CommonDebugLog.Log(" The event " + name + "already contains a definition for EventMapping");
			}
			else
			{
				mEventDefines[name] = eventId;
			}
		}

		// 获得event的ID
		public GameEventID GetEventId(string name)
		{
			if (mEventDefines.ContainsKey(name) == true)
			{
				return mEventDefines[name];
			}
			return GameEventID.GEIdCount;
		}

		void Init()
		{
			mAllEvents = new List<List<VoidDelegate>>();

			for (int i = 0; i < (int)GameEventID.GEIdCount; i++)
			{				
				List<VoidDelegate> funList = new List<VoidDelegate>();
				mAllEvents.Add(funList);				
			}

			mEventPool.Initialize(typeof(ClientEvent), null, 50, MaxEventCount);

		}

#if UNITY_EDITOR
		public string GetPoolStatusStr()
		{
			return mEventPool.PrintPoolStatus();
		}
#endif
		
		// 从对象池中得到一个Event;
		public ClientEvent AddEvent(GameEventID type, bool immediately = false)
		{
			if ( immediately )
			{
				ActiveFunction(type);
				return null;
			} 

			ClientEvent eve = mEventPool.RentObject() as ClientEvent;
			if( eve != null )
			{
				eve.SetID(type);
				GetProcessEvent().Add(eve);
			}
			else
			{
				UnityEngine.Debug.LogError("CreatEvent Faild ");
			}
			
			return eve;
		}

		// 对象池回收ClientEvent，并不是真正的销毁;
		private void DestoryEvent(ClientEvent eve)
		{
			mEventPool.GiveBackObject(eve.GetHashCode());
		}

		// 添加事件的关注处理函数;
		public void AddProcessFunction(GameEventID type, VoidDelegate function)
		{
			if (type >= 0 && type < GameEventID.GEIdCount)
			{
				List<VoidDelegate> mFuns = mAllEvents[(int)type];
#if UNITY_EDITOR
				for (Int32 index = 0; index < mFuns.Count; index++)
				{
					if (mFuns[index] == function)
					{
						UnityEngine.Debug.LogError("AddProcessFunction error =================" + type);
					}
				}
#endif
				mFuns.Add(function);
			}
		}

		// 删除事件的关注处理函数;
		public void RemoveProcessFunction(GameEventID type, VoidDelegate function)
		{
			if (type >= 0 && type < GameEventID.GEIdCount)
			{
				List<VoidDelegate> mFuns = mAllEvents[(int)type];
				
				if ( mFuns.Contains(function) == true )
				{
					mFuns.Remove(function);
				}
			}
		}

		// 指定一个事件被触发;
		public void ImmediatelyActiveEvent(ClientEvent eve)
		{
			if (eve == null)
			{
				return;
			}

			ActiveFunction(eve);
			DestoryEvent(eve);

			if ( mProcessEventsList.Contains(eve) )
			{
				mProcessEventsList.Remove(eve);
			}

			// 事件已经触发，先设置删除标志;
			eve.DeleteFlg = true;
			
		}

		private void ActiveFunction(ClientEvent proEvent)
		{
			if (proEvent == null)
			{
				return;
			}

			GameEventID type = proEvent.GetID();
			if (type >= 0 && type < GameEventID.GEIdCount)
			{
				List<VoidDelegate> mFuns = mAllEvents[(int)type];
				if (mFuns == null)
				{
					return;
				}

				for (int index = mFuns.Count; index > 0 ; index--)
				{
					VoidDelegate function = mFuns[index-1];
					if (function != null)
					{
						try
						{
							function(proEvent);
						}
						catch (System.Exception ex)
						{
							UnityEngine.Debug.LogWarning("ProcessEvent Exception: " + ex.ToString());
						}
					}
				}
			}
		}

		private void ActiveFunction(GameEventID type)
		{
			if (type < 0 || type >= GameEventID.GEIdCount)
			{
				return;
			}

			List<VoidDelegate> mFuns = mAllEvents[(int)type];
			if (mFuns == null)
			{
				return;
			}

			for (int index = mFuns.Count; index > 0; index--)
			{
				VoidDelegate function = mFuns[index - 1];
				if (function != null)
				{
					try
					{
						function(null);
					}
					catch (System.Exception ex)
					{
						UnityEngine.Debug.LogWarning("ProcessEvent Exception: " + ex.ToString());
					}
				}
			}
		}

		// 如果当前事件大于EventCount就把多余的事件直接触发了;
		private void CheckEventCount()
		{
			List<ClientEvent> mProcessEvents = GetProcessEvent();
			if (mProcessEvents == null)
			{
				return;
			}
			Int32 eventCount = mProcessEvents.Count - EventCount;
			for (Int32 i = 0; i < eventCount; i++)
			{
				ClientEvent proEvent = mProcessEvents[0];
				if (proEvent == null)
				{
					mProcessEvents.RemoveAt(0);
					continue;
				}

				if (proEvent.DeleteFlg)
				{
					DestoryEvent(proEvent);
					mProcessEvents.RemoveAt(0);
					continue;
				}
				
				ActiveFunction(proEvent);

				DestoryEvent(proEvent);
				mProcessEvents.RemoveAt(0);
			}
		}

		public void OnUpdate()
		{
			try
			{
				List<ClientEvent> mProcessEvents = GetProcessEvent();
				if (mProcessEvents == null)
				{
					return;
				}

				CheckEventCount();

				for (int i = 0; i < mProcessEvents.Count; )
				{
					ClientEvent proEvent = mProcessEvents[i];
					if (proEvent == null)
					{
						mProcessEvents.RemoveAt(i);
						continue;
					}

					if (proEvent.DeleteFlg)
					{
						DestoryEvent(proEvent);
						mProcessEvents.RemoveAt(i);
						continue;
					}

					float timelock = proEvent.GetTimelock();
					if ((timelock > UnityEngine.Time.unscaledTime))
					{
						i++;
						continue;
					}

					ActiveFunction(proEvent);

					DestoryEvent(proEvent);

					mProcessEvents.RemoveAt(i);
				}
			}
			catch (System.Exception ex)
			{
				UnityEngine.Debug.LogWarning("processEvent Exception: " + ex.ToString());	
			}
		}

	}

}