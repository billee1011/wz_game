using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

class HeroInfoProvider : BaseInfoProvider<Hero>
{

    protected override List<Hero> getConfList(string jsonStr)
    {
        try
        {
            HeroList list = JsonUtility.FromJson<HeroList>(jsonStr);
            return list.list;
        }
        catch
        {
            UnityEngine.Debug.Log("exception when convert into  herl list ");
            return null;
        }
    }

    public override ConfType getConfType()
    {
        return ConfType.HERO;
    }

    protected override string getJsonName()
    {
        return "Hero";
    }


}
