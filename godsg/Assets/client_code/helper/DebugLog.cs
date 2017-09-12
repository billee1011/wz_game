using UnityEngine;
using System.Collections;
public class Debuger  {
	
	private static bool g_EnableLog = true;

	public bool EnableLog {
		get { return g_EnableLog; }
		set { g_EnableLog = value; }
	}

	static public void Log(object message)
	{
		Log(message,null);
	}

	static public void Log(object message, Object context)
	{
		if(g_EnableLog && Debug.isDebugBuild)
		{
			Debug.Log(message,context);
		}
	}

	static public void LogError(object message)
	{
		LogError(message,null);
	}

	static public void LogError(object message, Object context)
	{
		if(g_EnableLog && Debug.isDebugBuild)
		{
			Debug.LogError(message,context);
		}
	}

	static public void LogWarning(object message)
	{
		if(g_EnableLog && Debug.isDebugBuild)
		{
			Debug.LogWarning(message, null);
		}
	}

	static public void LogWarning(object message, Object context)
	{
		if(g_EnableLog && Debug.isDebugBuild)
		{
			Debug.LogWarning(message,context);
		}
	}
}

public class DisplayObject
{
}