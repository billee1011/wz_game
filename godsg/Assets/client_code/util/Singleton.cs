
using System;
using System.Collections.Generic;
/// <summary>
/// Base class of Singleton
/// </summary>
/// <typeparam name="T"></typeparam>
public class Singleton<T>
    where T : class, new()
{
    static T _Instance = null;

    public static T GetInstance()
    {
        if (_Instance == null)
        {
            _Instance = new T();
        }
        return _Instance;
    }

    public static T Instance
    {
        get
        {
            if (_Instance == null)
            {
                _Instance = new T();
            }
            return _Instance;
        }
    }

    public static void CreateInstance()
    {
        if (_Instance == null)
        {
            _Instance = new T();
        }
    }

    public static void DestroyInstance()
    {
        _Instance = null;
    }
}
