
using System;
using System.Collections.Generic;


[System.Serializable]
public class Hero : IdParseConf
{
    public List<Pair<int, int>> growList;
    public int id;
    public int country;
    public int heroQuality;
    public string grow_awake_id;

    public int getId()
    {
        return this.id;
    }

    public bool parse()
    {
        growList = MiscUtil.getPairList(grow_awake_id, ',', '|');
        if( growList != null)
        {
            foreach (Pair<int, int> pair in growList)
            {
                UnityEngine.Debug.Log(pair.toString());
            }
        }
        return true;
    }
}


public class HeroList
{
    public List<Hero> list = new List<Hero>();
}
