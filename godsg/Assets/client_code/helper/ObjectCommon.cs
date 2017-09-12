using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;


public class ObjectCommon {
	/// <summary>
	/// Sets the object active.
	/// </summary>
	/// <param name="go">Go.</param>
	/// <param name="active">If set to <c>true</c> active.</param>
	public static void SetObjectActive(GameObject go, bool active)
	{
		if(go == null)
		{
			return;
		}

		go.SetActive(active);
	}
	/// <summary>
	/// Gets the child.
	/// </summary>
	/// <returns>The child.</returns>
	/// <param name="go">Go.</param>
	/// <param name="childname">Childname.</param>
	public static GameObject GetChild(GameObject go, string childname)
	{
		if(go == null || string.IsNullOrEmpty(childname))
		{
			return null;
		}

		Transform parentTransform = go.transform;
		Transform childTransform = null;
		// if not contains / means the base child
		childTransform = parentTransform.FindChild(childname);
		if(childTransform == null)
		{
			return null;
		}

		return childTransform.gameObject;
	}
	/// <summary>
	/// Gets the child component.
	/// </summary>
	/// <returns>The child component.</returns>
	/// <param name="go">Go.</param>
	/// <param name="childname">Childname.</param>
	/// <typeparam name="T">The 1st type parameter.</typeparam>
	public static T GetChildComponent<T>(GameObject go, string childname) where T : Component
	{
		if (go == null || string.IsNullOrEmpty(childname))
		{
			return null;
		}
		GameObject child = GetChild(go, childname);
		if (child != null)
		{
			return child.GetComponent(typeof(T)) as T;
		}
		else
		{
			return null;
		}
	}
	/// <summary>
	/// Adds the child component.
	/// </summary>
	/// <returns>The child component.</returns>
	/// <param name="go">Go.</param>
	/// <param name="childname">Childname.</param>
	/// <typeparam name="T">The 1st type parameter.</typeparam>
	public static T AddChildComponent<T>(GameObject go, string childname) where T : Component
	{
		if (go == null || string.IsNullOrEmpty(childname))
		{
			return null;
		}
		GameObject child = GetChild(go, childname);
		if (child != null)
		{
			T component = child.GetComponent<T>();
			if (component == null)
			{
				return child.AddComponent<T>();
			}
			else
			{
				return component;
			}
		}
		else
		{
			return null;
		}
	}
	/// <summary>
	/// Gets the parent.
	/// </summary>
	/// <returns>The parent.</returns>
	/// <param name="go">Go.</param>
	public static GameObject GetParent(GameObject go)
	{
		if(go == null)
		{
			return null;
		}

		Transform transform = go.transform.parent;
		if(transform == null)
		{
			return null;
		}

		return transform.gameObject;
	}

}

public class CommonPath
{
	public static string gJingJiChangBackgroun = "jingjichang";
	public static string gTaindibaokuBackGround = "tiandibaokuzhandou";
	public static string gWorldBossBackGround = "shikonghuanjingzhandou";
}

public class DescItem
{
	public string name;
	public string desc;
}

namespace CommonDelegate
{
	public delegate bool BoolDelegate();
	public delegate void VoidDelegate();
	public delegate void GuidDelegate(long guid);
	public delegate void IdDelegate(int id);
	public delegate void GuidListDelegate(List<long> guids);
}




