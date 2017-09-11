using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

abstract class BaseInfoProvider<T> where T : IdParseConf
{
    protected Dictionary<int, T> confMap = new Dictionary<int, T>();

    public bool loadConfig()
    {
        List<T> list = getConfList(getJsonStr());
        if (list == null)
        {
            return false;
        }
        foreach (T t in list)
        {
            if (!t.parse())
            {
                throw new Exception(" config parse error ");
            }
            confMap[t.getId()] = t;
        }
        return true;
    }

    private string getJsonStr()
    {
        TextAsset asset = Resources.Load("HeroBase") as TextAsset;
        if (asset == null)
        {
            return "";
        }
        UnityEngine.Debug.Log(asset.text);
        return asset.text;
    }

    protected abstract string getJsonName();

    public abstract ConfType getConfType();

    protected abstract List<T> getConfList(String jsonStr);

    public void printContent()
    {
        UnityEngine.Debug.Log(" the map size is " + confMap.Count);
    }

}
