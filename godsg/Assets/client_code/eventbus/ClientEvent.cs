using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;

namespace P3GameClient
{
	public class ClientEvent : IPooledObjSupporter
	{
		public static GameEventID String2EventType( string strEventType )
		{
			if( string.IsNullOrEmpty(strEventType) )
			{
                return GameEventID.GEIdUndefined;
			}
			
			if( !System.Enum.IsDefined( typeof(GameEventID), strEventType ))
			{
                return GameEventID.GEIdUndefined;
			}

			return (GameEventID)System.Enum.Parse(typeof(GameEventID), strEventType);			
		}

		public static GameEventID Int2EventType(int num)
		{
			if (Enum.IsDefined(typeof(GameEventID), num))
			{
				return (GameEventID)num;
			}
			else
			{
				return GameEventID.GEIdUndefined;
			}
		}

		public static GameEventID Int2EventType(string numStr)
		{
			int id = -1;
			if (int.TryParse(numStr, out id))
			{
				return P3GameClient.ClientEvent.Int2EventType(id);
			}
			else
			{
				return P3GameClient.GameEventID.GEIdUndefined;
			}
		}

		QuickList<object> mParameters = new QuickList<object>(2);
		GameEventID mID = GameEventID.GEIdCount;
		float mTimeLock = 0;
		bool mDeleteFlg = false;
		public bool DeleteFlg { get { return mDeleteFlg; } set { mDeleteFlg = value; } }

		/// <summary>
		/// 成员变量重置，为的是对象可以重用;
		/// 继承自IPooledObjSupporter;
		/// </summary>
		public void Reset()
		{
			if (mParameters != null)
			{
				mParameters.Clear();
			}
			else
			{
				mParameters = new QuickList<object>(2);
			}

			mID = GameEventID.GEIdCount;
			mTimeLock = 0;
			mDeleteFlg = false;
		}

		/// <summary>
		/// 对象销毁时做的一些资源回收，相当于析构函数;
		/// 继承自IPooledObjSupporter;
		/// </summary>
		public void Dispose()
		{

		}

		public GameEventID GetID()
		{
			return mID;
		}

		public void SetID(GameEventID id)
		{
			mID = id;
		}

		public void SetID(string eventName)
		{
			mID = ClientEventManager.GetInstance().GetEventId(eventName);
		}

		public float GetTimelock()
		{
			return mTimeLock;
		}

		// 设置时间锁;
		public void SetTimelock(float timelock)
		{
			mTimeLock = Time.unscaledTime + timelock;
		}

		// 设置延时;
		public void SetTimeLapse(float lapse)
		{
			mTimeLock = Time.time + lapse;
		}

		public int GetParametersCout()
		{
			return mParameters.Count;
		}

		public void AddParameter(object parameter)
		{
			mParameters.Add(parameter);
		}

		//public bool GetParameterBool(ref bool parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = false;
		//        return false;
		//    }
			
		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = false;
		//        return false;
		//    }

		//    parameter = (bool)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterByte(ref System.Byte parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }
			
		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (System.Byte)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterFloat(ref float parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (float)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterUInt64(ref System.UInt64 parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (System.UInt64)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterUInt32(ref System.UInt32 parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (System.UInt32)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterInt64(ref System.Int64 parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (System.Int64)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterInt32(ref System.Int32 parameter, int index)
		//{			
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = 0;
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = 0;
		//        return false;
		//    }

		//    parameter = (System.Int32)mParameters[index];
		//    return true;
		//}

		//public bool GetParameterString(ref string parameter, int index)
		//{
		//    if (mParameters == null || index >= mParameters.Count)
		//    {
		//        Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");
		//        parameter = "";
		//        return false;
		//    }

		//    if (mParameters[index] != null && mParameters[index].GetType() != parameter.GetType())
		//    {
		//        Debug.LogError("Error: The Event Parameter Type Error!!!");

		//        parameter = "";
		//        return false;
		//    }

		//    parameter = (string)mParameters[index];
		//    return true;
		//}

		public T GetParameter<T>(int index)
		{
			if (mParameters == null || index >= mParameters.Count)
			{
				Debug.LogError("Error: The Event Parameter index > mParameters.Count!!!");				
				return default(T);
			}

			if (mParameters[index] != null && !(mParameters[index] is T) )
			{
				Debug.LogError("Error: The Event Parameter Type Error!!!");				
				return default(T);
			}
			
			return (T)mParameters[index];
		}

		public T GetClassParameter<T>(int index) where T : class
		{			
			if (index >= mParameters.Count || index < 0)
			{
				Debug.LogError("Error: The Event Parameter index out of range!!!");
				return default(T);
			}

			if( mParameters[index] != null && mParameters[index].GetType() != typeof(T) )
			{
				Debug.LogError("Error: The Event Parameter Type Error!!!");
				return null;
			}

			T param = mParameters[index] as T;
			return param;
		}

	}
}