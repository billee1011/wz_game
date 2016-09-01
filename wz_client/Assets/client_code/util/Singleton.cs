using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


public class Singleton<T> where T : class, new()
{
    protected static T instance;

    protected Singleton()
    {

    }

    public static T getInst()
    {
        if( instance == null)
        {
            instance = new T();
        }
        return instance;
    }
}
