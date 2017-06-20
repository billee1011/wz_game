

using System;
using System.Collections;
using System.Reflection;

namespace P3GameClient
{
	/**/
	/// <summary>
	/// IObjectPool 的默认实现;
	/// </summary>
	#region ObjectPool
	public class ObjectPool : IObjectPool
	{
		#region members
		private Type destType = null;
		private object[] ctorArgs = null;
		private int minObjCount = 0;
		private int maxObjCount = 0;
		private int shrinkPoint = 0;
		private Hashtable hashTableObjs = new Hashtable();
		private Hashtable hashTableStatus = new Hashtable(); //key - isIdle 其中key就是hashcode
		private QuickList<Int32> keyList = new QuickList<Int32>();
		private bool supportReset = false;
		public static bool notUsePool = false; // 仅仅用于调试
		private Int32 mIdleObjCount = 0;
		#endregion


		public event CallBackObjPool PoolShrinked;
		public event CallBackObjPool MemoryUseOut;

		public bool Initialize(Type objType, object[] cArgs, int minNum, int maxNum)
		{
			if (minNum < 1)
			{
				minNum = 1;
			}
			if (maxNum < 2)
			{
				maxNum = 2;
			}

			this.destType = objType;
			this.ctorArgs = cArgs;
			this.minObjCount = minNum;
			this.maxObjCount = maxNum;
			double cof = 1 - ((double)minNum / (double)maxNum);
			this.shrinkPoint = (int)(cof * minNum);

			mIdleObjCount = 0;

			//缓存的类型是否支持IPooledObjSupporter接口;
			Type supType = typeof(IPooledObjSupporter);
			if (supType.IsAssignableFrom(objType))
			{
				this.supportReset = true;
			}

			if (notUsePool)
			{
				
			}
			else
			{
				// 不需要初始化的时候就创建对象;
				//this.InstanceObjects();
			}

			return true;
		}

		private void InstanceObjects()
		{
			for (int i = 0; i < this.minObjCount; i++)
			{
				this.CreateOneObject();
			}
		}

		#region CreateOneObject ,DistroyOneObject
		private int CreateOneObject()
		{
			object obj = null;

			try
			{
				obj = Activator.CreateInstance(this.destType, this.ctorArgs);
			}
			catch (Exception ee) //分配内存失败！
			{
				PrintPoolStatus();
				UnityEngine.Debug.LogError("ObjectPool " + destType.ToString() + " used out!!!!!" + ee.ToString());
				//ee = ee;
				this.maxObjCount = this.CurObjCount;
				if (this.minObjCount > this.CurObjCount)
				{
					this.minObjCount = this.CurObjCount;
				}

				if (this.MemoryUseOut != null)
				{
					this.MemoryUseOut();
				}
#if UNITY_EDITOR
				UnityEngine.Debug.Break();
#endif
				return -1;
			}

			int key = obj.GetHashCode();
			this.hashTableObjs.Add(key, obj);
			this.hashTableStatus.Add(key, true);
			this.keyList.Add(key);
			this.mIdleObjCount++;
			return key;
		}

		private void DistroyOneObject(int key)
		{
			object target = this.hashTableObjs[key];
			IDisposable tar = target as IDisposable;
			if (tar != null)
			{
				tar.Dispose();
			}

			if ((bool)this.hashTableStatus[key])
			{
				this.mIdleObjCount--;
			}
			
			this.hashTableObjs.Remove(key);
			this.hashTableStatus.Remove(key);
			this.keyList.Remove(key);
		}
		#endregion

		public object RentObject()
		{
			lock (this)
			{
				Int32 key = -1;

				if (notUsePool)
				{
					key = this.CreateOneObject();
					if (key != -1)
					{
						this.hashTableStatus[key] = false;
						this.mIdleObjCount--;
						return this.hashTableObjs[key];
					}
					else
					{
						return null;
					}
				}

				object target = null;

				for (Int32 index = 0; index < this.keyList.size; index++)
				{
					key = this.keyList[index];
					if ((bool)this.hashTableStatus[key]) //isIdle
					{
						this.hashTableStatus[key] = false;
						this.mIdleObjCount--;
						target = this.hashTableObjs[key];
						break;
					}
				}

				if (target == null)
				{
					if (this.keyList.size < this.maxObjCount)
					{
						key = this.CreateOneObject();
						if (key != -1)
						{
							this.hashTableStatus[key] = false;
							this.mIdleObjCount--;
							target = this.hashTableObjs[key];
						}
					}
				}

#if UNITY_EDOTIR
				if (target == null)
				{
					UnityEngine.Debug.LogError("Pool Rent Instance Failed! " + this.keyList.size);
				}
#endif
				return target;
			}

		}

		#region GiveBackObject
		public void GiveBackObject(int objHashCode)
		{
			if (this.hashTableStatus[objHashCode] == null)
			{
				return;
			}

			lock (this)
			{
				if (notUsePool)
				{
					DistroyOneObject(objHashCode);
					return;
				}


				this.hashTableStatus[objHashCode] = true;
				this.mIdleObjCount++;
				if (this.supportReset)
				{
					IPooledObjSupporter supporter = (IPooledObjSupporter)this.hashTableObjs[objHashCode];
					supporter.Reset();
				}

				if (this.CanShrink())
				{
					this.Shrink();
				}
			}
		}


		/// <summary>
		/// 能够收缩对象池
		/// 
		/// 根据我们的实际用法，这个算法可以简单理解，当正在使用的对象比MinNum小，并且已经创建的对象超过了MaxNum容量的50%.
		/// 这时就会触发收缩，实际会收缩到MinNum.
		/// </summary>
		/// <returns></returns>
		private bool CanShrink()
		{
			int idleCount = this.GetIdleObjCount();
			int busyCount = this.CurObjCount - idleCount;

			return (busyCount < this.shrinkPoint) && (this.CurObjCount > (this.minObjCount + (this.maxObjCount - this.minObjCount) / 2));
		}

		private void Shrink()
		{
			for (Int32 index = 0; index < this.keyList.size;)
			{
				Int32 key = this.keyList[index];
				if ((bool)this.hashTableStatus[key])
				{
					this.DistroyOneObject(key);
				}
				else
				{
					index++;
				}

				if (this.CurObjCount <= this.minObjCount)
				{
					break;
				}
			}

			if (this.PoolShrinked != null)
			{
				this.PoolShrinked();
			}
		}

		#endregion

		public bool CheckObjectStatus(Int32 hashCode)
		{
			if (hashTableStatus[hashCode] == null)
			{
				return false;
			}
			else
			{
				return (bool)(hashTableStatus[hashCode]);
			}
		}

		public void Dispose()
		{
			Type supType = typeof(System.IDisposable);
			if (supType.IsAssignableFrom(this.destType))
			{
				Int32 count = this.keyList.size - 1;
				for (Int32 index = count; index >= 0; index--)
				{
					this.DistroyOneObject(this.keyList[index]);
				}
			}

			this.hashTableStatus.Clear();
			this.hashTableObjs.Clear();
			this.keyList.Clear();
		}

		#region property
		public int MinObjCount
		{
			get
			{
				return this.minObjCount;
			}
		}

		public int MaxObjCount
		{
			get
			{
				return this.maxObjCount;
			}
		}

		public int CurObjCount
		{
			get
			{
				return this.keyList.size;
			}
		}

		public int IdleObjCount
		{
			get
			{
				lock (this)
				{
					return this.GetIdleObjCount();
				}
			}
		}

		private int GetIdleObjCount()
		{
			return this.mIdleObjCount;
		}
		#endregion

#region Debug

		public string PrintPoolStatus()
		{
			string log = string.Format("Object Type:{0} -> (Current:{1}/Idle:{2}/Max:{3})", destType.ToString(), CurObjCount, IdleObjCount, maxObjCount);
			return log;
		}

		public void SeeEveryNode()
		{	
			for (Int32 i = 0; i < keyList.size; ++i)
			{
				if ((bool)hashTableStatus[keyList[i]] == true)
				{
					System.Object obj = hashTableObjs[keyList[i]];
					CommonDebugLog.Log(obj.ToString());
				}
				else
				{
					System.Object obj = hashTableObjs[keyList[i]];
					CommonDebugLog.Log(obj.ToString());
				}
			}
		}

		public bool isInPool(Int32 hashCode)
		{
			bool inPool = false;
			if (keyList == null)
			{
				return false;
			}
			for (Int32 i = 0; i < keyList.Count; ++i)
			{
				if (keyList[i] == hashCode)
				{
					inPool = true;
					break;
				}
			}
			return inPool;
		}

		public bool isIdle(Int32 hashCode)
		{
			if (isInPool(hashCode) == false)
			{
				UnityEngine.Debug.LogError("Found Object not in pool when you want to check this object is idle in pool >_<");
				return false;
			}
			bool result = (bool)hashTableStatus[hashCode];
			return result;
		}

		public void RestPool()
		{
			Shrink();
		}

#endregion

	#endregion
	}

}
