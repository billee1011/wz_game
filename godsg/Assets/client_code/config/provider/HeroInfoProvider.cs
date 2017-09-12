using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;
using LitJson;

class HeroInfoProvider : Singleton<HeroInfoProvider>
{

    private Dictionary<int, HeroBase> heroConfigMap = new Dictionary<int, HeroBase>();

    public bool LoadConfig(string jsonStr)
    {
        heroConfigMap.Clear();
        JsonData data = JsonMapper.ToObject(jsonStr);
        for(int i = 0, count = data.Count; i< count; i++)
        {
            HeroBase hero = JsonMapper.ToObject<HeroBase>(data[i].ToJson());
            heroConfigMap[hero.getId()] = hero;
        }
        return true;
    }


    public HeroBase getHeroConfig(int confId)
    {
        return heroConfigMap[confId];
    }


}
