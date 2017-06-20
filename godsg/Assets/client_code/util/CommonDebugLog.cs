
using UnityEngine;

public class CommonDebugLog
{
	static public void Log(System.Object message)
	{
		UnityEngine.Debug.Log(message);
	}

	static public void Log(System.Object message, UnityEngine.Object context)
	{
		UnityEngine.Debug.Log(message, context);
	}

	static public void LogError(System.Object message) 
	{
		UnityEngine.Debug.LogError(message);
	}

	static public void LogError(System.Object message, UnityEngine.Object context)
	{
		UnityEngine.Debug.LogError(message, context);
	}

	static public void LogWarning(System.Object message)
	{
		UnityEngine.Debug.LogWarning(message);
	}

	static public void LogWarning(System.Object message, UnityEngine.Object context)
	{
		UnityEngine.Debug.LogWarning(message, context);
	}
}